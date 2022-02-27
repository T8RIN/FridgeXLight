package com.progix.fridgex.light.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final int DB_VERSION = 1;
    public static String DB_NAME = "FridgeXX_en.db";
    public static boolean mNeedUpdate = false;
    private static String DB_PATH = "";
    private final Context mContext;
    private SQLiteDatabase mDataBase;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        DB_PATH = context.getApplicationInfo().dataDir + "/databases/";
        this.mContext = context;
        copyDataBase();
        this.getReadableDatabase();
    }


    public void updateDataBase() {
        if (mNeedUpdate) {
            SQLiteDatabase mDb = this.getWritableDatabase();
            Cursor cursor = mDb.rawQuery("SELECT * FROM products", null);
            cursor.moveToFirst();
            ArrayList<ContentValues> dataItems = new ArrayList<>();
            ArrayList<String> starred = new ArrayList<>();
            ArrayList<String> banned = new ArrayList<>();
            ArrayList<ContentValues> strokes = new ArrayList<>();
            while (!cursor.isAfterLast()) {
                String fridge = cursor.getString(3);
                String cart = cursor.getString(4);
                String cross = cursor.getString(5);
                String id = cursor.getString(0);

                ContentValues newValues = new ContentValues();
                newValues.put("is_in_fridge", fridge);
                newValues.put("is_in_cart", cart);
                newValues.put("amount", cross);
                newValues.put("id", id);
                newValues.put("is_starred", cursor.getString(6));
                newValues.put("banned", cursor.getString(7));
                dataItems.add(newValues);

                cursor.moveToNext();
            }
            cursor.close();
            Cursor cursor1 = mDb.rawQuery("SELECT * FROM recipes WHERE source NOT LIKE ?", new String[]{"Авторский"});
            cursor1.moveToFirst();
            while (!cursor1.isAfterLast()) {
                starred.add(cursor1.getString(7));
                cursor1.moveToNext();
            }
            cursor1.close();
            Cursor cursor4 = mDb.rawQuery("SELECT * FROM recipes WHERE source NOT LIKE ?", new String[]{"Авторский"});
            cursor4.moveToFirst();
            while (!cursor4.isAfterLast()) {
                banned.add(cursor4.getString(14));
                cursor4.moveToNext();
            }
            cursor4.close();
            Cursor content = mDb.rawQuery("SELECT * FROM recipes WHERE source = ?", new String[]{"Авторский"});
            content.moveToFirst();
            while (!content.isAfterLast()) {
                ArrayList<String> errors = new ArrayList<>(Arrays.asList("Грибная окрошка", "Пряный чай-латте", "Безалкогольный глинтвейн", "Сбитень", "Горячий тыквенный смузи", "Горячий шоколад"));
                if (!errors.contains(content.getString(3))) {
                    ContentValues newValues = new ContentValues();
                    newValues.put("category_global", content.getString(1));
                    newValues.put("category_local", content.getString(2));
                    newValues.put("recipe_name", content.getString(3));
                    newValues.put("recipe", content.getString(4));
                    newValues.put("recipe_value", content.getString(5));
                    newValues.put("time", content.getString(6));
                    newValues.put("is_starred", content.getString(7));
                    newValues.put("actions", content.getString(8));
                    newValues.put("source", content.getString(9));
                    newValues.put("calories", content.getString(10));
                    newValues.put("proteins", content.getString(11));
                    newValues.put("fats", content.getString(12));
                    newValues.put("carboh", content.getString(13));
                    newValues.put("banned", content.getString(14));
                    strokes.add(newValues);
                }
                content.moveToNext();
            }
            content.close();
            File dbFile = new File(DB_PATH + DB_NAME);
            if (dbFile.exists()) dbFile.delete();
            copyDataBase();
            SQLiteDatabase mDb2 = this.getReadableDatabase();
            for (ContentValues item : dataItems) {
                mDb2.update("products", item, "id = " + item.getAsString("id"), null);
            }
            for (int i = 0; i < starred.size(); i++) {
                mDb2.execSQL("UPDATE recipes SET is_starred = ? WHERE id = ?", new String[]{starred.get(i), String.valueOf(i + 1)});
                mDb2.execSQL("UPDATE recipes SET banned = ? WHERE id = ?", new String[]{banned.get(i), String.valueOf(i + 1)});
            }
            for (ContentValues item : strokes) {
                Cursor temp = mDb2.rawQuery("SELECT * FROM recipes", null);
                temp.moveToFirst();
                int tt = temp.getCount() + 1;
                item.put("id", tt);
                mDb2.insert("recipes", null, item);
                temp.close();
            }
            Cursor cursor2 = mDb2.rawQuery("SELECT * FROM products", null);
            cursor2.moveToFirst();
            while (!cursor2.isAfterLast()) {
                String product = cursor2.getString(2);
                String new_product = product.toLowerCase();
                mDb2.execSQL("UPDATE products SET product = ? WHERE product = ?", new String[]{new_product, product});
                cursor2.moveToNext();
            }
            cursor2.close();

            mNeedUpdate = false;
        }
    }

    private boolean checkDataBase() {
        File dbFile = new File(DB_PATH + DB_NAME);
        return dbFile.exists();
    }

    private void copyDataBase() {
        if (!checkDataBase()) {
            this.getReadableDatabase();
            this.close();
            try {
                copyDBFile();
            } catch (IOException mIOException) {
                throw new Error("ErrorCopyingDataBase");
            }
        }
    }

    private void copyDBFile() throws IOException {
        InputStream mInput = mContext.getAssets().open(DB_NAME);
        OutputStream mOutput = new FileOutputStream(DB_PATH + DB_NAME);
        byte[] mBuffer = new byte[1024];
        int mLength;
        while ((mLength = mInput.read(mBuffer)) > 0)
            mOutput.write(mBuffer, 0, mLength);
        mOutput.flush();
        mOutput.close();
        mInput.close();
    }

    @Override
    public synchronized void close() {
        if (mDataBase != null)
            mDataBase.close();
        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion > oldVersion) mNeedUpdate = true;
    }
}
