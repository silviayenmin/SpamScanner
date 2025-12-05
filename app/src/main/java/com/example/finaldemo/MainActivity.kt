package com.example.finaldemo

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.role.RoleManager
import android.content.Intent
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import android.util.Log
import androidx.compose.runtime.collectAsState
import com.example.finaldemo.ui.theme.FinalDemoTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    private val smsDao by lazy { (application as SpamApp).database.smsDao() }
    private val smsListFlow by lazy { smsDao.getAll() }
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val smsObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean) {
            super.onChange(selfChange)
            refreshSmsMessages()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createNotificationChannel()

        // Request SMS & Notification permissions
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                android.Manifest.permission.READ_SMS,
                android.Manifest.permission.RECEIVE_SMS,
                android.Manifest.permission.POST_NOTIFICATIONS
            ),
            100
        )

        contentResolver.registerContentObserver(
            Uri.parse("content://sms/inbox"),
            true,
            smsObserver
        )

        refreshSmsMessages()

        setContent {
            FinalDemoTheme {
                val navController = rememberNavController()
                val currentSmsList by smsListFlow.collectAsState(initial = emptyList())
                val isLoading by this.isLoading.collectAsState()

                NavHost(navController = navController, startDestination = "inbox") {
                    composable("inbox") {
                        InboxScreen(
                            smsList = currentSmsList,
                            navController = navController,
                            isLoading = isLoading
                        )
                    }
                    composable(
                        "conversation/{sender}",
                        arguments = listOf(navArgument("sender") { type = androidx.navigation.NavType.StringType })
                    ) { backStackEntry ->
                        val sender = backStackEntry.arguments?.getString("sender") ?: ""
                        ConversationScreen(
                            sender = sender,
                            smsList = currentSmsList,
                            navController = navController
                        )
                    }
                }
            }
        }
        
        requestDefaultSmsRole()
    }

    private fun refreshSmsMessages() {
        _isLoading.value = true
        lifecycleScope.launch {
            val startTime = System.currentTimeMillis()
            Log.d("MainActivity", "Starting refreshSmsMessages at $startTime")

            val messagesFromDevice = readInboxSms(applicationContext)
            val readDeviceTime = System.currentTimeMillis()
            Log.d("MainActivity", "Read ${messagesFromDevice.size} messages from device in ${readDeviceTime - startTime} ms")

            val messagesFromDb = smsDao.getAll().first()
            val readDbTime = System.currentTimeMillis()
            Log.d("MainActivity", "Read ${messagesFromDb.size} messages from DB in ${readDbTime - readDeviceTime} ms")

            val dbIds = messagesFromDb.map { it.id }.toSet()
            val newMessages = messagesFromDevice.filter { it.id !in dbIds }
            val filterNewTime = System.currentTimeMillis()
            Log.d("MainActivity", "Found ${newMessages.size} new messages in ${filterNewTime - readDbTime} ms")


            if (newMessages.isNotEmpty()) {
                val classifiedMessages = withContext(Dispatchers.Default) {
                    newMessages.map { sms ->
                        try {
                            val (label, confidence) = SmsClassifier.predict(sms.body)
                            val category = when (label) {
                                "SPAM", "SMISHING" -> SmsCategory.SPAM
                                else -> SmsCategory.INBOX
                            }
                            sms.copy(label = label, confidence = confidence, category = category)
                        } catch (e: Exception) {
                            Log.e("MainActivity", "Error classifying SMS", e)
                            sms.copy(label = "ERROR")
                        }
                    }
                }
                val classifiedTime = System.currentTimeMillis()
                Log.d("MainActivity", "Classified ${classifiedMessages.size} messages in ${classifiedTime - filterNewTime} ms")

                smsDao.insertAll(classifiedMessages)
                val insertTime = System.currentTimeMillis()
                Log.d("MainActivity", "Inserted ${classifiedMessages.size} messages into DB in ${insertTime - classifiedTime} ms")
            }
            _isLoading.value = false
            val endTime = System.currentTimeMillis()
            Log.d("MainActivity", "Finished refreshSmsMessages in ${endTime - startTime} ms")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        contentResolver.unregisterContentObserver(smsObserver)
    }

    private fun requestDefaultSmsRole() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(RoleManager::class.java)
            if (!roleManager.isRoleHeld(RoleManager.ROLE_SMS)) {
                val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_SMS)
                startActivity(intent)
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "SMS_CHANNEL",
                getString(R.string.sms_alerts_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}

