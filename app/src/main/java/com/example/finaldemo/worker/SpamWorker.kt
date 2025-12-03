package com.example.finaldemo.worker

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.finaldemo.SmsClassifier
import com.example.finaldemo.utils.NotificationHelper

class SpamWorker(appContext: Context, params: WorkerParameters) : Worker(appContext, params) {

    override fun doWork(): Result {
        val smsText = inputData.getString("sms_text") ?: return Result.success()

        // Initialize model only once (safe even if already initialized)
        SmsClassifier.init(applicationContext)

        val (label, confidence) = SmsClassifier.predict(smsText)

        // Notify only if spam/scam
        if (label != "Safe") {
            NotificationHelper.sendNotification(
                applicationContext,
                "$label (${(confidence * 100).toInt()}%)",
                smsText
            )
        }

        return Result.success()
    }
}
