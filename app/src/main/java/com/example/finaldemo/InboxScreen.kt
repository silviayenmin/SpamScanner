package com.example.finaldemo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.finaldemo.readInboxSms
import com.example.finaldemo.SmsClassifier
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
@Composable
fun InboxScreen() {
    val context = LocalContext.current
    var smsList by remember { mutableStateOf(listOf<String>()) }
    var selectedSms by remember { mutableStateOf("") }
    var prediction by remember { mutableStateOf("") }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { }

    LaunchedEffect(Unit) {
        smsList = readInboxSms(context)
        SmsClassifier.init(context)   // 🔥 load model once
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {

        Text("Tap a message to check spam", style = MaterialTheme.typography.titleLarge)

        Spacer(Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(smsList) { sms ->
                SmsItem(
                    sms = sms,
                    onClick = {
                        selectedSms = sms
                        val (label, confidence) = SmsClassifier.predict(sms)
                        prediction = "$label (${(confidence * 100).toInt()}%)"
                    }
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        Text("Selected SMS:")
        Text(selectedSms, Modifier.padding(8.dp))

        Spacer(Modifier.height(10.dp))

        Text("Prediction: $prediction", color = Color.Red)
    }
}

@Composable
fun SmsItem(sms: String, onClick: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(10.dp)
            .background(Color(0xFFF0F0F0))
    ) {
        Text(sms)
    }
}
