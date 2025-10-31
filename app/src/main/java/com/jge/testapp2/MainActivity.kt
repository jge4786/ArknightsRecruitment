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

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Paint
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

    private var canUpdate: Boolean = false

    private fun requestPermission() {

        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.TIRAMISU) {
            if (PackageManager.PERMISSION_DENIED == ContextCompat.checkSelfPermission(
                    this, android.Manifest.permission.POST_NOTIFICATIONS)
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS,
                        android.Manifest.permission.FOREGROUND_SERVICE,
                        "android:project_media"),
                    5
                )
            }
        }
    }


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        val rootView = findViewById<View>(R.id.activity_main_layout)

        ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, insets ->
            val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            val navigationBarHeight = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom

            // 상단/하단 여백 확보
            view.setPadding(0, statusBarHeight, 0, navigationBarHeight)
            insets
        }

        val insetsController = WindowCompat.getInsetsController(window, rootView)
        insetsController.show(WindowInsetsCompat.Type.systemBars())
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        requestPermission()

        Pref.shared.preferences = this.getSharedPreferences("data", 0)

        projectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        setPolicyButton()

        val buttonWidth = (Resources.getSystem().displayMetrics.widthPixels * 0.8).toInt()

        val versionInfoView = findViewById<LinearLayout>(R.id.versionInfoView)
        versionInfoView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_UP -> {
                    if(canUpdate) {
                        onClickNewVersion()
                    }
                    true
                }
                else -> false
            }
        }

        CoroutineScope(Dispatchers.Main).launch() {
            loadData()

            setSettings()
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

        val overlayBtnLayoutParams = buttonShowOverlay.layoutParams as RelativeLayout.LayoutParams

        overlayBtnLayoutParams.width = buttonWidth
        buttonShowOverlay.layoutParams = overlayBtnLayoutParams

        val buttonSettings: Button = findViewById(R.id.buttonSettings)
        buttonSettings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
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

    fun setPolicyButton() {
        val policyButton = this.findViewById<TextView>(R.id.policyButton)
        policyButton.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        policyButton.setOnClickListener {
            val ii = Intent(Intent.ACTION_VIEW, Uri.parse("https://sites.google.com/view/eunprivacypolicy"))
            startActivity(ii)
        }
    }

    private fun onClickNewVersion() {
        val newVersionTextView = findViewById<TextView>(R.id.newVersionText)
        newVersionTextView.text = " 받아오는 중"

        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                Loader.getOpData()
            }

            val dvt = findViewById<TextView>(R.id.dataVersionText)
            dvt.text = Loader.newVersion

            newVersionTextView.visibility = View.GONE
            canUpdate = false
        }
    }

    fun setSettings() {
        val settings = listOf(
            SettingData(SettingType.RETRYLIMIT, Loader.retryLimit),
            SettingData(SettingType.SHOW_FAILED_TOAST, Loader.showFailedToastState),
            SettingData(SettingType.SHOW_FAILED_HIGHLIGHT, Loader.showFailedHighlightState),
            SettingData(SettingType.LANGUAGE, Loader.language)
        )

        // RecyclerView 초기화
        val recyclerView: RecyclerView = findViewById(R.id.settingsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = SettingsAdapter(this, settings) {
            when(it.type) {
                SettingType.RETRYLIMIT -> {
                    val value = it.value as Int
                    Loader.setRetryData(value)
                }
                SettingType.SHOW_FAILED_TOAST -> {
                    val value = it.value as Boolean
                    Loader.setShowFailedToast(value)
                }
                SettingType.SHOW_FAILED_HIGHLIGHT -> {
                    val value = it.value as Boolean
                    Loader.setShowFailedHighlight(value)
                }
                SettingType.LANGUAGE -> {
                    val value = it.value as LanguageType
                    Loader.updateLanguage(value)
                }
                else -> {}
            }
        }
    }

    fun getVersionData() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val data = withContext(Dispatchers.IO) {
                    Loader.getVersionData()
                }
                var newVersion = if (Loader.language != LanguageType.CHINESE) {
                    data.getString("dataVersion")
                } else {
                    data.getString("dataVersionCN")
                }

                var newCorrectionVersion = data.getString("correctionVersion")

                val dvt = findViewById<TextView>(R.id.dataVersionText)
                dvt.text = Loader.currentVersion

                if (Loader.currentVersion.compareTo(newVersion) != 0) {
                    Loader.newVersion = newVersion
                    val newVersionView = findViewById<TextView>(R.id.newVersionText)
                    newVersionView.visibility = View.VISIBLE
                    canUpdate = true
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

}