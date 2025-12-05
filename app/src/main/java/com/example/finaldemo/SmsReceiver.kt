package com.example.finaldemo

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.finaldemo.worker.SpamWorker

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION != intent.action) return

        val msgs = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        val smsText = msgs.joinToString("") { it.messageBody }

        // ---------------- RULE-BASED FILTERS ----------------
        // 1️⃣ If SMS is very short like "Ok", "Hi", "Bro", "Yes"
        if (smsText.length <= 4 || smsText.matches(Regex("^[a-zA-Z]+$"))) {
            // We can choose to not classify these messages or handle them differently.
            // For now, we will still pass them to the worker.
        }

        // 2️⃣ If message is ONLY 3–6 digit OTP (e.g., 4412, 923311)
        if (smsText.matches(Regex("^\\d{3,6}$"))) {
            // Similar to the above, we can decide what to do.
            // For now, let the worker handle it.
        }
        // ----------------------------------------------------

        val workRequest = OneTimeWorkRequestBuilder<SpamWorker>()
            .setInputData(Data.Builder().putString("sms_text", smsText).build())
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
    }
}
