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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

// Make sure SmsMessage and SmsCategory are defined (see earlier instructions)

@Composable
fun InboxScreen() {
    val context = LocalContext.current

    // List of SmsMessage (not String)
    var smsList by remember { mutableStateOf(listOf<SmsMessage>()) }

    // Selected SMS shown below
    var selectedSms by remember { mutableStateOf<SmsMessage?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { /* handle result if needed */ }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(
                android.Manifest.permission.RECEIVE_SMS,
                android.Manifest.permission.READ_SMS
            )
        )

        // load SMS on IO thread
        val loaded = withContext(Dispatchers.IO) {
            // readInboxSms must now return List<SmsMessage>
            readInboxSms(context)
        }

        // Optionally pre-run model prediction for each sms (this can be slow for many messages)
        // Here we load messages first, then keep their label/confidence empty until clicked.
        smsList = loaded
        SmsClassifier.init(context)   // load model once
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {

        Text("Tap a message to check spam", style = MaterialTheme.typography.titleLarge)

        Spacer(Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(smsList, key = { it.id }) { sms ->
                SmsItem(
                    sms = sms,
                    onClick = {
                        // on click run prediction and set selectedSms
                        val (label, confidence) = SmsClassifier.predict(sms.body)

                        // create a new copy to show prediction and keep UI immutable
                        selectedSms = sms.copy(label = label, confidence = confidence)
                    }
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        Text("Selected SMS:")
        Spacer(Modifier.height(6.dp))

        if (selectedSms != null) {
            SelectedSmsView(selectedSms!!)
        } else {
            Text("No SMS selected", Modifier.padding(8.dp))
        }
    }
}

@Composable
fun SmsItem(sms: SmsMessage, onClick: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(10.dp)
            .background(Color(0xFFF0F0F0))
    ) {
        // Sender row + timestamp
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = sms.sender.ifEmpty { "Unknown" },
                style = MaterialTheme.typography.titleMedium
            )

            // show short date
            val sdf = SimpleDateFormat("dd MMM HH:mm", Locale.getDefault())
            Text(text = sdf.format(Date(sms.timestamp)), style = MaterialTheme.typography.bodySmall)
        }

        Spacer(Modifier.height(6.dp))

        // Message preview
        Text(
            text = sms.body.take(150),
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 4
        )

        Spacer(Modifier.height(8.dp))

        // If we have model prediction already on this sms object, show badge + confidence
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            val labelText = if (sms.label.isBlank()) "UNKNOWN" else sms.label.uppercase()
            val labelColor = when (sms.label.lowercase()) {
                "spam" -> Color.Red
                "smishing" -> Color.Red
                "promotion" -> Color(0xFF0077FF)
                "ham" -> Color(0xFF2E7D32)
                else -> Color.Gray
            }

            Text(text = labelText, color = labelColor, style = MaterialTheme.typography.bodySmall)

            if (sms.confidence > 0f) {
                Text(text = "Confidence: ${(sms.confidence * 100).toInt()}%", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun SelectedSmsView(sms: SmsMessage) {
    Column(Modifier.padding(8.dp)) {
        Text(text = sms.sender.ifEmpty { "Unknown" }, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(6.dp))
        Text(text = sms.body, style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Prediction: ${if (sms.label.isBlank()) "Not checked" else sms.label} " +
                    if (sms.confidence > 0f) "(${(sms.confidence * 100).toInt()}%)" else "",
            color = if (sms.label.lowercase() == "spam") Color.Red else Color.Unspecified
        )
    }
}
