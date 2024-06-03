//Copyright [2024] [야호]
//
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at
//
//http://www.apache.org/licenses/LICENSE-2.0
//
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.


package com.jge.testapp2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Button
import android.app.Activity
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_OVERLAY_PERMISSION = 1
        private const val REQUEST_MEDIA_PROJECTION = 2
    }

    private lateinit var projectionManager: MediaProjectionManager

    fun onClickNewVersion() {
        val newVersionTextView = findViewById<TextView>(R.id.newVersionText)
        newVersionTextView.text = " 받아오는 중"

        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                Loader.getOpData()
            }

            val dvt = findViewById<TextView>(R.id.dataVersionText)
            dvt.text = Loader.newVersion

            newVersionTextView.text = ""
        }
    }

    fun getVersionData() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val data = withContext(Dispatchers.IO) {
                    Loader.getVersionData()
                }

                val newVersion = data.getString("dataVersion")

                val dvt = findViewById<TextView>(R.id.dataVersionText)
                dvt.text = Loader.currentVersion

                if (Loader.currentVersion.compareTo(newVersion) != 0) {
                    Loader.newVersion = newVersion
                    val newVersionTextView = findViewById<TextView>(R.id.newVersionText)
                    newVersionTextView.text = " ※새로운 버전이 있습니다."
                }
            } catch (e: Exception) {
                e.printStackTrace()

            }
        }
    }

    private suspend fun loadData() {
        withContext(Dispatchers.IO) {
            Loader.loadData(this@MainActivity)
        }
        getVersionData()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.TIRAMISU && PackageManager.PERMISSION_DENIED == ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                5
            )
        }

        Pref.shared.preferences = this.getSharedPreferences("data", 0)

        projectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager


        setLicenseButton()

        val newVersionTextView = findViewById<TextView>(R.id.newVersionText)
        newVersionTextView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_UP -> {
                    onClickNewVersion()
                    true
                }
                else -> false
            }
        }

        CoroutineScope(Dispatchers.Main).launch() {
            loadData()
        }

        val buttonShowOverlay: Button = findViewById(R.id.buttonShowOverlay)
        buttonShowOverlay.setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION)
            } else {
                startProjection()
            }
        }
    }
    fun setLicenseButton() {
        val mlkitButton = this.findViewById<Button>(R.id.mlkitButton)
        val flexboxButton = this.findViewById<Button>(R.id.flexboxButton)
        val gsonButton = this.findViewById<Button>(R.id.gsonButton)
        val mlkitLisenceButton = this.findViewById<Button>(R.id.mlkitLicenseButton)
        val flexboxLisenceButton = this.findViewById<Button>(R.id.flexboxLicenseButton)
        val gsonLisenceButton = this.findViewById<Button>(R.id.gsonLicenseButton)

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_OVERLAY_PERMISSION) {
            if (Settings.canDrawOverlays(this)) {
                startProjection()
            }
        } else if (requestCode == REQUEST_MEDIA_PROJECTION) {
            if (resultCode == Activity.RESULT_OK) {
                val serviceIntent = Intent(this, OverlayService::class.java)
                serviceIntent.putExtra("resultCode", resultCode)
                serviceIntent.putExtra("data", data)
                startForegroundService(serviceIntent)
                finish()
            }
        }
    }

    private fun startProjection() {
        val intent = projectionManager.createScreenCaptureIntent()
        startActivityForResult(intent, REQUEST_MEDIA_PROJECTION)
    }
}