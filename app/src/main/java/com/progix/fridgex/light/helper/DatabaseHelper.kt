package com.progix.fridgex.light.helper

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {
    private val mContext: Context
    private var mDataBase: SQLiteDatabase? = null


    fun updateDataBase() {
        if (mNeedUpdate) {
            val mDb = this.writableDatabase
            val cursor = mDb.rawQuery("SELECT * FROM products", null)
            cursor.moveToFirst()
            val dataItems = ArrayList<ContentValues>()
            val starred = ArrayList<String>()
            val banned = ArrayList<String>()
            val strokes = ArrayList<ContentValues>()
            while (!cursor.isAfterLast) {
                val fridge = cursor.getString(3)
                val cart = cursor.getString(4)
                val cross = cursor.getString(5)
                val id = cursor.getString(0)
                val newValues = ContentValues()
                newValues.put("is_in_fridge", fridge)
                newValues.put("is_in_cart", cart)
                newValues.put("amount", cross)
                newValues.put("id", id)
                newValues.put("is_starred", cursor.getString(6))
                newValues.put("banned", cursor.getString(7))
                dataItems.add(newValues)
                cursor.moveToNext()
            }
            cursor.close()
            val cursor1 =
                mDb.rawQuery("SELECT * FROM recipes WHERE source NOT LIKE ?", arrayOf("Авторский"))
            cursor1.moveToFirst()
            while (!cursor1.isAfterLast) {
                starred.add(cursor1.getString(7))
                cursor1.moveToNext()
            }
            cursor1.close()
            val cursor4 = mDb.rawQuery(
                "SELECT * FROM recipes WHERE source NOT LIKE ?",
                arrayOf("Авторский")
            )
            cursor4.moveToFirst()
            while (!cursor4.isAfterLast) {
                banned.add(cursor4.getString(14))
                cursor4.moveToNext()
            }
            cursor4.close()
            val content =
                mDb.rawQuery("SELECT * FROM recipes WHERE source = ?", arrayOf("Авторский"))
            content.moveToFirst()
            while (!content.isAfterLast) {
                val newValues = ContentValues()
                newValues.put("category_global", content.getString(1))
                newValues.put("category_local", content.getString(2))
                newValues.put("recipe_name", content.getString(3))
                newValues.put("recipe", content.getString(4))
                newValues.put("recipe_value", content.getString(5))
                newValues.put("time", content.getString(6))
                newValues.put("is_starred", content.getString(7))
                newValues.put("actions", content.getString(8))
                newValues.put("source", content.getString(9))
                newValues.put("calories", content.getString(10))
                newValues.put("proteins", content.getString(11))
                newValues.put("fats", content.getString(12))
                newValues.put("carboh", content.getString(13))
                newValues.put("banned", content.getString(14))
                strokes.add(newValues)
                content.moveToNext()
            }
            content.close()
            val dbFile = File(DB_PATH + DB_NAME)
            if (dbFile.exists()) dbFile.delete()
            copyDataBase()
            val mDb2 = this.readableDatabase
            for (item in dataItems) {
                mDb2.update("products", item, "id = " + item.getAsString("id"), null)
            }
            for (i in starred.indices) {
                mDb2.execSQL(
                    "UPDATE recipes SET is_starred = ? WHERE id = ?", arrayOf(
                        starred[i], (i + 1).toString()
                    )
                )
                mDb2.execSQL(
                    "UPDATE recipes SET banned = ? WHERE id = ?", arrayOf(
                        banned[i], (i + 1).toString()
                    )
                )
            }
            for (item in strokes) {
                val temp = mDb2.rawQuery("SELECT * FROM recipes", null)
                temp.moveToFirst()
                val tt = temp.count + 1
                item.put("id", tt)
                mDb2.insert("recipes", null, item)
                temp.close()
            }
            mNeedUpdate = false
        }
    }

    private fun checkDataBase(): Boolean {
        val dbFile = File(DB_PATH + DB_NAME)
        return dbFile.exists()
    }

    private fun copyDataBase() {
        if (!checkDataBase()) {
            this.readableDatabase
            close()
            copyDBFile()
        }
    }

    private fun copyDBFile() {
        val mInput = mContext.assets.open(DB_NAME)
        val mOutput: OutputStream = FileOutputStream(DB_PATH + DB_NAME)
        val mBuffer = ByteArray(1024)
        var mLength: Int
        while (mInput.read(mBuffer).also { mLength = it } > 0) mOutput.write(mBuffer, 0, mLength)
        mOutput.flush()
        mOutput.close()
        mInput.close()
    }

    @Synchronized
    override fun close() {
        if (mDataBase != null) mDataBase!!.close()
        super.close()
    }

    override fun onCreate(db: SQLiteDatabase) {}
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (newVersion > oldVersion) mNeedUpdate = true
    }

    companion object {
        private const val DB_VERSION = 1
        var DB_NAME = "FridgeXX_en.db"
        var mNeedUpdate = false
        private var DB_PATH = ""
    }

    init {
        DB_PATH = context.applicationInfo.dataDir + "/databases/"
        mContext = context
        copyDataBase()
        this.readableDatabase
    }
}