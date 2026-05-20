package com.example.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Admin::class, Employee::class, Attendance::class, Payment::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun adminDao(): AdminDao
    abstract fun employeeDao(): EmployeeDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun paymentDao(): PaymentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "staffflow_database"
                )
                .build().also { instance ->
                    INSTANCE = instance
                    // Seed data safely in background completely outside open callbacks
                    CoroutineScope(Dispatchers.IO).launch {
                        seedDefaultData(instance)
                    }
                }
            }
        }

        private suspend fun seedDefaultData(db: AppDatabase) {
            // Seed Admin
            val adminDao = db.adminDao()
            if (adminDao.getAdminCount() == 0) {
                adminDao.insertAdmin(
                    Admin(
                        username = "admin",
                        passwordHash = "admin", // Simple for testing
                        fullName = "Jean-Paul Dupont",
                        role = "Super Admin"
                    )
                )
                adminDao.insertAdmin(
                    Admin(
                        username = "manager",
                        passwordHash = "manager",
                        fullName = "Sophie Martin",
                        role = "Manager"
                    )
                )
            }

            // Seed Employees
            val employeeDao = db.employeeDao()
            val dummyEmployees = listOf(
                Employee(
                    matricule = "SF-0101",
                    fullName = "Amine Benali",
                    department = "Technique",
                    poste = "Ingénieur Lead",
                    hourlySalary = 25.0,
                    phone = "+33 6 12 34 56 78",
                    hireDate = "2024-01-15",
                    status = "Active",
                    paymentMode = "Banque Transfert",
                    pin = "1234"
                ),
                Employee(
                    matricule = "SF-0102",
                    fullName = "Clara Dubois",
                    department = "Design",
                    poste = "UI/UX Designer",
                    hourlySalary = 18.5,
                    phone = "+33 6 98 76 54 32",
                    hireDate = "2025-03-10",
                    status = "Active",
                    paymentMode = "Espèces",
                    pin = "1234"
                ),
                Employee(
                    matricule = "SF-0103",
                    fullName = "Mamadou Diallo",
                    department = "Ressources Humaines",
                    poste = "Chargé de Paie",
                    hourlySalary = 16.0,
                    phone = "+33 7 11 22 33 44",
                    hireDate = "2019-06-01",
                    status = "Active",
                    paymentMode = "Mobile Money",
                    pin = "1111"
                ),
                Employee(
                    matricule = "SF-0104",
                    fullName = "Yuki Tanaka",
                    department = "Marketing",
                    poste = "Spécialiste SEO",
                    hourlySalary = 17.0,
                    phone = "+33 6 55 44 33 22",
                    hireDate = "2026-02-28",
                    status = "Active",
                    paymentMode = "Banque Transfert",
                    pin = "4321"
                )
            )

            for (emp in dummyEmployees) {
                if (employeeDao.getEmployeeByMatricule(emp.matricule) == null) {
                    employeeDao.insertEmployee(emp)
                }
            }

            // Seed some historic attendances
            val attendanceDao = db.attendanceDao()
            val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
            val yday = "2026-05-19" // Standard date matching user's prompt (local time is 2026-05-20)
            
            // Add Mamadou Diallo checkIn for yesterday if not exists
            if (attendanceDao.getAttendanceForEmployeeAndDate("SF-0103", yday) == null) {
                attendanceDao.insertAttendance(
                    Attendance(
                        employeeMatricule = "SF-0103",
                        dateString = yday,
                        checkInTime = 1779264000000L + (8 * 3600 + 15 * 60) * 1000L, // 08:15
                        checkOutTime = 1779264000000L + (17 * 3600 + 30 * 60) * 1000L, // 17:30
                        workedHours = 9.25,
                        overtimeHours = 1.25,
                        isLate = true,
                        checkInLocation = "Bureau Paris",
                        checkOutLocation = "Bureau Paris"
                    )
                )
            }
 
            // Add Amine Benali checkIn for yesterday if not exists
            if (attendanceDao.getAttendanceForEmployeeAndDate("SF-0101", yday) == null) {
                attendanceDao.insertAttendance(
                    Attendance(
                        employeeMatricule = "SF-0101",
                        dateString = yday,
                        checkInTime = 1779264000000L + (7 * 3600 + 50 * 60) * 1000L, // 07:50
                        checkOutTime = 1779264000000L + (16 * 3600 + 30 * 60) * 1000L, // 16:30
                        workedHours = 8.6,
                        overtimeHours = 0.6,
                        isLate = false,
                        checkInLocation = "Bureau Lyon",
                        checkOutLocation = "Bureau Lyon"
                    )
                )
            }
        }
    }
}
