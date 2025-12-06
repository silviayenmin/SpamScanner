package com.example.finaldemo

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SmsDao {
    @Query("SELECT * FROM sms_messages ORDER BY timestamp DESC")
    fun getAll(): Flow<List<SmsMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(messages: List<SmsMessage>)

    @Query("SELECT * FROM sms_messages WHERE id = :id")
    suspend fun getById(id: Long): SmsMessage?

    @Query("SELECT * FROM sms_messages WHERE body LIKE '%' || :query || '%' OR sender LIKE '%' || :query || '%' OR senderName LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchMessages(query: String): Flow<List<SmsMessage>>

    @Query("SELECT * FROM sms_messages WHERE sender LIKE '%' || :senderQuery || '%' ORDER BY timestamp DESC")
    fun getMessagesFromSenders(senderQuery: String): Flow<List<SmsMessage>>

    @Query("UPDATE sms_messages SET isRead = 1 WHERE sender = :senderAddress")
    suspend fun markConversationAsRead(senderAddress: String)

    @Query("SELECT * FROM sms_messages WHERE isRead = 0 ORDER BY timestamp DESC")
    fun getUnreadMessages(): Flow<List<SmsMessage>>

    @Query("SELECT * FROM sms_messages WHERE isFromContact = 1 ORDER BY timestamp DESC")
    fun getKnownSenderMessages(): Flow<List<SmsMessage>>
}
