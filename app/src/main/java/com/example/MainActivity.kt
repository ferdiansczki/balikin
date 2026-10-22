package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.balikin.data.local.TransactionEntity
import com.example.balikin.ui.MainViewModel
import com.example.balikin.ui.components.AddEditTransactionDialog
import com.example.balikin.ui.components.TransactionDetailDialog
import com.example.ui.navigation.NavigationDestination
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.StatsScreen
import com.example.ui.theme.MyApplicationTheme
import androidx.compose.runtime.saveable.rememberSaveable
import com.example.ui.screens.SplashScreen
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )
        setContent {
            var showSplash by rememberSaveable { mutableStateOf(true) }

            MyApplicationTheme {
                if (showSplash) {
                    SplashScreen(onSplashFinished = { showSplash = false })
                } else {
                    BalikinApp(viewModel = mainViewModel)
                }
            }
        }
    }
}

@Composable
fun BalikinApp(
    viewModel: MainViewModel
) {
    var currentDestination by remember { mutableStateOf(NavigationDestination.BERANDA) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Observe app lifecycle to refresh time-dependent OVERDUE status on resume
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onResume()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Reactive State Collections
    val activeTransactions by viewModel.filteredActiveTransactions.collectAsStateWithLifecycle()
    val completedTransactions by viewModel.filteredCompletedTransactions.collectAsStateWithLifecycle()
    val homeSummary by viewModel.homeSummary.collectAsStateWithLifecycle()
    val statsData by viewModel.statsData.collectAsStateWithLifecycle()

    val homeSearch by viewModel.homeSearchQuery.collectAsStateWithLifecycle()
    val homeDirection by viewModel.homeDirectionFilter.collectAsStateWithLifecycle()

    val historySearch by viewModel.historySearchQuery.collectAsStateWithLifecycle()
    val historyDirection by viewModel.historyDirectionFilter.collectAsStateWithLifecycle()

    // Dialog state holders
    var showAddEditDialog by remember { mutableStateOf(false) }
    var transactionToEdit by remember { mutableStateOf<TransactionEntity?>(null) }
    var selectedTransactionForDetail by remember { mutableStateOf<TransactionEntity?>(null) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                NavigationDestination.entries.forEach { destination ->
                    val isSelected = currentDestination == destination
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentDestination = destination },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                                contentDescription = destination.title
                            )
                        },
                        label = {
                            Text(
                                text = destination.title,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag(destination.testTag)
                    )
                }
            }
        }
    ) { innerPadding ->
        AnimatedContent(
            targetState = currentDestination,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "screen_transition",
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) { destination ->
            when (destination) {
                NavigationDestination.BERANDA -> {
                    HomeScreen(
                        activeTransactions = activeTransactions,
                        summaryCounts = homeSummary,
                        searchQuery = homeSearch,
                        onSearchQueryChange = { viewModel.homeSearchQuery.value = it },
                        selectedDirection = homeDirection,
                        onDirectionFilterChange = { viewModel.homeDirectionFilter.value = it },
                        onAddClick = {
                            transactionToEdit = null
                            showAddEditDialog = true
                        },
                        onItemClick = { transaction ->
                            selectedTransactionForDetail = transaction
                        },
                        onMarkReturnedClick = { transaction ->
                            viewModel.markAsReturned(transaction.id)
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "Barang '${transaction.itemName}' ditandai sudah kembali."
                                )
                            }
                        }
                    )
                }
                NavigationDestination.RIWAYAT -> {
                    HistoryScreen(
                        completedTransactions = completedTransactions,
                        searchQuery = historySearch,
                        onSearchQueryChange = { viewModel.historySearchQuery.value = it },
                        selectedDirection = historyDirection,
                        onDirectionFilterChange = { viewModel.historyDirectionFilter.value = it },
                        onItemClick = { transaction ->
                            selectedTransactionForDetail = transaction
                        }
                    )
                }
                NavigationDestination.STATISTIK -> {
                    StatsScreen(statsData = statsData)
                }
            }
        }
    }

    // Add / Edit Transaction Dialog
    if (showAddEditDialog) {
        AddEditTransactionDialog(
            initialTransaction = transactionToEdit,
            onDismiss = {
                showAddEditDialog = false
                transactionToEdit = null
            },
            onSave = { entity, newPhotoUri ->
                viewModel.saveTransaction(
                    transaction = entity,
                    newPhotoUri = newPhotoUri,
                    onSuccess = {
                        showAddEditDialog = false
                        transactionToEdit = null
                        selectedTransactionForDetail = null
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Transaksi '${entity.itemName}' berhasil disimpan.")
                        }
                    },
                    onError = { errorMsg ->
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Error: $errorMsg")
                        }
                    }
                )
            }
        )
    }

    // Transaction Detail Dialog
    selectedTransactionForDetail?.let { transaction ->
        TransactionDetailDialog(
            transaction = transaction,
            onDismiss = { selectedTransactionForDetail = null },
            onEditClick = {
                transactionToEdit = transaction
                showAddEditDialog = true
            },
            onDeleteClick = {
                viewModel.deleteTransaction(transaction) {
                    selectedTransactionForDetail = null
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Transaksi '${transaction.itemName}' berhasil dihapus.")
                    }
                }
            },
            onMarkReturned = { returnDate ->
                viewModel.markAsReturned(transaction.id, returnDate)
                selectedTransactionForDetail = null
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Barang '${transaction.itemName}' ditandai sudah kembali.")
                }
            },
            onUnmarkReturned = {
                viewModel.unmarkReturned(transaction.id)
                selectedTransactionForDetail = null
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Barang '${transaction.itemName}' dikembalikan ke pinjaman aktif.")
                }
            }
        )
    }
}
