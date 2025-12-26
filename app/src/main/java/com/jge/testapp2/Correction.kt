package com.jge.testapp2

import java.util.UUID

data class CorrectionRule(
    val id: String,
    val keywords: List<String>,
    val tag: Int,
    val minLength: Int? = null,
    val maxLength: Int? = null,
    val exactLength: Int? = null
)
