package com.jge.testapp2

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Check

@Composable
fun RowScope.LengthField(
    label: String,
    value: Int?,
    onValueChange: (Int?) -> Unit
) {
    OutlinedTextField(
        value = value?.toString() ?: "",
        onValueChange = { onValueChange(it.toIntOrNull()) },
        label = { Text(label, fontSize = 12.sp) },
        modifier = Modifier.weight(1f),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true
    )
}

@Composable
fun TagHeader(
    tag: String,
    onAddClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Tag: $tag",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        FilledTonalButton(
            onClick = onAddClick,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(4.dp))
            Text("추가")
        }
    }
}

@Composable
fun RuleCard(
    rule: CorrectionRule,
    onUpdate: (CorrectionRule) -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var keywordText by remember(rule.id) {
        mutableStateOf(rule.keywords.joinToString(", "))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isEditing) {
                    OutlinedTextField(
                        value = keywordText,
                        onValueChange = { keywordText = it },
                        label = { Text("키워드") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                } else {
                    Text(
                        text = if (keywordText.isBlank()) "키워드 없음"
                        else keywordText,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                IconButton(
                    onClick = {
                        if (isEditing) {
                            onUpdate(
                                rule.copy(
                                    keywords = keywordText
                                        .split(",")
                                        .map { it.trim() }
                                )
                            )
                        }
                        isEditing = !isEditing
                    }
                ) {
                    Icon(
                        imageVector = if (isEditing)
                            Icons.Default.Check
                        else
                            Icons.Default.Edit,
                        contentDescription = null
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            if (isEditing) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LengthField("최소", rule.minLength) {
                        onUpdate(rule.copy(minLength = it))
                    }
                    LengthField("최대", rule.maxLength) {
                        onUpdate(rule.copy(maxLength = it))
                    }
                    LengthField("고정", rule.exactLength) {
                        onUpdate(rule.copy(exactLength = it))
                    }
                }
            } else {
                Text(
                    text = buildString {
                        append("최소: ${rule.minLength ?: "-"} / ")
                        append("최대: ${rule.maxLength ?: "-"} / ")
                        append("고정: ${rule.exactLength ?: "-"}")
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
