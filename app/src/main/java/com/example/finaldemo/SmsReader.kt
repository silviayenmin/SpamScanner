package com.example.finaldemo

import android.content.Context
import android.net.Uri

fun readInboxSms(context: Context): List<String> {
    val smsList = mutableListOf<String>()

    val cursor = context.contentResolver.query(
        Uri.parse("content://sms/inbox"),
        arrayOf("_id", "address", "body"),
        null,
        null,
        "date DESC"
    )

    cursor?.use {
        val bodyIndex = it.getColumnIndex("body")
        while (it.moveToNext()) {
            smsList.add(it.getString(bodyIndex))
        }
    }

    return smsList
}
