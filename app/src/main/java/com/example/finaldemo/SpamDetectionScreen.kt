package com.example.finaldemo

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.finaldemo.SmsClassifier   // 👈 add this import

@OptIn(ExperimentalMaterial3Api::class)
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

    var smsText by remember { mutableStateOf("") }
    var prediction by remember { mutableStateOf<Pair<String, Float>?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.spam_detection_title)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            OutlinedTextField(
                value = smsText,
                onValueChange = { smsText = it },
                label = { Text(stringResource(id = R.string.enter_sms_label)) },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 5
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    prediction = SmsClassifier.predict(smsText)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(id = R.string.check_sms_button))
            }

            Spacer(modifier = Modifier.height(32.dp))

            prediction?.let { (label, confidence) ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = when (label) {
                            "SPAM", "SMISHING" -> Color.Red.copy(alpha = 0.1f)
                            "HAM" -> Color.Green.copy(alpha = 0.1f)
                            else -> Color.Gray.copy(alpha = 0.1f)
                        }
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(id = R.string.result_label, label),
                            style = MaterialTheme.typography.titleLarge,
                            color = when (label) {
                                "SPAM", "SMISHING" -> Color.Red
                                "HAM" -> Color(0xFF2E7D32)
                                else -> Color.Gray
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { confidence },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(id = R.string.confidence_label, (confidence * 100).toInt()),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.align(Alignment.End)
                        )
                    }
                }
            }
        }
    }
}
