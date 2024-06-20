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
import android.graphics.Paint
import android.media.projection.MediaProjectionManager
import android.opengl.Visibility
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TableLayout
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
        val newVersionView = findViewById<LinearLayout>(R.id.newVersionView)
        val updateButton = findViewById<TextView>(R.id.updateButton)
        newVersionTextView.text = " 받아오는 중"

        updateButton.visibility = View.GONE
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                Loader.getOpData()
            }

            val dvt = findViewById<TextView>(R.id.dataVersionText)
            dvt.text = Loader.newVersion

            newVersionView.visibility = View.GONE
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
                    val newVersionView = findViewById<LinearLayout>(R.id.newVersionView)
                    newVersionView.visibility = View.VISIBLE
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
        setPolicyButton()

        val updateButton = findViewById<TextView>(R.id.updateButton)
        updateButton.setOnTouchListener { _, event ->
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
        val libraryButton = this.findViewById<TextView>(R.id.libraryButtn)

        libraryButton.setOnClickListener {
            val ll = this.findViewById<TableLayout>(R.id.libraryList)
            val lc = this.findViewById<ImageView>(R.id.libraryChevron)
            val isHidden = ll.visibility == View.GONE
            ll.visibility = if (isHidden) { View.VISIBLE } else { View.GONE }
            lc.background = if (isHidden) { ContextCompat.getDrawable(this, R.drawable.up_chevron) } else {
                ContextCompat.getDrawable(this, R.drawable.down_chevron)
            }
        }

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

    fun setPolicyButton() {
        val policyButton = this.findViewById<TextView>(R.id.policyButton)
        policyButton.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        policyButton.setOnClickListener {
            val ii = Intent(Intent.ACTION_VIEW, Uri.parse("https://sites.google.com/view/eunprivacypolicy"))
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