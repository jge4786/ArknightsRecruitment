package com.example.testapp2

import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlin.system.exitProcess

class ExitActivity : AppCompatActivity() {

    companion object {
        const val NOTI_ID = 1 // 알림 ID를 동일하게 설정
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTI_ID)

        if (Loader.serv != null) {
            Loader.serv!!.stopForeground(true)
        }

        finishAffinity()
        exitProcess(0)
    }
}