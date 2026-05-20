package com.example.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "admins")
data class Admin(
    @PrimaryKey val username: String,
    val passwordHash: String,
    val fullName: String,
    val role: String // Super Admin, Admin, Manager
) : Serializable

@Entity(tableName = "employees")
data class Employee(
    @PrimaryKey val matricule: String, // e.g. ST-001
    val fullName: String,
    val department: String,
    val poste: String,
    val hourlySalary: Double,
    val phone: String,
    val hireDate: String,
    val status: String = "Active", // Active, Archivé
    val paymentMode: String = "Espèces", // Espèces, Mobile Money, Banque Transfert
    val photoUrl: String? = null, // Base64 or standard photo uri
    val pin: String = "1234" // Default Login PIN for employees
) : Serializable {
    val qrCodeContent: String
        get() = "STAFF_FLOW_QR:$matricule"
}

@Entity(tableName = "attendance")
data class Attendance(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val employeeMatricule: String,
    val dateString: String, // YYYY-MM-DD
    val checkInTime: Long, // timestamp
    val checkOutTime: Long? = null, // timestamp
    val workedHours: Double = 0.0,
    val overtimeHours: Double = 0.0,
    val isLate: Boolean = false,
    val checkInLocation: String = "Siège Principal",
    val checkOutLocation: String? = null
) : Serializable

@Entity(tableName = "payments")
data class Payment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val employeeMatricule: String,
    val periodString: String, // e.g. "Mai 2026"
    val baseSalaryPaid: Double,
    val overtimeAmountPaid: Double,
    val bonusPaid: Double,
    val deductionPaid: Double,
    val netPaid: Double,
    val paymentDate: Long,
    val paymentMode: String,
    val transactionId: String,
    val digitalSignature: String // Dynamic signature text/flag
) : Serializable
