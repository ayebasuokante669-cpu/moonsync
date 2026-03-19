package com.example.moonsyncapp.ui.screens.community

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moonsyncapp.data.model.ContentType
import com.example.moonsyncapp.data.model.ReportReason

/**
 * Lightweight UI model for showing what is being reported.
 */
data class ReportContext(
    val title: String,          // e.g. "Post from Dr. Amara", "Message from Anonymous Sister"
    val snippet: String,        // Short preview of the content
    val meta: String? = null    // Optional: category, phase, time, etc.
)

/**
 * Unified report sheet for all community content types.
 *
 * Replaces the old AlertDialog-based ReportDialog.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityReportSheet(
    contentType: ContentType,
    context: ReportContext,
    onDismissRequest: () -> Unit,
    onSubmitReport: (reason: ReportReason, notes: String?) -> Unit
) {
    var selectedReason by remember { mutableStateOf<ReportReason?>(null) }
    var additionalNotes by remember { mutableStateOf("") }
    val notesLimit = 500

    // Reason list – can be reordered per content type if desired
    val reasons = remember(contentType) {
        when (contentType) {
            ContentType.PHASE_ROOM_MESSAGE -> listOf(
                ReportReason.MISINFORMATION,
                ReportReason.HARASSMENT,
                ReportReason.SELF_HARM,
                ReportReason.INAPPROPRIATE,
                ReportReason.SPAM,
                ReportReason.OTHER
            )
            ContentType.AI_MESSAGE -> listOf(
                ReportReason.MISINFORMATION,
                ReportReason.SELF_HARM,
                ReportReason.INAPPROPRIATE,
                ReportReason.OTHER
            )
            ContentType.POST,
            ContentType.COMMENT,
            ContentType.GROUP,
            ContentType.USER -> listOf(
                ReportReason.HARASSMENT,
                ReportReason.SELF_HARM,
                ReportReason.MISINFORMATION,
                ReportReason.INAPPROPRIATE,
                ReportReason.SPAM,
                ReportReason.UNDERAGE_CONTENT,
                ReportReason.FAKE_PROFESSIONAL,
                ReportReason.OTHER
            )
        }.distinct()
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.error.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🚩", fontSize = 18.sp)
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = when (contentType) {
                            ContentType.POST -> "Report post"
                            ContentType.COMMENT -> "Report comment"
                            ContentType.PHASE_ROOM_MESSAGE -> "Report message"
                            ContentType.GROUP -> "Report group"
                            ContentType.USER -> "Report user"
                            ContentType.AI_MESSAGE -> "Report AI response"
                        },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = when (contentType) {
                            ContentType.AI_MESSAGE -> "Help us improve Momo."
                            else -> "Help us keep MoonSync safe and kind."
                        },
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Context preview
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = context.title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    context.meta?.let { meta ->
                        Text(
                            text = meta,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = context.snippet,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Scrollable content (reasons + notes)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(weight = 1f, fill = false)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Why are you reporting this?",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(8.dp))

                reasons.forEach { reason ->
                    val selected = selectedReason == reason
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { selectedReason = reason },
                        color = if (selected)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                        else
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selected,
                                onClick = { selectedReason = reason }
                            )
                            Spacer(Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = reason.displayName,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = reasonDescription(reason),
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                if (selectedReason == ReportReason.OTHER) {
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = additionalNotes,
                        onValueChange = {
                            if (it.length <= notesLimit) {
                                additionalNotes = it
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Anything else you'd like us to know?") },
                        maxLines = 4,
                        shape = RoundedCornerShape(12.dp),
                        supportingText = {
                            Text(
                                text = "${additionalNotes.length}/$notesLimit",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    )
                }

                Spacer(Modifier.height(12.dp))

                // Crisis note if self-harm option is present
                if (reasons.contains(ReportReason.SELF_HARM)) {
                    Text(
                        text = "If you or someone you know is in immediate danger, " +
                                "please contact local emergency services.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Anonymity note
            Text(
                text = when (contentType) {
                    ContentType.AI_MESSAGE -> "Your feedback helps us make Momo safer and more helpful."
                    else -> "Your report is anonymous. The person you report won't see who reported them."
                },
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = {
                        selectedReason?.let { reason ->
                            val notes = additionalNotes.trim().ifBlank { null }
                            onSubmitReport(reason, notes)
                        }
                    },
                    enabled = selectedReason != null,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Submit report")
                }
            }
        }
    }
}

/**
 * Short helper to provide descriptions for reasons.
 * This is purely UI copy; no backend impact.
 */
@Composable
private fun reasonDescription(reason: ReportReason): String = when (reason) {
    ReportReason.HARASSMENT ->
        "Insults, bullying, or targeted attacks"

    ReportReason.MISINFORMATION ->
        "Potentially unsafe or misleading health info"

    ReportReason.SELF_HARM ->
        "Mentions of self-harm, suicide, or serious harm"

    ReportReason.INAPPROPRIATE ->
        "Graphic, sexual, or otherwise inappropriate content"

    ReportReason.SPAM ->
        "Scams, promotions, or irrelevant content"

    ReportReason.UNDERAGE_CONTENT ->
        "Not appropriate for this age group"

    ReportReason.FAKE_PROFESSIONAL ->
        "Claims to be a professional without verification"

    ReportReason.OTHER ->
        "Something else that doesn't fit above"
}