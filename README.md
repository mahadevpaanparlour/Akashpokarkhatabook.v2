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

- **🎨 Theme & Visual Customization**
  - **Light Mode, Dark Mode, or System Default**
  - **Color Themes**: Emerald Green, Royal Navy Indigo, Deep Purple, and Crimson Coral
  - Smooth dynamic Material 3 theming across all screens and dialogs

- **🏪 Owner / Business Profile Management**
  - Edit Shop/Business Name, Owner Name, Contact Phone Number, and Address
  - Edit Merchant UPI ID used for customer WhatsApp reminders and QR payments
  - Direct access from Home Dashboard header or Settings screen

- **🔒 Security & Backup**
  - 4-digit PIN lock screen protection
  - Full local persistence via **Room Database** (offline-first)
  - One-click JSON backup export & restore

---

## 🚀 Download & GitHub Publishing

### 1. Download APK Directly
The ready-to-install Android APK is compiled and placed in the project root:
- **File:** `KhataBook.apk` (and `app/build/outputs/apk/debug/app-debug.apk`)
- **Compatibility:** Android 7.0+ (Nougat, Oreo, Pie, 10, 11, 12, 13, 14, 15)

### 2. Publish APK to GitHub
To publish this project and its APK to your GitHub repository:
1. Export project or push via GitHub integration:
   - Click the **Export / Push to GitHub** option in the top right menu of AI Studio.
2. Create a GitHub Release:
   - On your GitHub repo page, navigate to **Releases** → **Draft a new release**.
   - Set tag version (e.g. `v1.0.0`), title it (e.g. `Khata Book v1.0.0`), and attach `KhataBook.apk`.
   - Click **Publish release**. Anyone can now download and install the APK on their Android phone!

---

## 🛠️ Tech Stack & Architecture

- **Language:** Kotlin
- **UI Framework:** Jetpack Compose (Material Design 3)
- **Architecture:** Clean MVVM (Model-View-ViewModel) + StateFlow
- **Database:** Room (SQLite) with relational foreign keys and indices
- **Printing:** Android WebView PrintAdapter & ISO A4 PrintManager
