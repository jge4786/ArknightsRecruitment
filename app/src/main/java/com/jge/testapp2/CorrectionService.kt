package com.jge.testapp2

import android.util.Log

object CorrectionService {
    fun applyCorrection(text: String, language: LanguageType): Int? {
        val rules = Loader.correctionData?.correction
            ?.firstOrNull { it.language == language.languageKey }
            ?.rules

        if (rules != null) {
            for (rule in rules) {
                if (text.contains(rule.keyword, ignoreCase = true)) {
                    val conditions = rule.conditions
                    if (conditions == null) {
                        return rule.tagId
                    } else {
                        var allConditionsMet = true
                        conditions.length_greater_than?.let {
                            if (text.length <= it) allConditionsMet = false
                        }
                        conditions.length_less_than?.let {
                            if (text.length >= it) allConditionsMet = false
                        }
                        conditions.length_equals?.let {
                            if (text.length != it) allConditionsMet = false
                        }

                        if (allConditionsMet) {
                            return rule.tagId
                        }
                    }
                }
            }
        }
        return null
    }
}
