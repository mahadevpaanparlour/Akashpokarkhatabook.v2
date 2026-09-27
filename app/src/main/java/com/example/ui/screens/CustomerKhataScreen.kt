package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.KhataViewModel
import com.example.data.CustomerEntity
import com.example.data.TransactionEntity
import com.example.localization.AppLanguage
import com.example.localization.KhataStrings
import com.example.ui.components.*
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerKhataScreen(
    customerId: Long,
    viewModel: KhataViewModel,
    onBack: () -> Unit,
    onViewStatement: (Long) -> Unit
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
    val allTransactions by transactionsFlow.collectAsStateWithLifecycle(emptyList())

    // Modal dialog states
    var showUdharDialog by remember { mutableStateOf(false) }
    var showJamaDialog by remember { mutableStateOf(false) }
    var transactionToEdit by remember { mutableStateOf<TransactionEntity?>(null) }
    var transactionToDelete by remember { mutableStateOf<TransactionEntity?>(null) }
    var showDeleteCustomerDialog by remember { mutableStateOf(false) }
    var showEditCustomerDialog by remember { mutableStateOf(false) }
    var showReminderPreviewDialog by remember { mutableStateOf(false) }

    // History filter
    var typeFilter by remember { mutableStateOf("ALL") } // ALL, UDHAR, JAMA

    BackHandler { onBack() }

    if (customer == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val displayTransactions = remember(allTransactions, typeFilter) {
        val reversed = allTransactions.reversed() // Show newest first
        when (typeFilter) {
            "UDHAR" -> reversed.filter { it.type.equals("UDHAR", ignoreCase = true) }
            "JAMA" -> reversed.filter { it.type.equals("JAMA", ignoreCase = true) }
            else -> reversed
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = customer.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (customer.mobile.isNotBlank()) {
                            Text(
                                text = "📞 ${customer.mobile}",
                                style = MaterialTheme.typography.bodySmall,
                                color = KhataTextSecondary
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showEditCustomerDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Customer")
                    }
                    IconButton(onClick = { showDeleteCustomerDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Customer", tint = UdharRed)
                    }
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
            // Outstanding Summary Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (customer.currentBalance > 0) Color(0xFFFEF2F2)
                    else if (customer.currentBalance < 0) Color(0xFFEFF6FF)
                    else Color(0xFFF0FDF4)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (customer.currentBalance > 0) UdharRed.copy(alpha = 0.3f)
                    else if (customer.currentBalance < 0) AdvanceBlue.copy(alpha = 0.3f)
                    else JamaGreen.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = when {
                            customer.currentBalance > 0 -> KhataStrings.get("net_outstanding", language) + " (લેવાના)"
                            customer.currentBalance < 0 -> "એડવાન્સ જમા (ગ્રાહક ના બાકી)"
                            else -> "હિસાબ સાફ (બધું ચૂકતે)"
                        },
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = if (customer.currentBalance > 0) UdharRed
                        else if (customer.currentBalance < 0) AdvanceBlue
                        else JamaGreen
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = formatInr(customer.currentBalance),
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = if (customer.currentBalance > 0) UdharRed
                        else if (customer.currentBalance < 0) AdvanceBlue
                        else JamaGreen
                    )

                    if (customer.openingBalance != 0.0) {
                        Text(
                            text = "Opening Balance: ${formatInr(customer.openingBalance)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = KhataTextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Secondary Action Buttons: Statement & WhatsApp Reminder
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onViewStatement(customer.id) },
                            modifier = Modifier.weight(1f).height(42.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("સ્ટેટમેન્ટ", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        }

                        if (customer.currentBalance > 0) {
                            Button(
                                onClick = { showReminderPreviewDialog = true },
                                modifier = Modifier.weight(1f).height(42.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = JamaGreen)
                            ) {
                                Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("રીમાઇન્ડર", style = MaterialTheme.typography.labelMedium, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Two Large Prominent Buttons: 🟢 JAMA and 🔴 UDHAR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 🔴 UDHAR BUTTON (Gave Udhar / Credit)
                BigKhataActionButton(
                    text = KhataStrings.get("btn_udhar", language),
                    subTitle = "+ ઉધાર આપ્યા",
                    icon = Icons.Default.ArrowUpward,
                    isUdhar = true,
                    onClick = { showUdharDialog = true },
                    modifier = Modifier.weight(1f),
                    testTag = "btn_add_udhar"
                )

                // 🟢 JAMA BUTTON (Got Jama / Payment)
                BigKhataActionButton(
                    text = KhataStrings.get("btn_jama", language),
                    subTitle = "- જમા મળ્યા",
                    icon = Icons.Default.ArrowDownward,
                    isUdhar = false,
                    onClick = { showJamaDialog = true },
                    modifier = Modifier.weight(1f),
                    testTag = "btn_add_jama"
                )
            }

            // Filter Chips for Transactions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = typeFilter == "ALL",
                    onClick = { typeFilter = "ALL" },
                    label = { Text("${KhataStrings.get("filter_all", language)} (${allTransactions.size})") }
                )
                FilterChip(
                    selected = typeFilter == "UDHAR",
                    onClick = { typeFilter = "UDHAR" },
                    label = {
                        val count = allTransactions.count { it.type.equals("UDHAR", true) }
                        Text("ઉધાર ($count)")
                    },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = UdharRedBg, selectedLabelColor = UdharRed)
                )
                FilterChip(
                    selected = typeFilter == "JAMA",
                    onClick = { typeFilter = "JAMA" },
                    label = {
                        val count = allTransactions.count { it.type.equals("JAMA", true) }
                        Text("જમા ($count)")
                    },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = JamaGreenBg, selectedLabelColor = JamaGreen)
                )
            }

            // Transaction History List
            if (displayTransactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Receipt,
                            contentDescription = null,
                            tint = KhataTextMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = KhataStrings.get("no_transactions", language),
                            style = MaterialTheme.typography.bodyMedium,
                            color = KhataTextSecondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(displayTransactions, key = { it.id }) { tx ->
                        TransactionHistoryCard(
                            transaction = tx,
                            language = language,
                            onEdit = { transactionToEdit = tx },
                            onDelete = { transactionToDelete = tx }
                        )
                    }
                }
            }
        }
    }

    // Udhar Dialog
    if (showUdharDialog) {
        TransactionEntryDialog(
            isUdhar = true,
            language = language,
            onDismiss = { showUdharDialog = false },
            onSave = { amount, desc, mode, note ->
                viewModel.addTransaction(
                    customerId = customer.id,
                    type = "UDHAR",
                    amount = amount,
                    description = desc,
                    paymentMethod = mode,
                    notes = note
                ) {
                    showUdharDialog = false
                    Toast.makeText(context, "ઉધાર સફળતાપૂર્વક ઉમેરાયું!", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    // Jama Dialog
    if (showJamaDialog) {
        TransactionEntryDialog(
            isUdhar = false,
            language = language,
            customerOutstanding = customer.currentBalance,
            onDismiss = { showJamaDialog = false },
            onSave = { amount, desc, mode, note ->
                viewModel.addTransaction(
                    customerId = customer.id,
                    type = "JAMA",
                    amount = amount,
                    description = desc,
                    paymentMethod = mode,
                    notes = note
                ) {
                    showJamaDialog = false
                    Toast.makeText(context, "જમા સફળતાપૂર્વક નોંધાયું!", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    // Edit Transaction Dialog
    transactionToEdit?.let { tx ->
        EditTransactionDialog(
            transaction = tx,
            language = language,
            onDismiss = { transactionToEdit = null },
            onSave = { updatedTx ->
                viewModel.updateTransaction(updatedTx) {
                    transactionToEdit = null
                    Toast.makeText(context, "વ્યવહાર સુધારાયો!", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    // Delete Transaction Confirmation Dialog
    transactionToDelete?.let { tx ->
        KhataConfirmDialog(
            show = true,
            title = KhataStrings.get("delete_confirm_title", language),
            message = KhataStrings.get("delete_tx_msg", language),
            onConfirm = {
                viewModel.deleteTransaction(tx.id, customer.id)
                transactionToDelete = null
                Toast.makeText(context, "વ્યવહાર ડિલીટ કર્યો", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { transactionToDelete = null }
        )
    }

    // Delete Customer Confirmation Dialog
    KhataConfirmDialog(
        show = showDeleteCustomerDialog,
        title = KhataStrings.get("delete_confirm_title", language),
        message = KhataStrings.get("delete_customer_msg", language),
        onConfirm = {
            viewModel.deleteCustomer(customer.id)
            showDeleteCustomerDialog = false
            onBack()
            Toast.makeText(context, "ગ્રાહક ડિલીટ કર્યો", Toast.LENGTH_SHORT).show()
        },
        onDismiss = { showDeleteCustomerDialog = false }
    )

    // Edit Customer Dialog
    if (showEditCustomerDialog) {
        AddEditCustomerDialog(
            customerToEdit = customer,
            language = language,
            onDismiss = { showEditCustomerDialog = false },
            onSave = { name, mobile, address, notes, _ ->
                viewModel.updateCustomer(customer.copy(name = name, mobile = mobile, address = address, notes = notes))
                showEditCustomerDialog = false
                Toast.makeText(context, "ગ્રાહક વિગતો સુધારી", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // WhatsApp Reminder Preview & Customization Dialog
    if (showReminderPreviewDialog) {
        var reminderText by remember {
            mutableStateOf(KhataShareHelper.generateReminderMessage(customer, profile, language))
        }

        Dialog(onDismissRequest = { showReminderPreviewDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "વોટ્સએપ તગાદો / રીમાઇન્ડર",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = KhataTextPrimary
                    )

                    OutlinedTextField(
                        value = reminderText,
                        onValueChange = { reminderText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 140.dp, max = 220.dp),
                        label = { Text("સંદેશો (સંપાદિત કરી શકો છો)") },
                        shape = RoundedCornerShape(10.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showReminderPreviewDialog = false }) {
                            Text(KhataStrings.get("btn_cancel", language))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                KhataShareHelper.openWhatsApp(context, customer.mobile, reminderText)
                                showReminderPreviewDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = JamaGreen)
                        ) {
                            Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("મોકલો (WhatsApp)", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionHistoryCard(
    transaction: TransactionEntity,
    language: AppLanguage,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isUdhar = transaction.type.equals("UDHAR", ignoreCase = true)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Type Badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isUdhar) UdharRedBg else JamaGreenBg
                ) {
                    Text(
                        text = if (isUdhar) "ઉધાર (UDHAR)" else "જમા (JAMA)",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isUdhar) UdharRed else JamaGreen,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "${transaction.dateString} ${transaction.timeString}",
                    style = MaterialTheme.typography.labelSmall,
                    color = KhataTextSecondary
                )

                Spacer(modifier = Modifier.weight(1f))

                // Actions: Edit and Delete
                IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp), tint = KhataTextSecondary)
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp), tint = UdharRed)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    if (transaction.description.isNotBlank()) {
                        Text(
                            text = transaction.description,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = KhataTextPrimary
                        )
                    }

                    val paymentDetails = buildString {
                        if (!isUdhar) append("માધ્યમ: ${transaction.paymentMethod} ")
                        if (transaction.notes.isNotBlank()) append("• ${transaction.notes}")
                    }

                    if (paymentDetails.isNotBlank()) {
                        Text(
                            text = paymentDetails,
                            style = MaterialTheme.typography.bodySmall,
                            color = KhataTextSecondary
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = (if (isUdhar) "+ " else "- ") + formatInr(transaction.amount),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = if (isUdhar) UdharRed else JamaGreen
                    )
                    Text(
                        text = "બાકી: ${formatInr(transaction.balanceAfter)}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = KhataTextSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionEntryDialog(
    isUdhar: Boolean,
    language: AppLanguage,
    customerOutstanding: Double = 0.0,
    onDismiss: () -> Unit,
    onSave: (amount: Double, description: String, paymentMode: String, notes: String) -> Unit
) {
    var amountStr by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var paymentMode by remember { mutableStateOf("Cash") } // Cash, UPI, Bank, Other
    var notes by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    val paymentOptions = listOf("Cash", "UPI", "Bank", "Other")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = if (isUdhar) "🔴 નવું ઉધાર આપ્યું (Add Udhar)" else "🟢 જમા રકમ આવી (Add Jama)",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = if (isUdhar) UdharRed else JamaGreen
                )

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = {
                        amountStr = it
                        if (it.isNotBlank()) error = false
                    },
                    label = { Text(KhataStrings.get("amount", language)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = error,
                    supportingText = {
                        if (error) Text("યોગ્ય રકમ દાખલ કરો", color = UdharRed)
                    },
                    modifier = Modifier.fillMaxWidth().testTag("input_tx_amount"),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                // Warning if Jama exceeds outstanding
                val enteredAmount = amountStr.toDoubleOrNull() ?: 0.0
                if (!isUdhar && customerOutstanding > 0 && enteredAmount > customerOutstanding) {
                    Surface(
                        color = AdvanceBlueBg,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = KhataStrings.get("advance_warning", language),
                            style = MaterialTheme.typography.bodySmall,
                            color = AdvanceBlue,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = {
                        Text(
                            if (isUdhar) KhataStrings.get("item_details", language)
                            else "વિગત (જેમ કે હપ્તો, હિસાબ, પેમેન્ટ)"
                        )
                    },
                    modifier = Modifier.fillMaxWidth().testTag("input_tx_desc"),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                // Payment mode selector for Jama
                if (!isUdhar) {
                    Column {
                        Text(
                            text = KhataStrings.get("payment_mode", language),
                            style = MaterialTheme.typography.labelMedium,
                            color = KhataTextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            paymentOptions.forEach { mode ->
                                FilterChip(
                                    selected = paymentMode == mode,
                                    onClick = { paymentMode = mode },
                                    label = { Text(mode) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(KhataStrings.get("notes", language)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(KhataStrings.get("btn_cancel", language))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val amt = amountStr.toDoubleOrNull()
                            if (amt == null || amt <= 0) {
                                error = true
                                return@Button
                            }
                            onSave(amt, description, paymentMode, notes)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isUdhar) UdharRed else JamaGreen
                        ),
                        modifier = Modifier.testTag("btn_save_tx")
                    ) {
                        Text(KhataStrings.get("btn_save", language), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun EditTransactionDialog(
    transaction: TransactionEntity,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onSave: (TransactionEntity) -> Unit
) {
    var amountStr by remember { mutableStateOf(transaction.amount.toString()) }
    var description by remember { mutableStateOf(transaction.description) }
    var paymentMode by remember { mutableStateOf(transaction.paymentMethod) }
    var notes by remember { mutableStateOf(transaction.notes) }
    val isUdhar = transaction.type.equals("UDHAR", true)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "વ્યવહારમાં ફેરફાર કરો (${if (isUdhar) "ઉધાર" else "જમા"})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = KhataTextPrimary
                )

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("રકમ (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("વિગત / વસ્તુ") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                if (!isUdhar) {
                    OutlinedTextField(
                        value = paymentMode,
                        onValueChange = { paymentMode = it },
                        label = { Text("ચુકવણી પ્રકાર (Cash / UPI / Bank)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("નોંધ") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(KhataStrings.get("btn_cancel", language))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val amt = amountStr.toDoubleOrNull() ?: transaction.amount
                            onSave(
                                transaction.copy(
                                    amount = amt,
                                    description = description,
                                    paymentMethod = paymentMode,
                                    notes = notes
                                )
                            )
                        }
                    ) {
                        Text(KhataStrings.get("btn_save", language))
                    }
                }
            }
        }
    }
}
