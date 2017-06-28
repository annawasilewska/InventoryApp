package com.example.android.inventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ProductDbHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "shop.db";

    public static final String SQL_CREATE_PRODUCTS_TABLE = "CREATE TABLE " + ProductContract.ProductEntry.TABLE_NAME + " (" +
            ProductContract.ProductEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            ProductContract.ProductEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL," +
            ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY + " INTEGER NOT NULL DEFAULT 0," +
            ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE + " TEXT NOT NULL," +
            ProductContract.ProductEntry.COLUMN_SUPPLIER_NAME + " TEXT NOT NULL," +
            ProductContract.ProductEntry.COLUMN_SUPPLIER_EMAIL + " TEXT NOT NULL," +
            ProductContract.ProductEntry.COLUMN_SECTION + " INTEGER NOT NULL," +
            ProductContract.ProductEntry.COLUMN_IMAGE + " BLOB NOT NULL)";

    public ProductDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_PRODUCTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
