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
 * 這是 "Generate Builder Chain" 的Action
 */
class GenerateBuilderAction : AnAction() {

    override fun update(e: AnActionEvent) {

        val editor = e.getData(CommonDataKeys.EDITOR)
        val psiFile = e.getData(CommonDataKeys.PSI_FILE)

        // 判斷方式：游標是否停在一個 Class 的引用上 (例如 "UserDto" 這個字)
        val element = getTargetClassElement(editor, psiFile)
        e.presentation.isEnabledAndVisible = (element != null)
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val psiFile = e.getData(CommonDataKeys.PSI_FILE) ?: return

        // 取得對應的類別定義
        val psiClass = getTargetClassElement(editor, psiFile) ?: return
        val className = psiClass.name ?: return

        // 尋找 Builder 模式的特徵
        // 1. 找是否有 "builder()" 靜態方法 (Lombok 風格)
        val builderMethod = psiClass.findMethodsByName("builder", false)
            .firstOrNull { it.hasModifierProperty(PsiModifier.STATIC) }

        // 如果找不到 builder()，這可能不是一個支援 Builder 的類別
        if (builderMethod == null) {
            // 這裡可以考慮顯示一個提示說 "找不到 builder() 方法"
            return
        }

        // 取得 Builder 的回傳型別，通常是 UserDtoBuilder
        val builderType = builderMethod.returnType as? PsiClassType
        val builderClass = builderType?.resolve() ?: return

        // 掃描 Builder Class 裡面的所有設定方法 (Setters)
        // Lombok 的 Builder setter 通常跟欄位同名 (例如 username(String val))，且回傳 Builder 自己
        val setters = builderClass.allMethods.filter { method ->
             // 必須是 public
            method.hasModifierProperty(PsiModifier.PUBLIC) &&
            // 非 static
            !method.hasModifierProperty(PsiModifier.STATIC) &&
            // 參數通常只有 1 個
            method.parameterList.parametersCount == 1 &&
            // 排除一些標準 Object 方法 (toString, etc) - 簡單過濾：宣告類別必須是 Builder 類本身或其父類
            method.containingClass?.name == builderClass.name
        }

        // 開始產生程式碼
        val sb = StringBuilder()
        val indent = "\n    " // 換行 + 縮排

        // 起手式: UserDto.builder()
        sb.append("$className.builder()")

        for (method in setters) {
            // 產生 .username(null)
            // 這裡我們預設填入 null，讓使用者自己改
            sb.append(indent).append(".${method.name}(null)")
        }

        // 結尾: .build();
        sb.append(indent).append(".build();")

        WriteCommandAction.runWriteCommandAction(project) {
            val document = editor.document
            // 插入在游標所在行的下一行
            val caretModel = editor.caretModel
            val lineEndOffset = document.getLineEndOffset(caretModel.logicalPosition.line)
            
            // 加個換行再印出來比較好看
            document.insertString(lineEndOffset, "\n" + sb.toString())
        }
    }

    // 輔助：嘗試取得游標下的 Class 引用
    private fun getTargetClassElement(editor: Editor?, file: PsiFile?): PsiClass? {
        if (editor == null || file == null) return null
        val offset = editor.caretModel.offset
        val element = file.findElementAt(offset) ?: return null

        // 情境 1: 游標在 "UserDto user;" 的 "UserDto" 上 (PsiTypeElement)
        val typeElement = PsiTreeUtil.getParentOfType(element, PsiTypeElement::class.java)
        if (typeElement != null) {
             val type = typeElement.type as? PsiClassType
             return type?.resolve()
        }

        // 增強：直接檢查是否有 Reference 指向 Class (例如 new UserDto(), 或泛型 <UserDto>)
        val reference = PsiTreeUtil.getParentOfType(element, PsiJavaCodeReferenceElement::class.java)
        if (reference != null) {
            val resolved = reference.resolve()
            if (resolved is PsiClass) {
                return resolved
            }
        }

        // 情境 2: 游標在 Class 定義本身 "public class UserDto" (PsiIdentifier -> PsiClass)
        val parentClass = PsiTreeUtil.getParentOfType(element, PsiClass::class.java)
        if (parentClass != null && parentClass.nameIdentifier == element) {
            return parentClass
        }
        
        // 情境 3: 游標在變數宣告 "UserDto user" (PsiVariable) -> 取其 Struct Type
        val variable = PsiTreeUtil.getParentOfType(element, PsiVariable::class.java)
        if (variable != null) {
            val type = variable.type as? PsiClassType
            return type?.resolve()
        }

        return null
    }
}
