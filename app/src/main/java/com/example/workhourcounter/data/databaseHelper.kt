package com.example.workhourcounter.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.util.Calendar

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "workhourcounter.db"
        private const val DATABASE_VERSION = 2

        // Table Names
        const val TABLE_WORKPLACE = "workplace"
        const val TABLE_RECORD = "work_record"

        // Workplace Columns
        const val WP_ID = "id"
        const val WP_NAME = "name"
        const val WP_START_DATE = "start_date"
        const val WP_STATUS = "status"

        // Record Columns
        const val REC_ID = "id"
        const val REC_WP_ID = "workplace_id"
        const val REC_DATE = "date"
        const val REC_SHIFT_TYPE = "shift_type"
        const val REC_BASE_HOURS = "base_hours"
        const val REC_OT_HOURS = "ot_hours"

        // Salary
        const val TABLE_SALARY = "salary_history"
        const val SAL_ID = "id"
        const val SAL_EFFECTIVE_DATE = "effective_date" // Long timestamp (e.g., 2025-01-01)
        const val SAL_AMOUNT = "amount" // Float (e.g., 25000)

        // Settings
        const val TABLE_SETTINGS = "settings"
        const val SET_KEY = "key_name"
        const val SET_VALUE = "value_int"
    }

    override fun onCreate(db: SQLiteDatabase) {
        try {
            // 1. Create Workplace Table
            db.execSQL("""
            CREATE TABLE IF NOT EXISTS $TABLE_WORKPLACE (
                $WP_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $WP_NAME TEXT,
                $WP_START_DATE INTEGER,
                $WP_STATUS TEXT
            )
        """.trimIndent())

            // 2. Create Record Table
            db.execSQL("""
            CREATE TABLE IF NOT EXISTS $TABLE_RECORD (
                $REC_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $REC_WP_ID INTEGER,
                $REC_DATE INTEGER,
                $REC_SHIFT_TYPE TEXT,
                $REC_BASE_HOURS REAL,
                $REC_OT_HOURS REAL,
                FOREIGN KEY($REC_WP_ID) REFERENCES $TABLE_WORKPLACE($WP_ID)
            )
        """.trimIndent())

            // 3. Create Salary History Table
            db.execSQL("""
            CREATE TABLE IF NOT EXISTS $TABLE_SALARY (
                $SAL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $SAL_EFFECTIVE_DATE INTEGER UNIQUE,
                $SAL_AMOUNT REAL
            )
        """.trimIndent())

            // 4. Create Settings Table
            db.execSQL("""
            CREATE TABLE IF NOT EXISTS $TABLE_SETTINGS (
                $SET_KEY TEXT PRIMARY KEY,
                $SET_VALUE INTEGER
            )
        """.trimIndent())

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Safely drop everything and rebuild
        db.execSQL("DROP TABLE IF EXISTS $TABLE_RECORD")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_WORKPLACE")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SALARY")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SETTINGS")
        onCreate(db)
    }

    // --- DATABASE OPERATIONS ---
    //  --- Workplace ---
    fun insertWorkplace(workplace: Workplace): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(WP_NAME, workplace.name)
            put(WP_START_DATE, workplace.startDate)
            put(WP_STATUS, workplace.status)
        }
        return db.insert(TABLE_WORKPLACE, null, values)
    }

    fun getAllWorkplaces(): List<Workplace> {
        val list = mutableListOf<Workplace>()
        val db = this.readableDatabase

        // SQL query sorting by status custom order, then by start date descending
        val query = """
        SELECT * FROM $TABLE_WORKPLACE
        ORDER BY 
            CASE $WP_STATUS
                WHEN '主力盤' THEN 1
                WHEN '較少去' THEN 2
                WHEN '已起貨' THEN 3
                ELSE 4
            End ASC,
            $WP_START_DATE DESC
    """.trimIndent()

        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                val workplaceId = cursor.getLong(cursor.getColumnIndexOrThrow(WP_ID))

                // Get total work days for this specific workplace
                val totalDays = getWorkDaysCount(workplaceId)

                val workplace = Workplace(
                    id = workplaceId,
                    name = cursor.getString(cursor.getColumnIndexOrThrow(WP_NAME)),
                    startDate = cursor.getLong(cursor.getColumnIndexOrThrow(WP_START_DATE)),
                    status = cursor.getString(cursor.getColumnIndexOrThrow(WP_STATUS)),
                    totalDays = totalDays // We will add this field to our model next
                )
                list.add(workplace)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    // Helper function to count rows in the record table for a workplace
    private fun getWorkDaysCount(workplaceId: Long): Int {
        val db = this.readableDatabase
        val query = "SELECT COUNT(*) FROM $TABLE_RECORD WHERE $REC_WP_ID = ?"
        val cursor = db.rawQuery(query, arrayOf(workplaceId.toString()))
        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }
        cursor.close()
        return count
    }

    // Update name and status of an existing workplace
    fun updateWorkplace(id: Long, newName: String, newStatus: String): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(WP_NAME, newName)
            put(WP_STATUS, newStatus)
        }
        // Updates the row where the ID matches
        return db.update(TABLE_WORKPLACE, values, "$WP_ID = ?", arrayOf(id.toString()))
    }

    // Delete a workplace AND its cascading records
    fun deleteWorkplaceWithRecords(workplaceId: Long) {
        val db = this.writableDatabase
        db.beginTransaction()
        try {
            // First, delete all records linked to this workplace
            db.delete(TABLE_RECORD, "$REC_WP_ID = ?", arrayOf(workplaceId.toString()))
            // Second, delete the workplace itself
            db.delete(TABLE_WORKPLACE, "$WP_ID = ?", arrayOf(workplaceId.toString()))

            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun getRecordsForWorkplace(workplaceId: Long): List<WorkRecord> {
        val list = mutableListOf<WorkRecord>()
        val db = this.readableDatabase
        val query = """
        SELECT * FROM $TABLE_RECORD 
        WHERE $REC_WP_ID = ? 
        ORDER BY $REC_DATE DESC
    """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(workplaceId.toString()))
        if (cursor.moveToFirst()) {
            do {
                val record = WorkRecord(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(REC_ID)),
                    workplaceId = cursor.getLong(cursor.getColumnIndexOrThrow(REC_WP_ID)),
                    date = cursor.getLong(cursor.getColumnIndexOrThrow(REC_DATE)),
                    shiftType = cursor.getString(cursor.getColumnIndexOrThrow(REC_SHIFT_TYPE)),
                    baseHours = cursor.getFloat(cursor.getColumnIndexOrThrow(REC_BASE_HOURS)),
                    otHours = cursor.getFloat(cursor.getColumnIndexOrThrow(REC_OT_HOURS))
                )
                list.add(record)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    // --- SALARY & SETTINGS OPERATIONS ---
    fun insertOrUpdateSalary(effectiveDateMs: Long, amount: Float) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(SAL_EFFECTIVE_DATE, effectiveDateMs)
            put(SAL_AMOUNT, amount)
        }
        // INSERT OR REPLACE handles updating the salary if the exact date entry already exists
        db.insertWithOnConflict(TABLE_SALARY, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun getSalaryHistory(): List<Pair<Long, Float>> {
        val list = mutableListOf<Pair<Long, Float>>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_SALARY ORDER BY $SAL_EFFECTIVE_DATE DESC", null)

        if (cursor.moveToFirst()) {
            do {
                val date = cursor.getLong(cursor.getColumnIndexOrThrow(SAL_EFFECTIVE_DATE))
                val amount = cursor.getFloat(cursor.getColumnIndexOrThrow(SAL_AMOUNT))
                list.add(Pair(date, amount))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun deleteSalaryRecord(effectiveDateMs: Long) {
        val db = this.writableDatabase
        db.delete(TABLE_SALARY, "$SAL_EFFECTIVE_DATE = ?", arrayOf(effectiveDateMs.toString()))
    }

    fun savePaymentDay(day: Int) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(SET_KEY, "payment_day")
            put(SET_VALUE, day)
        }
        db.insertWithOnConflict(TABLE_SETTINGS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun getPaymentDay(): Int {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT $SET_VALUE FROM $TABLE_SETTINGS WHERE $SET_KEY = 'payment_day'", null)
        var day = 7 // Default fallback to the 7th
        if (cursor.moveToFirst()) {
            day = cursor.getInt(0)
        }
        cursor.close()
        return day
    }

    //  --- Home & Record ---
    // Insert a shift log record
    fun insertRecord(record: WorkRecord): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(REC_WP_ID, record.workplaceId)
            put(REC_DATE, record.date)
            put(REC_SHIFT_TYPE, record.shiftType)
            put(REC_BASE_HOURS, record.baseHours)
            put(REC_OT_HOURS, record.otHours)
        }
        return db.insert(TABLE_RECORD, null, values)
    }

    // Fetch all records logged within a specific timestamp window range
    fun getRecordsInWindow(startTimestamp: Long, endTimestamp: Long): List<WorkRecord> {
        val list = mutableListOf<WorkRecord>()
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_RECORD WHERE $REC_DATE >= ? AND $REC_DATE <= ?"
        val cursor = db.rawQuery(query, arrayOf(startTimestamp.toString(), endTimestamp.toString()))

        if (cursor.moveToFirst()) {
            do {
                val record = WorkRecord(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(REC_ID)),
                    workplaceId = cursor.getLong(cursor.getColumnIndexOrThrow(REC_WP_ID)),
                    date = cursor.getLong(cursor.getColumnIndexOrThrow(REC_DATE)),
                    shiftType = cursor.getString(cursor.getColumnIndexOrThrow(REC_SHIFT_TYPE)),
                    baseHours = cursor.getFloat(cursor.getColumnIndexOrThrow(REC_BASE_HOURS)),
                    otHours = cursor.getFloat(cursor.getColumnIndexOrThrow(REC_OT_HOURS))
                )
                list.add(record)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    // Check if a record already exists for a specific day/workplace combo
    fun checkRecordExists(workplaceId: Long, dateMs: Long): Boolean {
        val db = this.readableDatabase
        // Zero out hours to check the exact calendar day boundaries
        val cal = Calendar.getInstance().apply { timeInMillis = dateMs }
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        val startOfDay = cal.timeInMillis
        cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59); cal.set(Calendar.SECOND, 59)
        val endOfDay = cal.timeInMillis

        val query = "SELECT COUNT(*) FROM $TABLE_RECORD WHERE $REC_WP_ID = ? AND $REC_DATE >= ? AND $REC_DATE <= ?"
        val cursor = db.rawQuery(query, arrayOf(workplaceId.toString(), startOfDay.toString(), endOfDay.toString()))
        var exists = false
        if (cursor.moveToFirst()) {
            exists = cursor.getInt(0) > 0
        }
        cursor.close()
        return exists
    }

    // Replace old record for that day, or insert fresh if new
    fun overrideOrInsertRecord(record: WorkRecord) {
        val db = this.writableDatabase

        val cal = Calendar.getInstance().apply { timeInMillis = record.date }
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        val startOfDay = cal.timeInMillis
        cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59); cal.set(Calendar.SECOND, 59)
        val endOfDay = cal.timeInMillis

        // Delete old record if it exists first
        db.delete(TABLE_RECORD, "$REC_WP_ID = ? AND $REC_DATE >= ? AND $REC_DATE <= ?",
            arrayOf(record.workplaceId.toString(), startOfDay.toString(), endOfDay.toString()))

        // Insert new record
        insertRecord(record)
    }

    // Delete a single record directly by ID
    fun deleteRecordById(recordId: Long) {
        val db = this.writableDatabase
        db.delete(TABLE_RECORD, "$REC_ID = ?", arrayOf(recordId.toString()))
    }
}