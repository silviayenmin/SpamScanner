package com.example.finaldemo

data class SmsMessage(
    val id: Long,
    val sender: String,
    val body: String,
    val timestamp: Long,
    val label: String = "",       // spam/ham
    val confidence: Float = 0f,   // 0.0 - 1.0
    val category: SmsCategory = SmsCategory.INBOX
)

enum class SmsCategory {
    INBOX, SPAM, PROMOTIONS
}
