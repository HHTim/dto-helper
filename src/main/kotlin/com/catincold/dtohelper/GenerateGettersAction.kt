package com.catincold.dtohelper

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil

/**
 * 這是 "Generate All Getters" 的核心邏輯
 * 繼承 AnAction 代表這是一個可執行的動作
 */
class GenerateGettersAction : AnAction() {

    /**
     * update() 方法會頻繁被呼叫 (例如滑鼠移動時)，用來決定這個 Action 是否啟用 (Enable) 或顯示
     * 這裡我們判斷：如果游標下是一個變數，就顯示；否則隱藏
     */
    override fun update(e: AnActionEvent) {
        val project = e.project
        val editor = e.getData(CommonDataKeys.EDITOR)
        val psiFile = e.getData(CommonDataKeys.PSI_FILE)

        // 基本檢查：如果沒有專案、編輯器或檔案，就隱藏
        if (project == null || editor == null || psiFile == null) {
            e.presentation.isEnabledAndVisible = false
            return
        }

        // 取得游標下的元素
        val element = getTargetElement(editor, psiFile)
        
        // 如果元素存在且是一個區域變數 (LocalVariable) 或欄位 (Field)，就顯示
        e.presentation.isEnabledAndVisible = element is PsiLocalVariable || element is PsiField
    }

    /**
     * 當使用者真的點擊 Action 時，執行這裡的邏輯
     */
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val psiFile = e.getData(CommonDataKeys.PSI_FILE) ?: return

        // 再次取得目標變數
        val element = getTargetElement(editor, psiFile) as? PsiVariable ?: return
        
        // 取得該變數的名稱，例如 "user"
        val variableName = element.name ?: return
        
        // 取得該變數的型別，並解析成 PsiClass (類別定義)
        val psiType = element.type
        val psiClass = (psiType as? PsiClassType)?.resolve() ?: return

        // 找出所有 Public 且無參數的方法 (可能是 getter)
        val getters = psiClass.allMethods.filter { method ->
            isGetter(method)
        }

        if (getters.isEmpty()) return

        // 準備要插入的程式碼
        // 我們會在游標所在行的下一行開始插入
        val sb = StringBuilder()
        val indent = "\n" // 簡單起見，直接換行，IntelliJ 之後會自動幫忙排版 (Auto Indent)

        for (method in getters) {
            // 產生 "user.getName();"
            sb.append(indent).append("$variableName.${method.name}();")
        }

        // 對編輯器進行寫入操作，必須包在 WriteCommandAction 裡，這是 IntelliJ 的規定 (Undo/Redo 支援)
        WriteCommandAction.runWriteCommandAction(project) {
            val document = editor.document
            // 找到變數宣告結束的地方 (通常是分號後面)，或者直接插在下一行
            // 這裡我們簡單計算一下插入點：游標所在行的行尾
            val caretModel = editor.caretModel
            val lineEndOffset = document.getLineEndOffset(caretModel.logicalPosition.line)
            
            document.insertString(lineEndOffset, sb.toString())
        }
    }

    // 輔助函式：取得游標下的 PsiElement
    private fun getTargetElement(editor: Editor, file: PsiFile): PsiElement? {
        val offset = editor.caretModel.offset
        val element = file.findElementAt(offset) ?: return null
        // 往上找，直到找到由 Variable 定義的節點
        return PsiTreeUtil.getParentOfType(element, PsiVariable::class.java)
    }

    // 輔助函式：判斷是否為 Getter
    private fun isGetter(method: PsiMethod): Boolean {
        // 1. 必須是 public
        if (!method.hasModifierProperty(PsiModifier.PUBLIC)) return false
        // 2. 也是 static 就跳過
        if (method.hasModifierProperty(PsiModifier.STATIC)) return false
        // 3. 檔參數個數必須為 0
        if (method.parameterList.parametersCount != 0) return false
        // 4. 回傳值不能是 void
        if (method.returnType == PsiTypes.voidType()) return false
        
        val name = method.name
        // 5. 名稱以 get 開頭 (例如 getName) 或是 is 開頭 (例如 isActive)
        return (name.startsWith("get") && name.length > 3) || 
               (name.startsWith("is") && name.length > 2)
    }
}
