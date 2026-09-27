package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.KhataViewModel
import com.example.data.BusinessProfileEntity
import com.example.localization.AppLanguage
import com.example.localization.KhataStrings
import com.example.ui.components.KhataConfirmDialog
import com.example.ui.components.KhataShareHelper
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: KhataViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val language by viewModel.language.collectAsStateWithLifecycle()
    val profile by viewModel.businessProfile.collectAsStateWithLifecycle()
    val settings by viewModel.appSettings.collectAsStateWithLifecycle()

    var showProfileDialog by remember { mutableStateOf(false) }
    var showPinDialog by remember { mutableStateOf(false) }
    var showClearConfirm by remember { mutableStateOf(false) }
    var showRestoreDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = KhataStrings.get("nav_settings", language),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
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
            // Language Selection Card
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Translate, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = KhataStrings.get("app_language", language),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = KhataTextPrimary
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AppLanguage.values().forEach { lang ->
                                val isSelected = language == lang
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.setLanguage(lang) },
                                    label = {
                                        Text(
                                            text = lang.nativeName,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // Business Profile Card
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Storefront, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = KhataStrings.get("business_profile", language),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = KhataTextPrimary
                                )
                            }
                            TextButton(onClick = { showProfileDialog = true }) {
                                Text(KhataStrings.get("btn_edit", language))
                            }
                        }

                        Text(
                            text = profile?.businessName ?: "દુકાનનું નામ સેટ કરો",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = KhataTextPrimary
                        )

                        if (!profile?.ownerName.isNullOrBlank()) {
                            Text(text = "વેપારી: ${profile?.ownerName}", style = MaterialTheme.typography.bodySmall, color = KhataTextSecondary)
                        }
                        if (!profile?.phone.isNullOrBlank()) {
                            Text(text = "મોબાઈલ: ${profile?.phone}", style = MaterialTheme.typography.bodySmall, color = KhataTextSecondary)
                        }
                        if (!profile?.upiId.isNullOrBlank()) {
                            Text(text = "UPI ID: ${profile?.upiId}", style = MaterialTheme.typography.bodySmall, color = AdvanceBlue)
                        }
                        if (!profile?.gstNumber.isNullOrBlank()) {
                            Text(text = "GST: ${profile?.gstNumber}", style = MaterialTheme.typography.bodySmall, color = KhataTextMuted)
                        }
                    }
                }
            }

            // Security PIN Lock
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = KhataStrings.get("security_pin", language),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = KhataTextPrimary
                                    )
                                    Text(
                                        text = if (settings?.isPinEnabled == true) "PIN લોક ચાલુ છે" else "PIN લોક બંધ છે",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = KhataTextSecondary
                                    )
                                }
                            }

                            Switch(
                                checked = settings?.isPinEnabled == true,
                                onCheckedChange = { checked ->
                                    if (checked) {
                                        showPinDialog = true
                                    } else {
                                        viewModel.setPin("", false)
                                        Toast.makeText(context, "PIN લોક નિષ્ક્રિય કર્યો", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // Backup & Data Export Card
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Backup, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = KhataStrings.get("backup_restore", language),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = KhataTextPrimary
                            )
                        }

                        // Export Backup
                        OutlinedButton(
                            onClick = {
                                coroutineScope.launch {
                                    val json = viewModel.exportBackupJson()
                                    KhataShareHelper.shareText(context, json, "Khata Book Backup JSON")
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(KhataStrings.get("export_backup", language))
                        }

                        // Restore Data
                        OutlinedButton(
                            onClick = { showRestoreDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(KhataStrings.get("restore_data", language))
                        }

                        // Seed Demo Sample Data
                        OutlinedButton(
                            onClick = {
                                viewModel.seedSampleData()
                                Toast.makeText(context, "ડેમો ગ્રાહકો ઉમેરાઈ ગયા!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.AutoMode, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(KhataStrings.get("seed_sample_data", language))
                        }

                        // Clear All Data
                        Button(
                            onClick = { showClearConfirm = true },
                            colors = ButtonDefaults.buttonColors(containerColor = UdharRedBg),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.DeleteForever, contentDescription = null, tint = UdharRed, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(KhataStrings.get("clear_all_data", language), color = UdharRed, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Business Profile Edit Dialog
    if (showProfileDialog) {
        var bName by remember { mutableStateOf(profile?.businessName ?: "") }
        var oName by remember { mutableStateOf(profile?.ownerName ?: "") }
        var phone by remember { mutableStateOf(profile?.phone ?: "") }
        var address by remember { mutableStateOf(profile?.address ?: "") }
        var gst by remember { mutableStateOf(profile?.gstNumber ?: "") }
        var upi by remember { mutableStateOf(profile?.upiId ?: "") }

        Dialog(onDismissRequest = { showProfileDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "બિઝનેસ પ્રોફાઇલ સંપાદન",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = KhataTextPrimary
                    )

                    OutlinedTextField(
                        value = bName,
                        onValueChange = { bName = it },
                        label = { Text("દુકાન / પેઢીનું નામ") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = oName,
                        onValueChange = { oName = it },
                        label = { Text("માલિકનું નામ") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("મોબાઈલ નંબર") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = upi,
                        onValueChange = { upi = it },
                        label = { Text("UPI ID (GooglePay / PhonePe)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("સરનામું") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = gst,
                        onValueChange = { gst = it },
                        label = { Text("GST નંબર (મરજિયાત)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showProfileDialog = false }) {
                            Text(KhataStrings.get("btn_cancel", language))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                viewModel.updateBusinessProfile(
                                    BusinessProfileEntity(
                                        id = 1,
                                        businessName = bName.trim(),
                                        ownerName = oName.trim(),
                                        phone = phone.trim(),
                                        address = address.trim(),
                                        gstNumber = gst.trim(),
                                        upiId = upi.trim()
                                    )
                                )
                                showProfileDialog = false
                                Toast.makeText(context, "પ્રોફાઇલ સાચવવામાં આવી!", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Text(KhataStrings.get("btn_save", language))
                        }
                    }
                }
            }
        }
    }

    // Set PIN Dialog
    if (showPinDialog) {
        var pin by remember { mutableStateOf("") }
        var confirmPin by remember { mutableStateOf("") }
        var pinError by remember { mutableStateOf(false) }

        Dialog(onDismissRequest = { showPinDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "4-અંકનો સુરક્ષા PIN સેટ કરો",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    OutlinedTextField(
                        value = pin,
                        onValueChange = { if (it.length <= 4) pin = it },
                        label = { Text("નવો PIN (4 અંકો)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = confirmPin,
                        onValueChange = { if (it.length <= 4) confirmPin = it },
                        label = { Text("PIN ફરીથી દાખલ કરો") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        isError = pinError,
                        supportingText = {
                            if (pinError) Text("બંને PIN સરખા હોવા જોઈએ અને 4 અંકના હોવા જોઈએ", color = UdharRed)
                        }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showPinDialog = false }) {
                            Text(KhataStrings.get("btn_cancel", language))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (pin.length == 4 && pin == confirmPin) {
                                    viewModel.setPin(pin, true)
                                    showPinDialog = false
                                    Toast.makeText(context, "સુરક્ષા PIN સેટ કર્યો!", Toast.LENGTH_SHORT).show()
                                } else {
                                    pinError = true
                                }
                            }
                        ) {
                            Text("સક્રિય કરો")
                        }
                    }
                }
            }
        }
    }

    // Restore Dialog (paste JSON)
    if (showRestoreDialog) {
        var jsonInput by remember { mutableStateOf("") }
        var restoreError by remember { mutableStateOf(false) }

        Dialog(onDismissRequest = { showRestoreDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "બેકઅપ ડેટા રિસ્ટોર કરો",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    Text(
                        text = "તમારો સેવ કરેલો JSON બેકઅપ કોડ અહીં પેસ્ટ કરો:",
                        style = MaterialTheme.typography.bodySmall,
                        color = KhataTextSecondary
                    )

                    OutlinedTextField(
                        value = jsonInput,
                        onValueChange = { jsonInput = it; restoreError = false },
                        label = { Text("JSON બેકઅપ ટેક્સ્ટ") },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp, max = 200.dp),
                        isError = restoreError,
                        supportingText = {
                            if (restoreError) Text("અમાન્ય બેકઅપ ફોર્મેટ", color = UdharRed)
                        }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showRestoreDialog = false }) {
                            Text(KhataStrings.get("btn_cancel", language))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    val success = viewModel.restoreBackupJson(jsonInput)
                                    if (success) {
                                        showRestoreDialog = false
                                        Toast.makeText(context, "ડેટા સફળતાપૂર્વક રિસ્ટોર થયો!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        restoreError = true
                                    }
                                }
                            }
                        ) {
                            Text("રિસ્ટોર કરો")
                        }
                    }
                }
            }
        }
    }

    // Clear All Confirmation
    KhataConfirmDialog(
        show = showClearConfirm,
        title = "બધો ડેટા સાફ કરો?",
        message = "ચેતવણી: આનાથી તમામ ગ્રાહકો અને તેમના તમામ ઉધાર-જમા વ્યવહારો કાયમ માટે ડિલીટ થઈ જશે!",
        confirmText = "હા, બધું ડિલીટ કરો",
        onConfirm = {
            viewModel.clearAllData()
            showClearConfirm = false
            Toast.makeText(context, "બધો ડેટા સાફ કર્યો", Toast.LENGTH_SHORT).show()
        },
        onDismiss = { showClearConfirm = false }
    )
}
