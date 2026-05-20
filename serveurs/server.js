const express = require('express');
const cors = require('cors');
const sqlite3 = require('sqlite3').verbose();
const path = require('path');

const app = express();
const PORT = process.env.PORT || 3000;

// Middleware
app.use(cors());
app.use(express.json({ limit: '50mb' }));

// Database initialization
const dbPath = path.join(__dirname, 'staffflow.db');
const db = new sqlite3.Database(dbPath, (err) => {
    if (err) {
        console.error('Error opening database', err.message);
    } else {
        console.log('Connected to the SQLite database.');
        createTables();
    }
});

function createTables() {
    db.serialize(() => {
        // Admins Table
        db.run(`CREATE TABLE IF NOT EXISTS admins (
            username TEXT PRIMARY KEY,
            passwordHash TEXT,
            fullName TEXT,
            role TEXT
        )`);

        // Employees Table
        db.run(`CREATE TABLE IF NOT EXISTS employees (
            matricule TEXT PRIMARY KEY,
            fullName TEXT,
            department TEXT,
            poste TEXT,
            hourlySalary REAL,
            phone TEXT,
            hireDate TEXT,
            status TEXT DEFAULT 'Active',
            paymentMode TEXT DEFAULT 'Espèces',
            photoUrl TEXT,
            pin TEXT DEFAULT '1234'
        )`);

        // Attendance Table
        // Note: Using employeeMatricule + dateString as unique index to allow easy updates during synchronization
        db.run(`CREATE TABLE IF NOT EXISTS attendance (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            employeeMatricule TEXT,
            dateString TEXT,
            checkInTime INTEGER,
            checkOutTime INTEGER,
            workedHours REAL DEFAULT 0.0,
            overtimeHours REAL DEFAULT 0.0,
            isLate INTEGER DEFAULT 0,
            checkInLocation TEXT,
            checkOutLocation TEXT,
            UNIQUE(employeeMatricule, dateString)
        )`);

        // Payments Table
        db.run(`CREATE TABLE IF NOT EXISTS payments (
            id INTEGER PRIMARY KEY,
            employeeMatricule TEXT,
            periodString TEXT,
            baseSalaryPaid REAL,
            overtimeAmountPaid REAL,
            bonusPaid REAL,
            deductionPaid REAL,
            netPaid REAL,
            paymentDate INTEGER,
            paymentMode TEXT,
            transactionId TEXT UNIQUE,
            digitalSignature TEXT
        )`);

        // Seed default records if empty
        seedData();
    });
}

function seedData() {
    db.get("SELECT COUNT(*) AS count FROM admins", (err, row) => {
        if (!err && row.count === 0) {
            db.run(`INSERT INTO admins (username, passwordHash, fullName, role) VALUES 
                ('admin', 'admin', 'Jean-Paul Dupont', 'Super Admin'),
                ('manager', 'manager', 'Sophie Martin', 'Manager')`);
        }
    });

    db.get("SELECT COUNT(*) AS count FROM employees", (err, row) => {
        if (!err && row.count === 0) {
            db.run(`INSERT INTO employees (matricule, fullName, department, poste, hourlySalary, phone, hireDate, status, paymentMode, pin) VALUES 
                ('SF-0101', 'Amine Benali', 'Technique', 'Ingénieur Lead', 25.0, '+33 6 12 34 56 78', '2024-01-15', 'Active', 'Banque Transfert', '1234'),
                ('SF-0102', 'Clara Dubois', 'Design', 'UI/UX Designer', 18.5, '+33 6 98 76 54 32', '2025-03-10', 'Active', 'Espèces', '1234'),
                ('SF-0103', 'Mamadou Diallo', 'Ressources Humaines', 'Chargé de Paie', 16.0, '+33 7 11 22 33 44', '2019-06-01', 'Active', 'Mobile Money', '1111'),
                ('SF-0104', 'Yuki Tanaka', 'Marketing', 'Spécialiste SEO', 17.0, '+33 6 55 44 33 22', '2026-02-28', 'Active', 'Banque Transfert', '4321')`);
        }
    });
}

// REST Endpoints
app.get('/', (req, res) => {
    res.json({
        message: "StaffFlow Backend Server is running!",
        status: "OK",
        syncEndpoint: "/api/sync"
    });
});

// GET all tables (Raw Dump for debug or sync)
app.get('/api/admins', (req, res) => {
    db.all("SELECT * FROM admins", [], (err, rows) => {
        if (err) return res.status(500).json({ error: err.message });
        res.json(rows);
    });
});

app.get('/api/employees', (req, res) => {
    db.all("SELECT * FROM employees", [], (err, rows) => {
        if (err) return res.status(500).json({ error: err.message });
        res.json(rows);
    });
});

app.get('/api/attendance', (req, res) => {
    db.all("SELECT * FROM attendance", [], (err, rows) => {
        if (err) return res.status(500).json({ error: err.message });
        res.json(rows);
    });
});

app.get('/api/payments', (req, res) => {
    db.all("SELECT * FROM payments", [], (err, rows) => {
        if (err) return res.status(500).json({ error: err.message });
        res.json(rows);
    });
});

// BI-DIRECTIONAL MASTER SYNC ENDPOINT
// This receives local entries from the Android App, merges them with the server-side, 
// and returns the unified state of all collections back to the client!
app.post('/api/sync', async (req, res) => {
    const { admins, employees, attendance, payments } = req.body;
    console.log(`Sync request received! Payloads: admins(${admins?.length || 0}), employees(${employees?.length || 0}), attendance(${attendance?.length || 0}), payments(${payments?.length || 0})`);

    try {
        db.serialize(() => {
            // 1. Sync Admins
            if (Array.isArray(admins)) {
                const stmt = db.prepare(`INSERT INTO admins (username, passwordHash, fullName, role) 
                                       VALUES (?, ?, ?, ?) 
                                       ON CONFLICT(username) DO UPDATE SET 
                                        passwordHash=excluded.passwordHash, 
                                        fullName=excluded.fullName, 
                                        role=excluded.role`);
                admins.forEach(a => {
                    stmt.run(a.username, a.passwordHash, a.fullName, a.role);
                });
                stmt.finalize();
            }

            // 2. Sync Employees
            if (Array.isArray(employees)) {
                const stmt = db.prepare(`INSERT INTO employees (matricule, fullName, department, poste, hourlySalary, phone, hireDate, status, paymentMode, photoUrl, pin) 
                                       VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) 
                                       ON CONFLICT(matricule) DO UPDATE SET 
                                        fullName=excluded.fullName, 
                                        department=excluded.department, 
                                        poste=excluded.poste, 
                                        hourlySalary=excluded.hourlySalary, 
                                        phone=excluded.phone, 
                                        hireDate=excluded.hireDate, 
                                        status=excluded.status, 
                                        paymentMode=excluded.paymentMode, 
                                        photoUrl=excluded.photoUrl,
                                        pin=excluded.pin`);
                employees.forEach(e => {
                    stmt.run(e.matricule, e.fullName, e.department, e.poste, e.hourlySalary, e.phone, e.hireDate, e.status, e.paymentMode, e.photoUrl, e.pin);
                });
                stmt.finalize();
            }

            // 3. Sync Attendance
            if (Array.isArray(attendance)) {
                const stmt = db.prepare(`INSERT OR REPLACE INTO attendance (employeeMatricule, dateString, checkInTime, checkOutTime, workedHours, overtimeHours, isLate, checkInLocation, checkOutLocation) 
                                       VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)`);
                attendance.forEach(att => {
                    stmt.run(
                        att.employeeMatricule, 
                        att.dateString, 
                        att.checkInTime, 
                        att.checkOutTime, 
                        att.workedHours || 0.0, 
                        att.overtimeHours || 0.0, 
                        att.isLate ? 1 : 0, 
                        att.checkInLocation, 
                        att.checkOutLocation
                    );
                });
                stmt.finalize();
            }

            // 4. Sync Payments
            if (Array.isArray(payments)) {
                const stmt = db.prepare(`INSERT INTO payments (employeeMatricule, periodString, baseSalaryPaid, overtimeAmountPaid, bonusPaid, deductionPaid, netPaid, paymentDate, paymentMode, transactionId, digitalSignature) 
                                       VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) 
                                       ON CONFLICT(transactionId) DO UPDATE SET 
                                        baseSalaryPaid=excluded.baseSalaryPaid,
                                        overtimeAmountPaid=excluded.overtimeAmountPaid,
                                        bonusPaid=excluded.bonusPaid,
                                        deductionPaid=excluded.deductionPaid,
                                        netPaid=excluded.netPaid,
                                        paymentDate=excluded.paymentDate,
                                        paymentMode=excluded.paymentMode,
                                        digitalSignature=excluded.digitalSignature`);
                payments.forEach(p => {
                    stmt.run(p.employeeMatricule, p.periodString, p.baseSalaryPaid, p.overtimeAmountPaid, p.bonusPaid, p.deductionPaid, p.netPaid, p.paymentDate, p.paymentMode, p.transactionId, p.digitalSignature);
                });
                stmt.finalize();
            }

            // Read the full updated databases to return to client
            db.all("SELECT * FROM admins", [], (err, listAdmins) => {
                if (err) return res.status(500).json({ error: err.message });

                db.all("SELECT * FROM employees", [], (err, listEmployees) => {
                    if (err) return res.status(500).json({ error: err.message });

                    db.all("SELECT * FROM attendance", [], (err, listAttendance) => {
                        if (err) return res.status(500).json({ error: err.message });

                        db.all("SELECT * FROM payments", [], (err, listPayments) => {
                            if (err) return res.status(500).json({ error: err.message });

                            // Adapt SQLite types back to JSON/Android models
                            const mappedAttendance = listAttendance.map(att => ({
                                id: att.id,
                                employeeMatricule: att.employeeMatricule,
                                dateString: att.dateString,
                                checkInTime: att.checkInTime,
                                checkOutTime: att.checkOutTime || null,
                                workedHours: att.workedHours,
                                overtimeHours: att.overtimeHours,
                                isLate: att.isLate === 1,
                                checkInLocation: att.checkInLocation,
                                checkOutLocation: att.checkOutLocation || null
                            }));

                            res.json({
                                admins: listAdmins,
                                employees: listEmployees,
                                attendance: mappedAttendance,
                                payments: listPayments
                            });
                        });
                    });
                });
            });
        });
    } catch (e) {
        res.status(500).json({ error: e.message });
    }
});

// Reset endpoint for evaluation
app.post('/api/reset', (req, res) => {
    db.serialize(() => {
        db.run("DELETE FROM admins");
        db.run("DELETE FROM employees");
        db.run("DELETE FROM attendance");
        db.run("DELETE FROM payments");
        seedData();
        res.json({ message: "Database reseeded to defaults successfully!" });
    });
});

app.listen(PORT, '0.0.0.0', () => {
    console.log(`StaffFlow Backend Server listening on http://0.0.0.0:${PORT}`);
});
