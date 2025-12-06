package com.example.finaldemo

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sms_messages")
data class SmsMessage(
    @PrimaryKey val id: Long,
    val sender: String,
    val body: String,
    val timestamp: Long,
    val label: String = "",       // spam/ham
    val confidence: Float = 0f,   // 0.0 - 1.0
    val category: SmsCategory = SmsCategory.INBOX,
    val isRead: Boolean = false,
    val isFromContact: Boolean = false,
    val senderName: String? = null
)

enum class SmsCategory {
    INBOX, SPAM, PROMOTIONS
}
