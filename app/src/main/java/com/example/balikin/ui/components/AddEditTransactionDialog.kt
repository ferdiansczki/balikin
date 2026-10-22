package com.example.balikin.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.StatusBorrowedOrange
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.balikin.data.local.ItemCategory
import com.example.balikin.data.local.TransactionDirection
import com.example.balikin.data.local.TransactionEntity
import com.example.balikin.util.DateFormatter
import com.example.balikin.util.PhotoStorageHelper
import com.example.balikin.util.StatusHelper
import com.example.ui.theme.CustomizeUI

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTransactionDialog(
    initialTransaction: TransactionEntity? = null,
    onDismiss: () -> Unit,
    onSave: (transaction: TransactionEntity, newPhotoUri: Uri?) -> Unit
) {
    val context = LocalContext.current

    // Form states
    var direction by remember {
        mutableStateOf(
            if (initialTransaction != null) {
                TransactionDirection.valueOf(initialTransaction.direction)
            } else {
                TransactionDirection.LENT
            }
        )
    }
    var itemName by remember { mutableStateOf(initialTransaction?.itemName ?: "") }
    var personName by remember { mutableStateOf(initialTransaction?.personName ?: "") }
    var category by remember {
        mutableStateOf(
            initialTransaction?.category ?: ItemCategory.ELECTRONICS.name
        )
    }

    val defaultLendDate = remember {
        initialTransaction?.lentOrBorrowedDate ?: System.currentTimeMillis()
    }
    val defaultDueDate = remember {
        initialTransaction?.dueDate ?: (System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000L)
    }

    var lendDate by remember { mutableLongStateOf(defaultLendDate) }
    var dueDate by remember { mutableLongStateOf(defaultDueDate) }

    var contactInfo by remember { mutableStateOf(initialTransaction?.contactInfo ?: "") }
    var notes by remember { mutableStateOf(initialTransaction?.notes ?: "") }

    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var existingPhotoPath by remember { mutableStateOf(initialTransaction?.photoPath) }
    val resolvedPhotoFile = PhotoStorageHelper.resolvePhotoFile(context, existingPhotoPath)
    var showFullscreenPhoto by remember { mutableStateOf(false) }

    // Validation error states
    var itemNameError by remember { mutableStateOf<String?>(null) }
    var personNameError by remember { mutableStateOf<String?>(null) }
    var dateRangeError by remember { mutableStateOf<String?>(null) }

    // Dialog & Picker controls
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var showLendDatePicker by remember { mutableStateOf(false) }
    var showDueDatePicker by remember { mutableStateOf(false) }

    // Photo picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            photoUri = uri
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .imePadding()
                .padding(horizontal = CustomizeUI.FormHorizontalPadding, vertical = CustomizeUI.FormVerticalPadding),
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
                    // Top Header Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (initialTransaction == null) "Tambah Transaksi" else "Edit Transaksi",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("dialog_close_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Tutup Formulir"
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Scrollable Form Body
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                    // 1. Direction Switch
                    Text(
                        text = "Jenis Transaksi",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        SegmentedButton(
                            selected = direction == TransactionDirection.LENT,
                            onClick = { direction = TransactionDirection.LENT },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                            colors = SegmentedButtonDefaults.colors(
                                activeContainerColor = MaterialTheme.colorScheme.primary,
                                activeContentColor = MaterialTheme.colorScheme.onPrimary,
                                inactiveContainerColor = MaterialTheme.colorScheme.surface,
                                inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                activeBorderColor = MaterialTheme.colorScheme.primary,
                                inactiveBorderColor = MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier.testTag("direction_lent_btn")
                        ) {
                            Text(
                                "Dipinjamkan",
                                fontWeight = if (direction == TransactionDirection.LENT) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                        SegmentedButton(
                            selected = direction == TransactionDirection.BORROWED,
                            onClick = { direction = TransactionDirection.BORROWED },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                            colors = SegmentedButtonDefaults.colors(
                                activeContainerColor = StatusBorrowedOrange,
                                activeContentColor = Color.White,
                                inactiveContainerColor = MaterialTheme.colorScheme.surface,
                                inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                activeBorderColor = StatusBorrowedOrange,
                                inactiveBorderColor = MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier.testTag("direction_borrowed_btn")
                        ) {
                            Text(
                                "Dipinjam",
                                fontWeight = if (direction == TransactionDirection.BORROWED) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }

                    // 2. Item Name
                    OutlinedTextField(
                        value = itemName,
                        onValueChange = {
                            itemName = it
                            if (it.isNotBlank()) itemNameError = null
                        },
                        label = { Text("Nama Barang *") },
                        placeholder = { Text("Contoh: PlayStation 5") },
                        isError = itemNameError != null,
                        supportingText = { itemNameError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_item_name")
                    )

                    // 3. Person Name
                    OutlinedTextField(
                        value = personName,
                        onValueChange = {
                            personName = it
                            if (it.isNotBlank()) personNameError = null
                        },
                        label = {
                            Text(if (direction == TransactionDirection.LENT) "Nama Peminjam *" else "Nama Pemilik Barang *")
                        },
                        placeholder = { Text("Contoh: Fondnatticha") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null)
                        },
                        isError = personNameError != null,
                        supportingText = { personNameError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_person_name")
                    )

                    // 4. Category Selector
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = CategoryUtils.getDisplayName(category),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Kategori Barang *") },
                            leadingIcon = {
                                Icon(
                                    imageVector = CategoryUtils.getIcon(category),
                                    contentDescription = null,
                                    tint = CategoryUtils.getColor(category)
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { showCategoryDropdown = !showCategoryDropdown }) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Buka Kategori"
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("select_category_input")
                        )

                        // Transparent Clickable Overlay
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showCategoryDropdown = true }
                        )

                        DropdownMenu(
                            expanded = showCategoryDropdown,
                            onDismissRequest = { showCategoryDropdown = false },
                            modifier = Modifier.fillMaxWidth(0.85f)
                        ) {
                            ItemCategory.entries.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat.displayName) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = CategoryUtils.getIcon(cat.name),
                                            contentDescription = null,
                                            tint = CategoryUtils.getColor(cat.name)
                                        )
                                    },
                                    onClick = {
                                        category = cat.name
                                        showCategoryDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    // 5. Date Selectors
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = DateFormatter.formatStandard(lendDate),
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Tgl Pinjam") },
                                trailingIcon = {
                                    IconButton(onClick = { showLendDatePicker = true }) {
                                        Icon(Icons.Default.CalendarMonth, contentDescription = "Pilih Tanggal Pinjam")
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_lend_date")
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { showLendDatePicker = true }
                            )
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = DateFormatter.formatStandard(dueDate),
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Jatuh Tempo") },
                                trailingIcon = {
                                    IconButton(onClick = { showDueDatePicker = true }) {
                                        Icon(Icons.Default.CalendarMonth, contentDescription = "Pilih Jatuh Tempo")
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_due_date")
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { showDueDatePicker = true }
                            )
                        }
                    }

                    // Inline Date Range Error
                    dateRangeError?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    // 6. Photo Picker Section
                    Text(
                        text = "Foto Barang (Opsional)",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            if (photoUri != null) {
                                AsyncImage(
                                    model = photoUri,
                                    contentDescription = "Foto yang dipilih",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clickable { showFullscreenPhoto = true }
                                )
                                IconButton(
                                    onClick = { photoUri = null },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(6.dp)
                                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f), CircleShape)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Hapus Foto", tint = MaterialTheme.colorScheme.error)
                                }
                            } else if (resolvedPhotoFile != null) {
                                AsyncImage(
                                    model = resolvedPhotoFile,
                                    contentDescription = "Foto tersimpan",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clickable { showFullscreenPhoto = true }
                                )
                                IconButton(
                                    onClick = { existingPhotoPath = null },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(6.dp)
                                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f), CircleShape)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Hapus Foto", tint = MaterialTheme.colorScheme.error)
                                }
                            } else {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clickable {
                                            photoPickerLauncher.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            )
                                        }
                                        .padding(16.dp)
                                        .testTag("pick_photo_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AddAPhoto,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Pilih Foto dari Galeri",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    // 7. Contact Information
                    OutlinedTextField(
                        value = contactInfo,
                        onValueChange = { contactInfo = it },
                        label = { Text("Nomor Kontak (Opsional)") },
                        placeholder = { Text("Contoh: 08123456789") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_contact_info")
                    )

                    // 8. Additional Notes
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Catatan Tambahan (Opsional)") },
                        placeholder = { Text("Kelengkapan barang, kondisi fisik, perjanjian") },
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.Notes, contentDescription = null) },
                        minLines = 2,
                        maxLines = 4,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_notes")
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Bottom Save Action
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Button(
                        onClick = {
                            var hasError = false
                            if (itemName.trim().isBlank()) {
                                itemNameError = "Nama barang tidak boleh kosong."
                                hasError = true
                            }
                            if (personName.trim().isBlank()) {
                                personNameError = "Nama pihak terkait tidak boleh kosong."
                                hasError = true
                            }
                            if (!StatusHelper.isDueDateValid(lendDate, dueDate)) {
                                dateRangeError = "Batas tanggal pengembalian tidak boleh sebelum tanggal pinjam."
                                hasError = true
                            } else {
                                dateRangeError = null
                            }

                            if (!hasError) {
                                val entity = (initialTransaction ?: TransactionEntity(
                                    itemName = itemName.trim(),
                                    direction = direction.name,
                                    category = category,
                                    personName = personName.trim(),
                                    lentOrBorrowedDate = lendDate,
                                    dueDate = dueDate,
                                    returnedDate = null
                                )).copy(
                                    itemName = itemName.trim(),
                                    direction = direction.name,
                                    category = category,
                                    personName = personName.trim(),
                                    contactInfo = contactInfo.trim().ifEmpty { null },
                                    lentOrBorrowedDate = lendDate,
                                    dueDate = dueDate,
                                    returnedDate = initialTransaction?.returnedDate,
                                    photoPath = if (photoUri != null) null else existingPhotoPath,
                                    notes = notes.trim().ifEmpty { null }
                                )
                                onSave(entity, photoUri)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("submit_transaction_btn"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Text(
                            text = if (initialTransaction == null) "Simpan Transaksi" else "Perbarui Transaksi",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}

    // Lending DatePicker Dialog
    if (showLendDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = DateFormatter.localDateMillisToUtcMillis(lendDate)
        )
        DatePickerDialog(
            onDismissRequest = { showLendDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        lendDate = DateFormatter.utcMillisToLocalDateMillis(it)
                    }
                    showLendDatePicker = false
                }) {
                    Text("Pilih")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLendDatePicker = false }) {
                    Text("Batal")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Due DatePicker Dialog
    if (showDueDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = DateFormatter.localDateMillisToUtcMillis(dueDate)
        )
        DatePickerDialog(
            onDismissRequest = { showDueDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        dueDate = DateFormatter.utcMillisToLocalDateMillis(it)
                    }
                    showDueDatePicker = false
                }) {
                    Text("Pilih")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDueDatePicker = false }) {
                    Text("Batal")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Fullscreen Photo Viewer
    if (showFullscreenPhoto) {
        val activePhoto = photoUri ?: resolvedPhotoFile
        if (activePhoto != null) {
            FullscreenImageViewer(
                model = activePhoto,
                title = itemName.ifBlank { "Foto Barang" },
                onDismiss = { showFullscreenPhoto = false }
            )
        }
    }
}
