package com.example.finaldemo

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationScreen(
    sender: String,
    smsList: List<SmsMessage>,
    onSmsListChange: (List<SmsMessage>) -> Unit,
    navController: NavController,
    isClassifying: Boolean // New parameter
) {
    val conversationMessages = smsList.filter { it.sender == sender }.sortedBy { it.timestamp }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(sender) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isClassifying) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = paddingValues
            ) {
                items(conversationMessages) { sms ->
                    SmsItem(sms = sms, onClick = {})
                }
            }
        }
    }
}
