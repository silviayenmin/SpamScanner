package com.example.finaldemo

import android.Manifest
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import android.util.Log
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.snapshotFlow
import com.example.finaldemo.ui.theme.FinalDemoTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    // Use a StateFlow to manage the SMS list, for debouncing
    private val _smsListFlow = MutableStateFlow(listOf<SmsMessage>())
    // Expose as an immutable StateFlow
    val smsListFlow: StateFlow<List<SmsMessage>> = _smsListFlow.asStateFlow()

    private var isClassifying by mutableStateOf(false) // New state variable

    private val smsObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean) {
            super.onChange(selfChange)
            lifecycleScope.launch {
                // When content changes, reload messages and push to flow
                _smsListFlow.value = readInboxSms(applicationContext)
            }
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

        // Initial load should push to the flow
        lifecycleScope.launch {
            _smsListFlow.value = readInboxSms(applicationContext)
        }

        setContent {
            FinalDemoTheme {
                val navController = rememberNavController()
                // Collect the StateFlow as Compose state
                val currentSmsList by smsListFlow.collectAsState()

                LaunchedEffect(currentSmsList) {
                    if (currentSmsList.isEmpty()) return@LaunchedEffect // Avoid processing empty list

                    // Debounce the actual heavy classification work
                    snapshotFlow { currentSmsList }
                        .debounce(300L) // Wait for 300ms of no changes before starting classification
                        .onEach { latestList ->
                            isClassifying = true // Start classifying, set state to true
                            val classifiedMessages = withContext(Dispatchers.Default) {
                                val tempClassifiedList: MutableList<SmsMessage> = latestList.toMutableList()

                                for (i in tempClassifiedList.indices) {
                                    val sms: SmsMessage = tempClassifiedList[i]
                                    if (sms.label.isBlank() || sms.label == "ERROR") {
                                        try {
                                            val sender = sms.sender
                                            val isProvider = sender.matches(Regex("[A-Z]{2}-.+"))

                                            val (label, confidence) = if (isProvider) {
                                                Pair("HAM", 1.0f)
                                            } else {
                                                SmsClassifier.predict(sms.body)
                                            }

                                            val category = when (label) {
                                                "SPAM", "SMISHING" -> SmsCategory.SPAM
                                                else -> SmsCategory.INBOX
                                            }
                                            val updatedSms = sms.copy(label = label, confidence = confidence, category = category)
                                            tempClassifiedList[i] = updatedSms
                                        } catch (e: Exception) {
                                            Log.e("MainActivity", "Error classifying SMS", e)
                                            val updatedSms = sms.copy(label = "ERROR", confidence = 0f, category = SmsCategory.INBOX)
                                            tempClassifiedList[i] = updatedSms
                                        }
                                    }
                                }
                                tempClassifiedList // Return the fully classified list
                            }

                            // Final update after all batches are processed (if any changes were made)
                            if (latestList != classifiedMessages) {
                                _smsListFlow.value = classifiedMessages
                            }
                            isClassifying = false // Classification complete, set state to false
                        }.launchIn(this) // Launch the flow collection within the LaunchedEffect's scope
                }

                NavHost(navController = navController, startDestination = "inbox") {
                    composable("inbox") {
                        InboxScreen(
                            smsList = currentSmsList, // Pass the collected StateFlow value
                            onSmsListChange = { newList -> _smsListFlow.value = newList },
                            navController = navController,
                            isClassifying = isClassifying // Pass new state to UI
                        )
                    }
                    composable(
                        "conversation/{sender}",
                        arguments = listOf(navArgument("sender") { type = androidx.navigation.NavType.StringType })
                    ) { backStackEntry ->
                        val sender = backStackEntry.arguments?.getString("sender") ?: ""
                        ConversationScreen(
                            sender = sender,
                            smsList = currentSmsList, // Pass the collected StateFlow value
                            onSmsListChange = { newList -> _smsListFlow.value = newList },
                            navController = navController,
                            isClassifying = isClassifying // Pass new state to UI
                        )
                    }
                }
            }
        }

        // 🔥 Ask user to make this the default SMS app
        requestDefaultSmsRole()
    }

    override fun onDestroy() {
        super.onDestroy()
        contentResolver.unregisterContentObserver(smsObserver)
    }

    private fun requestDefaultSmsRole() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(RoleManager::class.java)
            val isDefault = roleManager.isRoleHeld(RoleManager.ROLE_SMS)
            if (!isDefault) {
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
