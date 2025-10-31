package com.jge.testapp2

data class CorrectionRule(
    val keywords: List<String>,
    val tag: Int,
    val minLength: Int? = null,
    val maxLength: Int? = null,
    val exactLength: Int? = null
)
