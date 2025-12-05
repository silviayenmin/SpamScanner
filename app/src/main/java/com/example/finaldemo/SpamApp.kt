package com.example.finaldemo

import android.app.Application

class SpamApp : Application() {

    val database: SmsDatabase by lazy { SmsDatabase.getDatabase(this) }

    override fun onCreate() {
        super.onCreate()
        SmsClassifier.init(this)
    }
}
