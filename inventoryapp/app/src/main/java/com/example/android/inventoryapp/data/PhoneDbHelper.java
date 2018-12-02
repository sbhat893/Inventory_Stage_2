package com.example.android.inventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class PhoneDbHelper extends SQLiteOpenHelper {
    /**
     * Name of the database file
     */
    private static final String DATABASE_NAME = "electronics.db";
    /**
     * If you change the database schema, you must increment the database version.
     */
    private static final int DATABASE_VERSION = 1;

    public PhoneDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * This is called when the database is created for the first time.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a String that contains the SQL statement to create the books table
        String SQL_CREATE_PHONES_TABLE = "CREATE TABLE " + PhoneContract.PhoneEntry.TABLE_NAME + " ("
                + PhoneContract.PhoneEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + PhoneContract.PhoneEntry.COLUMN_PRODUCT_BRAND + " TEXT NOT NULL, "
                + PhoneContract.PhoneEntry.COLUMN_PRODUCT_MODEL + " TEXT NOT NULL, "
                + PhoneContract.PhoneEntry.COLUMN_SUPPLIER_NAME + " INTEGER NOT NULL, "
                + PhoneContract.PhoneEntry.COLUMN_SUPPLIER_PHONE + " TEXT NOT NULL, "
                + PhoneContract.PhoneEntry.COLUMN_QUANTITY + " INTEGER NOT NULL DEFAULT 0,"
                + PhoneContract.PhoneEntry.COLUMN_PRICE + " INTEGER NOT NULL DEFAULT 0)";
        // Execute the SQL statement
        db.execSQL(SQL_CREATE_PHONES_TABLE);
    }

    /**
     * This is called when the database needs to be upgraded.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // The database is still at version 1, so there's nothing to do be done here.
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}