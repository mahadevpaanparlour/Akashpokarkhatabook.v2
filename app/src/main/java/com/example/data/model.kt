package com.example.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val mobile: String = "",
    val address: String = "",
    val notes: String = "",
    val openingBalance: Double = 0.0,
    val currentBalance: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = CustomerEntity::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("customerId"),
        Index("dateMillis")
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val customerId: Long,
    val type: String, // "UDHAR" or "JAMA"
    val amount: Double,
    val dateMillis: Long = System.currentTimeMillis(),
    val dateString: String = "",
    val timeString: String = "",
    val description: String = "",
    val paymentMethod: String = "Cash", // "Cash", "UPI", "Bank", "Other"
    val notes: String = "",
    val balanceAfter: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "business_profile")
data class BusinessProfileEntity(
    @PrimaryKey
    val id: Int = 1,
    val businessName: String = "મહાદેવ પાન પાર્લર & જનરલ સ્ટોર",
    val ownerName: String = "હરેશભાઈ પટેલ",
    val phone: String = "9876543210",
    val address: String = "સ્ટેશન રોડ, બજાર, ગુજરાત",
    val gstNumber: String = "",
    val upiId: String = "mahadevstore@upi"
)

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey
    val id: Int = 1,
    val language: String = "gu", // "gu", "hi", "en"
    val isPinEnabled: Boolean = false,
    val pinCode: String = ""
)
