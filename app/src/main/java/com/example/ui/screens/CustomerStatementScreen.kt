package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.KhataViewModel
import com.example.data.BusinessProfileEntity
import com.example.data.CustomerEntity
import com.example.data.TransactionEntity
import com.example.localization.KhataStrings
import com.example.ui.components.KhataShareHelper
import com.example.ui.components.formatInr
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerStatementScreen(
    customerId: Long,
    viewModel: KhataViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val language by viewModel.language.collectAsStateWithLifecycle()
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    val profile by viewModel.businessProfile.collectAsStateWithLifecycle()

    val customer = remember(customers, customerId) {
        customers.find { it.id == customerId }
    }

    val transactionsFlow = remember(customerId) {
        viewModel.getTransactionsForCustomer(customerId)
    }
    val transactions by transactionsFlow.collectAsStateWithLifecycle(emptyList())

    BackHandler { onBack() }

    if (customer == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val totalUdhar = remember(transactions) {
        transactions.filter { it.type.equals("UDHAR", true) }.sumOf { it.amount }
    }
    val totalJama = remember(transactions) {
        transactions.filter { it.type.equals("JAMA", true) }.sumOf { it.amount }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "ખાતાવહી સ્ટેટમેન્ટ (Statement)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Share Text
                    IconButton(
                        onClick = {
                            val text = buildStatementText(customer, profile, transactions, totalUdhar, totalJama)
                            KhataShareHelper.shareText(context, text, "Khata Statement - ${customer.name}")
                        }
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share Statement")
                    }
                    // Print / PDF
                    IconButton(
                        onClick = {
                            val html = buildStatementHtml(customer, profile, transactions, totalUdhar, totalJama)
                            KhataShareHelper.printOrSavePdf(context, html, "Statement_${customer.name}")
                        }
                    ) {
                        Icon(Icons.Default.Print, contentDescription = "Print / Save PDF")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            Surface(
                shadowElevation = 8.dp,
                color = Color.White
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Send to WhatsApp
                    Button(
                        onClick = {
                            val text = buildStatementText(customer, profile, transactions, totalUdhar, totalJama)
                            KhataShareHelper.openWhatsApp(context, customer.mobile, text)
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = JamaGreen)
                    ) {
                        Icon(Icons.Default.Chat, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("WhatsApp શેર", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    // Print / Download PDF
                    Button(
                        onClick = {
                            val html = buildStatementHtml(customer, profile, transactions, totalUdhar, totalJama)
                            KhataShareHelper.printOrSavePdf(context, html, "Khata_${customer.name}")
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("PDF / પ્રિન્ટ", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(KhataBg),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Business & Customer Header Card (Invoice style)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Business Details
                        Text(
                            text = profile?.businessName ?: "વેપારી ખાતા બુક",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = KhataGreenDark
                        )
                        if (!profile?.ownerName.isNullOrBlank()) {
                            Text(
                                text = "પ્રોપ્રાઇટર: ${profile?.ownerName}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = KhataTextSecondary
                            )
                        }
                        if (!profile?.phone.isNullOrBlank()) {
                            Text(
                                text = "સંપર્ક: ${profile?.phone}",
                                style = MaterialTheme.typography.bodySmall,
                                color = KhataTextSecondary
                            )
                        }
                        if (!profile?.address.isNullOrBlank()) {
                            Text(
                                text = "સરનામું: ${profile?.address}",
                                style = MaterialTheme.typography.bodySmall,
                                color = KhataTextMuted
                            )
                        }
                        if (!profile?.gstNumber.isNullOrBlank()) {
                            Text(
                                text = "GST: ${profile?.gstNumber}",
                                style = MaterialTheme.typography.bodySmall,
                                color = KhataTextMuted
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                        // Customer Details
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "ગ્રાહક: ${customer.name}",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = KhataTextPrimary
                                )
                                if (customer.mobile.isNotBlank()) {
                                    Text(
                                        text = "મોબાઈલ: ${customer.mobile}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = KhataTextSecondary
                                    )
                                }
                                if (customer.address.isNotBlank()) {
                                    Text(
                                        text = "સરનામું: ${customer.address}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = KhataTextMuted
                                    )
                                }
                            }
                            Text(
                                text = "તારીખ: " + SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).format(Date()),
                                style = MaterialTheme.typography.labelSmall,
                                color = KhataTextSecondary
                            )
                        }
                    }
                }
            }

            // Summary Breakdown
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "ખાતાવહી સારાંશ (Statement Summary)",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = KhataTextPrimary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("શરૂઆતનું બાકી (Opening Balance):", style = MaterialTheme.typography.bodySmall, color = KhataTextSecondary)
                            Text(formatInr(customer.openingBalance), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("કુલ ઉધાર (+ Total Udhar):", style = MaterialTheme.typography.bodySmall, color = UdharRed)
                            Text(formatInr(totalUdhar), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = UdharRed)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("કુલ જમા (- Total Jama):", style = MaterialTheme.typography.bodySmall, color = JamaGreen)
                            Text(formatInr(totalJama), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = JamaGreen)
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "નેટ બાકી રકમ (Current Balance):",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = KhataTextPrimary
                            )
                            Text(
                                text = formatInr(customer.currentBalance),
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                color = if (customer.currentBalance > 0) UdharRed else JamaGreen
                            )
                        }
                    }
                }
            }

            // Transaction Table Header
            item {
                Text(
                    text = "તમામ વ્યવહારોની વિગત (${transactions.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = KhataTextPrimary
                )
            }

            if (transactions.isEmpty()) {
                item {
                    Text(
                        text = "કોઈ વ્યવહાર ઉપલબ્ધ નથી.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = KhataTextSecondary
                    )
                }
            } else {
                items(transactions, key = { it.id }) { tx ->
                    val isUdhar = tx.type.equals("UDHAR", true)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, KhataCardBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = tx.dateString.ifEmpty { tx.timeString },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = KhataTextSecondary
                                )
                                Text(
                                    text = if (isUdhar) "ઉધાર: ${tx.description.ifEmpty { "વસ્તુ" }}"
                                    else "જમા (${tx.paymentMethod}): ${tx.description.ifEmpty { "પેમેન્ટ" }}",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = KhataTextPrimary
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = (if (isUdhar) "+ " else "- ") + formatInr(tx.amount),
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (isUdhar) UdharRed else JamaGreen
                                )
                                Text(
                                    text = "બાકી: ${formatInr(tx.balanceAfter)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = KhataTextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun buildStatementText(
    customer: CustomerEntity,
    profile: BusinessProfileEntity?,
    transactions: List<TransactionEntity>,
    totalUdhar: Double,
    totalJama: Double
): String {
    val sb = StringBuilder()
    sb.append("📋 *ખાતાવહી સ્ટેટમેન્ટ (Khata Statement)*\n")
    sb.append("🏢 *${profile?.businessName ?: "વેપારી ખાતા બુક"}*\n")
    if (!profile?.phone.isNullOrBlank()) sb.append("સંપર્ક: ${profile?.phone}\n")
    sb.append("----------------------------\n")
    sb.append("👤 *ગ્રાહક:* ${customer.name}\n")
    if (customer.mobile.isNotBlank()) sb.append("📱 *મોબાઈલ:* ${customer.mobile}\n")
    sb.append("📅 *તારીખ:* ${SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).format(Date())}\n")
    sb.append("----------------------------\n")
    sb.append("• શરૂઆતનું બાકી: ${formatInr(customer.openingBalance)}\n")
    sb.append("• કુલ ઉધાર (+): ${formatInr(totalUdhar)}\n")
    sb.append("• કુલ જમા (-): ${formatInr(totalJama)}\n")
    sb.append("💰 *નેટ બાકી રકમ: ${formatInr(customer.currentBalance)}*\n")
    sb.append("----------------------------\n")
    sb.append("📝 *છેલ્લા વ્યવહારો:*\n")

    val recent = transactions.takeLast(10)
    for (t in recent) {
        val sign = if (t.type.equals("UDHAR", true)) "(+ ઉધાર)" else "(- જમા)"
        val desc = if (t.description.isNotBlank()) " - ${t.description}" else ""
        sb.append("${t.dateString}: ${formatInr(t.amount)} $sign$desc (બાકી: ${formatInr(t.balanceAfter)})\n")
    }

    if (!profile?.upiId.isNullOrBlank()) {
        sb.append("\n💳 *UPI ચુકવણી માટે ID:* ${profile?.upiId}\n")
    }
    sb.append("\nઆભાર!")
    return sb.toString()
}

private fun buildStatementHtml(
    customer: CustomerEntity,
    profile: BusinessProfileEntity?,
    transactions: List<TransactionEntity>,
    totalUdhar: Double,
    totalJama: Double
): String {
    val rows = StringBuilder()
    for (tx in transactions) {
        val isUdhar = tx.type.equals("UDHAR", true)
        val typeBadge = if (isUdhar) "<span style='color:#DC2626;font-weight:bold;'>ઉધાર (+)</span>"
        else "<span style='color:#16A34A;font-weight:bold;'>જમા (-)</span>"
        val udharAmt = if (isUdhar) formatInr(tx.amount) else "-"
        val jamaAmt = if (!isUdhar) formatInr(tx.amount) else "-"

        rows.append("""
            <tr>
                <td style='padding:8px;border:1px solid #ddd;'>${tx.dateString}</td>
                <td style='padding:8px;border:1px solid #ddd;'>${tx.description.ifEmpty { tx.paymentMethod }}</td>
                <td style='padding:8px;border:1px solid #ddd;'>$typeBadge</td>
                <td style='padding:8px;border:1px solid #ddd;color:#DC2626;text-align:right;'>$udharAmt</td>
                <td style='padding:8px;border:1px solid #ddd;color:#16A34A;text-align:right;'>$jamaAmt</td>
                <td style='padding:8px;border:1px solid #ddd;text-align:right;font-weight:bold;'>${formatInr(tx.balanceAfter)}</td>
            </tr>
        """.trimIndent())
    }

    return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <title>Khata Statement</title>
            <style>
                body { font-family: 'Noto Sans', sans-serif, Arial; margin: 20px; color: #1E293B; }
                .header { text-align: center; border-bottom: 2px solid #0F766E; padding-bottom: 15px; margin-bottom: 20px; }
                .biz-title { font-size: 24px; font-weight: bold; color: #0F766E; margin: 0; }
                .info-table { width: 100%; margin-bottom: 20px; border-collapse: collapse; }
                .summary-box { background: #F8FAFC; border: 1px solid #E2E8F0; padding: 15px; border-radius: 8px; margin-bottom: 20px; }
                .data-table { width: 100%; border-collapse: collapse; font-size: 13px; }
                .data-table th { background: #0F766E; color: white; padding: 8px; text-align: left; }
                .footer { text-align: center; margin-top: 30px; font-size: 12px; color: #64748B; }
            </style>
        </head>
        <body>
            <div class="header">
                <h1 class="biz-title">${profile?.businessName ?: "વેપારી ખાતા બુક"}</h1>
                <p style="margin:4px 0;">${profile?.ownerName ?: ""} | ફોન: ${profile?.phone ?: ""}</p>
                <p style="margin:4px 0;font-size:12px;">${profile?.address ?: ""} ${if (!profile?.gstNumber.isNullOrBlank()) " | GST: " + profile?.gstNumber else ""}</p>
            </div>

            <table class="info-table">
                <tr>
                    <td>
                        <strong>ગ્રાહક:</strong> ${customer.name}<br>
                        <strong>મોબાઈલ:</strong> ${customer.mobile}<br>
                        <strong>સરનામું:</strong> ${customer.address}
                    </td>
                    <td style="text-align:right;vertical-align:top;">
                        <strong>સ્ટેટમેન્ટ તારીખ:</strong> ${SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).format(Date())}
                    </td>
                </tr>
            </table>

            <div class="summary-box">
                <table style="width:100%;">
                    <tr>
                        <td>શરૂઆતનું બાકી: <strong>${formatInr(customer.openingBalance)}</strong></td>
                        <td style="color:#DC2626;">કુલ ઉધાર (+): <strong>${formatInr(totalUdhar)}</strong></td>
                        <td style="color:#16A34A;">કુલ જમા (-): <strong>${formatInr(totalJama)}</strong></td>
                        <td style="text-align:right;font-size:16px;">નેટ બાકી રકમ: <strong style="color:#DC2626;">${formatInr(customer.currentBalance)}</strong></td>
                    </tr>
                </table>
            </div>

            <table class="data-table">
                <thead>
                    <tr>
                        <th>તારીખ</th>
                        <th>વિગત</th>
                        <th>પ્રકાર</th>
                        <th style="text-align:right;">ઉધાર (+)</th>
                        <th style="text-align:right;">જમા (-)</th>
                        <th style="text-align:right;">બાકી રકમ</th>
                    </tr>
                </thead>
                <tbody>
                    $rows
                </tbody>
            </table>

            <div class="footer">
                ${if (!profile?.upiId.isNullOrBlank()) "<p><strong>UPI ID:</strong> " + profile?.upiId + "</p>" else ""}
                <p>આ ડિજિટલ ખાતાવહી દ્વારા જનરેટ કરેલ કમ્પ્યુટરાઇઝ્ડ સ્ટેટમેન્ટ છે. આભાર!</p>
            </div>
        </body>
        </html>
    """.trimIndent()
}
