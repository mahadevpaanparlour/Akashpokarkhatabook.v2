package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.KhataViewModel
import com.example.data.CustomerEntity
import com.example.localization.KhataStrings
import com.example.ui.components.BalanceStatusChip
import com.example.ui.components.KhataShareHelper
import com.example.ui.components.formatInr
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomersScreen(
    viewModel: KhataViewModel,
    onNavigateToCustomerKhata: (Long) -> Unit,
    showAddDialogInitially: Boolean = false,
    onResetAddDialogFlag: () -> Unit = {}
) {
    val context = LocalContext.current
    val language by viewModel.language.collectAsStateWithLifecycle()
    val allCustomers by viewModel.customers.collectAsStateWithLifecycle()
    val profile by viewModel.businessProfile.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") } // ALL, PENDING, SETTLED, ADVANCE, HIGHEST
    var showAddDialog by remember { mutableStateOf(showAddDialogInitially) }

    LaunchedEffect(showAddDialogInitially) {
        if (showAddDialogInitially) {
            showAddDialog = true
            onResetAddDialogFlag()
        }
    }

    val filteredCustomers = remember(allCustomers, searchQuery, selectedFilter) {
        var list = allCustomers

        if (searchQuery.isNotBlank()) {
            val q = searchQuery.trim().lowercase()
            list = list.filter {
                it.name.lowercase().contains(q) || it.mobile.contains(q) || it.address.lowercase().contains(q)
            }
        }

        when (selectedFilter) {
            "PENDING" -> list.filter { it.currentBalance > 0 }
            "SETTLED" -> list.filter { it.currentBalance == 0.0 }
            "ADVANCE" -> list.filter { it.currentBalance < 0 }
            "HIGHEST" -> list.sortedByDescending { it.currentBalance }
            else -> list
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = KhataStrings.get("nav_customers", language),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("fab_add_customer")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = "Add Customer")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = KhataStrings.get("btn_add_customer", language),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(KhataBg)
        ) {
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("customer_search_input"),
                placeholder = {
                    Text(
                        text = KhataStrings.get("search_hint", language),
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = KhataTextSecondary)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            // Filter Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == "ALL",
                    onClick = { selectedFilter = "ALL" },
                    label = { Text("${KhataStrings.get("filter_all", language)} (${allCustomers.size})") }
                )
                FilterChip(
                    selected = selectedFilter == "PENDING",
                    onClick = { selectedFilter = "PENDING" },
                    label = {
                        val count = allCustomers.count { it.currentBalance > 0 }
                        Text("${KhataStrings.get("filter_pending", language)} ($count)")
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = UdharRedBg,
                        selectedLabelColor = UdharRed
                    )
                )
                FilterChip(
                    selected = selectedFilter == "SETTLED",
                    onClick = { selectedFilter = "SETTLED" },
                    label = {
                        val count = allCustomers.count { it.currentBalance == 0.0 }
                        Text("${KhataStrings.get("filter_settled", language)} ($count)")
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = JamaGreenBg,
                        selectedLabelColor = JamaGreen
                    )
                )
                FilterChip(
                    selected = selectedFilter == "HIGHEST",
                    onClick = { selectedFilter = "HIGHEST" },
                    label = { Text(KhataStrings.get("filter_highest", language)) }
                )
                FilterChip(
                    selected = selectedFilter == "ADVANCE",
                    onClick = { selectedFilter = "ADVANCE" },
                    label = {
                        val count = allCustomers.count { it.currentBalance < 0 }
                        Text("${KhataStrings.get("filter_advance", language)} ($count)")
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AdvanceBlueBg,
                        selectedLabelColor = AdvanceBlue
                    )
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Customer List
            if (filteredCustomers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.PersonSearch,
                            contentDescription = null,
                            tint = KhataTextMuted,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = KhataStrings.get("no_customers", language),
                            style = MaterialTheme.typography.titleMedium,
                            color = KhataTextSecondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredCustomers, key = { it.id }) { customer ->
                        CustomerListItem(
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
            }
        }
    }

    if (showAddDialog) {
        AddEditCustomerDialog(
            customerToEdit = null,
            language = language,
            onDismiss = { showAddDialog = false },
            onSave = { name, mobile, address, notes, openingBalance ->
                viewModel.addCustomer(name, mobile, address, notes, openingBalance) { newId ->
                    showAddDialog = false
                    onNavigateToCustomerKhata(newId)
                }
            }
        )
    }
}

@Composable
fun CustomerListItem(
    customer: CustomerEntity,
    language: com.example.localization.AppLanguage,
    onClick: () -> Unit,
    onCall: () -> Unit,
    onWhatsApp: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("customer_item_${customer.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(
                            if (customer.currentBalance > 0) UdharRedBg
                            else if (customer.currentBalance < 0) AdvanceBlueBg
                            else JamaGreenBg
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = customer.name.firstOrNull()?.toString() ?: "C",
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
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (customer.mobile.isNotBlank()) {
                        Text(
                            text = customer.mobile,
                            style = MaterialTheme.typography.bodySmall,
                            color = KhataTextSecondary
                        )
                    }
                    if (customer.address.isNotBlank()) {
                        Text(
                            text = customer.address,
                            style = MaterialTheme.typography.bodySmall,
                            color = KhataTextMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
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

            // Bottom action icons for easy access
            if (customer.mobile.isNotBlank()) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 10.dp),
                    color = KhataCardBorder.copy(alpha = 0.6f)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilledTonalButton(
                            onClick = onCall,
                            modifier = Modifier.height(34.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(containerColor = AdvanceBlueBg)
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = AdvanceBlue, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(KhataStrings.get("btn_call", language), style = MaterialTheme.typography.labelSmall, color = AdvanceBlue)
                        }

                        if (customer.currentBalance > 0) {
                            FilledTonalButton(
                                onClick = onWhatsApp,
                                modifier = Modifier.height(34.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(containerColor = JamaGreenBg)
                            ) {
                                Icon(Icons.Default.Chat, contentDescription = null, tint = JamaGreen, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(KhataStrings.get("btn_whatsapp", language), style = MaterialTheme.typography.labelSmall, color = JamaGreen)
                            }
                        }
                    }

                    Text(
                        text = "ખાતાવહી →",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun AddEditCustomerDialog(
    customerToEdit: CustomerEntity?,
    language: com.example.localization.AppLanguage,
    onDismiss: () -> Unit,
    onSave: (name: String, mobile: String, address: String, notes: String, openingBalance: Double) -> Unit
) {
    var name by remember { mutableStateOf(customerToEdit?.name ?: "") }
    var mobile by remember { mutableStateOf(customerToEdit?.mobile ?: "") }
    var address by remember { mutableStateOf(customerToEdit?.address ?: "") }
    var notes by remember { mutableStateOf(customerToEdit?.notes ?: "") }
    var openingBalanceStr by remember {
        mutableStateOf(if (customerToEdit != null) customerToEdit.openingBalance.toString() else "")
    }
    var nameError by remember { mutableStateOf(false) }

    val inputColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
        focusedLabelColor = MaterialTheme.colorScheme.primary,
        unfocusedLabelColor = MaterialTheme.colorScheme.onSurface,
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
        cursorColor = MaterialTheme.colorScheme.primary,
        focusedContainerColor = MaterialTheme.colorScheme.surface,
        unfocusedContainerColor = MaterialTheme.colorScheme.surface
    )

    val inputTextStyle = MaterialTheme.typography.bodyLarge.copy(
        color = MaterialTheme.colorScheme.onSurface,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(22.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = if (customerToEdit == null) KhataStrings.get("btn_add_customer", language)
                    else KhataStrings.get("btn_edit", language),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (it.isNotBlank()) nameError = false
                    },
                    label = {
                        Text(
                            KhataStrings.get("customer_name", language),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    },
                    isError = nameError,
                    supportingText = {
                        if (nameError) Text("નામ દાખલ કરવું જરૂરી છે", color = UdharRed, fontWeight = FontWeight.Bold)
                    },
                    textStyle = inputTextStyle,
                    colors = inputColors,
                    modifier = Modifier.fillMaxWidth().testTag("input_customer_name"),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = mobile,
                    onValueChange = { mobile = it },
                    label = {
                        Text(
                            KhataStrings.get("mobile_number", language),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    textStyle = inputTextStyle,
                    colors = inputColors,
                    modifier = Modifier.fillMaxWidth().testTag("input_customer_mobile"),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = {
                        Text(
                            KhataStrings.get("address", language),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    },
                    textStyle = inputTextStyle,
                    colors = inputColors,
                    modifier = Modifier.fillMaxWidth().testTag("input_customer_address"),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                if (customerToEdit == null) {
                    OutlinedTextField(
                        value = openingBalanceStr,
                        onValueChange = { openingBalanceStr = it },
                        label = {
                            Text(
                                KhataStrings.get("opening_balance", language),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        textStyle = inputTextStyle,
                        colors = inputColors,
                        modifier = Modifier.fillMaxWidth().testTag("input_customer_opening_balance"),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = {
                        Text(
                            KhataStrings.get("notes", language),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    },
                    textStyle = inputTextStyle,
                    colors = inputColors,
                    modifier = Modifier.fillMaxWidth().testTag("input_customer_notes"),
                    singleLine = false,
                    maxLines = 2,
                    shape = RoundedCornerShape(10.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            KhataStrings.get("btn_cancel", language),
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isBlank()) {
                                nameError = true
                                return@Button
                            }
                            val ob = openingBalanceStr.toDoubleOrNull() ?: 0.0
                            onSave(name, mobile, address, notes, ob)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.testTag("btn_save_customer")
                    ) {
                        Text(KhataStrings.get("btn_save", language), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
