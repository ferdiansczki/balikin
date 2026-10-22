package com.example.balikin.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.balikin.data.local.TransactionDirection
import com.example.balikin.data.local.TransactionEntity
import com.example.balikin.util.DateFormatter
import com.example.balikin.util.PhotoStorageHelper
import com.example.balikin.util.StatusHelper
import com.example.balikin.util.TransactionStatus
import com.example.ui.theme.CustomizeUI
import com.example.ui.theme.StatusActiveGreen
import com.example.ui.theme.StatusActiveGreenBorder
import com.example.ui.theme.StatusActiveGreenContainer
import com.example.ui.theme.StatusActiveGreenOnContainer
import com.example.ui.theme.StatusBorrowedBorder
import com.example.ui.theme.StatusBorrowedContainer
import com.example.ui.theme.StatusBorrowedOnContainer
import com.example.ui.theme.StatusBorrowedOrange
import com.example.ui.theme.StatusLentBlue
import com.example.ui.theme.StatusLentBorder
import com.example.ui.theme.StatusLentContainer
import com.example.ui.theme.StatusLentOnContainer
import com.example.ui.theme.StatusOverdueBorder
import com.example.ui.theme.StatusOverdueContainer
import com.example.ui.theme.StatusOverdueOnContainer
import com.example.ui.theme.StatusOverdueRed

@Composable
fun TransactionItemCard(
    transaction: TransactionEntity,
    onClick: () -> Unit,
    onMarkReturnedClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val photoFile = PhotoStorageHelper.resolvePhotoFile(context, transaction.photoPath)
    val status = StatusHelper.calculateStatus(transaction.returnedDate, transaction.dueDate)
    val isLent = transaction.direction == TransactionDirection.LENT.name
    val directionColor = if (isLent) StatusLentBlue else StatusBorrowedOrange
    var showFullscreenImage by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("transaction_card_${transaction.id}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header Row: Direction Pill & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Direction Badge
                val dirBg = if (isLent) StatusLentContainer else StatusBorrowedContainer
                val dirBorder = if (isLent) StatusLentBorder else StatusBorrowedBorder
                val dirText = if (isLent) StatusLentOnContainer else StatusBorrowedOnContainer
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = dirBg,
                    border = BorderStroke(1.dp, dirBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isLent) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                            contentDescription = null,
                            tint = dirText,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isLent) "Dipinjamkan" else "Dipinjam",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = dirText
                        )
                    }
                }

                // Status Badge
                when (status) {
                    TransactionStatus.COMPLETED -> {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = StatusActiveGreenContainer,
                            border = BorderStroke(1.dp, StatusActiveGreenBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = StatusActiveGreenOnContainer,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Selesai",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = StatusActiveGreenOnContainer
                                )
                            }
                        }
                    }
                    TransactionStatus.OVERDUE -> {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = StatusOverdueContainer,
                            border = BorderStroke(1.dp, StatusOverdueBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WarningAmber,
                                    contentDescription = null,
                                    tint = StatusOverdueOnContainer,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = DateFormatter.getFriendlyDueLabel(transaction.dueDate),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = StatusOverdueOnContainer
                                )
                            }
                        }
                    }
                    TransactionStatus.ACTIVE -> {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = StatusActiveGreenContainer,
                            border = BorderStroke(1.dp, StatusActiveGreenBorder)
                        ) {
                            Text(
                                text = DateFormatter.getFriendlyDueLabel(transaction.dueDate),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = StatusActiveGreenOnContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Content: Thumbnail + Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Photo Thumbnail or Category Icon Fallback
                if (photoFile != null) {
                    AsyncImage(
                        model = photoFile,
                        contentDescription = transaction.itemName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showFullscreenImage = true }
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(CategoryUtils.getColor(transaction.category).copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = CategoryUtils.getIcon(transaction.category),
                            contentDescription = null,
                            tint = CategoryUtils.getColor(transaction.category),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = transaction.itemName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (isLent) "Ke: ${transaction.personName}" else "Dari: ${transaction.personName}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Jatuh tempo: ${DateFormatter.formatStandard(transaction.dueDate)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            // Returned Date Indicator
            if (transaction.returnedDate != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = StatusActiveGreenContainer.copy(alpha = 0.6f),
                    border = BorderStroke(1.dp, StatusActiveGreenBorder.copy(alpha = 0.8f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = StatusActiveGreen,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Dikembalikan pada: ${DateFormatter.formatStandard(transaction.returnedDate)}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = StatusActiveGreenOnContainer
                        )
                    }
                }
            }

            // Quick Action Button
            if (onMarkReturnedClick != null && transaction.returnedDate == null) {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onMarkReturnedClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(CustomizeUI.QuickActionButtonHeight)
                        .testTag("btn_mark_returned_${transaction.id}"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CustomizeUI.ButtonMarkReturnedGreen,
                        contentColor = CustomizeUI.ButtonMarkReturnedText
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Tandai Sudah Kembali",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }

    // Fullscreen Photo Viewer
    if (showFullscreenImage && photoFile != null) {
        FullscreenImageViewer(
            model = photoFile,
            title = transaction.itemName,
            onDismiss = { showFullscreenImage = false }
        )
    }
}
