package com.example.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AdminDao {
    @Query("SELECT * FROM admins WHERE username = :username LIMIT 1")
    suspend fun getAdminByUsername(username: String): Admin?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdmin(admin: Admin)

    @Query("SELECT COUNT(*) FROM admins")
    suspend fun getAdminCount(): Int

    @Query("SELECT * FROM admins")
    suspend fun getAllAdmins(): List<Admin>
}

@Dao
interface EmployeeDao {
    @Query("SELECT * FROM employees WHERE status = 'Active' ORDER BY fullName ASC")
    fun getActiveEmployees(): Flow<List<Employee>>

    @Query("SELECT * FROM employees ORDER BY fullName ASC")
    fun getAllEmployees(): Flow<List<Employee>>

    @Query("SELECT * FROM employees")
    suspend fun getAllEmployeesList(): List<Employee>

    @Query("SELECT * FROM employees WHERE matricule = :matricule LIMIT 1")
    suspend fun getEmployeeByMatricule(matricule: String): Employee?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployee(employee: Employee)

    @Update
    suspend fun updateEmployee(employee: Employee)

    @Query("UPDATE employees SET status = 'Archivé' WHERE matricule = :matricule")
    suspend fun archiveEmployee(matricule: String)

    @Query("DELETE FROM employees WHERE matricule = :matricule")
    suspend fun deleteEmployeeByMatricule(matricule: String)
}

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance ORDER BY checkInTime DESC")
    fun getAllAttendance(): Flow<List<Attendance>>

    @Query("SELECT * FROM attendance WHERE employeeMatricule = :matricule ORDER BY checkInTime DESC")
    fun getAttendanceForEmployee(matricule: String): Flow<List<Attendance>>

    @Query("SELECT * FROM attendance WHERE dateString = :dateString")
    fun getAttendanceForDateSync(dateString: String): List<Attendance>

    @Query("SELECT * FROM attendance WHERE dateString = :dateString")
    fun getAttendanceForDate(dateString: String): Flow<List<Attendance>>

    @Query("SELECT * FROM attendance WHERE employeeMatricule = :matricule AND dateString = :dateString LIMIT 1")
    suspend fun getAttendanceForEmployeeAndDate(matricule: String, dateString: String): Attendance?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: Attendance)

    @Update
    suspend fun updateAttendance(attendance: Attendance)

    @Query("SELECT * FROM attendance WHERE dateString = :dateString AND checkOutTime IS NULL")
    fun getActiveAttendancesForDate(dateString: String): Flow<List<Attendance>>

    @Query("SELECT * FROM attendance")
    suspend fun getAllAttendanceList(): List<Attendance>
}

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments ORDER BY paymentDate DESC")
    fun getAllPayments(): Flow<List<Payment>>

    @Query("SELECT * FROM payments WHERE employeeMatricule = :matricule ORDER BY paymentDate DESC")
    fun getPaymentsForEmployee(matricule: String): Flow<List<Payment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: Payment)

    @Query("SELECT * FROM payments")
    suspend fun getAllPaymentsList(): List<Payment>
}
