package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.KhataViewModel
import com.example.ui.theme.KhataGreenPrimary
import com.example.ui.theme.KhataTextPrimary
import com.example.ui.theme.UdharRed

@Composable
fun PinLockScreen(
    viewModel: KhataViewModel,
    onSuccess: () -> Unit
) {
    var enteredPin by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(KhataGreenPrimary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = KhataGreenPrimary,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "ખાતા બુક લોક છે",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = KhataTextPrimary
        )

        Text(
            text = "ચાલુ રાખવા 4-અંકનો PIN દાખલ કરો",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(24.dp))

        // PIN Dot indicators
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            for (i in 0 until 4) {
                val filled = i < enteredPin.length
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(
                            if (showError) UdharRed
                            else if (filled) KhataGreenPrimary
                            else Color.LightGray.copy(alpha = 0.5f)
                        )
                )
            }
        }

        if (showError) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "ખોટો PIN! કૃપા કરીને ફરી પ્રયાસ કરો.",
                color = UdharRed,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
            )
        }

        Spacer(modifier = Modifier.height(36.dp))

        // Keypad grid (1-9, C, 0, Backspace)
        val keys = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("C", "0", "DEL")
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            for (row in keys) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (key in row) {
                        Button(
                            onClick = {
                                showError = false
                                when (key) {
                                    "C" -> enteredPin = ""
                                    "DEL" -> if (enteredPin.isNotEmpty()) enteredPin = enteredPin.dropLast(1)
                                    else -> {
                                        if (enteredPin.length < 4) {
                                            val newPin = enteredPin + key
                                            enteredPin = newPin
                                            if (newPin.length == 4) {
                                                if (viewModel.unlockWithPin(newPin)) {
                                                    onSuccess()
                                                } else {
                                                    showError = true
                                                    enteredPin = ""
                                                }
                                            }
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.size(68.dp),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (key == "C" || key == "DEL") Color(0xFFF1F5F9) else Color(0xFFF8FAFC),
                                contentColor = KhataTextPrimary
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp)
                        ) {
                            if (key == "DEL") {
                                Icon(Icons.Default.Backspace, contentDescription = "Delete", tint = Color.Gray)
                            } else {
                                Text(
                                    text = key,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = KhataTextPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
