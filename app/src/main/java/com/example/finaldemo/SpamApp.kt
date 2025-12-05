package com.example.finaldemo

import android.app.Application

class SpamApp : Application() {
    override fun onCreate() {
        super.onCreate()
        SmsClassifier.init(this)
    }
}
