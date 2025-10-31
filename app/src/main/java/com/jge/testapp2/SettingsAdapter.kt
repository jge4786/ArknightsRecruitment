package com.jge.testapp2


import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.Spinner
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

enum class SettingType(val title: String) {
    RETRYLIMIT("재시도 횟수"),
    SHOW_FAILED_TOAST("인식 실패 토스트"),
    SHOW_FAILED_HIGHLIGHT("인식 실패 강조 효과"),
    LANGUAGE("언어 설정")
}

data class SettingData(val type: SettingType, var value: Any)

class SettingsAdapter(
    private val context: Context,
    private val settings: List<SettingData>,
    private val onSettingChanged: (SettingData) -> Unit
) : RecyclerView.Adapter<SettingsAdapter.SettingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SettingViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_setting, parent, false)
        return SettingViewHolder(view)
    }

    override fun onBindViewHolder(holder: SettingViewHolder, position: Int) {
        val setting = settings[position]
        holder.settingName.text = setting.type.title
        when(setting.type) {
            SettingType.RETRYLIMIT -> {
                setRetryLimit(holder, setting)
            }
            SettingType.SHOW_FAILED_TOAST -> {
                setFailedToast(holder, setting)
            }
            SettingType.SHOW_FAILED_HIGHLIGHT -> {
                setFailedHighlight(holder, setting)
            }
            SettingType.LANGUAGE -> {
                updateLanguage(holder, setting)
            }
            else -> {}
        }
    }

    override fun getItemCount(): Int = settings.size

    class SettingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val settingName: TextView = view.findViewById(R.id.settingName)
        val settingValue: Spinner = view.findViewById(R.id.settingValue)
        val settingCheckBox: CheckBox? = view.findViewById(R.id.settingCheckBox)
    }


    // 스피너 초기화 및 값 설정
    private fun setRetryLimit(holder: SettingViewHolder, setting: SettingData) {
        holder.settingName.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("재시도 횟수")
                .setMessage("재시도 횟수는 인식된 태그가 5개 미만일 시, 자동으로 다시 인식을 시도하는 최대 횟수입니다.")
                .setPositiveButton("확인") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        val spinnerAdapter = ArrayAdapter.createFromResource(
            context,
            R.array.retry_spinner_values,
            android.R.layout.simple_spinner_item
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        holder.settingValue.adapter = spinnerAdapter
        val settingValue = setting.value as Int
        holder.settingValue.setSelection(settingValue - 1) // 초기값

        // 값 변경 처리
        holder.settingValue.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {

                if (setting.value != position + 1) { // 값이 변경되었을 때만 처리
                    setting.value = position + 1
                    onSettingChanged(setting) // 콜백 호출
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setFailedToast(holder: SettingViewHolder, setting: SettingData) {
        holder.settingValue.visibility = View.GONE
        holder.settingCheckBox?.visibility = View.VISIBLE
        holder.settingCheckBox?.isChecked = setting.value as Boolean


        holder.settingName.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("인식 실패 토스트")
                .setMessage("지정된 횟수까지 인식을 재시도한 뒤에도 인식된 태그가 5개 미만일 시, 이를 알리는 토스트를 띄울지 결정합니다.")
                .setPositiveButton("확인") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        holder.settingCheckBox?.setOnCheckedChangeListener { _, isChecked ->
            setting.value = isChecked
            onSettingChanged(setting)
        }
    }

    private fun setFailedHighlight(holder: SettingViewHolder, setting: SettingData) {
        holder.settingValue.visibility = View.GONE
        holder.settingCheckBox?.visibility = View.VISIBLE
        holder.settingCheckBox?.isChecked = setting.value as Boolean

        holder.settingName.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("인식 실패 강조 효과")
                .setMessage("지정된 횟수까지 인식을 재시도한 뒤에도 인식된 태그가 5개 미만일 시, 선택된 태그의 색상을 검정색이 아닌 빨간색으로 표시해 줍니다.")
                .setPositiveButton("확인") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        holder.settingCheckBox?.setOnCheckedChangeListener { _, isChecked ->
            setting.value = isChecked
            onSettingChanged(setting)
        }
    }

    private fun updateLanguage(holder: SettingViewHolder, setting: SettingData) {
        holder.settingName.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("태그 인식 언어")
                .setMessage("화면에 보이는 태그를 어떤 언어로 인식할지 결정합니다.")
                .setPositiveButton("확인") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        val spinnerAdapter = ArrayAdapter.createFromResource(
            context,
            R.array.language_spinner_values,
            android.R.layout.simple_spinner_item
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        holder.settingValue.adapter = spinnerAdapter
        val currentLanguage = setting.value as LanguageType

        holder.settingValue.setSelection(currentLanguage.spinnerPosition)
        // 값 변경 처리
// Int 대신 LanguageType의 값을 비교하고 설정합니다.
        holder.settingValue.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                // 스피너 position(0, 1, 2, 3...)에 해당하는 LanguageType을 가져옵니다.
                val selectedLanguage = LanguageType.entries[position] // Kotlin 1.9+
                // val selectedLanguage = LanguageType.values()[position] // 이전 버전

                // 현재 설정된 LanguageType 값과 스피너에서 선택된 값이 다를 때만 처리
                if (setting.value != selectedLanguage) {
                    // setting.value의 타입을 LanguageType으로 변경
                    setting.value = selectedLanguage
                    onSettingChanged(setting) // 콜백 호출
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }
}
