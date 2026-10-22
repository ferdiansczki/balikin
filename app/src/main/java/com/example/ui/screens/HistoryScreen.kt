package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.balikin.data.local.TransactionDirection
import com.example.balikin.data.local.TransactionEntity
import com.example.balikin.ui.components.TransactionItemCard
import com.example.ui.theme.StatusActiveGreen
import com.example.ui.theme.StatusActiveGreenBorder
import com.example.ui.theme.StatusActiveGreenContainer
import com.example.ui.theme.StatusBorrowedBorder
import com.example.ui.theme.StatusBorrowedContainer
import com.example.ui.theme.StatusBorrowedOrange
import com.example.ui.theme.StatusLentBlue
import com.example.ui.theme.StatusLentBorder
import com.example.ui.theme.StatusLentContainer
import com.example.ui.theme.CustomizeUI

@Composable
fun HistoryScreen(
    completedTransactions: List<TransactionEntity>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedDirection: TransactionDirection?,
    onDirectionFilterChange: (TransactionDirection?) -> Unit,
    onItemClick: (TransactionEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalCompleted = completedTransactions.size
    val lentCompleted = completedTransactions.count { it.direction == TransactionDirection.LENT.name }
    val borrowedCompleted = completedTransactions.count { it.direction == TransactionDirection.BORROWED.name }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header Section
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Riwayat Pengembalian",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 21.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.testTag("history_title")
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Daftar barang yang sudah dikembalikan",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Summary Metric Cards in History
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HistoryMetricCard(
                    title = "Total Selesai",
                    count = totalCompleted,
                    icon = Icons.Default.CheckCircle,
                    accentColor = CustomizeUI.MetricColorCompleted,
                    containerBg = CustomizeUI.MetricBgCompleted,
                    borderColor = CustomizeUI.MetricBorderCompleted,
                    modifier = Modifier.weight(1f)
                )
                HistoryMetricCard(
                    title = "Dipinjamkan",
                    count = lentCompleted,
                    icon = Icons.Default.ArrowUpward,
                    accentColor = CustomizeUI.MetricColorLent,
                    containerBg = CustomizeUI.MetricBgLent,
                    borderColor = CustomizeUI.MetricBorderLent,
                    modifier = Modifier.weight(1f)
                )
                HistoryMetricCard(
                    title = "Dipinjam",
                    count = borrowedCompleted,
                    icon = Icons.Default.ArrowDownward,
                    accentColor = CustomizeUI.MetricColorBorrowed,
                    containerBg = CustomizeUI.MetricBgBorrowed,
                    borderColor = CustomizeUI.MetricBorderBorrowed,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Search Bar and Filter Chips
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("history_search_input"),
                    placeholder = {
                        Text(
                            "Cari riwayat transaksi...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Ikon Cari",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Hapus Pencarian"
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val isAllSelected = selectedDirection == null
                    FilterChip(
                        selected = isAllSelected,
                        onClick = { onDirectionFilterChange(null) },
                        label = { Text("Semua", fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Normal) },
                        shape = RoundedCornerShape(8.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isAllSelected,
                            borderColor = if (isAllSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier.testTag("history_filter_all")
                    )
                    val isLentSelected = selectedDirection == TransactionDirection.LENT
                    FilterChip(
                        selected = isLentSelected,
                        onClick = { onDirectionFilterChange(TransactionDirection.LENT) },
                        label = { Text("Dipinjamkan", fontWeight = if (isLentSelected) FontWeight.Bold else FontWeight.Normal) },
                        shape = RoundedCornerShape(8.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isLentSelected,
                            borderColor = if (isLentSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier.testTag("history_filter_lent")
                    )
                    val isBorrowedSelected = selectedDirection == TransactionDirection.BORROWED
                    FilterChip(
                        selected = isBorrowedSelected,
                        onClick = { onDirectionFilterChange(TransactionDirection.BORROWED) },
                        label = { Text("Dipinjam", fontWeight = if (isBorrowedSelected) FontWeight.Bold else FontWeight.Normal) },
                        shape = RoundedCornerShape(8.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isBorrowedSelected,
                            borderColor = if (isBorrowedSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier.testTag("history_filter_borrowed")
                    )
                }
            }
        }

        // Transactions List or Empty State
        if (completedTransactions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 36.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                                .background(StatusActiveGreenContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CheckCircleOutline,
                                contentDescription = null,
                                tint = StatusActiveGreen,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "Belum Ketemu, Nih" else "Belum Ada Riwayat",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (searchQuery.isNotBlank())
                                "Belum ada riwayat yang cocok dengan kata kunci '$searchQuery'. Coba kata lain, yuk."
                            else
                                "Belum ada riwayat pengembalian. Barangnya masih sibuk jalan-jalan, mungkin.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        } else {
            items(
                items = completedTransactions,
                key = { it.id }
            ) { transaction ->
                TransactionItemCard(
                    transaction = transaction,
                    onClick = { onItemClick(transaction) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}


@Composable
private fun HistoryMetricCard(
    title: String,
    count: Int,
    icon: ImageVector,
    accentColor: Color,
    containerBg: Color,
    borderColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(CustomizeUI.MetricCardHeight),
        shape = RoundedCornerShape(CustomizeUI.MetricCardCornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = containerBg
        ),
        border = BorderStroke(2.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(accentColor.copy(alpha = 0.28f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(17.dp)
                )
            }

            Spacer(modifier = Modifier.height(CustomizeUI.MetricCardIconToTextSpacing))

            Column {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 26.sp,
                        lineHeight = 30.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.5.sp,
                        lineHeight = 14.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                    maxLines = 1
                )
            }
        }
    }
}
