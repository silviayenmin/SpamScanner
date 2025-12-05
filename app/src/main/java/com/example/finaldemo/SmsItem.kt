package com.example.finaldemo

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SmsItem(sms: SmsMessage, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            // Removed Text(text = sms.sender.ifEmpty { "Unknown" }) as it's in TopAppBar
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                // Display timestamp on its own to control spacing
                val sdf = SimpleDateFormat("dd MMM HH:mm", Locale.getDefault())
                Text(text = sdf.format(Date(sms.timestamp)), style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = sms.body, // Display full message
                style = MaterialTheme.typography.bodyMedium,
                softWrap = true // Allow natural wrapping
            )
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) { // Align to end
                val labelColor = when {
                    sms.label.equals("SPAM", true) || sms.label.equals("SMISHING", true) -> Color.Red
                    sms.label.equals("HAM", true) -> Color(0xFF2E7D32)
                    sms.label.equals("ERROR", true) -> Color.Magenta
                    else -> Color.Gray
                }
                Text(
                    text = if (sms.label.isBlank()) "UNCLASSIFIED" else sms.label.uppercase(),
                    color = labelColor,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis, // Ellipsize long labels
                    modifier = Modifier.weight(1f, fill = false).padding(end = 4.dp) // Give it space, but don't fill
                )
                if (sms.confidence > 0f) {
                    Text(
                        text = stringResource(id = R.string.confidence_label, (sms.confidence * 100).toInt()),
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis // Ellipsize long confidence
                    )
                }
            }
        }
    }
}