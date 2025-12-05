package com.example.finaldemo.worker

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.finaldemo.SmsClassifier
import com.example.finaldemo.utils.NotificationHelper

class SpamWorker(appContext: Context, params: WorkerParameters) : Worker(appContext, params) {

    override fun doWork(): Result {
        val smsText = inputData.getString("sms_text") ?: return Result.failure()

        // Rule-based filters from SmsReceiver are now here
        if (smsText.length <= 4 || smsText.matches(Regex("^[a-zA-Z]+$"))) {
            return Result.success() // It's likely HAM, so we don't notify
        }
        if (smsText.matches(Regex("^\\d{3,6}$"))) {
            return Result.success() // It's likely an OTP, so we don't notify
        }

        val (label, confidence) = SmsClassifier.predict(smsText)

        // Notify only if spam or smishing with high confidence
        if ((label == "SPAM" || label == "SMISHING") && confidence >= 0.8) {
            NotificationHelper.sendNotification(
                applicationContext,
                "$label (${(confidence * 100).toInt()}%)",
                smsText
            )
        }

        return Result.success()
    }
}
