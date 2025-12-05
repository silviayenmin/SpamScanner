package com.example.finaldemo

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun readInboxSms(context: Context): List<SmsMessage> = withContext(Dispatchers.IO) {
    val smsList = mutableListOf<SmsMessage>()

    val cursor = context.contentResolver.query(
        Uri.parse("content://sms/inbox"),
        arrayOf("_id", "address", "body", "date"),
        null,
        null,
        "date DESC"
    )

    cursor?.use {
        val idIndex = it.getColumnIndex("_id")
        val senderIndex = it.getColumnIndex("address")
        val bodyIndex = it.getColumnIndex("body")
        val dateIndex = it.getColumnIndex("date")

        while (it.moveToNext()) {
            smsList.add(
                SmsMessage(
                    id = it.getLong(idIndex),
                    sender = it.getString(senderIndex) ?: "Unknown",
                    body = it.getString(bodyIndex) ?: "",
                    timestamp = it.getLong(dateIndex)
                )
            )
        }
    }

    smsList
}


