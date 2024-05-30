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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_OVERLAY_PERMISSION = 1
        private const val REQUEST_MEDIA_PROJECTION = 2
    }

    private lateinit var projectionManager: MediaProjectionManager

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

        projectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        val mlkitButton = this.findViewById<Button>(R.id.mlkitButton)
        val flexboxButton = this.findViewById<Button>(R.id.flexboxButton)
        val gsonButton = this.findViewById<Button>(R.id.gsonButton)

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


        Loader.readJsonFile(this, "opData.json")

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