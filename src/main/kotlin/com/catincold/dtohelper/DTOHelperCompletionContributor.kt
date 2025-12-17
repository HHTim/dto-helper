package com.catincold.dtohelper

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.codeInsight.template.TemplateManager
import com.intellij.codeInsight.template.impl.ConstantNode
import com.intellij.openapi.util.TextRange
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import java.beans.Introspector

class DTOHelperCompletionContributor : CompletionContributor() {

    init {
        // 1. "allGetters" Provider
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement(),
            object : CompletionProvider<CompletionParameters>() {
                override fun addCompletions(
                    parameters: CompletionParameters,
                    context: ProcessingContext,
                    result: CompletionResultSet
                ) {
                    val element = parameters.position
                    val parent = element.parent
                    if (parent !is PsiReferenceExpression) return
                    
                    val qualifier = parent.qualifierExpression ?: return
                    val type = qualifier.type as? PsiClassType ?: return
                    val psiClass = type.resolve() ?: return

                    val getters = psiClass.allMethods.filter { isGetter(it) }
                    if (getters.isEmpty()) return

                    val lookupElement = LookupElementBuilder.create("allGetters")
                        .withTypeText("Generate all getters")
                        .withInsertHandler { ctx, _ ->
                            val document = ctx.document
                            val startOffset = ctx.startOffset
                            val tailOffset = ctx.tailOffset
                            
                            // 取得 qualifier 的文字長度 (例如 "request")
                            // 用意：我們要刪除 "request.allGetters" 整段，然後重寫
                            // 假設結構是 "request." + "allGetters"
                            // ctx.startOffset 是在 dot 之後
                            val qualifierLength = qualifier.textLength
                            // +1 是因為有點 (.)
                            val deleteStart = startOffset - qualifierLength - 1
                            
                            // 安全檢查：確保我們刪除的範圍合理
                            if (deleteStart < 0) return@withInsertHandler

                            // 計算目前的縮排 (Indent)
                            // 往回找直到換行符號
                            val lineStartOffset = document.getLineStartOffset(document.getLineNumber(deleteStart))
                            val indent = document.getText(TextRange(lineStartOffset, deleteStart))
                                .takeWhile { it.isWhitespace() }

                            // 產生程式碼
                            val sb = StringBuilder()
                            val variableName = qualifier.text
                            
                            getters.forEachIndexed { index, method ->
                                if (index > 0) {
                                    sb.append("\n").append(indent)
                                }
                                
                                // 1. 取得回傳型別文字 (例如 String)
                                val returnType = method.returnType?.presentableText ?: "var"
                                
                                // 2. 取得屬性名稱 (例如 getPdfFile -> pdfFile, isAllow -> allow)
                                val propertyName = getPropertyName(method.name)

                                // 3. 組合: String pdfFile = request.getPdfFile();
                                sb.append("$returnType $propertyName = $variableName.${method.name}();")
                            }

                            // 執行替換
                            document.replaceString(deleteStart, tailOffset, sb.toString())
                        }
                    
                    result.addElement(lookupElement)
                }
            }
        )

        // 2. "builderChain" Provider
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement(),
            object : CompletionProvider<CompletionParameters>() {
                override fun addCompletions(
                    parameters: CompletionParameters,
                    context: ProcessingContext,
                    result: CompletionResultSet
                ) {
                    val element = parameters.position
                    val parent = element.parent
                    if (parent !is PsiReferenceExpression) return
                    
                    val qualifier = parent.qualifierExpression
                    if (qualifier !is PsiReferenceExpression) return
                    
                    val target = qualifier.resolve() as? PsiClass ?: return
                    
                    val builderMethod = target.findMethodsByName("builder", false)
                        .firstOrNull { it.hasModifierProperty(PsiModifier.STATIC) } ?: return

                    val lookupElement = LookupElementBuilder.create("builderChain")
                        .withTypeText("Generate builder chain")
                        .withInsertHandler { ctx, item ->
                            val document = ctx.document
                            val startOffset = ctx.startOffset
                            val tailOffset = ctx.tailOffset
                            
                            val qualifierText = qualifier.text
                            val qualifierLength = qualifier.textLength
                            val deleteStart = startOffset - qualifierLength - 1
                            if (deleteStart < 0) return@withInsertHandler
                            
                            val builderType = builderMethod.returnType as? PsiClassType
                            val builderClass = builderType?.resolve() ?: return@withInsertHandler
                            val builderClassName = builderClass.name
                            
                            val setters = builderClass.allMethods.filter { method ->
                                method.hasModifierProperty(PsiModifier.PUBLIC) &&
                                !method.hasModifierProperty(PsiModifier.STATIC) &&
                                method.parameterList.parametersCount == 1 &&
                                method.containingClass?.name == builderClassName
                            }

                            // Clean up the text first
                            document.deleteString(deleteStart, tailOffset)
                            
                            val project = ctx.project
                            val templateManager = TemplateManager.getInstance(project)
                            val template = templateManager.createTemplate("", "")
                            
                            // Smart Context Check:
                            // Check indentation/prefix before the deletion point
                            val lineStartOffset = document.getLineStartOffset(document.getLineNumber(deleteStart))
                            val prefix = document.getText(TextRange(lineStartOffset, deleteStart))
                            // If prefix is only whitespace, we assume it's a standalone statement
                            val isStandAlone = prefix.all { it.isWhitespace() }
                            
                            if (isStandAlone) {
                                val dtoName = target.name
                                val varNameDefault = Introspector.decapitalize(dtoName ?: "dto")
                                template.addTextSegment("$dtoName ")
                                template.addVariable("VAR_NAME", ConstantNode(varNameDefault), ConstantNode(varNameDefault), true)
                                template.addTextSegment(" = ")
                            }
                            
                            template.addTextSegment("$qualifierText.builder()")
                            
                            setters.forEach { method ->
                                template.addTextSegment("\n    .${method.name}(")
                                
                                // Smart Default Value
                                val param = method.parameterList.parameters[0]
                                val defaultValue = getDefaultValue(param.type)
                                
                                template.addTextSegment(defaultValue)
                                template.addTextSegment(")")
                            }
                            
                            template.addTextSegment("\n    .build();")
                            
                            templateManager.startTemplate(ctx.editor, template)
                        }
                    
                    result.addElement(lookupElement)
                }
            }
        )
    }

    private fun isGetter(method: PsiMethod): Boolean {
        if (!method.hasModifierProperty(PsiModifier.PUBLIC)) return false
        if (method.hasModifierProperty(PsiModifier.STATIC)) return false
        if (method.parameterList.parametersCount != 0) return false
        if (method.returnType == PsiTypes.voidType()) return false
        val name = method.name
        if (name == "getClass") return false
        return (name.startsWith("get") && name.length > 3) || 
               (name.startsWith("is") && name.length > 2)
    }

    private fun getPropertyName(methodName: String): String {
        return if (methodName.startsWith("get")) {
            Introspector.decapitalize(methodName.substring(3))
        } else if (methodName.startsWith("is")) {
            Introspector.decapitalize(methodName.substring(2))
        } else {
            methodName
        }
    }
    
    private fun getDefaultValue(type: PsiType): String {
        if (PsiTypes.booleanType() == type || "java.lang.Boolean" == type.canonicalText) {
            return "false"
        }
        if (PsiTypes.intType() == type || "java.lang.Integer" == type.canonicalText) {
            return "0"
        }
        if (PsiTypes.longType() == type || "java.lang.Long" == type.canonicalText) {
            return "0L"
        }
        if (PsiTypes.floatType() == type || "java.lang.Float" == type.canonicalText) {
            return "0.0f"
        }
        if (PsiTypes.doubleType() == type || "java.lang.Double" == type.canonicalText) {
            return "0.0"
        }
        if ("java.lang.String" == type.canonicalText) {
            return "\"\""
        }
        return "null"
    }
}
