package com.example.finaldemo

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.finaldemo.SmsClassifier

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION != intent.action) return

        val msgs = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        val smsText = msgs.joinToString("") { it.messageBody }

        // initialize model once (fixes crash)
        SmsClassifier.init(context)

        // ---------------- RULE-BASED FILTERS ----------------
        // 1️⃣ If SMS is very short like "Ok", "Hi", "Bro", "Yes"
        if (smsText.length <= 4 || smsText.matches(Regex("^[a-zA-Z]+$"))) {
            showNotification(context, "HAM", 1.0f, smsText)
            return
        }

        // 2️⃣ If message is ONLY 3–6 digit OTP (e.g., 4412, 923311)
        if (smsText.matches(Regex("^\\d{3,6}$"))) {
            showNotification(context, "HAM", 1.0f, smsText)
            return
        }
        // ----------------------------------------------------
        val (label, confidence) = SmsClassifier.predict(smsText)
        showNotification(context, label, confidence, smsText)
    }

    private fun showNotification(context: Context, label: String, confidence: Float, msg: String) {
        val builder = NotificationCompat.Builder(context, "SMS_CHANNEL")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("SMS Detected: $label")
            .setContentText("Confidence: ${(confidence * 100).toInt()}%")
            .setStyle(NotificationCompat.BigTextStyle().bigText(msg))
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        NotificationManagerCompat.from(context).notify(System.currentTimeMillis().toInt(), builder.build())
    }
}
