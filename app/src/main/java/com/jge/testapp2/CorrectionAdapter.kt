package com.jge.testapp2

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CorrectionAdapter(
    private val context: Context,
    correctionData: Map<String, List<CorrectionRule>>
) : RecyclerView.Adapter<CorrectionAdapter.HeaderViewHolder>() {

    private val groupedData = correctionData.entries.toList()
    private val ruleAdapters = mutableMapOf<String, RuleAdapter>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeaderViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_correction_header, parent, false)
        return HeaderViewHolder(view)
    }

    override fun onBindViewHolder(holder: HeaderViewHolder, position: Int) {
        val (tag, rules) = groupedData[position]
        holder.bind(tag, rules.toMutableList())
    }

    override fun getItemCount(): Int = groupedData.size

    fun getUpdatedData(): Map<String, List<CorrectionRule>> {
        val updatedData = mutableMapOf<String, List<CorrectionRule>>()
        ruleAdapters.forEach { (tag, adapter) ->
            updatedData[tag] = adapter.getRules()
        }
        return updatedData
    }

    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val headerTitle: TextView = itemView.findViewById(R.id.headerTitle)
        private val rulesRecyclerView: RecyclerView = itemView.findViewById(R.id.rulesRecyclerView)
        private val addButton: Button = itemView.findViewById(R.id.addButton)

        fun bind(tag: String, rules: MutableList<CorrectionRule>) {
            headerTitle.text = "Tag: $tag"
            val ruleAdapter = RuleAdapter(context, rules)
            rulesRecyclerView.layoutManager = LinearLayoutManager(context)
            rulesRecyclerView.adapter = ruleAdapter
            ruleAdapters[tag] = ruleAdapter

            addButton.setOnClickListener {
                val newRule = CorrectionRule(keywords = listOf(""), tag = tag.toInt())
                rules.add(newRule)
                ruleAdapter.notifyItemInserted(rules.size - 1)
            }
        }
    }
}

class RuleAdapter(
    private val context: Context,
    private var rules: MutableList<CorrectionRule>
) : RecyclerView.Adapter<RuleAdapter.RuleViewHolder>() {

    fun getRules(): List<CorrectionRule> {
        return rules
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RuleViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_correction_rule, parent, false)
        return RuleViewHolder(view)
    }

    override fun onBindViewHolder(holder: RuleViewHolder, position: Int) {
        holder.bind(rules[position])
    }

    override fun getItemCount(): Int = rules.size

    inner class RuleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val keywordEditText: EditText = itemView.findViewById(R.id.keywordEditText)
        private val minLengthEditText: EditText = itemView.findViewById(R.id.minLengthEditText)
        private val maxLengthEditText: EditText = itemView.findViewById(R.id.maxLengthEditText)
        private val exactLengthEditText: EditText = itemView.findViewById(R.id.exactLengthEditText)
        private val deleteButton: Button = itemView.findViewById(R.id.deleteButton)

        private var textWatcher: TextWatcher? = null

        fun bind(rule: CorrectionRule) {
            keywordEditText.removeTextChangedListener(textWatcher)
            minLengthEditText.removeTextChangedListener(textWatcher)
            maxLengthEditText.removeTextChangedListener(textWatcher)
            exactLengthEditText.removeTextChangedListener(textWatcher)

            keywordEditText.setText(rule.keywords.joinToString(","))
            minLengthEditText.setText(rule.minLength?.toString() ?: "")
            maxLengthEditText.setText(rule.maxLength?.toString() ?: "")
            exactLengthEditText.setText(rule.exactLength?.toString() ?: "")

            textWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val currentRule = rules[position]
                        val updatedRule = currentRule.copy(
                            keywords = keywordEditText.text.toString().split(",").map { it.trim() },
                            minLength = minLengthEditText.text.toString().toIntOrNull(),
                            maxLength = maxLengthEditText.text.toString().toIntOrNull(),
                            exactLength = exactLengthEditText.text.toString().toIntOrNull()
                        )
                        rules[position] = updatedRule
                    }
                }
            }

            keywordEditText.addTextChangedListener(textWatcher)
            minLengthEditText.addTextChangedListener(textWatcher)
            maxLengthEditText.addTextChangedListener(textWatcher)
            exactLengthEditText.addTextChangedListener(textWatcher)

            deleteButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    rules.removeAt(position)
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, rules.size)
                }
            }
        }
    }
}
