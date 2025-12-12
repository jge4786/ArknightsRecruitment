package com.jge.testapp2

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson

class CorrectionActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CorrectionAdapter
    private var correctionData: MutableMap<String, List<CorrectionRule>> = mutableMapOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_correction)

        recyclerView = findViewById(R.id.correctionRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        loadCorrectionData()
        setupRecyclerView()
        setupSaveButton()

        val backButton = findViewById<Button>(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }
    }

    private fun loadCorrectionData() {
        Loader.correctionData?.correction?.let {
            correctionData = it.toMutableMap()
        }
    }

    private fun setupRecyclerView() {
        val currentLanguageKey = Loader.language.languageKey
        val dataForLanguage = correctionData[currentLanguageKey]?.groupBy { it.tag.toString() } ?: emptyMap()

        // Flatten the grouped data to a list of pairs (tag, rules)
        val flattenedData = dataForLanguage.mapValues { entry -> entry.value }

        adapter = CorrectionAdapter(this, flattenedData)
        recyclerView.adapter = adapter
    }

    private fun setupSaveButton() {
        val saveButton = findViewById<Button>(R.id.saveButton)
        saveButton.setOnClickListener {
            saveCorrectionData()
        }
    }

    private fun saveCorrectionData() {
        val updatedRules = adapter.getUpdatedData()
        val currentLanguageKey = Loader.language.languageKey

        // Create a mutable copy to work with
        val currentCorrectionData = Loader.correctionData?.correction?.toMutableMap() ?: mutableMapOf()

        // Update the rules for the current language
        currentCorrectionData[currentLanguageKey] = updatedRules.values.flatten()

        // Create a new CorrectionFile with the updated data
        val newCorrectionFile = Loader.correctionData?.copy(correction = currentCorrectionData)

        // Save to SharedPreferences
        val editor = Pref.shared.preferences.edit()
        editor.putString(DataType.CORRECTION_DATA.key, Gson().toJson(newCorrectionFile))
        editor.apply()

        // Update the Loader's in-memory data
        Loader.correctionData = newCorrectionFile

        finish()
    }
}
