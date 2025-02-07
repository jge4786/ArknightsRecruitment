package com.jge.testapp2

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        addListner()
    }

    private fun addListner() {
        setLicenseButton()
//        val updateButton = findViewById<TextView>(R.id.updateButton)
//        updateButton.setOnTouchListener { _, event ->
//            when (event.action) {
//                MotionEvent.ACTION_UP -> {
//                    onClickNewVersion()
//                    true
//                }
//                else -> false
//            }
//        }
    }

//    fun onClickNewVersion(view: View) {
//        val newVersionAlertView = findViewById<TextView>(R.id.tvNewVersionAlert)
//        newVersionAlertView.text = " 받아오는 중"
//
//        CoroutineScope(Dispatchers.Main).launch {
//            withContext(Dispatchers.IO) {
//                Loader.getOpData()
//            }
//
//            val dvt = findViewById<TextView>(R.id.tvCurrentVersion)
//            dvt.text = Loader.newVersion
//
//            newVersionAlertView.visibility = View.GONE
//        }
//    }

    fun setLicenseButton() {
        val mlkitButton = this.findViewById<TextView>(R.id.mlkitView)
        val flexboxButton = this.findViewById<TextView>(R.id.flexboxView)
        val gsonButton = this.findViewById<TextView>(R.id.gsonView)
        val mlkitLisenceButton = this.findViewById<TextView>(R.id.mlkitLicenseView)
        val flexboxLisenceButton = this.findViewById<TextView>(R.id.flexboxLicenseView)
        val gsonLisenceButton = this.findViewById<TextView>(R.id.gsonLicenseView)

        flexboxButton.setOnClickListener {
            val ii = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/google/flexbox-layout"))
            startActivity(ii)
        }
        mlkitButton.setOnClickListener {
            val ii = Intent(Intent.ACTION_VIEW, Uri.parse("https://developers.google.com/ml-kit/vision/text-recognition/v2/android?hl=ko"))
            startActivity(ii)
        }
        gsonButton.setOnClickListener {
            val ii = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/google/gson"))
            startActivity(ii)
        }
        flexboxLisenceButton.setOnClickListener {
            val ii = Intent(Intent.ACTION_VIEW, Uri.parse("https://raw.githubusercontent.com/google/flexbox-layout/main/LICENSE"))
            startActivity(ii)
        }
        mlkitLisenceButton.setOnClickListener {
            val ii = Intent(Intent.ACTION_VIEW, Uri.parse("https://developers.google.com/ml-kit/terms"))
            startActivity(ii)
        }
        gsonLisenceButton.setOnClickListener {
            val ii = Intent(Intent.ACTION_VIEW, Uri.parse("https://raw.githubusercontent.com/google/gson/main/LICENSE"))
            startActivity(ii)
        }
    }
}