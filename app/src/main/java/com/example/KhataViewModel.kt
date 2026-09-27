package com.example

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.localization.AppLanguage
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class DashboardSummary(
    val totalCustomers: Int = 0,
    val pendingCustomersCount: Int = 0,
    val settledCustomersCount: Int = 0,
    val totalReceivable: Double = 0.0, // Total positive pending balances
    val totalUdharGiven: Double = 0.0, // All-time udhar sum
    val totalJamaReceived: Double = 0.0, // All-time jama sum
    val todayUdhar: Double = 0.0,
    val todayJama: Double = 0.0,
    val todayTxCount: Int = 0
)

class KhataViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = KhataRepository(KhataDatabase.getDatabase(application))

    val customers: StateFlow<List<CustomerEntity>> = repository.allCustomers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val businessProfile: StateFlow<BusinessProfileEntity?> = repository.businessProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val appSettings: StateFlow<AppSettingsEntity?> = repository.appSettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Language state (default: Gujarati)
    private val _language = MutableStateFlow(AppLanguage.GUJARATI)
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    // PIN lock state
    private val _isUnlocked = MutableStateFlow(true)
    val isUnlocked: StateFlow<Boolean> = _isUnlocked.asStateFlow()

    // Customer Search and Filters
    val customerSearchQuery = MutableStateFlow("")
    val customerFilter = MutableStateFlow("ALL") // "ALL", "PENDING", "SETTLED", "ADVANCE", "HIGHEST"

    // Transaction filter
    val transactionTypeFilter = MutableStateFlow("ALL") // "ALL", "UDHAR", "JAMA"
    val transactionDateFilter = MutableStateFlow("ALL") // "ALL", "TODAY", "THIS_MONTH"

    // Selected customer for detail/khata view
    private val _selectedCustomerId = MutableStateFlow<Long?>(null)
    val selectedCustomerId: StateFlow<Long?> = _selectedCustomerId.asStateFlow()

    init {
        // Observe settings to sync language & lock status
        viewModelScope.launch {
            appSettings.collect { settings ->
                if (settings != null) {
                    val lang = when (settings.language) {
                        "hi" -> AppLanguage.HINDI
                        "en" -> AppLanguage.ENGLISH
                        else -> AppLanguage.GUJARATI
                    }
                    _language.value = lang

                    if (settings.isPinEnabled && settings.pinCode.isNotBlank()) {
                        _isUnlocked.value = false
                    } else {
                        _isUnlocked.value = true
                    }
                } else {
                    // Seed initial business profile and sample data if empty
                    repository.seedSampleData()
                }
            }
        }
    }

    // Dashboard calculations
    val dashboardSummary: StateFlow<DashboardSummary> = combine(customers, transactions) { custList, txList ->
        val todayCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfToday = todayCalendar.timeInMillis

        var totalReceivable = 0.0
        var pendingCount = 0
        var settledCount = 0

        for (c in custList) {
            if (c.currentBalance > 0) {
                totalReceivable += c.currentBalance
                pendingCount++
            } else if (c.currentBalance == 0.0) {
                settledCount++
            }
        }

        var totalUdharAll = 0.0
        var totalJamaAll = 0.0
        var todayUdhar = 0.0
        var todayJama = 0.0
        var todayTxCount = 0

        for (tx in txList) {
            if (tx.type.equals("UDHAR", ignoreCase = true)) {
                totalUdharAll += tx.amount
                if (tx.dateMillis >= startOfToday) {
                    todayUdhar += tx.amount
                    todayTxCount++
                }
            } else {
                totalJamaAll += tx.amount
                if (tx.dateMillis >= startOfToday) {
                    todayJama += tx.amount
                    todayTxCount++
                }
            }
        }

        DashboardSummary(
            totalCustomers = custList.size,
            pendingCustomersCount = pendingCount,
            settledCustomersCount = settledCount,
            totalReceivable = totalReceivable,
            totalUdharGiven = totalUdharAll,
            totalJamaReceived = totalJamaAll,
            todayUdhar = todayUdhar,
            todayJama = todayJama,
            todayTxCount = todayTxCount
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardSummary())

    fun selectCustomer(id: Long?) {
        _selectedCustomerId.value = id
    }

    fun getCustomerById(id: Long): CustomerEntity? {
        return customers.value.find { it.id == id }
    }

    fun getTransactionsForCustomer(id: Long): Flow<List<TransactionEntity>> {
        return repository.getCustomerTransactions(id)
    }

    fun addCustomer(
        name: String,
        mobile: String,
        address: String,
        notes: String,
        openingBalance: Double,
        onComplete: (Long) -> Unit
    ) {
        viewModelScope.launch {
            val newId = repository.addCustomer(name, mobile, address, notes, openingBalance)
            onComplete(newId)
        }
    }

    fun updateCustomer(customer: CustomerEntity) {
        viewModelScope.launch {
            repository.updateCustomer(customer)
        }
    }

    fun deleteCustomer(id: Long) {
        viewModelScope.launch {
            repository.deleteCustomer(id)
            if (_selectedCustomerId.value == id) {
                _selectedCustomerId.value = null
            }
        }
    }

    fun addTransaction(
        customerId: Long,
        type: String, // "UDHAR" or "JAMA"
        amount: Double,
        dateMillis: Long = System.currentTimeMillis(),
        description: String = "",
        paymentMethod: String = "Cash",
        notes: String = "",
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.ENGLISH)
            val dateStr = dateFormat.format(Date(dateMillis))
            val timeStr = timeFormat.format(Date(dateMillis))

            repository.addTransaction(
                customerId = customerId,
                type = type,
                amount = amount,
                dateMillis = dateMillis,
                dateString = dateStr,
                timeString = timeStr,
                description = description,
                paymentMethod = paymentMethod,
                notes = notes
            )
            onComplete()
        }
    }

    fun updateTransaction(transaction: TransactionEntity, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.updateTransaction(transaction)
            onComplete()
        }
    }

    fun deleteTransaction(transactionId: Long, customerId: Long) {
        viewModelScope.launch {
            repository.deleteTransaction(transactionId, customerId)
        }
    }

    fun setLanguage(lang: AppLanguage) {
        _language.value = lang
        viewModelScope.launch {
            val current = appSettings.value ?: AppSettingsEntity()
            repository.updateAppSettings(current.copy(language = lang.code))
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            val current = appSettings.value ?: AppSettingsEntity()
            repository.updateAppSettings(current.copy(themeMode = mode))
        }
    }

    fun setThemeColor(colorName: String) {
        viewModelScope.launch {
            val current = appSettings.value ?: AppSettingsEntity()
            repository.updateAppSettings(current.copy(themeColor = colorName))
        }
    }

    fun setPin(pin: String, enabled: Boolean) {
        viewModelScope.launch {
            val current = appSettings.value ?: AppSettingsEntity()
            repository.updateAppSettings(current.copy(pinCode = pin, isPinEnabled = enabled))
            if (!enabled) {
                _isUnlocked.value = true
            }
        }
    }

    fun unlockWithPin(enteredPin: String): Boolean {
        val currentPin = appSettings.value?.pinCode ?: ""
        if (enteredPin == currentPin) {
            _isUnlocked.value = true
            return true
        }
        return false
    }

    fun updateBusinessProfile(profile: BusinessProfileEntity) {
        viewModelScope.launch {
            repository.updateBusinessProfile(profile)
        }
    }

    fun seedSampleData() {
        viewModelScope.launch {
            repository.seedSampleData()
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
            _selectedCustomerId.value = null
        }
    }

    suspend fun exportBackupJson(): String {
        return repository.exportDataAsJson()
    }

    suspend fun restoreBackupJson(json: String): Boolean {
        return repository.restoreDataFromJson(json)
    }
}
