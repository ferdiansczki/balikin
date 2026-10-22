package com.example.balikin.ui.components

import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.balikin.data.local.TransactionDirection
import com.example.balikin.data.local.TransactionEntity
import com.example.balikin.util.DateFormatter
import com.example.balikin.util.PhotoStorageHelper
import com.example.balikin.util.StatusHelper
import com.example.balikin.util.TransactionStatus
import com.example.ui.theme.StatusActiveGreen
import com.example.ui.theme.StatusBorrowedOrange
import com.example.ui.theme.StatusBorrowedContainer
import com.example.ui.theme.StatusBorrowedBorder
import com.example.ui.theme.StatusLentBlue
import com.example.ui.theme.StatusLentContainer
import com.example.ui.theme.StatusLentBorder
import com.example.ui.theme.StatusOverdueContainer
import com.example.ui.theme.StatusOverdueOnContainer
import com.example.ui.theme.CustomizeUI

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailDialog(
    transaction: TransactionEntity,
    onDismiss: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onMarkReturned: (returnDate: Long) -> Unit,
    onUnmarkReturned: () -> Unit = {}
) {
    val context = LocalContext.current
    val photoFile = PhotoStorageHelper.resolvePhotoFile(context, transaction.photoPath)
    val status = StatusHelper.calculateStatus(transaction.returnedDate, transaction.dueDate)
    val isLent = transaction.direction == TransactionDirection.LENT.name
    val directionColor = if (isLent) StatusLentBlue else StatusBorrowedOrange

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showReturnConfirmDialog by remember { mutableStateOf(false) }
    var returnDateMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showFullscreenImage by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .imePadding()
                .padding(
                    start = CustomizeUI.FormHorizontalPadding,
                    end = CustomizeUI.FormHorizontalPadding,
                    top = CustomizeUI.FormDetailTopPadding,
                    bottom = 20.dp
                ),
            contentAlignment = Alignment.TopCenter
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = CustomizeUI.FormDialogMaxHeight),
                shape = RoundedCornerShape(CustomizeUI.FormDialogCornerRadius),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Header with "Detail Barang" title and action buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Detail Barang",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = CustomizeUI.DialogTitleFontSize
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.testTag("detail_title")
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onEditClick,
                                modifier = Modifier.testTag("detail_edit_btn")
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Transaksi", tint = MaterialTheme.colorScheme.primary)
                            }
                            IconButton(
                                onClick = { showDeleteConfirmDialog = true },
                                modifier = Modifier.testTag("detail_delete_btn")
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Hapus Transaksi", tint = MaterialTheme.colorScheme.error)
                            }
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier.testTag("detail_close_btn")
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Tutup")
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Detail Content Scrollable
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                    // Photo Banner or Category Banner
                    if (photoFile != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { showFullscreenImage = true }
                        ) {
                            AsyncImage(
                                model = photoFile,
                                contentDescription = transaction.itemName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                            )
                            // Fullscreen badge hint overlay
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.65f),
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Fullscreen,
                                        contentDescription = "Layar Penuh",
                                        tint = androidx.compose.ui.graphics.Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Layar Penuh",
                                        style = MaterialTheme.typography.labelSmall.copy(color = androidx.compose.ui.graphics.Color.White)
                                    )
                                }
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(CategoryUtils.getColor(transaction.category).copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = CategoryUtils.getIcon(transaction.category),
                                    contentDescription = null,
                                    tint = CategoryUtils.getColor(transaction.category),
                                    modifier = Modifier.size(54.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = CategoryUtils.getDisplayName(transaction.category),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = CategoryUtils.getColor(transaction.category)
                                )
                            }
                        }
                    }

                    // Status Highlight Banner
                    when (status) {
                        TransactionStatus.COMPLETED -> {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = StatusActiveGreen.copy(alpha = 0.14f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = StatusActiveGreen)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Status: Sudah Selesai Dikembalikan",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = StatusActiveGreen
                                        )
                                        transaction.returnedDate?.let {
                                            Text(
                                                text = "Tanggal Kembali: ${DateFormatter.formatFull(it)}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = StatusActiveGreen
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        TransactionStatus.OVERDUE -> {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = StatusOverdueContainer,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.WarningAmber, contentDescription = null, tint = StatusOverdueOnContainer)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Status: Terlambat Pengembalian",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = StatusOverdueOnContainer
                                        )
                                        Text(
                                            text = DateFormatter.getFriendlyDueLabel(transaction.dueDate),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = StatusOverdueOnContainer
                                        )
                                    }
                                }
                            }
                        }
                        TransactionStatus.ACTIVE -> {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Status: Pinjaman Aktif",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Text(
                                            text = DateFormatter.getFriendlyDueLabel(transaction.dueDate),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Item Name & Direction Header
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val dirBg = if (isLent) StatusLentContainer else StatusBorrowedContainer
                            val dirBorder = if (isLent) StatusLentBorder else StatusBorrowedBorder
                            val dirColor = if (isLent) StatusLentBlue else StatusBorrowedOrange
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = dirBg,
                                border = BorderStroke(1.5.dp, dirBorder)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (isLent) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                        contentDescription = null,
                                        tint = dirColor,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isLent) "Dipinjamkan" else "Dipinjam",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = dirColor
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = CategoryUtils.getDisplayName(transaction.category),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = transaction.itemName,
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    // Related Person Info
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isLent) "Peminjam" else "Pemilik Barang",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = transaction.personName,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Call / WhatsApp shortcut if phone exists
                        if (!transaction.contactInfo.isNullOrBlank()) {
                            IconButton(
                                onClick = {
                                    val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                                        data = "tel:${transaction.contactInfo}".toUri()
                                    }
                                    context.startActivity(dialIntent)
                                }
                            ) {
                                Icon(Icons.Default.Call, contentDescription = "Hubungi Kontak", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    // Contact info text if provided
                    if (!transaction.contactInfo.isNullOrBlank()) {
                        Text(
                            text = "Kontak: ${transaction.contactInfo}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 54.dp)
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    // Dates Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Tanggal Pinjam",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = DateFormatter.formatStandard(transaction.lentOrBorrowedDate),
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Batas Pengembalian",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = DateFormatter.formatStandard(transaction.dueDate),
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                                color = if (status == TransactionStatus.OVERDUE) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Notes if provided
                    if (!transaction.notes.isNullOrBlank()) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Notes,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Catatan",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = transaction.notes,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Bottom Action Buttons Docked at the bottom of the card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    if (transaction.returnedDate == null) {
                        Button(
                            onClick = { showReturnConfirmDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(CustomizeUI.FormButtonHeight)
                                .testTag("detail_mark_returned_btn"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CustomizeUI.ButtonMarkReturnedGreen,
                                contentColor = CustomizeUI.ButtonMarkReturnedText
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Tandai Sudah Kembali",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    } else {
                        // Revert transaction to active status
                        Button(
                            onClick = onUnmarkReturned,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(CustomizeUI.FormButtonHeight)
                                .testTag("detail_revert_returned_btn"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                        ) {
                            Icon(Icons.Default.Replay, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Kembalikan ke Pinjaman Aktif",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }
    }
}

    // Delete Confirmation Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Hapus Transaksi?") },
            text = { Text("Apakah Anda yakin ingin menghapus data transaksi '${transaction.itemName}'? Tindakan ini tidak dapat dibatalkan.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDeleteClick()
                    }
                ) {
                    Text("Hapus", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // Return Date Confirmation Dialog
    if (showReturnConfirmDialog) {
        val returnDatePickerState = rememberDatePickerState(
            initialSelectedDateMillis = DateFormatter.localDateMillisToUtcMillis(returnDateMillis)
        )
        DatePickerDialog(
            onDismissRequest = { showReturnConfirmDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        val selectedUtc = returnDatePickerState.selectedDateMillis ?: System.currentTimeMillis()
                        val localSelectedDate = DateFormatter.utcMillisToLocalDateMillis(selectedUtc)
                        showReturnConfirmDialog = false
                        onMarkReturned(localSelectedDate)
                    }
                ) {
                    Text("Konfirmasi Pengembalian")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReturnConfirmDialog = false }) {
                    Text("Batal")
                }
            }
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Konfirmasi Tanggal Pengembalian",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Pilih tanggal kapan barang '${transaction.itemName}' dikembalikan:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                DatePicker(state = returnDatePickerState)
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
