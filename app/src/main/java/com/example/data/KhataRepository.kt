package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class KhataRepository(private val db: KhataDatabase) {
    private val customerDao = db.customerDao()
    private val transactionDao = db.transactionDao()
    private val businessProfileDao = db.businessProfileDao()
    private val appSettingsDao = db.appSettingsDao()

    val allCustomers: Flow<List<CustomerEntity>> = customerDao.getAllCustomers()
    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
    val businessProfile: Flow<BusinessProfileEntity?> = businessProfileDao.getProfile()
    val appSettings: Flow<AppSettingsEntity?> = appSettingsDao.getSettings()

    fun getCustomer(id: Long): Flow<CustomerEntity?> = customerDao.getCustomerById(id)
    fun getCustomerTransactions(customerId: Long): Flow<List<TransactionEntity>> =
        transactionDao.getTransactionsForCustomer(customerId)

    suspend fun addCustomer(
        name: String,
        mobile: String,
        address: String,
        notes: String,
        openingBalance: Double
    ): Long = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val customer = CustomerEntity(
            name = name.trim(),
            mobile = mobile.trim(),
            address = address.trim(),
            notes = notes.trim(),
            openingBalance = openingBalance,
            currentBalance = openingBalance,
            createdAt = now,
            updatedAt = now
        )
        customerDao.insertCustomer(customer)
    }

    suspend fun updateCustomer(customer: CustomerEntity) = withContext(Dispatchers.IO) {
        val updated = customer.copy(updatedAt = System.currentTimeMillis())
        customerDao.updateCustomer(updated)
        recalculateCustomerBalance(customer.id)
    }

    suspend fun deleteCustomer(customerId: Long) = withContext(Dispatchers.IO) {
        customerDao.deleteCustomerById(customerId)
    }

    suspend fun addTransaction(
        customerId: Long,
        type: String, // "UDHAR" or "JAMA"
        amount: Double,
        dateMillis: Long,
        dateString: String,
        timeString: String,
        description: String,
        paymentMethod: String,
        notes: String
    ): Long = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val tx = TransactionEntity(
            customerId = customerId,
            type = type,
            amount = amount,
            dateMillis = dateMillis,
            dateString = dateString,
            timeString = timeString,
            description = description.trim(),
            paymentMethod = paymentMethod,
            notes = notes.trim(),
            balanceAfter = 0.0,
            createdAt = now,
            updatedAt = now
        )
        val id = transactionDao.insertTransaction(tx)
        recalculateCustomerBalance(customerId)
        id
    }

    suspend fun updateTransaction(transaction: TransactionEntity) = withContext(Dispatchers.IO) {
        val updated = transaction.copy(updatedAt = System.currentTimeMillis())
        transactionDao.updateTransaction(updated)
        recalculateCustomerBalance(transaction.customerId)
    }

    suspend fun deleteTransaction(transactionId: Long, customerId: Long) = withContext(Dispatchers.IO) {
        transactionDao.deleteTransactionById(transactionId)
        recalculateCustomerBalance(customerId)
    }

    /**
     * Mathematical integrity recalculation:
     * Current Balance = Opening Balance + Total Udhar - Total Jama
     * Running balance for each transaction is computed chronologically.
     */
    suspend fun recalculateCustomerBalance(customerId: Long) = withContext(Dispatchers.IO) {
        val customer = customerDao.getCustomerByIdOnce(customerId) ?: return@withContext
        val transactions = transactionDao.getTransactionsForCustomerOnce(customerId)

        var runningBalance = customer.openingBalance

        for (tx in transactions) {
            if (tx.type.equals("UDHAR", ignoreCase = true)) {
                runningBalance += tx.amount
            } else {
                runningBalance -= tx.amount
            }
            if (tx.balanceAfter != runningBalance) {
                transactionDao.updateTransaction(tx.copy(balanceAfter = runningBalance))
            }
        }

        val now = System.currentTimeMillis()
        customerDao.updateCustomerBalance(customerId, runningBalance, now)
    }

    suspend fun updateBusinessProfile(profile: BusinessProfileEntity) = withContext(Dispatchers.IO) {
        businessProfileDao.insertOrUpdate(profile)
    }

    suspend fun updateAppSettings(settings: AppSettingsEntity) = withContext(Dispatchers.IO) {
        appSettingsDao.insertOrUpdate(settings)
    }

    suspend fun seedSampleData() = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val dayMillis = 24L * 60 * 60 * 1000
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.ENGLISH)

        // 1. Rajeshbhai Shah (Regular Grocery/Kirana customer with pending balance)
        val c1Id = customerDao.insertCustomer(
            CustomerEntity(
                name = "રાજેશભાઈ શાહ (કરિયાણા)",
                mobile = "9825012345",
                address = "બી-૧૨, શિવમ સોસાયટી, અમદાવાદ",
                notes = "નિયમિત ગ્રાહક - દર મહિને ચૂકવણી કરે છે",
                openingBalance = 1500.0,
                currentBalance = 1500.0,
                createdAt = now - 15 * dayMillis,
                updatedAt = now
            )
        )
        transactionDao.insertTransaction(
            TransactionEntity(
                customerId = c1Id,
                type = "UDHAR",
                amount = 2400.0,
                dateMillis = now - 10 * dayMillis,
                dateString = dateFormat.format(Date(now - 10 * dayMillis)),
                timeString = "11:30 AM",
                description = "તેલ નો ડબ્બો અને કરિયાણું",
                paymentMethod = "Cash",
                notes = "દુકાનેથી લઈ ગયા"
            )
        )
        transactionDao.insertTransaction(
            TransactionEntity(
                customerId = c1Id,
                type = "JAMA",
                amount = 2000.0,
                dateMillis = now - 5 * dayMillis,
                dateString = dateFormat.format(Date(now - 5 * dayMillis)),
                timeString = "05:15 PM",
                description = "GooglePay દ્વારા જમા",
                paymentMethod = "UPI",
                notes = "UPI ટ્રાન્ઝેક્શન"
            )
        )
        transactionDao.insertTransaction(
            TransactionEntity(
                customerId = c1Id,
                type = "UDHAR",
                amount = 850.0,
                dateMillis = now - 1 * dayMillis,
                dateString = dateFormat.format(Date(now - 1 * dayMillis)),
                timeString = "08:45 PM",
                description = "મસાલા, ચા અને ખાંડ",
                paymentMethod = "Cash",
                notes = "છોકરો લેવા આવ્યો હતો"
            )
        )
        recalculateCustomerBalance(c1Id)

        // 2. Bharatbhai Prajapati (Dairy / Paan customer)
        val c2Id = customerDao.insertCustomer(
            CustomerEntity(
                name = "ભરતભાઈ પ્રજાપતિ (પાન-મસાલા)",
                mobile = "9426098765",
                address = "ચાર રસ્તા પાસે, બજાર",
                notes = "દર રવિવારે હિસાબ ક્લિયર કરે છે",
                openingBalance = 500.0,
                currentBalance = 500.0,
                createdAt = now - 12 * dayMillis,
                updatedAt = now
            )
        )
        transactionDao.insertTransaction(
            TransactionEntity(
                customerId = c2Id,
                type = "UDHAR",
                amount = 1200.0,
                dateMillis = now - 4 * dayMillis,
                dateString = dateFormat.format(Date(now - 4 * dayMillis)),
                timeString = "02:30 PM",
                description = "પાન મસાલા & સિગારેટ પેકેટ",
                paymentMethod = "Cash",
                notes = ""
            )
        )
        transactionDao.insertTransaction(
            TransactionEntity(
                customerId = c2Id,
                type = "JAMA",
                amount = 1500.0,
                dateMillis = now - 2 * dayMillis,
                dateString = dateFormat.format(Date(now - 2 * dayMillis)),
                timeString = "07:00 PM",
                description = "રોકડ ચૂકવણી",
                paymentMethod = "Cash",
                notes = ""
            )
        )
        recalculateCustomerBalance(c2Id)

        // 3. Amit Patel (Fully settled / Paid)
        val c3Id = customerDao.insertCustomer(
            CustomerEntity(
                name = "અમિતભાઈ પટેલ (ચૂકતે ગ્રાહક)",
                mobile = "9712345678",
                address = "પ્લોટ નં. ૪૪, શાંતિનિકેતન",
                notes = "હિસાબ સમયસર પતાવે છે",
                openingBalance = 0.0,
                currentBalance = 0.0,
                createdAt = now - 20 * dayMillis,
                updatedAt = now
            )
        )
        transactionDao.insertTransaction(
            TransactionEntity(
                customerId = c3Id,
                type = "UDHAR",
                amount = 3500.0,
                dateMillis = now - 8 * dayMillis,
                dateString = dateFormat.format(Date(now - 8 * dayMillis)),
                timeString = "10:15 AM",
                description = "ઘઉં નો કટ્ટો અને બાજરી",
                paymentMethod = "Cash",
                notes = ""
            )
        )
        transactionDao.insertTransaction(
            TransactionEntity(
                customerId = c3Id,
                type = "JAMA",
                amount = 3500.0,
                dateMillis = now - 3 * dayMillis,
                dateString = dateFormat.format(Date(now - 3 * dayMillis)),
                timeString = "04:30 PM",
                description = "PhonePe થી પૂરો હિસાબ ચૂકતે",
                paymentMethod = "UPI",
                notes = "હિસાબ સાફ"
            )
        )
        recalculateCustomerBalance(c3Id)

        // 4. Sanjaybhai Mehta (Advance Customer)
        val c4Id = customerDao.insertCustomer(
            CustomerEntity(
                name = "સંજયભાઈ મહેતા (એડવાન્સ જમા)",
                mobile = "9898123456",
                address = "સી-૫, દર્શન એપાર્ટમેન્ટ",
                notes = "એડવાન્સ પૈસા જમા કરાવે છે",
                openingBalance = 0.0,
                currentBalance = 0.0,
                createdAt = now - 7 * dayMillis,
                updatedAt = now
            )
        )
        transactionDao.insertTransaction(
            TransactionEntity(
                customerId = c4Id,
                type = "JAMA",
                amount = 5000.0,
                dateMillis = now - 3 * dayMillis,
                dateString = dateFormat.format(Date(now - 3 * dayMillis)),
                timeString = "11:00 AM",
                description = "એડવાન્સ ડિપોઝીટ",
                paymentMethod = "Bank",
                notes = "બેંક ટ્રાન્સફર"
            )
        )
        transactionDao.insertTransaction(
            TransactionEntity(
                customerId = c4Id,
                type = "UDHAR",
                amount = 1800.0,
                dateMillis = now - 1 * dayMillis,
                dateString = dateFormat.format(Date(now - 1 * dayMillis)),
                timeString = "06:15 PM",
                description = "ઘરવપરાશ ની વસ્તુઓ",
                paymentMethod = "Cash",
                notes = "એડવાન્સમાંથી કપાત"
            )
        )
        recalculateCustomerBalance(c4Id)

        // Seed default profile if absent
        if (businessProfileDao.getProfileOnce() == null) {
            businessProfileDao.insertOrUpdate(
                BusinessProfileEntity(
                    id = 1,
                    businessName = "મહાદેવ પાન પાર્લર & જનરલ સ્ટોર",
                    ownerName = "હરેશભાઈ પટેલ",
                    phone = "9876543210",
                    address = "સ્ટેશન રોડ, મેઈન બજાર, ગુજરાત",
                    gstNumber = "24AAAAA0000A1Z5",
                    upiId = "mahadevpaanparlour@upi"
                )
            )
        }
    }

    suspend fun clearAllData() = withContext(Dispatchers.IO) {
        transactionDao.clearAllTransactions()
        customerDao.clearAllCustomers()
    }

    suspend fun exportDataAsJson(): String = withContext(Dispatchers.IO) {
        val customers = customerDao.getAllCustomersOnce()
        val transactions = transactionDao.getAllTransactionsOnce()
        val profile = businessProfileDao.getProfileOnce()

        val root = JSONObject()
        root.put("app", "KhataBook")
        root.put("version", 1)
        root.put("exportTimestamp", System.currentTimeMillis())
        root.put("exportDate", SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.ENGLISH).format(Date()))

        // Profile
        if (profile != null) {
            val pObj = JSONObject()
            pObj.put("businessName", profile.businessName)
            pObj.put("ownerName", profile.ownerName)
            pObj.put("phone", profile.phone)
            pObj.put("address", profile.address)
            pObj.put("gstNumber", profile.gstNumber)
            pObj.put("upiId", profile.upiId)
            root.put("profile", pObj)
        }

        // Customers
        val cArr = JSONArray()
        for (c in customers) {
            val cObj = JSONObject()
            cObj.put("id", c.id)
            cObj.put("name", c.name)
            cObj.put("mobile", c.mobile)
            cObj.put("address", c.address)
            cObj.put("notes", c.notes)
            cObj.put("openingBalance", c.openingBalance)
            cObj.put("currentBalance", c.currentBalance)
            cObj.put("createdAt", c.createdAt)
            cObj.put("updatedAt", c.updatedAt)
            cArr.put(cObj)
        }
        root.put("customers", cArr)

        // Transactions
        val tArr = JSONArray()
        for (t in transactions) {
            val tObj = JSONObject()
            tObj.put("id", t.id)
            tObj.put("customerId", t.customerId)
            tObj.put("type", t.type)
            tObj.put("amount", t.amount)
            tObj.put("dateMillis", t.dateMillis)
            tObj.put("dateString", t.dateString)
            tObj.put("timeString", t.timeString)
            tObj.put("description", t.description)
            tObj.put("paymentMethod", t.paymentMethod)
            tObj.put("notes", t.notes)
            tObj.put("balanceAfter", t.balanceAfter)
            tObj.put("createdAt", t.createdAt)
            tObj.put("updatedAt", t.updatedAt)
            tArr.put(tObj)
        }
        root.put("transactions", tArr)

        root.toString(2)
    }

    suspend fun restoreDataFromJson(jsonString: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)
            if (!root.has("customers")) return@withContext false

            clearAllData()

            // Map old customer id to new customer id
            val idMap = mutableMapOf<Long, Long>()

            val cArr = root.getJSONArray("customers")
            for (i in 0 until cArr.length()) {
                val cObj = cArr.getJSONObject(i)
                val oldId = cObj.optLong("id", 0L)
                val customer = CustomerEntity(
                    name = cObj.getString("name"),
                    mobile = cObj.optString("mobile", ""),
                    address = cObj.optString("address", ""),
                    notes = cObj.optString("notes", ""),
                    openingBalance = cObj.optDouble("openingBalance", 0.0),
                    currentBalance = cObj.optDouble("currentBalance", 0.0),
                    createdAt = cObj.optLong("createdAt", System.currentTimeMillis()),
                    updatedAt = cObj.optLong("updatedAt", System.currentTimeMillis())
                )
                val newId = customerDao.insertCustomer(customer)
                if (oldId != 0L) {
                    idMap[oldId] = newId
                }
            }

            if (root.has("transactions")) {
                val tArr = root.getJSONArray("transactions")
                for (i in 0 until tArr.length()) {
                    val tObj = tArr.getJSONObject(i)
                    val oldCustId = tObj.getLong("customerId")
                    val newCustId = idMap[oldCustId] ?: oldCustId
                    val tx = TransactionEntity(
                        customerId = newCustId,
                        type = tObj.getString("type"),
                        amount = tObj.getDouble("amount"),
                        dateMillis = tObj.optLong("dateMillis", System.currentTimeMillis()),
                        dateString = tObj.optString("dateString", ""),
                        timeString = tObj.optString("timeString", ""),
                        description = tObj.optString("description", ""),
                        paymentMethod = tObj.optString("paymentMethod", "Cash"),
                        notes = tObj.optString("notes", ""),
                        balanceAfter = tObj.optDouble("balanceAfter", 0.0),
                        createdAt = tObj.optLong("createdAt", System.currentTimeMillis()),
                        updatedAt = tObj.optLong("updatedAt", System.currentTimeMillis())
                    )
                    transactionDao.insertTransaction(tx)
                }
            }

            // Recalculate balance for all customers to ensure 100% integrity
            val allNewCustomers = customerDao.getAllCustomersOnce()
            for (c in allNewCustomers) {
                recalculateCustomerBalance(c.id)
            }

            if (root.has("profile")) {
                val pObj = root.getJSONObject("profile")
                businessProfileDao.insertOrUpdate(
                    BusinessProfileEntity(
                        id = 1,
                        businessName = pObj.optString("businessName", "Khata Book"),
                        ownerName = pObj.optString("ownerName", ""),
                        phone = pObj.optString("phone", ""),
                        address = pObj.optString("address", ""),
                        gstNumber = pObj.optString("gstNumber", ""),
                        upiId = pObj.optString("upiId", "")
                    )
                )
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
