package com.jge.testapp2

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class OperatorDataActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.operator_view)
    }

    

    fun onClickSaveButton() {
        Loader.writeData(this.getPreferences(0))
    }
}