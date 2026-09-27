package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.BusinessProfileEntity
import com.example.data.CustomerEntity
import com.example.data.TransactionEntity
import com.example.localization.AppLanguage
import com.example.localization.KhataStrings
import com.example.ui.theme.*
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Formats a double amount into Indian Rupee format (e.g. ₹1,25,500 or ₹8,500)
 */
fun formatInr(amount: Double, withSymbol: Boolean = true): String {
    val isNegative = amount < 0
    val absAmount = Math.abs(amount)
    
    val whole = absAmount.toLong()
    val fraction = ((absAmount - whole) * 100).toInt()

    val wholeStr = whole.toString()
    val formattedWhole = if (wholeStr.length <= 3) {
        wholeStr
    } else {
        val last3 = wholeStr.substring(wholeStr.length - 3)
        val rest = wholeStr.substring(0, wholeStr.length - 3)
        // Group remaining digits by 2s from right to left
        val sb = StringBuilder()
        var count = 0
        for (i in rest.length - 1 downTo 0) {
            sb.append(rest[i])
            count++
            if (count % 2 == 0 && i != 0) {
                sb.append(',')
            }
        }
        sb.reverse().toString() + "," + last3
    }

    val fractionStr = if (fraction > 0) String.format(".%02d", fraction) else ""
    val signStr = if (isNegative) "-" else ""
    val symbolStr = if (withSymbol) "₹" else ""
    
    return "$signStr$symbolStr$formattedWhole$fractionStr"
}

@Composable
fun StatCard(
    title: String,
    amount: String,
    icon: ImageVector,
    iconColor: Color,
    containerColor: Color,
    modifier: Modifier = Modifier,
    subText: String? = null,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = KhataTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(iconColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Text(
                text = amount,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = iconColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (!subText.isNullOrEmpty()) {
                Text(
                    text = subText,
                    style = MaterialTheme.typography.bodySmall,
                    color = KhataTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Large tactile action button for shopkeeper: 🟢 JAMA or 🔴 UDHAR
 */
@Composable
fun BigKhataActionButton(
    text: String,
    subTitle: String,
    icon: ImageVector,
    isUdhar: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    val bgColor = if (isUdhar) UdharRed else JamaGreen
    val rippleColor = if (isUdhar) UdharRedDark else JamaGreenDark

    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp)
            .testTag(testTag),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = bgColor,
            contentColor = Color.White
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 4.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Text(
                    text = subTitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
fun BalanceStatusChip(
    balance: Double,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val (label, bgColor, textColor) = when {
        balance > 0 -> Triple(
            KhataStrings.get("filter_pending", language),
            UdharRedBg,
            UdharRed
        )
        balance < 0 -> Triple(
            KhataStrings.get("filter_advance", language),
            AdvanceBlueBg,
            AdvanceBlue
        )
        else -> Triple(
            KhataStrings.get("filter_settled", language),
            JamaGreenBg,
            JamaGreen
        )
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Text(
            text = label,
            color = textColor,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun KhataConfirmDialog(
    show: Boolean,
    title: String,
    message: String,
    confirmText: String = "હા, ડીલીટ કરો",
    dismissText: String = "રદ કરો",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!show) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = KhataTextSecondary
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = UdharRed)
            ) {
                Text(confirmText, color = Color.White)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(dismissText)
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

object KhataShareHelper {

    fun generateReminderMessage(
        customer: CustomerEntity,
        business: BusinessProfileEntity?,
        language: AppLanguage
    ): String {
        val bizName = business?.businessName ?: "ખાતા બુક"
        val upiInfo = if (!business?.upiId.isNullOrBlank()) {
            "\n\nતમે Google Pay / PhonePe દ્વારા આ UPI પર પણ ચુકવી શકો છો:\nUPI ID: ${business?.upiId}"
        } else ""

        val upiInfoHi = if (!business?.upiId.isNullOrBlank()) {
            "\n\nआप Google Pay / PhonePe द्वारा इस UPI पर भी भुगतान कर सकते हैं:\nUPI ID: ${business?.upiId}"
        } else ""

        val upiInfoEn = if (!business?.upiId.isNullOrBlank()) {
            "\n\nYou can also pay via Google Pay / PhonePe using UPI:\nUPI ID: ${business?.upiId}"
        } else ""

        return when (language) {
            AppLanguage.GUJARATI -> """
નમસ્તે ${customer.name},
તમારા ખાતામાં ${formatInr(customer.currentBalance)} બાકી છે.
કૃપા કરીને અનુકૂળ સમયે ચુકવણી કરી આપશો.
$upiInfo

આભાર,
$bizName
${business?.phone ?: ""}
            """.trimIndent()

            AppLanguage.HINDI -> """
नमस्ते ${customer.name},
आपके खाते में ${formatInr(customer.currentBalance)} बकाया है।
कृपया समय पर भुगतान करने की कृपा करें।
$upiInfoHi

धन्यवाद,
$bizName
${business?.phone ?: ""}
            """.trimIndent()

            AppLanguage.ENGLISH -> """
Dear ${customer.name},
Friendly reminder that ${formatInr(customer.currentBalance)} is pending in your khata account with $bizName.
Please clear the dues at your earliest convenience.
$upiInfoEn

Thank you,
$bizName
${business?.phone ?: ""}
            """.trimIndent()
        }
    }

    fun openWhatsApp(context: Context, mobile: String, message: String) {
        val cleanMobile = mobile.replace("+", "").replace(" ", "").replace("-", "")
        val formattedMobile = if (cleanMobile.length == 10) "91$cleanMobile" else cleanMobile

        try {
            val uri = Uri.parse("https://api.whatsapp.com/send?phone=$formattedMobile&text=" + Uri.encode(message))
            val intent = Intent(Intent.ACTION_VIEW, uri)
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to standard share
            shareText(context, message, "WhatsApp Reminder")
        }
    }

    fun callCustomer(context: Context, mobile: String) {
        try {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$mobile"))
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open dialer", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareText(context: Context, text: String, title: String = "Share") {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
            }
            context.startActivity(Intent.createChooser(intent, title))
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to share", Toast.LENGTH_SHORT).show()
        }
    }

    fun printOrSavePdf(context: Context, htmlContent: String, jobName: String) {
        try {
            val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
            if (printManager == null) {
                Toast.makeText(context, "Print service not available", Toast.LENGTH_SHORT).show()
                return
            }

            val webView = WebView(context)
            webView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String) {
                    val printAdapter = webView.createPrintDocumentAdapter(jobName)
                    val attributes = PrintAttributes.Builder()
                        .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                        .setResolution(PrintAttributes.Resolution("pdf", "pdf", 300, 300))
                        .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                        .build()
                    printManager.print(jobName, printAdapter, attributes)
                }
            }
            webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to print: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }
}

/**
 * Reusable dialog for editing Shop / Merchant / App Owner details
 */
@Composable
fun BusinessProfileEditDialog(
    profile: BusinessProfileEntity?,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onSave: (BusinessProfileEntity) -> Unit
) {
    val context = LocalContext.current
    var businessName by remember { mutableStateOf(profile?.businessName ?: "") }
    var ownerName by remember { mutableStateOf(profile?.ownerName ?: "") }
    var phone by remember { mutableStateOf(profile?.phone ?: "") }
    var address by remember { mutableStateOf(profile?.address ?: "") }
    var gstNumber by remember { mutableStateOf(profile?.gstNumber ?: "") }
    var upiId by remember { mutableStateOf(profile?.upiId ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Storefront,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = KhataStrings.get("edit_owner_profile", language),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = KhataStrings.get("owner_profile_title", language),
                            style = MaterialTheme.typography.bodySmall,
                            color = KhataTextSecondary
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                OutlinedTextField(
                    value = businessName,
                    onValueChange = { businessName = it },
                    label = { Text(KhataStrings.get("business_name", language)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("business_name_input")
                )

                OutlinedTextField(
                    value = ownerName,
                    onValueChange = { ownerName = it },
                    label = { Text(KhataStrings.get("owner_name", language)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("owner_name_input")
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(KhataStrings.get("phone_number", language)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("owner_phone_input")
                )

                OutlinedTextField(
                    value = upiId,
                    onValueChange = { upiId = it },
                    label = { Text(KhataStrings.get("upi_id_label", language)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("owner_upi_input")
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text(KhataStrings.get("profile_address", language)) },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth().testTag("owner_address_input")
                )

                OutlinedTextField(
                    value = gstNumber,
                    onValueChange = { gstNumber = it },
                    label = { Text(KhataStrings.get("gst_number_label", language)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("owner_gst_input")
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
                            val updated = BusinessProfileEntity(
                                id = 1,
                                businessName = businessName.trim().ifEmpty { "મહાદેવ પાન પાર્લર & જનરલ સ્ટોર" },
                                ownerName = ownerName.trim().ifEmpty { "હરેશભાઈ પટેલ" },
                                phone = phone.trim(),
                                address = address.trim(),
                                gstNumber = gstNumber.trim(),
                                upiId = upiId.trim()
                            )
                            onSave(updated)
                            Toast.makeText(
                                context,
                                KhataStrings.get("profile_saved_toast", language),
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        modifier = Modifier.testTag("save_owner_profile_button")
                    ) {
                        Text(KhataStrings.get("btn_save", language))
                    }
                }
            }
        }
    }
}

