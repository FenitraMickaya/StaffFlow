package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.database.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class StaffFlowViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = StaffFlowRepository(application)

    private val sharedPrefs = application.getSharedPreferences("staffflow_prefs", Context.MODE_PRIVATE)

    val rememberAdminEnabled: Boolean get() = sharedPrefs.getBoolean("remember_admin_enabled", false)
    val rememberedAdminUsername: String get() = sharedPrefs.getString("remember_admin_username", "") ?: ""
    val rememberedAdminPassword: String get() = sharedPrefs.getString("remember_admin_password", "") ?: ""

    val rememberEmployeeEnabled: Boolean get() = sharedPrefs.getBoolean("remember_employee_enabled", false)
    val rememberedEmployeeMatricule: String get() = sharedPrefs.getString("remember_employee_matricule", "") ?: ""
    val rememberedEmployeePin: String get() = sharedPrefs.getString("remember_employee_pin", "") ?: ""

    fun saveAdminRemembered(enabled: Boolean, user: String, pass: String) {
        sharedPrefs.edit().apply {
            putBoolean("remember_admin_enabled", enabled)
            if (enabled) {
                putString("remember_admin_username", user)
                putString("remember_admin_password", pass)
            } else {
                remove("remember_admin_username")
                remove("remember_admin_password")
            }
        }.apply()
    }

    fun saveEmployeeRemembered(enabled: Boolean, mat: String, pin: String) {
        sharedPrefs.edit().apply {
            putBoolean("remember_employee_enabled", enabled)
            if (enabled) {
                putString("remember_employee_matricule", mat)
                putString("remember_employee_pin", pin)
            } else {
                remove("remember_employee_matricule")
                remove("remember_employee_pin")
            }
        }.apply()
    }

    private val _serverIpAddress = MutableStateFlow(sharedPrefs.getString("server_ip", "http://192.168.1.100:3000") ?: "http://192.168.1.100:3000")
    val serverIpAddress: StateFlow<String> = _serverIpAddress.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _syncResultMessage = MutableStateFlow<String?>(null)
    val syncResultMessage: StateFlow<String?> = _syncResultMessage.asStateFlow()

    fun updateServerIpAddress(ip: String) {
        _serverIpAddress.value = ip
        sharedPrefs.edit().putString("server_ip", ip).apply()
    }

    fun clearSyncResultMessage() {
        _syncResultMessage.value = null
    }

    fun triggerMasterSync() {
        viewModelScope.launch {
            _isSyncing.value = true
            _syncResultMessage.value = null
            try {
                val (success, message) = repository.syncWithServer(_serverIpAddress.value)
                _syncResultMessage.value = message
            } catch (e: Exception) {
                _syncResultMessage.value = "Erreur inattendue : ${e.localizedMessage}"
            } finally {
                _isSyncing.value = false
            }
        }
    }

    // Current Login Role States
    private val _adminSession = MutableStateFlow<Admin?>(null)
    val adminSession: StateFlow<Admin?> = _adminSession.asStateFlow()

    private val _employeeSession = MutableStateFlow<Employee?>(null)
    val employeeSession: StateFlow<Employee?> = _employeeSession.asStateFlow()

    // Screen State Navigation
    private val _currentScreen = MutableStateFlow<String>("home")
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    // Keep history of previous screen for effortless back clicks!
    private val screenStack = Stack<String>()

    init {
        screenStack.push("home")
    }

    fun navigateTo(screen: String) {
        if (_currentScreen.value != screen) {
            screenStack.push(_currentScreen.value)
            _currentScreen.value = screen
        }
    }

    fun navigateBack() {
        if (screenStack.isNotEmpty()) {
            _currentScreen.value = screenStack.pop()
        } else {
            _currentScreen.value = "home"
        }
    }

    // DB Reactive Flows
    val activeEmployees: StateFlow<List<Employee>> = repository.activeEmployees
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allEmployees: StateFlow<List<Employee>> = repository.allEmployees
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAttendance: StateFlow<List<Attendance>> = repository.allAttendance
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPayments: StateFlow<List<Payment>> = repository.allPayments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered lists for Admin Dashboard
    private val _attendanceFilterDate = MutableStateFlow(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
    val attendanceFilterDate: StateFlow<String> = _attendanceFilterDate.asStateFlow()

    private val _employeeSearchQuery = MutableStateFlow("")
    val employeeSearchQuery: StateFlow<String> = _employeeSearchQuery.asStateFlow()

    fun updateEmployeeSearch(query: String) {
        _employeeSearchQuery.value = query
    }

    fun updateAttendanceFilterDate(date: String) {
        _attendanceFilterDate.value = date
    }

    // Dynamic state trackers
    private val _scanResultStatus = MutableStateFlow<ScanResult?>(null)
    val scanResultStatus: StateFlow<ScanResult?> = _scanResultStatus.asStateFlow()

    fun clearScanResult() {
        _scanResultStatus.value = null
    }

    // Authentication Actions
    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    fun clearAuthError() {
        _authError.value = null
    }

    fun loginAdmin(username: String, pass: String) {
        viewModelScope.launch {
            val admin = repository.authenticateAdmin(username, pass)
            if (admin != null) {
                _adminSession.value = admin
                _authError.value = null
                navigateTo("admin_dashboard")
            } else {
                _authError.value = "Identifiants administrateur incorrects."
            }
        }
    }

    fun registerAdmin(username: String, pass: String, fullName: String, role: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (username.isBlank() || pass.isBlank() || fullName.isBlank()) {
                _authError.value = "Tous les champs sont obligatoires."
                return@launch
            }
            val existing = repository.authenticateAdmin(username, pass)
            if (existing != null) {
                _authError.value = "Un compte avec cet identifiant existe déjà."
                return@launch
            }
            val admin = Admin(username, pass, fullName, role)
            repository.registerAdmin(admin)
            _authError.value = null
            onSuccess()
        }
    }

    fun loginEmployee(matricule: String, pin: String) {
        viewModelScope.launch {
            val employee = repository.getEmployeeByMatricule(matricule)
            if (employee != null && employee.pin == pin) {
                _employeeSession.value = employee
                _authError.value = null
                navigateTo("employee_dashboard")
            } else {
                _authError.value = "Numéro matricule ou code PIN incorrect."
            }
        }
    }

    fun logout() {
        _adminSession.value = null
        _employeeSession.value = null
        screenStack.clear()
        screenStack.push("home")
        _currentScreen.value = "home"
    }

    // CRUD Employees
    fun addEmployee(emp: Employee) {
        viewModelScope.launch {
            repository.saveEmployee(emp)
        }
    }

    fun updateEmployeeDetails(emp: Employee) {
        viewModelScope.launch {
            repository.updateEmployee(emp)
        }
    }

    fun archiveEmployeeDetails(matricule: String) {
        viewModelScope.launch {
            repository.archiveEmployee(matricule)
        }
    }

    fun deleteEmployeeDetails(matricule: String) {
        viewModelScope.launch {
            repository.deleteEmployee(matricule)
        }
    }

    // Pointage Scan simulator with live notification feedback!
    fun triggerQrScanSimulated(qrContent: String, location: String = "Bureau Principal") {
        viewModelScope.launch {
            val matriculeParsed = if (qrContent.startsWith("STAFF_FLOW_QR:")) {
                qrContent.substringAfter("STAFF_FLOW_QR:")
            } else {
                qrContent
            }
            val res = repository.processQrScan(matriculeParsed, location)
            _scanResultStatus.value = res
        }
    }

    // SALARY PAYROLL DISBURSAL
    // This allows admin to calculate and payout months instantly
    private val _calculatedPayroll = MutableStateFlow<PayrollCalculation?>(null)
    val calculatedPayroll: StateFlow<PayrollCalculation?> = _calculatedPayroll.asStateFlow()

    fun selectEmployeeForPayrollCheck(matricule: String, period: String) {
        viewModelScope.launch {
            // First look at attendances to compute parameters
            val list = allAttendance.value.filter { 
                it.employeeMatricule == matricule && 
                it.dateString.startsWith(getPeriodPrefix(period)) &&
                it.checkOutTime != null
            }
            val employee = repository.getEmployeeByMatricule(matricule)
            if (employee != null) {
                var totalW = 0.0
                var totalO = 0.0
                val dWorked = list.size

                for (att in list) {
                    totalW += att.workedHours
                    totalO += att.overtimeHours
                }

                val baseRate = employee.hourlySalary
                val computedBase = totalW * baseRate
                val computedOvertime = totalO * (baseRate * 1.25)
                val bonus = 15.0 * dWorked
                val deduction = 0.0
                val net = computedBase + computedOvertime + bonus - deduction

                _calculatedPayroll.value = PayrollCalculation(
                    employeeName = employee.fullName,
                    employeeMatricule = employee.matricule,
                    period = period,
                    totalHours = Math.round(totalW * 100.0) / 100.0,
                    overtimeHours = Math.round(totalO * 100.0) / 100.0,
                    baseSalary = Math.round(computedBase * 100.0) / 100.0,
                    overtimeAmount = Math.round(computedOvertime * 100.0) / 100.0,
                    bonus = Math.round(bonus * 100.0) / 100.0,
                    deduction = deduction,
                    netPaid = Math.round(net * 100.0) / 100.0,
                    hourlyRate = baseRate,
                    daysWorked = dWorked
                )
            } else {
                _calculatedPayroll.value = null
            }
        }
    }

    fun executePayrollPayout(calc: PayrollCalculation) {
        viewModelScope.launch {
            val transactionId = "TX-${System.currentTimeMillis() % 100000000}"
            val signature = "SIGNED-BY-${_adminSession.value?.fullName ?: "Admin"}-${calc.employeeMatricule}"
            
            val newPayment = Payment(
                employeeMatricule = calc.employeeMatricule,
                periodString = calc.period,
                baseSalaryPaid = calc.baseSalary,
                overtimeAmountPaid = calc.overtimeAmount,
                bonusPaid = calc.bonus,
                deductionPaid = calc.deduction,
                netPaid = calc.netPaid,
                paymentDate = System.currentTimeMillis(),
                paymentMode = "Virement Mobile",
                transactionId = transactionId,
                digitalSignature = signature
            )
            repository.savePayment(newPayment)
            _calculatedPayroll.value = null // Reset
        }
    }

    private fun getPeriodPrefix(period: String): String {
        val monthCode = when (period.split(" ").firstOrNull()?.lowercase()) {
            "janvier" -> "01"
            "février" -> "02"
            "mars" -> "03"
            "avril" -> "04"
            "mai" -> "05"
            "juin" -> "06"
            "juillet" -> "07"
            "août" -> "08"
            "septembre" -> "09"
            "octobre" -> "10"
            "novembre" -> "11"
            "décembre" -> "12"
            else -> "05"
        }
        val year = period.split(" ").lastOrNull() ?: "2026"
        return "$year-$monthCode"
    }
}
