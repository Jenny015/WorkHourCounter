package com.example.workhourcounter

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "workhourcounter.db"
        private const val DATABASE_VERSION = 1

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
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Create Workplace Table
        val createWorkplaceTable = """
            CREATE TABLE $TABLE_WORKPLACE (
                $WP_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $WP_NAME TEXT,
                $WP_START_DATE INTEGER,
                $WP_STATUS TEXT
            )
        """.trimIndent()

        // Create Record Table
        val createRecordTable = """
            CREATE TABLE $TABLE_RECORD (
                $REC_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $REC_WP_ID INTEGER,
                $REC_DATE INTEGER,
                $REC_SHIFT_TYPE TEXT,
                $REC_BASE_HOURS REAL,
                $REC_OT_HOURS REAL,
                FOREIGN KEY($REC_WP_ID) REFERENCES $TABLE_WORKPLACE($WP_ID)
            )
        """.trimIndent()

        db.execSQL(createWorkplaceTable)
        db.execSQL(createRecordTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_RECORD")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_WORKPLACE")
        onCreate(db)
    }

    // --- DATABASE OPERATIONS ---

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
}