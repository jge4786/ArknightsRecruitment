package com.jge.testapp2

import android.util.Log
object CorrectionService {
    fun applyCorrection(text: String, language: LanguageType): Int? {
        val rules = Loader.correctionData?.get(language.name) ?: return null

        for (rule in rules) {
            for (keyword in rule.keywords) {
                if (text.contains(keyword, ignoreCase = true)) {
                    val textLength = text.length
                    val minLength = rule.minLength
                    val maxLength = rule.maxLength
                    val exactLength = rule.exactLength

                    if (minLength != null && textLength < minLength) continue
                    if (maxLength != null && textLength > maxLength) continue
                    if (exactLength != null && textLength != exactLength) continue

                    return rule.tag
                }
            }
        }
        return null
    }
}
