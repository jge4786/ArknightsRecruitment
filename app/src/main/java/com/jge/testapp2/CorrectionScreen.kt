package com.jge.testapp2

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CorrectionScreen(
    onBack: () -> Unit,
    viewModel: CorrectionViewModel = viewModel()
) {
    val items by viewModel.uiItems.collectAsState()
    val lang = Loader.language

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "수정 규칙 설정",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(
                        onClick = { viewModel.saveCorrectionData(onBack) },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("저장")
                    }
                }
            )
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            items(
                items = items,
                key = {
                    when (it) {
                        is CorrectionUiItem.Header ->
                            "header_${it.tag}"

                        is CorrectionUiItem.RuleItem ->
                            it.rule.id
                    }
                }
            ) { item ->
                when (item) {
                    is CorrectionUiItem.Header -> {
                        TagHeader(
                            tag = TagStrings.getString(item.tag.toInt(), lang),
                            onAddClick = { viewModel.addRule(item.tag) }
                        )
                    }

                    is CorrectionUiItem.RuleItem -> {
                        RuleCard(
                            rule = item.rule,
                            onUpdate = viewModel::updateRule
                        )
                    }
                }
            }
        }
    }
}
