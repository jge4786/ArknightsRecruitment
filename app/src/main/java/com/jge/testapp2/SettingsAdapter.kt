package com.jge.testapp2


import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

enum class SettingType(val title: String) {
    RETRYLIMIT("재시도 횟수")
}

data class SettingData(val type: SettingType, var value: Int)

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
                holder.settingName.setOnClickListener {
                    AlertDialog.Builder(context)
                        .setTitle("재시도 횟수")
                        .setMessage("재시도 횟수는 인식된 태그가 5개 미만일 시, 자동으로 다시 인식을 시도하는 최대 횟수입니다.")
                        .setPositiveButton("확인") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .show()
                }
            }
            else -> {}
        }
        // 스피너 초기화 및 값 설정
        val spinnerAdapter = ArrayAdapter.createFromResource(
            context,
            R.array.spinner_values,
            android.R.layout.simple_spinner_item
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        holder.settingValue.adapter = spinnerAdapter
        holder.settingValue.setSelection(setting.value - 1) // 초기값

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

    override fun getItemCount(): Int = settings.size

    class SettingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val settingName: TextView = view.findViewById(R.id.settingName)
        val settingValue: Spinner = view.findViewById(R.id.settingValue)
    }
}
