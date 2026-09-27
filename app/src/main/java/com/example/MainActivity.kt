package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.localization.KhataStrings
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

enum class KhataScreen {
    HOME,
    CUSTOMERS,
    TRANSACTIONS,
    REPORTS,
    SETTINGS,
    CUSTOMER_KHATA,
    CUSTOMER_STATEMENT
}

data class NavItem(
    val screen: KhataScreen,
    val titleKey: String,
    val icon: ImageVector,
    val testTag: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: KhataViewModel = viewModel()
            val appSettings by viewModel.appSettings.collectAsStateWithLifecycle()
            val isDark = when (appSettings?.themeMode) {
                "DARK" -> true
                "LIGHT" -> false
                else -> androidx.compose.foundation.isSystemInDarkTheme()
            }
            val themeColor = appSettings?.themeColor ?: "EMERALD"

            MyApplicationTheme(
                darkTheme = isDark,
                themeColorName = themeColor
            ) {
                KhataApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun KhataApp(viewModel: KhataViewModel = viewModel()) {
    val language by viewModel.language.collectAsStateWithLifecycle()
    val isUnlocked by viewModel.isUnlocked.collectAsStateWithLifecycle()

    var currentScreen by remember { mutableStateOf(KhataScreen.HOME) }
    var activeCustomerId by remember { mutableStateOf<Long?>(null) }
    var openAddCustomerDialog by remember { mutableStateOf(false) }

    // If locked by PIN, show PIN screen
    if (!isUnlocked) {
        PinLockScreen(
            viewModel = viewModel,
            onSuccess = { /* ViewModel sets isUnlocked = true */ }
        )
        return
    }

    val navItems = listOf(
        NavItem(KhataScreen.HOME, "nav_home", Icons.Default.Home, "nav_tab_home"),
        NavItem(KhataScreen.CUSTOMERS, "nav_customers", Icons.Default.People, "nav_tab_customers"),
        NavItem(KhataScreen.TRANSACTIONS, "nav_transactions", Icons.Default.ReceiptLong, "nav_tab_transactions"),
        NavItem(KhataScreen.REPORTS, "nav_reports", Icons.Default.BarChart, "nav_tab_reports"),
        NavItem(KhataScreen.SETTINGS, "nav_settings", Icons.Default.Settings, "nav_tab_settings")
    )

    // Back handling for sub-screens and tabs
    BackHandler(enabled = currentScreen != KhataScreen.HOME) {
        when (currentScreen) {
            KhataScreen.CUSTOMER_STATEMENT -> {
                currentScreen = KhataScreen.CUSTOMER_KHATA
            }
            KhataScreen.CUSTOMER_KHATA -> {
                currentScreen = KhataScreen.CUSTOMERS
            }
            else -> {
                currentScreen = KhataScreen.HOME
            }
        }
    }

    val showBottomBar = currentScreen !in listOf(KhataScreen.CUSTOMER_KHATA, KhataScreen.CUSTOMER_STATEMENT)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = NavigationBarDefaults.Elevation
                ) {
                    navItems.forEach { item ->
                        val selected = currentScreen == item.screen
                        NavigationBarItem(
                            selected = selected,
                            onClick = { currentScreen = item.screen },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = KhataStrings.get(item.titleKey, language)
                                )
                            },
                            label = {
                                Text(
                                    text = KhataStrings.get(item.titleKey, language),
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            modifier = Modifier.testTag(item.testTag)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                KhataScreen.HOME -> {
                    HomeScreen(
                        viewModel = viewModel,
                        onNavigateToCustomers = { currentScreen = KhataScreen.CUSTOMERS },
                        onNavigateToCustomerKhata = { id ->
                            activeCustomerId = id
                            currentScreen = KhataScreen.CUSTOMER_KHATA
                        },
                        onOpenAddCustomerDialog = {
                            openAddCustomerDialog = true
                            currentScreen = KhataScreen.CUSTOMERS
                        },
                        onNavigateToReports = { currentScreen = KhataScreen.REPORTS }
                    )
                }
                KhataScreen.CUSTOMERS -> {
                    CustomersScreen(
                        viewModel = viewModel,
                        onNavigateToCustomerKhata = { id ->
                            activeCustomerId = id
                            currentScreen = KhataScreen.CUSTOMER_KHATA
                        },
                        showAddDialogInitially = openAddCustomerDialog,
                        onResetAddDialogFlag = { openAddCustomerDialog = false }
                    )
                }
                KhataScreen.TRANSACTIONS -> {
                    TransactionsScreen(
                        viewModel = viewModel,
                        onNavigateToCustomerKhata = { id ->
                            activeCustomerId = id
                            currentScreen = KhataScreen.CUSTOMER_KHATA
                        }
                    )
                }
                KhataScreen.REPORTS -> {
                    ReportsScreen(
                        viewModel = viewModel,
                        onNavigateToCustomerKhata = { id ->
                            activeCustomerId = id
                            currentScreen = KhataScreen.CUSTOMER_KHATA
                        }
                    )
                }
                KhataScreen.SETTINGS -> {
                    SettingsScreen(viewModel = viewModel)
                }
                KhataScreen.CUSTOMER_KHATA -> {
                    activeCustomerId?.let { id ->
                        CustomerKhataScreen(
                            customerId = id,
                            viewModel = viewModel,
                            onBack = { currentScreen = KhataScreen.CUSTOMERS },
                            onViewStatement = { custId ->
                                activeCustomerId = custId
                                currentScreen = KhataScreen.CUSTOMER_STATEMENT
                            }
                        )
                    }
                }
                KhataScreen.CUSTOMER_STATEMENT -> {
                    activeCustomerId?.let { id ->
                        CustomerStatementScreen(
                            customerId = id,
                            viewModel = viewModel,
                            onBack = { currentScreen = KhataScreen.CUSTOMER_KHATA }
                        )
                    }
                }
            }
        }
    }
}
