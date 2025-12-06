package com.example.finaldemo

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.finaldemo.ui.theme.TextSecondary
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, kotlinx.coroutines.FlowPreview::class)
@Composable
fun SearchScreen(navController: NavController, smsDao: SmsDao) {
    var searchQuery by remember { mutableStateOf("") }

    val searchResults by produceState<List<SmsMessage>>(initialValue = emptyList(), searchQuery) {
        snapshotFlow { searchQuery } 
            .debounce(300)
            .distinctUntilChanged()
            .collect { query ->
                value = when {
                    query.isBlank() -> emptyList()
                    query.startsWith("sender:", ignoreCase = true) -> {
                        val sender = query.substringAfter("sender:").trim()
                        smsDao.getMessagesFromSenders(sender).first()
                    }
                    query.equals("filter:unread", ignoreCase = true) -> {
                        smsDao.getUnreadMessages().first()
                    }
                    query.equals("filter:known", ignoreCase = true) -> {
                        smsDao.getKnownSenderMessages().first()
                    }
                    else -> smsDao.searchMessages(query).first()
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search Message") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            cursorColor = MaterialTheme.colorScheme.onPrimary,
                            focusedTextColor = MaterialTheme.colorScheme.onPrimary,
                            unfocusedTextColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                            unfocusedPlaceholderColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
                            focusedPlaceholderColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) {
paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    FilterChip(icon = Icons.Default.Email, label = "Unread", onClick = { searchQuery = "filter:unread" })
                }
                Box(modifier = Modifier.weight(1f)) {
                    FilterChip(icon = Icons.Default.Person, label = "Know", onClick = { searchQuery = "filter:known" })
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    FilterChip(icon = Icons.Default.PhoneAndroid, label = "Airtel", onClick = { searchQuery = "sender:Airtel" })
                }
                Box(modifier = Modifier.weight(1f)) {
                    FilterChip(icon = Icons.Default.PhoneAndroid, label = "VI", onClick = { searchQuery = "sender:VI" })
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                if (searchResults.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                            val text = if (searchQuery.isBlank()) {
                                "Enter a query or select a filter to start searching."
                            } else {
                                "No results found for \"$searchQuery\""
                            }
                            Text(text, color = TextSecondary)
                        }
                    }
                } else {
                    items(searchResults, key = { it.id }) { message ->
                        SearchResultItem(
                            message = message,
                            onClick = {
                                navController.navigate("conversation/${message.sender}")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SearchResultItem(message: SmsMessage, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = message.senderName ?: message.sender,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())
                Text(
                    text = sdf.format(Date(message.timestamp)),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = message.body,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun FilterChip(modifier: Modifier = Modifier, icon: ImageVector, label: String, onClick: () -> Unit = {}) {
    ElevatedAssistChip(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = label) }
    )
}
