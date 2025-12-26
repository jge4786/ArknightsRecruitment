package com.jge.testapp2
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

sealed interface CorrectionUiItem {
    data class Header(val tag: Int) : CorrectionUiItem
    data class RuleItem(val rule: CorrectionRule) : CorrectionUiItem
}

class CorrectionViewModel : ViewModel() {

    private val currentLanguageKey = Loader.language.languageKey

    private val _uiItems = MutableStateFlow<List<CorrectionUiItem>>(emptyList())
    val uiItems: StateFlow<List<CorrectionUiItem>> = _uiItems

    init {
        loadData()
    }

    private fun loadData() {
        val allData = Loader.correctionData?.correction ?: return
        val dataForLanguage = allData[currentLanguageKey] ?: emptyList()

        val fixedData = dataForLanguage.map { rule ->
            if (rule.id.isNullOrBlank()) {
                rule.copy(id = UUID.randomUUID().toString())
            } else {
                rule
            }
        }

        _uiItems.value = buildUiItems(fixedData)
    }

    private fun buildUiItems(rules: List<CorrectionRule>): List<CorrectionUiItem> {
        return rules
            .groupBy { it.tag }
            .toSortedMap()
            .flatMap { (tag, rulesForTag) ->
                buildList {
                    add(CorrectionUiItem.Header(tag))
                    rulesForTag.forEach {
                        add(CorrectionUiItem.RuleItem(it))
                    }
                }
            }
    }

    fun addRule(tag: Int) {
        _uiItems.update { items ->
            val newRule = CorrectionRule(
                id = UUID.randomUUID().toString(),
                keywords = listOf(""),
                tag = tag
            )

            val index = items.indexOfLast {
                it is CorrectionUiItem.RuleItem && it.rule.tag == tag
            }

            items.toMutableList().apply {
                add(index + 1, CorrectionUiItem.RuleItem(newRule))
            }
        }
    }

    fun updateRule(updatedRule: CorrectionRule) {
        _uiItems.update { items ->
            items.map {
                if (it is CorrectionUiItem.RuleItem && it.rule.id == updatedRule.id) {
                    it.copy(rule = updatedRule)
                } else {
                    it
                }
            }
        }
    }

    fun saveCorrectionData(onSuccess: () -> Unit) {
        val flattenedRules = uiItems.value
            .filterIsInstance<CorrectionUiItem.RuleItem>()
            .map { it.rule }

        val currentCorrectionData =
            Loader.correctionData?.correction?.toMutableMap() ?: mutableMapOf()

        currentCorrectionData[currentLanguageKey] = flattenedRules

        val newCorrectionFile =
            Loader.correctionData?.copy(correction = currentCorrectionData)

        Pref.shared.preferences.edit()
            .putString(
                DataType.CORRECTION_DATA.key,
                Gson().toJson(newCorrectionFile)
            )
            .apply()

        Loader.correctionData = newCorrectionFile
        onSuccess()
    }
}