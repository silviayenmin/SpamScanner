package com.example.finaldemo

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.example.finaldemo.ui.theme.TextSecondary
import com.example.finaldemo.utils.ContactHelper
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun InboxScreen(
    smsList: List<SmsMessage>,
    navController: NavController,
    isLoading: Boolean
) {
    var selectedTab by remember { mutableStateOf(SmsCategory.INBOX) }

    val conversations = smsList
        .groupBy { it.sender }
        .mapNotNull { (sender, messages) ->
            messages.maxByOrNull { it.timestamp }?.let { lastMessage ->
                Conversation(sender, lastMessage)
            }
        }
        .sortedByDescending { it.lastMessage.timestamp }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(Modifier.fillMaxSize()) {
            TabRow(
                selectedTabIndex = selectedTab.ordinal,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                SmsCategory.values().forEach { category ->
                    Tab(
                        selected = selectedTab == category,
                        onClick = { selectedTab = category },
                        text = {
                            val text = when (category) {
                                SmsCategory.INBOX -> stringResource(id = R.string.inbox_tab)
                                SmsCategory.SPAM -> stringResource(id = R.string.spam_tab)
                                SmsCategory.PROMOTIONS -> stringResource(id = R.string.promotions_tab)
                            }
                            Text(text)
                        }
                    )
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (smsList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No messages")
                }
            } else {
                val filteredConversations = conversations.filter {
                    val category = when (it.lastMessage.label) {
                        "SPAM", "SMISHING" -> SmsCategory.SPAM
                        else -> SmsCategory.INBOX
                    }
                    category == selectedTab
                }

                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    items(filteredConversations, key = { it.sender }) { conversation ->
                        ConversationItem(
                            conversation = conversation,
                            onClick = {
                                navController.navigate("conversation/${conversation.sender}")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ConversationItem(conversation: Conversation, onClick: () -> Unit) {
    val context = LocalContext.current
    val contactName = ContactHelper.getContactName(context, conversation.sender)
    val senderDisplayName = contactName ?: conversation.sender

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = senderDisplayName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                val sdf = SimpleDateFormat("dd MMM HH:mm", Locale.getDefault())
                Text(
                    text = sdf.format(Date(conversation.lastMessage.timestamp)),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = conversation.lastMessage.body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                val (label, color) = when {
                    conversation.lastMessage.label.equals("SPAM", true) -> "SPAM" to Color.Red.copy(alpha = 0.7f)
                    conversation.lastMessage.label.equals("SMISHING", true) -> "SMISHING" to Color(0xFFD32F2F) // Darker Red
                    conversation.lastMessage.label.equals("HAM", true) -> "HAM" to Color(0xFF388E3C) // Darker Green
                    else -> "" to Color.Transparent
                }

                if (label.isNotEmpty() && label != "HAM") {
                    Spacer(modifier = Modifier.width(8.dp))
                    Card(
                        shape = RoundedCornerShape(4.dp),
                        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
                    ) {
                        Text(
                            text = label,
                            color = color,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}