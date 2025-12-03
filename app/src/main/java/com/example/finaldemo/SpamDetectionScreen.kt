package com.example.finaldemo

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.finaldemo.SmsClassifier   // 👈 add this import

@Composable
fun SpamDetectionScreen(
    context: Context = LocalContext.current
) {
    // Request runtime SMS permissions
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(
                android.Manifest.permission.RECEIVE_SMS,
                android.Manifest.permission.READ_SMS
            )
        )
    }

    // Initialize classifier only once
    LaunchedEffect(Unit) {
        SmsClassifier.init(context)
    }

    var smsText by remember { mutableStateOf("") }
    var prediction by remember { mutableStateOf("") }

    Column(Modifier.padding(20.dp)) {

        OutlinedTextField(
            value = smsText,
            onValueChange = { smsText = it },
            label = { Text("Enter SMS") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                val (label, confidence) = SmsClassifier.predict(smsText)
                prediction = "$label   (${(confidence * 100).toInt()}%)"
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Check SMS")
        }

        Text(
            text = "Result: $prediction",
            modifier = Modifier.padding(top = 20.dp)
        )
    }
}
