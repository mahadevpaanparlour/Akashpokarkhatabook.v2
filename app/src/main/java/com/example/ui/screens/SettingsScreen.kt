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
import com.example.ui.components.BusinessProfileEditDialog
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
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                                color = MaterialTheme.colorScheme.onSurface
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

            // Theme & Color Customization Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = KhataStrings.get("app_theme", language),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Light / Dark / System Mode
                        Text(
                            text = KhataStrings.get("theme_mode", language),
                            style = MaterialTheme.typography.labelMedium,
                            color = KhataTextSecondary
                        )

                        val currentThemeMode = settings?.themeMode ?: "LIGHT"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = currentThemeMode == "LIGHT",
                                onClick = { viewModel.setThemeMode("LIGHT") },
                                leadingIcon = { Icon(Icons.Default.LightMode, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                label = { Text(KhataStrings.get("theme_light", language)) },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = currentThemeMode == "DARK",
                                onClick = { viewModel.setThemeMode("DARK") },
                                leadingIcon = { Icon(Icons.Default.DarkMode, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                label = { Text(KhataStrings.get("theme_dark", language)) },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = currentThemeMode == "SYSTEM",
                                onClick = { viewModel.setThemeMode("SYSTEM") },
                                leadingIcon = { Icon(Icons.Default.SettingsBrightness, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                label = { Text(KhataStrings.get("theme_system", language)) },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Accent Color Palette
                        Text(
                            text = KhataStrings.get("theme_color", language),
                            style = MaterialTheme.typography.labelMedium,
                            color = KhataTextSecondary
                        )

                        val currentThemeColor = settings?.themeColor ?: "EMERALD"
                        val colorOptions = listOf(
                            Triple("EMERALD", KhataStrings.get("theme_color_emerald", language), KhataGreenPrimary),
                            Triple("NAVY", KhataStrings.get("theme_color_navy", language), KhataNavyPrimary),
                            Triple("MAROON", KhataStrings.get("theme_color_maroon", language), KhataMaroonPrimary),
                            Triple("PURPLE", KhataStrings.get("theme_color_purple", language), KhataPurplePrimary)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            colorOptions.take(2).forEach { (colorKey, colorLabel, colorVal) ->
                                val isSelected = currentThemeColor.equals(colorKey, ignoreCase = true)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.setThemeColor(colorKey) },
                                    leadingIcon = {
                                        Box(
                                            modifier = Modifier
                                                .size(14.dp)
                                                .clip(CircleShape)
                                                .background(colorVal)
                                        )
                                    },
                                    label = { Text(colorLabel, maxLines = 1) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            colorOptions.drop(2).forEach { (colorKey, colorLabel, colorVal) ->
                                val isSelected = currentThemeColor.equals(colorKey, ignoreCase = true)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.setThemeColor(colorKey) },
                                    leadingIcon = {
                                        Box(
                                            modifier = Modifier
                                                .size(14.dp)
                                                .clip(CircleShape)
                                                .background(colorVal)
                                        )
                                    },
                                    label = { Text(colorLabel, maxLines = 1) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // Shop Owner / Business Profile Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Storefront,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = KhataStrings.get("owner_profile_title", language),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "ખાતાવહી અને સ્ટેટમેન્ટ પર પ્રદર્શિત થશે",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = KhataTextSecondary
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = KhataCardBorder.copy(alpha = 0.6f))

                        // Details grid
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "દુકાન: ",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = KhataTextSecondary
                                )
                                Text(
                                    text = profile?.businessName ?: "મહાદેવ પાન પાર્લર & જનરલ સ્ટોર",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "માલિક: ",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = KhataTextSecondary
                                )
                                Text(
                                    text = profile?.ownerName ?: "હરેશભાઈ પટેલ",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            if (!profile?.phone.isNullOrBlank()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "ફોન / WhatsApp: ",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = KhataTextSecondary
                                    )
                                    Text(
                                        text = profile?.phone ?: "",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            if (!profile?.upiId.isNullOrBlank()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "તગાદા માટે UPI ID: ",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = AdvanceBlue
                                    )
                                    Text(
                                        text = profile?.upiId ?: "",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = AdvanceBlue
                                    )
                                }
                            }

                            if (!profile?.address.isNullOrBlank()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "સરનામું: ",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = KhataTextSecondary
                                    )
                                    Text(
                                        text = profile?.address ?: "",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = KhataTextMuted
                                    )
                                }
                            }

                            if (!profile?.gstNumber.isNullOrBlank()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "GSTIN: ",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = KhataTextSecondary
                                    )
                                    Text(
                                        text = profile?.gstNumber ?: "",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = KhataTextMuted
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = { showProfileDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("edit_owner_profile_button"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = KhataStrings.get("edit_owner_profile", language),
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }

            // Security PIN Lock
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                                        color = MaterialTheme.colorScheme.onSurface
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
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                                color = MaterialTheme.colorScheme.onSurface
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
        BusinessProfileEditDialog(
            profile = profile,
            language = language,
            onDismiss = { showProfileDialog = false },
            onSave = { updated ->
                viewModel.updateBusinessProfile(updated)
                showProfileDialog = false
            }
        )
    }

    // Set PIN Dialog
    if (showPinDialog) {
        var pin by remember { mutableStateOf("") }
        var confirmPin by remember { mutableStateOf("") }
        var pinError by remember { mutableStateOf(false) }

        Dialog(onDismissRequest = { showPinDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
