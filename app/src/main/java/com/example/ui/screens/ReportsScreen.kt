package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.KhataViewModel
import com.example.localization.KhataStrings
import com.example.ui.components.KhataShareHelper
import com.example.ui.components.formatInr
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: KhataViewModel,
    onNavigateToCustomerKhata: (Long) -> Unit
) {
    val context = LocalContext.current
    val language by viewModel.language.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    val profile by viewModel.businessProfile.collectAsStateWithLifecycle()

    var selectedPeriod by remember { mutableStateOf("THIS_MONTH") } // TODAY, THIS_WEEK, THIS_MONTH, ALL

    val filteredTransactions = remember(transactions, selectedPeriod) {
        val now = Calendar.getInstance()
        val startOfToday = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val startOfWeek = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val startOfMonth = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        when (selectedPeriod) {
            "TODAY" -> transactions.filter { it.dateMillis >= startOfToday }
            "THIS_WEEK" -> transactions.filter { it.dateMillis >= startOfWeek }
            "THIS_MONTH" -> transactions.filter { it.dateMillis >= startOfMonth }
            else -> transactions
        }
    }

    val periodUdhar = remember(filteredTransactions) {
        filteredTransactions.filter { it.type.equals("UDHAR", true) }.sumOf { it.amount }
    }
    val periodJama = remember(filteredTransactions) {
        filteredTransactions.filter { it.type.equals("JAMA", true) }.sumOf { it.amount }
    }

    val totalOutstanding = remember(customers) {
        customers.filter { it.currentBalance > 0 }.sumOf { it.currentBalance }
    }

    // Payment mode breakdown
    val paymentModes = remember(filteredTransactions) {
        val jamaList = filteredTransactions.filter { it.type.equals("JAMA", true) }
        val cash = jamaList.filter { it.paymentMethod.equals("Cash", true) }.sumOf { it.amount }
        val upi = jamaList.filter { it.paymentMethod.equals("UPI", true) }.sumOf { it.amount }
        val bank = jamaList.filter { it.paymentMethod.equals("Bank", true) }.sumOf { it.amount }
        val other = jamaList.filter { !it.paymentMethod.equals("Cash", true) && !it.paymentMethod.equals("UPI", true) && !it.paymentMethod.equals("Bank", true) }.sumOf { it.amount }
        mapOf("Cash" to cash, "UPI" to upi, "Bank" to bank, "Other" to other)
    }

    // Top pending customers
    val topPendingCustomers = remember(customers) {
        customers.filter { it.currentBalance > 0 }.sortedByDescending { it.currentBalance }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = KhataStrings.get("nav_reports", language),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                actions = {
                    IconButton(
                        onClick = {
                            val reportText = buildString {
                                append("📊 *${profile?.businessName ?: "ખાતા બુક"} - વ્યાપાર રિપોર્ટ*\n")
                                append("તારીખ: ${SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).format(Date())}\n")
                                append("સમયગાળો: $selectedPeriod\n")
                                append("---------------------------\n")
                                append("• કુલ બાકી રકમ (Outstanding): ${formatInr(totalOutstanding)}\n")
                                append("• પસંદ કરેલ સમયગાળામાં ઉધાર: ${formatInr(periodUdhar)}\n")
                                append("• પસંદ કરેલ સમયગાળામાં જમા: ${formatInr(periodJama)}\n")
                                append("• રોકડ (Cash): ${formatInr(paymentModes["Cash"] ?: 0.0)}\n")
                                append("• UPI: ${formatInr(paymentModes["UPI"] ?: 0.0)}\n")
                                append("---------------------------\n")
                                append("ટોચના બાકી ગ્રાહકો:\n")
                                for (c in topPendingCustomers.take(5)) {
                                    append("${c.name}: ${formatInr(c.currentBalance)}\n")
                                }
                            }
                            KhataShareHelper.shareText(context, reportText, "Khata Business Report")
                        }
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share Report")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
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
            // Period Selector Chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedPeriod == "TODAY",
                        onClick = { selectedPeriod = "TODAY" },
                        label = { Text("આજ (Today)") }
                    )
                    FilterChip(
                        selected = selectedPeriod == "THIS_WEEK",
                        onClick = { selectedPeriod = "THIS_WEEK" },
                        label = { Text("આ અઠવાડિયું (This Week)") }
                    )
                    FilterChip(
                        selected = selectedPeriod == "THIS_MONTH",
                        onClick = { selectedPeriod = "THIS_MONTH" },
                        label = { Text("આ મહિનો (This Month)") }
                    )
                    FilterChip(
                        selected = selectedPeriod == "ALL",
                        onClick = { selectedPeriod = "ALL" },
                        label = { Text("તમામ સમય (All Time)") }
                    )
                }
            }

            // Period Summary Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "સમયગાળાનો હિસાબ સારાંશ",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = KhataTextPrimary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Period Udhar
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = UdharRedBg)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("ઉધાર આપ્યા (+)", style = MaterialTheme.typography.labelSmall, color = UdharRed)
                                    Text(
                                        formatInr(periodUdhar),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                        color = UdharRed
                                    )
                                }
                            }

                            // Period Jama
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = JamaGreenBg)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("જમા મળ્યા (-)", style = MaterialTheme.typography.labelSmall, color = JamaGreen)
                                    Text(
                                        formatInr(periodJama),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                        color = JamaGreen
                                    )
                                }
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "નેટ વ્યાપાર તફાવત:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = KhataTextSecondary
                            )
                            val diff = periodUdhar - periodJama
                            Text(
                                text = formatInr(diff),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (diff > 0) UdharRed else JamaGreen
                            )
                        }
                    }
                }
            }

            // Payment Mode Summary (Cash vs UPI vs Bank)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "ચુકવણી સંગ્રહ પદ્ધતિઓ (Payment Collection)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = KhataTextPrimary
                        )

                        PaymentMethodRow("રોકડ (Cash)", paymentModes["Cash"] ?: 0.0, Icons.Default.Payments, JamaGreen)
                        PaymentMethodRow("UPI (GPay / PhonePe)", paymentModes["UPI"] ?: 0.0, Icons.Default.QrCodeScanner, AdvanceBlue)
                        PaymentMethodRow("બેંક ટ્રાન્સફર (Bank)", paymentModes["Bank"] ?: 0.0, Icons.Default.AccountBalance, KhataAmber)
                    }
                }
            }

            // Top Outstanding Customers Leaderboard
            item {
                Text(
                    text = "સૌથી વધુ બાકી વાળા ગ્રાહકો (${topPendingCustomers.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = KhataTextPrimary
                )
            }

            if (topPendingCustomers.isEmpty()) {
                item {
                    Text(
                        text = "કોઈ ગ્રાહકનું બાકી નથી.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = KhataTextSecondary
                    )
                }
            } else {
                items(topPendingCustomers, key = { it.id }) { cust ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToCustomerKhata(cust.id) },
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
                                    .background(UdharRedBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = cust.name.firstOrNull()?.toString() ?: "C",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = UdharRed
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = cust.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = KhataTextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = cust.mobile.ifEmpty { "મોબાઈલ નથી" },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = KhataTextSecondary
                                )
                            }
                            Text(
                                text = formatInr(cust.currentBalance),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = UdharRed
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentMethodRow(
    title: String,
    amount: Double,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(title, style = MaterialTheme.typography.bodyMedium, color = KhataTextPrimary)
        }
        Text(
            formatInr(amount),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = KhataTextPrimary
        )
    }
}
