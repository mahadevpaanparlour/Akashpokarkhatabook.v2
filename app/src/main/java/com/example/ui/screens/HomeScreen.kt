package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.KhataViewModel
import com.example.data.CustomerEntity
import com.example.localization.AppLanguage
import com.example.localization.KhataStrings
import com.example.ui.components.BalanceStatusChip
import com.example.ui.components.KhataShareHelper
import com.example.ui.components.StatCard
import com.example.ui.components.formatInr
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: KhataViewModel,
    onNavigateToCustomers: () -> Unit,
    onNavigateToCustomerKhata: (Long) -> Unit,
    onOpenAddCustomerDialog: () -> Unit,
    onNavigateToReports: () -> Unit
) {
    val context = LocalContext.current
    val language by viewModel.language.collectAsStateWithLifecycle()
    val summary by viewModel.dashboardSummary.collectAsStateWithLifecycle()
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val profile by viewModel.businessProfile.collectAsStateWithLifecycle()

    val pendingCustomers = remember(customers) {
        customers.filter { it.currentBalance > 0 }.sortedByDescending { it.currentBalance }
    }

    val recentTransactions = remember(transactions) {
        transactions.take(5)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = profile?.businessName ?: KhataStrings.get("app_title", language),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = profile?.ownerName ?: "ડિજિટલ ખાતાવહી",
                            style = MaterialTheme.typography.bodySmall,
                            color = KhataTextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                actions = {
                    // Quick language toggle chip
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clickable {
                                val nextLang = when (language) {
                                    AppLanguage.GUJARATI -> AppLanguage.HINDI
                                    AppLanguage.HINDI -> AppLanguage.ENGLISH
                                    AppLanguage.ENGLISH -> AppLanguage.GUJARATI
                                }
                                viewModel.setLanguage(nextLang)
                            }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Translate,
                                contentDescription = "Language",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = language.nativeName,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(KhataBg),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Main Hero Card: Total Receivable / Outstanding
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = KhataStrings.get("total_receivable", language),
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "${summary.pendingCustomersCount} ${KhataStrings.get("pending_customers", language)}",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Text(
                            text = formatInr(summary.totalReceivable),
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp
                            ),
                            color = Color.White
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = Color.White.copy(alpha = 0.2f)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = KhataStrings.get("today_udhar", language),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                                Text(
                                    text = formatInr(summary.todayUdhar),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFFFFD1D1) // Light red tint for Udhar
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(30.dp)
                                    .background(Color.White.copy(alpha = 0.2f))
                            )
                            Column {
                                Text(
                                    text = KhataStrings.get("today_jama", language),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                                Text(
                                    text = formatInr(summary.todayJama),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFFD1FAE5) // Light green tint for Jama
                                )
                            }
                        }
                    }
                }
            }

            // Quick Action Buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onOpenAddCustomerDialog,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("home_add_customer_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = KhataStrings.get("btn_add_customer", language),
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1
                        )
                    }

                    OutlinedButton(
                        onClick = onNavigateToReports,
                        modifier = Modifier
                            .height(50.dp)
                            .testTag("home_view_reports_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = KhataGreenDark)
                    ) {
                        Icon(Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(KhataStrings.get("nav_reports", language), fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Dashboard Grid Stat Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = KhataStrings.get("total_received", language),
                        amount = formatInr(summary.totalJamaReceived),
                        icon = Icons.Default.ArrowDownward,
                        iconColor = JamaGreen,
                        containerColor = JamaGreenBg.copy(alpha = 0.5f),
                        modifier = Modifier.weight(1f),
                        subText = "કુલ આવેલી રકમ"
                    )

                    StatCard(
                        title = KhataStrings.get("total_customers", language),
                        amount = "${summary.totalCustomers}",
                        icon = Icons.Default.Groups,
                        iconColor = AdvanceBlue,
                        containerColor = AdvanceBlueBg.copy(alpha = 0.5f),
                        modifier = Modifier.weight(1f),
                        subText = "${summary.settledCustomersCount} ${KhataStrings.get("settled_customers", language)}",
                        onClick = onNavigateToCustomers
                    )
                }
            }

            // High Pending Customers Section
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = KhataStrings.get("pending_customers", language),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = KhataTextPrimary
                    )
                    TextButton(onClick = onNavigateToCustomers) {
                        Text(
                            text = KhataStrings.get("filter_all", language) + " →",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            if (pendingCustomers.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = JamaGreen,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "કોઈ ગ્રાહકનું બાકી નથી! બધા ચૂકતે છે.",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = KhataTextSecondary
                            )
                        }
                    }
                }
            } else {
                items(pendingCustomers.take(5), key = { it.id }) { customer ->
                    CustomerCardItem(
                        customer = customer,
                        language = language,
                        onClick = { onNavigateToCustomerKhata(customer.id) },
                        onCall = { KhataShareHelper.callCustomer(context, customer.mobile) },
                        onWhatsApp = {
                            val msg = KhataShareHelper.generateReminderMessage(customer, profile, language)
                            KhataShareHelper.openWhatsApp(context, customer.mobile, msg)
                        }
                    )
                }
            }

            // Recent Transactions Section
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = KhataStrings.get("today_activity", language),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = KhataTextPrimary
                    )
                }
            }

            if (recentTransactions.isEmpty()) {
                item {
                    Text(
                        text = KhataStrings.get("no_transactions", language),
                        style = MaterialTheme.typography.bodySmall,
                        color = KhataTextMuted,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            } else {
                items(recentTransactions, key = { it.id }) { tx ->
                    val cust = customers.find { it.id == tx.customerId }
                    RecentTransactionItem(
                        customerName = cust?.name ?: "ગ્રાહક",
                        type = tx.type,
                        amount = tx.amount,
                        date = tx.dateString.ifEmpty { tx.timeString },
                        description = tx.description.ifEmpty { tx.paymentMethod },
                        language = language,
                        onClick = { cust?.let { onNavigateToCustomerKhata(it.id) } }
                    )
                }
            }
        }
    }
}

@Composable
fun CustomerCardItem(
    customer: CustomerEntity,
    language: AppLanguage,
    onClick: () -> Unit,
    onCall: () -> Unit,
    onWhatsApp: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("customer_card_${customer.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar with initial letter
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        if (customer.currentBalance > 0) UdharRedBg
                        else if (customer.currentBalance < 0) AdvanceBlueBg
                        else JamaGreenBg
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = customer.name.firstOrNull()?.toString() ?: "G",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (customer.currentBalance > 0) UdharRed
                    else if (customer.currentBalance < 0) AdvanceBlue
                    else JamaGreen
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = customer.name,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = KhataTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (customer.mobile.isNotBlank()) {
                    Text(
                        text = "📞 ${customer.mobile}",
                        style = MaterialTheme.typography.bodySmall,
                        color = KhataTextSecondary,
                        maxLines = 1
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatInr(customer.currentBalance),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (customer.currentBalance > 0) UdharRed
                    else if (customer.currentBalance < 0) AdvanceBlue
                    else JamaGreen
                )

                BalanceStatusChip(balance = customer.currentBalance, language = language)
            }
        }
    }
}

@Composable
fun RecentTransactionItem(
    customerName: String,
    type: String,
    amount: Double,
    date: String,
    description: String,
    language: AppLanguage,
    onClick: () -> Unit
) {
    val isUdhar = type.equals("UDHAR", ignoreCase = true)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isUdhar) UdharRedBg else JamaGreenBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isUdhar) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                    contentDescription = null,
                    tint = if (isUdhar) UdharRed else JamaGreen,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = customerName,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = KhataTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "$date • $description",
                    style = MaterialTheme.typography.bodySmall,
                    color = KhataTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = (if (isUdhar) "+ " else "- ") + formatInr(amount),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = if (isUdhar) UdharRed else JamaGreen
            )
        }
    }
}
