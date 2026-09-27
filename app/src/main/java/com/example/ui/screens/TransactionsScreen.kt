package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.KhataViewModel
import com.example.localization.KhataStrings
import com.example.ui.components.formatInr
import com.example.ui.theme.*
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    viewModel: KhataViewModel,
    onNavigateToCustomerKhata: (Long) -> Unit
) {
    val language by viewModel.language.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val customers by viewModel.customers.collectAsStateWithLifecycle()

    var typeFilter by remember { mutableStateOf("ALL") } // ALL, UDHAR, JAMA
    var timeFilter by remember { mutableStateOf("ALL") } // ALL, TODAY, THIS_MONTH

    val customerMap = remember(customers) {
        customers.associateBy { it.id }
    }

    val filteredTransactions = remember(transactions, typeFilter, timeFilter) {
        val now = Calendar.getInstance()
        val startOfToday = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val startOfMonth = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1); set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        transactions.filter { tx ->
            val matchesType = when (typeFilter) {
                "UDHAR" -> tx.type.equals("UDHAR", true)
                "JAMA" -> tx.type.equals("JAMA", true)
                else -> true
            }
            val matchesTime = when (timeFilter) {
                "TODAY" -> tx.dateMillis >= startOfToday
                "THIS_MONTH" -> tx.dateMillis >= startOfMonth
                else -> true
            }
            matchesType && matchesTime
        }
    }

    val totalUdhar = remember(filteredTransactions) {
        filteredTransactions.filter { it.type.equals("UDHAR", true) }.sumOf { it.amount }
    }
    val totalJama = remember(filteredTransactions) {
        filteredTransactions.filter { it.type.equals("JAMA", true) }.sumOf { it.amount }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = KhataStrings.get("nav_transactions", language),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(KhataBg)
        ) {
            // Summary Strip
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("કુલ ઉધાર", style = MaterialTheme.typography.labelSmall, color = KhataTextSecondary)
                        Text(formatInr(totalUdhar), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = UdharRed)
                    }
                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(KhataCardBorder))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("કુલ જમા", style = MaterialTheme.typography.labelSmall, color = KhataTextSecondary)
                        Text(formatInr(totalJama), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = JamaGreen)
                    }
                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(KhataCardBorder))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("કુલ વ્યવહારો", style = MaterialTheme.typography.labelSmall, color = KhataTextSecondary)
                        Text("${filteredTransactions.size}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = AdvanceBlue)
                    }
                }
            }

            // Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = typeFilter == "ALL",
                    onClick = { typeFilter = "ALL" },
                    label = { Text("બધા") }
                )
                FilterChip(
                    selected = typeFilter == "UDHAR",
                    onClick = { typeFilter = "UDHAR" },
                    label = { Text("માત્ર ઉધાર") },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = UdharRedBg, selectedLabelColor = UdharRed)
                )
                FilterChip(
                    selected = typeFilter == "JAMA",
                    onClick = { typeFilter = "JAMA" },
                    label = { Text("માત્ર જમા") },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = JamaGreenBg, selectedLabelColor = JamaGreen)
                )
                FilterChip(
                    selected = timeFilter == "TODAY",
                    onClick = { timeFilter = if (timeFilter == "TODAY") "ALL" else "TODAY" },
                    label = { Text("આજ") }
                )
            }

            // Transactions Feed
            if (filteredTransactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Receipt, contentDescription = null, tint = KhataTextMuted, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "કોઈ વ્યવહાર મળ્યો નથી",
                            style = MaterialTheme.typography.bodyMedium,
                            color = KhataTextSecondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredTransactions, key = { it.id }) { tx ->
                        val isUdhar = tx.type.equals("UDHAR", true)
                        val customer = customerMap[tx.customerId]

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { customer?.let { onNavigateToCustomerKhata(it.id) } },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(14.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(if (isUdhar) UdharRedBg else JamaGreenBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isUdhar) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                        contentDescription = null,
                                        tint = if (isUdhar) UdharRed else JamaGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = customer?.name ?: "ગ્રાહક #${tx.customerId}",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = KhataTextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${tx.dateString} ${tx.timeString} • ${tx.description.ifEmpty { tx.paymentMethod }}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = KhataTextSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = (if (isUdhar) "+ " else "- ") + formatInr(tx.amount),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (isUdhar) UdharRed else JamaGreen
                                    )
                                    Text(
                                        text = "ખાતાવહી →",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
