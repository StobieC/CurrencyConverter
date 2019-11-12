package d.stobie.currencyconverter.storage

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import d.stobie.currencyconverter.model.ConvertedCurrency

class DbHandler(context: Context): SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "ConversionDatabase"
        private const val TABLE_CONVERSION = "ConversionTable"
        private const val KEY_ID = "id"
        private const val KEY_TITLE = "title"
        private const val KEY_CURRENCY = "currency"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val CREATE_CONTACTS_TABLE = ("CREATE TABLE " + TABLE_CONVERSION + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_TITLE + " TEXT,"
                + KEY_CURRENCY + " TEXT" + ")")
        db?.execSQL(CREATE_CONTACTS_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, old: Int, new: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS $TABLE_CONVERSION")
        onCreate(db)
    }

    //method to read data
    fun viewEmployee():List<ConvertedCurrency>{
        val currencyList:ArrayList<ConvertedCurrency> = ArrayList<ConvertedCurrency>()
        val selectQuery = "SELECT  * FROM $TABLE_CONVERSION"
        val db = this.readableDatabase
        var cursor: Cursor? = null
        try{
            cursor = db.rawQuery(selectQuery, null)
        }catch (e: SQLiteException) {
            db.execSQL(selectQuery)
            return ArrayList()
        }
        var id: Int
        var title: String
        var amount: String
        if (cursor.moveToFirst()) {
            do {
                id = cursor.getInt(cursor.getColumnIndex("id"))
                title = cursor.getString(cursor.getColumnIndex("name"))
                amount = cursor.getString(cursor.getColumnIndex("email"))
                val currency= ConvertedCurrency(id, title, amount)
                currencyList.add(currency)
            } while (cursor.moveToNext())
        }
        return currencyList
    }

    //method to insert data
    fun addCurrency(currency: ConvertedCurrency):Long{
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(KEY_ID, currency.id)
        contentValues.put(KEY_TITLE, currency.title) // EmpModelClass Name
        contentValues.put(KEY_CURRENCY,currency.amount ) // EmpModelClass Phone
        // Inserting Row
        val success = db.insert(TABLE_CONVERSION, null, contentValues)
        //2nd argument is String containing nullColumnHack
        db.close() // Closing database connection
        return success

    }

    //method to update data
    fun updateCurrency(currency: ConvertedCurrency):Int{
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(KEY_ID, currency.id)
        contentValues.put(KEY_TITLE, currency.title) // EmpModelClass Name
        contentValues.put(KEY_CURRENCY,currency.amount) // EmpModelClass Email

        // Updating Row
        val success = db.update(TABLE_CONVERSION, contentValues,"id="+currency.id,null)
        //2nd argument is String containing nullColumnHack
        db.close() // Closing database connection
        return success
    }

    fun reWriteDatabase() {
        val db = this.writableDatabase
        db!!.execSQL("DROP TABLE IF EXISTS $TABLE_CONVERSION")
        onCreate(db)
    }
}