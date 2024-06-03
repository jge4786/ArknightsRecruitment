package com.jge.testapp2

import android.content.SharedPreferences

class Pref {
    companion object {
        val shared: Pref = Pref()
    }

    lateinit var preferences: SharedPreferences
}