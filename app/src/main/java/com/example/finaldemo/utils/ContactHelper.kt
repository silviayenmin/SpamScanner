package com.example.finaldemo.utils

import android.content.Context
import android.provider.ContactsContract
import android.net.Uri

object ContactHelper {

    fun getContactName(context: Context, phoneNumber: String): String? {
        val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber))
        val projection = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME)
        var contactName: String? = null

        context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameColumnIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)
                if (nameColumnIndex >= 0) {
                    contactName = cursor.getString(nameColumnIndex)
                }
            }
        }
        return contactName
    }
}
