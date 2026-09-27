# 📱 Khata Book (ખાતા બુક / खाता बुक)

> A modern, simple, and professional Customer Udhar–Jama (Credit & Debit) Khata Book application tailored for small businesses, shops, and merchants in India.

---

## ✨ Features

- **📊 Smart Dashboard (₹ INR)**
  - Total Outstanding / Amount Receivable (`₹`)
  - Total Payments Received (Jama)
  - Daily & Monthly Udhar vs. Jama breakdown
  - Live count of Pending and Settled customers

- **👥 Customer Khata Management**
  - Add customers with contact numbers, addresses, and opening balances
  - Two prominent merchant action buttons:
    - 🔴 **UDHAR (ઉધાર આપ્યા / उधार दिया)**: Record credit sales with description, date, time, and notes
    - 🟢 **JAMA (જમા મળ્યા / जमा मिला)**: Record payments (Cash, UPI, Bank Transfer) with automatic advance credit detection
  - Complete chronological running balance recalculation with strict mathematical integrity

- **🌐 Trilingual Localization**
  - **Gujarati (ગુજરાતી)** - Default
  - **Hindi (हिन्दी)**
  - **English**
  - Instant language switcher directly from the app bar

- **💬 WhatsApp Payment Reminders**
  - Pre-filled polite payment reminders in Gujarati, Hindi, or English
  - Includes merchant's UPI ID for direct QR/UPI payments (Google Pay, PhonePe, Paytm)
  - One-tap WhatsApp launch (`wa.me` intent)

- **📄 Khata Statements & PDF Export**
  - Customer statement generator formatted like an invoice
  - Built-in Android `PrintManager` integration allowing direct printing or **"Save as PDF"**
  - Plain-text statement sharing for WhatsApp & SMS

- **📈 Business Reports & Insights**
  - Time period filtering (*Today*, *This Week*, *This Month*, *All Time*)
  - Payment mode split (Cash vs. UPI vs. Bank)
  - Highest pending customer leaderboard

- **🔒 Security & Backup**
  - 4-digit PIN lock screen protection
  - Full local persistence via **Room Database** (offline-first)
  - One-click JSON backup export & restore

---

## 🚀 Download APK

You can download the ready-to-install Android APK file:
- **Direct File:** `KhataBook.apk`
- **Architecture:** Android 7.0+ (API 24+)
- **Size:** ~22 MB

---

## 🛠️ Tech Stack & Architecture

- **Language:** Kotlin
- **UI Framework:** Jetpack Compose (Material Design 3)
- **Architecture:** Clean MVVM (Model-View-ViewModel) + StateFlow
- **Database:** Room (SQLite) with relational foreign keys and indices
- **Printing:** Android WebView PrintAdapter & ISO A4 PrintManager
