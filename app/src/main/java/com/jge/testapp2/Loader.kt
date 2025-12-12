package com.jge.testapp2

import android.app.Service
import android.content.Context
import android.graphics.Color
import java.io.BufferedReader
import java.io.InputStreamReader
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import kotlin.Exception


data class Item(
    val tag: Int,
    val id: String,
    val name: String,
    val rarity: String
)

data class CorrectionFile(
    val correction: Map<String, List<CorrectionRule>>,
    val version: String
)

data class CorrectionConditions(
    val length_greater_than: Int? = null,
    val length_less_than: Int? = null,
    val length_equals: Int? = null
)


enum class DataType(val key: String) {
    OPDATA("opData"),
    RETRYLIMIT("retryLimit"),
    SHOW_FAILED_TOAST("showFailedToastState"),
    SHOW_FAILED_HIGHLIGHT("showFailedHighlightState"),
    LANGUAGE("language"),
    CORRECTION_DATA("correctionData")
}

enum class RarityType {
    ONE,
    FOUR,
    FIVE,
    SIX
}
enum class LanguageType(val key: String, val languageKey: String) {
    KOREAN("opData", "korean"),
    CHINESE("opDataCN", "chinese"),
    JAPANESE("opDataJP", "japanese"),
    ENGLISH("opDataEN", "english");

    val spinnerPosition: Int
        get() = this.ordinal
}

class Loader {
    companion object {
        lateinit var data: List<Item>
        lateinit var currentVersion: String

        var language: LanguageType = LanguageType.KOREAN

        /* 메인 설정값 */
        var retryLimit: Int = 3
        var showFailedToastState: Boolean = false
        var showFailedHighlightState: Boolean = false

        var newVersion: String = "0.0.0"

        var correctionData: CorrectionFile? = null
        var newCorrectionVersion: String = "1"

        fun setRetryData(newValue: Int) {
            retryLimit = newValue
            writeData(DataType.RETRYLIMIT, retryLimit)
        }

        fun updateLanguage(newValue: LanguageType) {
            language = newValue
            writeData(DataType.LANGUAGE, language)
        }

        private fun loadRetryData(context: Context) {
            val retryLimitPref = Pref.shared.preferences.getInt(DataType.RETRYLIMIT.key, -1)

            if(retryLimitPref <= 0) {
                writeData(DataType.RETRYLIMIT, 3)
                retryLimit = 3
            } else {
                retryLimit = retryLimitPref
            }
        }

        private fun loadCheckboxData(context: Context) {
            showFailedToastState = Pref.shared.preferences.getBoolean(DataType.SHOW_FAILED_TOAST.key, false)
            showFailedHighlightState = Pref.shared.preferences.getBoolean(DataType.SHOW_FAILED_HIGHLIGHT.key, false)
        }

        fun setShowFailedToast(newValue: Boolean) {
            showFailedToastState = newValue
            writeData(DataType.SHOW_FAILED_TOAST, showFailedToastState)
        }

        fun setShowFailedHighlight(newValue: Boolean) {
            showFailedHighlightState = newValue
            writeData(DataType.SHOW_FAILED_HIGHLIGHT, showFailedHighlightState)
        }

        private fun loadLanguageData(context: Context) {
            val languagePref = Pref.shared.preferences.getString(DataType.LANGUAGE.key, LanguageType.KOREAN.name)

            language = languagePref?.let { LanguageType.valueOf(it) } ?: LanguageType.KOREAN

            loadOpData(context, language)
        }

        private fun loadSettingsData(context: Context) {
            loadRetryData(context)
            loadCheckboxData(context)
        }

        private fun loadOpData(context: Context, language: LanguageType) {
            val rawPref = Pref.shared.preferences.getString(DataType.OPDATA.key, null)

            currentVersion = Pref.shared.preferences.getString("opVersion", null).toString()

            if (currentVersion == "null") {
                loadDefaultVersion(context)
            }

            if(rawPref != null && rawPref != "[]"){
                try {
                    data = GsonBuilder().create().fromJson(
                        rawPref, object : TypeToken<ArrayList<Item>>() {}.type
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                val fileName = language.key + ".json"
                data = readJsonFile(context, fileName)
                writeData(DataType.OPDATA, data)
            }
        }

        fun loadData(context: Context) {
            loadLanguageData(context)
            loadSettingsData(context)
            loadCorrectionData(context)
        }

        private fun writeData(type: DataType, data: Any?) {
            val editor = Pref.shared.preferences.edit()

            when (type) {
                DataType.OPDATA,
                DataType.CORRECTION_DATA -> {
                    if (data != null) {
                        val json = GsonBuilder().create().toJson(data)
                        editor.putString(type.key, json)
                    }
                }
                DataType.RETRYLIMIT -> {
                    val tmp = data is Int

                    if (data is Int) { editor.putInt(type.key, data) }
                    else             { editor.putInt(type.key, 3) }
                }
                DataType.SHOW_FAILED_TOAST, DataType.SHOW_FAILED_HIGHLIGHT -> {
                    val tmp = data is Boolean

                    if (data is Boolean) { editor.putBoolean(type.key, data) }
                    else                 { editor.putBoolean(type.key, false) }
                }
                DataType.LANGUAGE -> {
                    if (data is LanguageType) { editor.putString(type.key, data.name) }
                    else                      { editor.putString(type.key, LanguageType.KOREAN.name) }
                }

            }
            editor.apply()
        }

        private fun loadCorrectionData(context: Context) {
            val rawPref = Pref.shared.preferences.getString(DataType.CORRECTION_DATA.key, null)

            if (rawPref != null) {
                try {
                    correctionData = GsonBuilder().create().fromJson(
                        rawPref, object : TypeToken<CorrectionFile>() {}.type
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    // 파싱 실패 시 기본값으로 대체
                    loadDefaultCorrectionData(context)
                }
            } else {
                loadDefaultCorrectionData(context)
            }
        }

        private fun loadDefaultCorrectionData(context: Context) {
            val jsonString = readAssetFile(context, "correction.json")
            if (jsonString != null) {
                correctionData = Gson().fromJson(jsonString, CorrectionFile::class.java)
                writeData(DataType.CORRECTION_DATA, correctionData)
                val editor = Pref.shared.preferences.edit()
                editor.putString("correctionVersion", correctionData?.version ?: "1")
                editor.apply()
            }
        }


        private fun loadDefaultVersion(context: Context) {
            val jsonString = readAssetFile(context, "defaultVersion.json")
            if (jsonString != null) {
                val jsonObject = JSONObject(jsonString)
                currentVersion = if (language != LanguageType.CHINESE) {
                    jsonObject.getString("dataVersion")
                } else {
                    jsonObject.getString("dataVersionCN")
                }
                val editor = Pref.shared.preferences.edit()
                editor.putString("opVersion", currentVersion)
                editor.apply()
            }
        }

        fun readAssetFile(context: Context, fileName: String): String? {
            return try {
                val inputStream = context.assets.open(fileName)
                val reader = BufferedReader(InputStreamReader(inputStream))
                val stringBuilder = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    stringBuilder.append(line)
                }
                reader.close()
                inputStream.close()
                stringBuilder.toString()
            } catch (e: IOException) {
                e.printStackTrace()
                null
            }
        }


        fun readJsonFile(context: Context, fileName: String): List<Item> {
            val jsonString = readAssetFile(context, fileName)
            val listType = object : TypeToken<List<Item>>() {}.type
            return Gson().fromJson(jsonString, listType)
        }

        fun tagToArray(tag: Int): List<String> {
            val result = mutableListOf<String>()
            val array = convert(tag)

            array.forEach { id ->
                val name = TagStrings.getString(id, language)
                result.add(name)
            }

            return result.ifEmpty { listOf("Unknown") }
        }

        fun tagToInt(tag: String): Int {
            return TagStrings.tagToInt(tag, language)
        }

        fun convert(number: Int): List<Int> {
            val binaryString = Integer.toBinaryString(number).reversed()
            val onesIndices = mutableListOf<Int>()

            for (i in binaryString.indices) {
                if (binaryString[i] == '1') {
                    onesIndices.add(i)
                }
            }

            return onesIndices
        }

        public var serv: Service? = null


        private const val versionUrl = "https://raw.githubusercontent.com/jge4786/ArknightsRecruitment/main/app/src/main/assets/Versions.json"
        private const val opUrl = "https://raw.githubusercontent.com/jge4786/ArknightsRecruitment/main/app/src/main/assets/"
        private const val correctionUrl = "https://raw.githubusercontent.com/jge4786/ArknightsRecruitment/main/app/src/main/assets/correction.json"
        @Throws(IOException::class)
        fun getVersionData(): JSONObject {
            val url = URL(versionUrl)
            val connection = url.openConnection() as HttpURLConnection
            val resultData = try {
                connection.inputStream.bufferedReader().readText()
            } finally {
                connection.disconnect()
            }

            return JSONObject(resultData)
        }

        @Throws(IOException::class)
        fun getOpData() {
            val realUrl = opUrl + language.key + ".json"
            val url = URL(realUrl)
            val connection = url.openConnection() as HttpURLConnection
            val resultData = try {
                connection.inputStream.bufferedReader().readText()
            } finally {
                connection.disconnect()
            }

            data = GsonBuilder().create().fromJson(
                resultData, object: TypeToken<ArrayList<Item>>(){}.type
            )

            val editor = Pref.shared.preferences.edit()

            editor.putString("opData", resultData)
            try {
                editor.putString("opVersion", newVersion)
                editor.apply()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun getCorrectionData() {
            val url = URL(correctionUrl)
            val connection = url.openConnection() as HttpURLConnection
            val resultData = try {
                connection.inputStream.bufferedReader().readText()
            } finally {
                connection.disconnect()
            }

            correctionData = GsonBuilder().create().fromJson(
                resultData, object: TypeToken<CorrectionFile>(){}.type
            )


            val editor = Pref.shared.preferences.edit()

            editor.putString(DataType.CORRECTION_DATA.key, resultData)

            try {
                editor.putString("correctionVersion", correctionData?.version)
                editor.apply()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}