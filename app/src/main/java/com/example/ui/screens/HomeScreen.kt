package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.balikin.data.local.TransactionDirection
import com.example.balikin.data.local.TransactionEntity
import com.example.balikin.ui.components.TransactionItemCard
import com.example.ui.theme.StatusBorrowedBorder
import com.example.ui.theme.StatusBorrowedContainer
import com.example.ui.theme.StatusBorrowedOrange
import com.example.ui.theme.StatusLentBlue
import com.example.ui.theme.StatusLentBorder
import com.example.ui.theme.StatusLentContainer
import com.example.ui.theme.StatusOverdueBorder
import com.example.ui.theme.StatusOverdueContainer
import com.example.ui.theme.StatusOverdueRed
import com.example.ui.theme.CustomizeUI

@Composable
fun HomeScreen(
    activeTransactions: List<TransactionEntity>,
    summaryCounts: Triple<Int, Int, Int>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedDirection: TransactionDirection?,
    onDirectionFilterChange: (TransactionDirection?) -> Unit,
    onAddClick: () -> Unit,
    onItemClick: (TransactionEntity) -> Unit,
    onMarkReturnedClick: (TransactionEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val (lentCount, borrowedCount, overdueCount) = summaryCounts

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Section
            item {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Image(
                        painter = painterResource(id = com.example.R.drawable.img_balikin_logo),
                        contentDescription = "Balikin",
                        modifier = Modifier
                            .height(28.dp)
                            .wrapContentWidth(align = Alignment.Start)
                            .testTag("app_logo"),
                        contentScale = ContentScale.FillHeight,
                        alignment = Alignment.CenterStart
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Catat pinjaman, ingat pengembalian",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Summary Metric Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = "Dipinjamkan",
                        count = lentCount,
                        icon = Icons.Default.ArrowUpward,
                        accentColor = CustomizeUI.MetricColorLent,
                        containerBg = CustomizeUI.MetricBgLent,
                        borderColor = CustomizeUI.MetricBorderLent,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Dipinjam",
                        count = borrowedCount,
                        icon = Icons.Default.ArrowDownward,
                        accentColor = CustomizeUI.MetricColorBorrowed,
                        containerBg = CustomizeUI.MetricBgBorrowed,
                        borderColor = CustomizeUI.MetricBorderBorrowed,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Terlambat",
                        count = overdueCount,
                        icon = Icons.Default.HourglassEmpty,
                        accentColor = CustomizeUI.MetricColorOverdue,
                        containerBg = CustomizeUI.MetricBgOverdue,
                        borderColor = CustomizeUI.MetricBorderOverdue,
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
                            .testTag("home_search_input"),
                        placeholder = {
                            Text(
                                "Cari barang atau nama orang...",
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
                            modifier = Modifier.testTag("filter_chip_all")
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
                            modifier = Modifier.testTag("filter_chip_lent")
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
                            modifier = Modifier.testTag("filter_chip_borrowed")
                        )
                    }
                }
            }

            // Transaction Cards or Empty State
            if (activeTransactions.isEmpty()) {
                item {
                    EmptyHomeContent(
                        searchQuery = searchQuery,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp)
                    )
                }
            } else {
                items(
                    items = activeTransactions,
                    key = { it.id }
                ) { transaction ->
                    TransactionItemCard(
                        transaction = transaction,
                        onClick = { onItemClick(transaction) },
                        onMarkReturnedClick = { onMarkReturnedClick(transaction) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = onAddClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("fab_add_transaction"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Catat Baru",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}


@Composable
private fun MetricCard(
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

@Composable
private fun EmptyHomeContent(
    searchQuery: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Inventory2,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (searchQuery.isNotBlank()) "Belum Ketemu, Nih" else "Belum Ada Pinjaman Aktif",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (searchQuery.isNotBlank())
                    "Belum ada catatan yang cocok dengan kata kunci '$searchQuery'. Coba kata lain, yuk."
                else
                    "Barang yang kamu pinjam atau pinjamkan bakal muncul di sini. Yuk, catat biar nggak lupa.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }
    }
}
