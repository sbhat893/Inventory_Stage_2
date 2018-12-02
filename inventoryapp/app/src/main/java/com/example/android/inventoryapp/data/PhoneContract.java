package com.example.android.inventoryapp.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class PhoneContract {

    public static final String CONTENT_AUTHORITY = "com.example.android.Phones";
    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    // Possible path (appended to base content URI for possible URI's)
    public static final String PATH_PHONES = "Phones";

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    private PhoneContract() {
    }

    /**
     * Inner class that defines constant values for the  database table.
     * Each entry in the table represents a single entry.
     */
    public static final class PhoneEntry implements BaseColumns {
        /**
         * The content URI to access the  data in the provider
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PHONES);
        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of entries.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PHONES;
        /**
         * The MIME type of the {@link #CONTENT_URI} for a single entry.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PHONES;
        /**
         * Name of database table
         */
        public final static String TABLE_NAME = "phones";
        /**
         * Unique ID number for the entry
         * Type: INTEGER
         */
        public final static String _ID = BaseColumns._ID;
        /**
         * Name of the brand.
         * Type: TEXT
         */
        public final static String COLUMN_PRODUCT_BRAND = "product_brand";
        /**
         * Model of the phone.
         * Type: TEXT
         */
        public final static String COLUMN_PRODUCT_MODEL = "product_model";
        //Supplier of the book.
        public final static String COLUMN_SUPPLIER_NAME = "supplier_name";
        /**
         * Supplier Phone
         * Type: TEXT
         */
        public final static String COLUMN_SUPPLIER_PHONE = "supplier_phone";
        /**
         * Quantity of the phone.
         * Type: INTEGER
         */
        public final static String COLUMN_QUANTITY = "quantity";
        /**
         * Price of the phone.
         * Type: INTEGER
         */
        public final static String COLUMN_PRICE = "price";
        /**
         * Possible values for the supplier.
         */
        public static final int SUPPLIER_SELECT = 0;
        public static final int SUPPLIER_1 = 1;
        public static final int SUPPLIER_2 = 2;
        public static final int SUPPLIER_3 = 3;
        public static final int SUPPLIER_4 = 4;
    }

}