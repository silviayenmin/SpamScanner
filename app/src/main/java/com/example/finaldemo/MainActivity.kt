package com.example.finaldemo

import android.Manifest
import android.app.role.RoleManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.app.ActivityCompat
import android.app.NotificationChannel
import android.app.NotificationManager
import com.example.finaldemo.InboxScreen
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createNotificationChannel()

        // Request SMS & Notification permissions
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.READ_SMS,
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.POST_NOTIFICATIONS
            ),
            100
        )

        setContent {
            InboxScreen()
        }

        // 🔥 Ask user to make this the default SMS app
        requestDefaultSmsRole()
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
                "SMS Alerts",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}
