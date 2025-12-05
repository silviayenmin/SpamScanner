package com.example.finaldemo

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InboxScreen(
    smsList: List<SmsMessage>,
    onSmsListChange: (List<SmsMessage>) -> Unit,
    navController: NavController,
    isClassifying: Boolean // New parameter
) {
    var selectedTab by remember { mutableStateOf(SmsCategory.INBOX) }

    val conversations = smsList
        .groupBy { it.sender }
        .map { (sender, messages) ->
            val lastMessage = messages.maxByOrNull { it.timestamp }!!
            Conversation(sender, lastMessage)
        }
        .sortedByDescending { it.lastMessage.timestamp }

    Column(Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab.ordinal) {
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

        if (isClassifying) { // Use isClassifying for loading indicator
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
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
                contentPadding = PaddingValues(top = 20.dp, start = 8.dp, end = 8.dp, bottom = 8.dp)
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

@Composable
fun ConversationItem(conversation: Conversation, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = conversation.sender,
                    style = MaterialTheme.typography.titleMedium
                )
                val sdf = SimpleDateFormat("dd MMM HH:mm", Locale.getDefault())
                Text(
                    text = sdf.format(Date(conversation.lastMessage.timestamp)),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = conversation.lastMessage.body.take(50), // Show snippet of last message
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                val labelColor = when {
                    conversation.lastMessage.label.equals("SPAM", true) || conversation.lastMessage.label.equals("SMISHING", true) -> Color.Red
                    conversation.lastMessage.label.equals("HAM", true) -> Color(0xFF2E7D32)
                    conversation.lastMessage.label.equals("ERROR", true) -> Color.Magenta
                    else -> Color.Gray
                }
                Text(
                    text = if (conversation.lastMessage.label.isBlank()) "UNCLASSIFIED" else conversation.lastMessage.label.uppercase(),
                    color = labelColor,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.padding(start = 8.dp).wrapContentWidth(Alignment.End)
                )
            }
        }
    }
}


