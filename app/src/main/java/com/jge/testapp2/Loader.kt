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

enum class DataType(val key: String) {
    OPDATA("opData"),
    RETRYLIMIT("retryLimit"),
    SHOW_FAILED_TOAST("showFailedToastState"),
    SHOW_FAILED_HIGHLIGHT("showFailedHighlightState")
}

enum class RarityType {
    ONE,
    FOUR,
    FIVE,
    SIX
}
enum class LanguageType {
    KOREAN,
    CHINESE,
    JAPANESE,
    ENGLISH
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

        fun setRetryData(newValue: Int) {
            retryLimit = newValue
            writeData(DataType.RETRYLIMIT, retryLimit)
        }

        private fun loadSettingData(context: Context) {
            loadRetryData(context)
            loadCheckboxData(context)
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
//            val languagePref = Pref.shared.preferences.getInt("language", -1)
//
//            if(languagePref < 0) {
//                writeData(DataType.RETRYLIMIT, 0)
//            } else {
//                language = LanguageType.values().getOrNull(languagePref) ?: LanguageType.KOREAN
//            }
        }

        private fun loadSettingsData(context: Context) {
            loadSettingData(context)
            loadLanguageData(context)


        }

        private fun loadOpData(context: Context) {
            val rawPref = Pref.shared.preferences.getString("opData", null)
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
                data = readJsonFile(context, "opData.json")
                writeData(DataType.OPDATA, data)
            }
        }

        fun loadData(context: Context) {
            loadOpData(context)
            loadSettingsData(context)
        }

        private fun writeData(type: DataType, data: Any) {
            val editor = Pref.shared.preferences.edit()

            when (type) {
                DataType.OPDATA -> {
                    val json = GsonBuilder().create().toJson(data)
                    editor.putString(type.key, json)
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
            }
            editor.apply()
        }

        private fun loadDefaultVersion(context: Context) {
            val inputStream = context.assets.open("defaultVersion.json")
            val reader = BufferedReader(InputStreamReader(inputStream))
            val stringBuilder = StringBuilder()
            var line: String? = reader.readLine()
            while (line != null) {
                stringBuilder.append(line)
                line = reader.readLine()
            }
            reader.close()
            inputStream.close()

            val jsonString = stringBuilder.toString()
            val jsonObject = JSONObject(jsonString)
            currentVersion = jsonObject.getString("dataVersion")
            val editor = Pref.shared.preferences.edit()
            editor.putString("opVersion", currentVersion)
            editor.apply()
        }


        fun readJsonFile(context: Context, fileName: String): List<Item> {
            val inputStream = context.assets.open(fileName)
            val reader = BufferedReader(InputStreamReader(inputStream))
            val stringBuilder = StringBuilder()
            var line: String? = reader.readLine()
            while (line != null) {
                stringBuilder.append(line)
                line = reader.readLine()
            }
            val listType = object : TypeToken<List<Item>>() {}.type
            return Gson().fromJson(stringBuilder.toString(), listType)
        }

        fun tagToArray(tag: Int): List<String> {
            var result: MutableList<String> = mutableListOf()

            var array = convert(tag)

            array.forEach {
                when (it) {
                    1 -> result.add("가드")
                    2 -> result.add("스나이퍼")
                    3 -> result.add("디펜더")
                    4 -> result.add("메딕")
                    5 -> result.add("서포터")
                    6 -> result.add("캐스터")
                    7 -> result.add("스페셜리스트")
                    8 -> result.add("뱅가드")
                    9 -> result.add("근거리")
                    10 -> result.add("원거리")
                    11 -> result.add("고급 특별 채용")
                    12 -> result.add("제어형")
                    13 -> result.add("누커")
                    14 -> result.add("특별 채용")
                    15 -> result.add("힐링")
                    16 -> result.add("지원")
                    17 -> result.add("신입")
                    18 -> result.add("코스트+")
                    19 -> result.add("딜러")
                    20 -> result.add("생존형")
                    21 -> result.add("범위공격")
                    22 -> result.add("방어형")
                    23 -> result.add("감속")
                    24 -> result.add("디버프")
                    25 -> result.add("쾌속부활")
                    26 -> result.add("강제이동")
                    27 -> result.add("소환")
                    28 -> result.add("로봇")
                    29 -> result.add("원소")
                    else -> result.add("Unknown")
                }
            }

            return result
        }

        fun tagToInt(tag: String): Int {
            return when (tag) {
                "가드" -> 2
                "스나이퍼" -> 4
                "디펜더" -> 8
                "메딕" -> 16
                "서포터" -> 32
                "캐스터" -> 64
                "스페셜리스트" -> 128
                "뱅가드" -> 256
                "근거리" -> 512
                "원거리" -> 1024
                "고급특별채용" -> 2048
                "제어형" -> 4096
                "누커" -> 8192
                "특별채용" -> 16384
                "힐링" -> 32768
                "지원" -> 65536
                "신입" -> 131072
                "코스트+" -> 262144
                "딜러" -> 524288
                "생존형" -> 1048576
                "범위공격" -> 2097152
                "방어형" -> 4194304
                "감속" -> 8388608
                "디버프" -> 16777216
                "쾌속부활" -> 33554432
                "강제이동" -> 67108864
                "소환" -> 134217728
                "로봇" -> 268435456
                "원소" -> 536870912
                else -> -1 // Unknown
            }
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
        private const val opUrl = "https://raw.githubusercontent.com/jge4786/ArknightsRecruitment/main/app/src/main/assets/opData.json"
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
            val url = URL(opUrl)
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
    }

}