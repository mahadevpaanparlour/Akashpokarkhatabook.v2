package com.example.localization

enum class AppLanguage(val code: String, val displayName: String, val nativeName: String) {
    GUJARATI("gu", "Gujarati", "ગુજરાતી"),
    HINDI("hi", "Hindi", "हिन्दी"),
    ENGLISH("en", "English", "English")
}

object KhataStrings {
    fun get(key: String, lang: AppLanguage): String {
        return strings[key]?.get(lang) ?: strings[key]?.get(AppLanguage.GUJARATI) ?: key
    }

    private val strings = mapOf(
        // Navigation & Titles
        "app_title" to mapOf(
            AppLanguage.GUJARATI to "ખાતા બુક",
            AppLanguage.HINDI to "खाता बुक",
            AppLanguage.ENGLISH to "Khata Book"
        ),
        "nav_home" to mapOf(
            AppLanguage.GUJARATI to "હોમ",
            AppLanguage.HINDI to "होम",
            AppLanguage.ENGLISH to "Home"
        ),
        "nav_customers" to mapOf(
            AppLanguage.GUJARATI to "ગ્રાહકો",
            AppLanguage.HINDI to "ग्राहक",
            AppLanguage.ENGLISH to "Customers"
        ),
        "nav_transactions" to mapOf(
            AppLanguage.GUJARATI to "વ્યવહાર",
            AppLanguage.HINDI to "लेन-देन",
            AppLanguage.ENGLISH to "Transactions"
        ),
        "nav_reports" to mapOf(
            AppLanguage.GUJARATI to "રિપોર્ટ્સ",
            AppLanguage.HINDI to "रिपोर्ट्स",
            AppLanguage.ENGLISH to "Reports"
        ),
        "nav_settings" to mapOf(
            AppLanguage.GUJARATI to "સેટિંગ્સ",
            AppLanguage.HINDI to "सेटिंग्स",
            AppLanguage.ENGLISH to "Settings"
        ),

        // Dashboard Metrics
        "total_receivable" to mapOf(
            AppLanguage.GUJARATI to "કુલ ઉધાર (લેવાના)",
            AppLanguage.HINDI to "कुल उधार (लेना है)",
            AppLanguage.ENGLISH to "Total Udhar (Receivable)"
        ),
        "total_received" to mapOf(
            AppLanguage.GUJARATI to "કુલ જમા (આવેલા)",
            AppLanguage.HINDI to "कुल जमा (मिला)",
            AppLanguage.ENGLISH to "Total Jama (Received)"
        ),
        "net_outstanding" to mapOf(
            AppLanguage.GUJARATI to "નેટ બાકી રકમ",
            AppLanguage.HINDI to "कुल बकाया राशि",
            AppLanguage.ENGLISH to "Net Outstanding"
        ),
        "total_customers" to mapOf(
            AppLanguage.GUJARATI to "કુલ ગ્રાહકો",
            AppLanguage.HINDI to "कुल ग्राहक",
            AppLanguage.ENGLISH to "Total Customers"
        ),
        "pending_customers" to mapOf(
            AppLanguage.GUJARATI to "બાકી વાળા ગ્રાહકો",
            AppLanguage.HINDI to "बकाया वाले ग्राहक",
            AppLanguage.ENGLISH to "Pending Customers"
        ),
        "settled_customers" to mapOf(
            AppLanguage.GUJARATI to "ચૂકતે ગ્રાહકો",
            AppLanguage.HINDI to "चुक्ता ग्राहक",
            AppLanguage.ENGLISH to "Settled Customers"
        ),
        "today_udhar" to mapOf(
            AppLanguage.GUJARATI to "આજનું ઉધાર",
            AppLanguage.HINDI to "आज का उधार",
            AppLanguage.ENGLISH to "Today's Udhar"
        ),
        "today_jama" to mapOf(
            AppLanguage.GUJARATI to "આજની જમા",
            AppLanguage.HINDI to "आज का जमा",
            AppLanguage.ENGLISH to "Today's Jama"
        ),
        "today_activity" to mapOf(
            AppLanguage.GUJARATI to "આજના વ્યવહારો",
            AppLanguage.HINDI to "आज की गतिविधियाँ",
            AppLanguage.ENGLISH to "Today's Transactions"
        ),

        // Actions & Buttons
        "btn_add_customer" to mapOf(
            AppLanguage.GUJARATI to "+ નવો ગ્રાહક ઉમેરો",
            AppLanguage.HINDI to "+ नया ग्राहक जोड़ें",
            AppLanguage.ENGLISH to "+ Add Customer"
        ),
        "btn_udhar" to mapOf(
            AppLanguage.GUJARATI to "ઉધાર આપ્યા",
            AppLanguage.HINDI to "उधार दिया",
            AppLanguage.ENGLISH to "Gave Udhar"
        ),
        "btn_jama" to mapOf(
            AppLanguage.GUJARATI to "જમા આવ્યા",
            AppLanguage.HINDI to "जमा मिला",
            AppLanguage.ENGLISH to "Got Jama"
        ),
        "btn_save" to mapOf(
            AppLanguage.GUJARATI to "સાચવો",
            AppLanguage.HINDI to "सुरक्षित करें",
            AppLanguage.ENGLISH to "Save"
        ),
        "btn_cancel" to mapOf(
            AppLanguage.GUJARATI to "રદ કરો",
            AppLanguage.HINDI to "रद्द करें",
            AppLanguage.ENGLISH to "Cancel"
        ),
        "btn_delete" to mapOf(
            AppLanguage.GUJARATI to "ડીલીટ કરો",
            AppLanguage.HINDI to "हटाएं",
            AppLanguage.ENGLISH to "Delete"
        ),
        "btn_edit" to mapOf(
            AppLanguage.GUJARATI to "ફેરફાર કરો",
            AppLanguage.HINDI to "बदलें",
            AppLanguage.ENGLISH to "Edit"
        ),
        "btn_send_reminder" to mapOf(
            AppLanguage.GUJARATI to "તગાદો / રીમાઇન્ડર મોકલો",
            AppLanguage.HINDI to "तगादा / रिमाइंडर भेजें",
            AppLanguage.ENGLISH to "Send WhatsApp Reminder"
        ),
        "btn_view_statement" to mapOf(
            AppLanguage.GUJARATI to "ખાતાવહી સ્ટેટમેન્ટ જુઓ",
            AppLanguage.HINDI to "खाता स्टेटमेंट देखें",
            AppLanguage.ENGLISH to "View Khata Statement"
        ),
        "btn_call" to mapOf(
            AppLanguage.GUJARATI to "કોલ કરો",
            AppLanguage.HINDI to "कॉल करें",
            AppLanguage.ENGLISH to "Call"
        ),
        "btn_whatsapp" to mapOf(
            AppLanguage.GUJARATI to "વોટ્સએપ",
            AppLanguage.HINDI to "व्हाट्सएप",
            AppLanguage.ENGLISH to "WhatsApp"
        ),
        "btn_share" to mapOf(
            AppLanguage.GUJARATI to "શેર કરો",
            AppLanguage.HINDI to "साझा करें",
            AppLanguage.ENGLISH to "Share"
        ),
        "btn_print_pdf" to mapOf(
            AppLanguage.GUJARATI to "પ્રિન્ટ / PDF ડાઉનલોડ",
            AppLanguage.HINDI to "प्रिंट / PDF डाउनलोड",
            AppLanguage.ENGLISH to "Print / Download PDF"
        ),

        // Forms & Labels
        "customer_name" to mapOf(
            AppLanguage.GUJARATI to "ગ્રાહકનું નામ *",
            AppLanguage.HINDI to "ग्राहक का नाम *",
            AppLanguage.ENGLISH to "Customer Name *"
        ),
        "mobile_number" to mapOf(
            AppLanguage.GUJARATI to "મોબાઈલ નંબર",
            AppLanguage.HINDI to "मोबाइल नंबर",
            AppLanguage.ENGLISH to "Mobile Number"
        ),
        "address" to mapOf(
            AppLanguage.GUJARATI to "સરનામું",
            AppLanguage.HINDI to "पता",
            AppLanguage.ENGLISH to "Address"
        ),
        "opening_balance" to mapOf(
            AppLanguage.GUJARATI to "શરૂઆતનું બાકી (Opening Balance) ₹",
            AppLanguage.HINDI to "प्रारंभिक शेष (Opening Balance) ₹",
            AppLanguage.ENGLISH to "Opening Balance ₹"
        ),
        "amount" to mapOf(
            AppLanguage.GUJARATI to "રકમ (₹) *",
            AppLanguage.HINDI to "राशि (₹) *",
            AppLanguage.ENGLISH to "Amount (₹) *"
        ),
        "item_details" to mapOf(
            AppLanguage.GUJARATI to "વસ્તુ / વિગત (જેમ કે કરિયાણું, પાન, દૂધ)",
            AppLanguage.HINDI to "वस्तु / विवरण (जैसे किराना, पान, दूध)",
            AppLanguage.ENGLISH to "Item details / Description"
        ),
        "payment_mode" to mapOf(
            AppLanguage.GUJARATI to "ચુકવણી પ્રકાર (રોકડ, UPI, બેંક)",
            AppLanguage.HINDI to "भुगतान माध्यम (नकद, UPI, बैंक)",
            AppLanguage.ENGLISH to "Payment Method"
        ),
        "notes" to mapOf(
            AppLanguage.GUJARATI to "નોંધ (મરજિયાત)",
            AppLanguage.HINDI to "टिप्पणी (वैकल्पिक)",
            AppLanguage.ENGLISH to "Optional Note"
        ),
        "date" to mapOf(
            AppLanguage.GUJARATI to "તારીખ",
            AppLanguage.HINDI to "दिनांक",
            AppLanguage.ENGLISH to "Date"
        ),
        "time" to mapOf(
            AppLanguage.GUJARATI to "સમય",
            AppLanguage.HINDI to "समय",
            AppLanguage.ENGLISH to "Time"
        ),
        "balance_after" to mapOf(
            AppLanguage.GUJARATI to "વ્યવહાર પછી બાકી",
            AppLanguage.HINDI to "लेन-देन बाद शेष",
            AppLanguage.ENGLISH to "Balance After"
        ),

        // Filters
        "filter_all" to mapOf(
            AppLanguage.GUJARATI to "બધા",
            AppLanguage.HINDI to "सभी",
            AppLanguage.ENGLISH to "All"
        ),
        "filter_pending" to mapOf(
            AppLanguage.GUJARATI to "બાકી રકમ વાળા",
            AppLanguage.HINDI to "बकाया वाले",
            AppLanguage.ENGLISH to "Pending"
        ),
        "filter_settled" to mapOf(
            AppLanguage.GUJARATI to "ચૂકતે",
            AppLanguage.HINDI to "चुक्ता",
            AppLanguage.ENGLISH to "Settled"
        ),
        "filter_advance" to mapOf(
            AppLanguage.GUJARATI to "એડવાન્સ જમા",
            AppLanguage.HINDI to "एडवांस",
            AppLanguage.ENGLISH to "Advance"
        ),
        "filter_highest" to mapOf(
            AppLanguage.GUJARATI to "સૌથી વધુ બાકી",
            AppLanguage.HINDI to "अधिकतम बकाया",
            AppLanguage.ENGLISH to "Highest Due"
        ),
        "search_hint" to mapOf(
            AppLanguage.GUJARATI to "ગ્રાહકનું નામ અથવા મોબાઈલથી શોધો...",
            AppLanguage.HINDI to "ग्राहक का नाम या मोबाइल नंबर खोजें...",
            AppLanguage.ENGLISH to "Search by name or mobile..."
        ),

        // Status messages
        "no_customers" to mapOf(
            AppLanguage.GUJARATI to "હજુ સુધી કોઈ ગ્રાહક ઉમેરેલા નથી",
            AppLanguage.HINDI to "अभी तक कोई ग्राहक नहीं जोड़ा गया है",
            AppLanguage.ENGLISH to "No customers added yet"
        ),
        "no_transactions" to mapOf(
            AppLanguage.GUJARATI to "આ ખાતામાં કોઈ વ્યવહાર નથી",
            AppLanguage.HINDI to "इस खाते में कोई लेन-देन नहीं है",
            AppLanguage.ENGLISH to "No transactions in this khata"
        ),
        "delete_confirm_title" to mapOf(
            AppLanguage.GUJARATI to "ડીલીટ કરવા માટે પુષ્ટિ કરો",
            AppLanguage.HINDI to "हटाने की पुष्टि करें",
            AppLanguage.ENGLISH to "Confirm Delete"
        ),
        "delete_customer_msg" to mapOf(
            AppLanguage.GUJARATI to "શું તમે ખરેખર આ ગ્રાહક અને તેમના તમામ ખાતાવહી વ્યવહારો ડિલીટ કરવા માંગો છો?",
            AppLanguage.HINDI to "क्या आप वाकई इस ग्राहक और उनके सभी लेन-देन को हटाना चाहते हैं?",
            AppLanguage.ENGLISH to "Are you sure you want to delete this customer and all their transactions?"
        ),
        "delete_tx_msg" to mapOf(
            AppLanguage.GUJARATI to "આ વ્યવહાર ડિલીટ કરવાથી ગ્રાહકનું બાકી આપોઆપ ફરીથી ગણાશે.",
            AppLanguage.HINDI to "इस लेन-देन को हटाने पर ग्राहक का बकाया स्वतः पुनः गणना हो जाएगा।",
            AppLanguage.ENGLISH to "Deleting this transaction will automatically recalculate the customer's balance."
        ),
        "advance_warning" to mapOf(
            AppLanguage.GUJARATI to "ધ્યાન આપો: જમા રકમ બાકી રકમ કરતાં વધુ છે. ગ્રાહકના ખાતામાં એડવાન્સ જમા રહેશે.",
            AppLanguage.HINDI to "ध्यान दें: जमा राशि बकाया से अधिक है। ग्राहक के खाते में एडवांस रहेगा।",
            AppLanguage.ENGLISH to "Notice: Jama amount is greater than outstanding balance. Customer will have advance credit."
        ),

        // Business Profile & Settings
        "business_profile" to mapOf(
            AppLanguage.GUJARATI to "દુકાન / બિઝનેસ પ્રોફાઇલ",
            AppLanguage.HINDI to "दुकान / व्यापार प्रोफ़ाइल",
            AppLanguage.ENGLISH to "Business Profile"
        ),
        "business_name" to mapOf(
            AppLanguage.GUJARATI to "દુકાન / વેપારનું નામ",
            AppLanguage.HINDI to "दुकान / व्यापार का नाम",
            AppLanguage.ENGLISH to "Business Name"
        ),
        "owner_name" to mapOf(
            AppLanguage.GUJARATI to "માલિકનું નામ",
            AppLanguage.HINDI to "मालिक का नाम",
            AppLanguage.ENGLISH to "Owner Name"
        ),
        "gst_number" to mapOf(
            AppLanguage.GUJARATI to "GST નંબર (મરજિયાત)",
            AppLanguage.HINDI to "GST नंबर (वैकल्पिक)",
            AppLanguage.ENGLISH to "GST Number (Optional)"
        ),
        "upi_id" to mapOf(
            AppLanguage.GUJARATI to "UPI ID (GooglePay / PhonePe)",
            AppLanguage.HINDI to "UPI ID (भुगतान प्राप्त करने हेतु)",
            AppLanguage.ENGLISH to "UPI ID (For Payment Reminders)"
        ),
        "app_language" to mapOf(
            AppLanguage.GUJARATI to "ભાષા પસંદ કરો (Language)",
            AppLanguage.HINDI to "भाषा चुनें (Language)",
            AppLanguage.ENGLISH to "App Language"
        ),
        "security_pin" to mapOf(
            AppLanguage.GUJARATI to "એપ સુરક્ષા PIN",
            AppLanguage.HINDI to "ऐप सुरक्षा पिन",
            AppLanguage.ENGLISH to "App Security PIN"
        ),
        "pin_enabled" to mapOf(
            AppLanguage.GUJARATI to "PIN લોક સક્રિય કરો",
            AppLanguage.HINDI to "पिन लॉक चालू करें",
            AppLanguage.ENGLISH to "Enable PIN Lock"
        ),
        "enter_pin" to mapOf(
            AppLanguage.GUJARATI to "4-અંકનો સુરક્ષા PIN દાખલ કરો",
            AppLanguage.HINDI to "4-अंकों का सुरक्षा पिन दर्ज करें",
            AppLanguage.ENGLISH to "Enter 4-Digit Security PIN"
        ),
        "backup_restore" to mapOf(
            AppLanguage.GUJARATI to "ડેટા બેકઅપ અને રિસ્ટોર",
            AppLanguage.HINDI to "डेटा बैकअप एवं रीस्टोर",
            AppLanguage.ENGLISH to "Backup & Restore"
        ),
        "export_backup" to mapOf(
            AppLanguage.GUJARATI to "ડેટા બેકઅપ ફાઇલ શેર / સેવ કરો",
            AppLanguage.HINDI to "डेटा बैकअप फ़ाइल शेयर / सेव करें",
            AppLanguage.ENGLISH to "Export Backup File (JSON)"
        ),
        "restore_data" to mapOf(
            AppLanguage.GUJARATI to "બેકઅપમાંથી ડેટા રિસ્ટોર કરો",
            AppLanguage.HINDI to "बैकअप से डेटा रीस्टोर करें",
            AppLanguage.ENGLISH to "Restore Data from Backup"
        ),
        "seed_sample_data" to mapOf(
            AppLanguage.GUJARATI to "ડેમો / નમૂના ગ્રાહકો ઉમેરો",
            AppLanguage.HINDI to "डेमो / नमूना ग्राहक जोड़ें",
            AppLanguage.ENGLISH to "Load Sample Demo Data"
        ),
        "clear_all_data" to mapOf(
            AppLanguage.GUJARATI to "બધો ડેટા સાફ કરો (Reset)",
            AppLanguage.HINDI to "सभी डेटा हटाएं (Reset)",
            AppLanguage.ENGLISH to "Clear All Data"
        )
    )
}
