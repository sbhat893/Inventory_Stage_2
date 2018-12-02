package com.example.android.inventoryapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

public class PhoneProvider extends ContentProvider {
    //Tag for log messages
    public static final String LOG_TAG = PhoneProvider.class.getSimpleName();
    /**
     * URI matcher code for the content URI for the  table
     */
    private static final int PHONES = 100;
    /**
     * URI matcher code for the content URI for a single entry in the table
     */
    private static final int PHONES_ID = 101;
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        sUriMatcher.addURI(PhoneContract.CONTENT_AUTHORITY, PhoneContract.PATH_PHONES, PHONES);
        sUriMatcher.addURI(PhoneContract.CONTENT_AUTHORITY, PhoneContract.PATH_PHONES + "/#", PHONES_ID);
    }

    //Database Helper object
    private PhoneDbHelper mDbHelper;

    // Initialize the provider and the database helper object.
    @Override
    public boolean onCreate() {
        mDbHelper = new PhoneDbHelper(getContext());
        return true;
    }

    //Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case PHONES:
                cursor = database.query(PhoneContract.PhoneEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case PHONES_ID:
                selection = PhoneContract.PhoneEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                // Cursor containing that row of the table.
                cursor = database.query(PhoneContract.PhoneEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        //Return the cursor
        return cursor;
    }

    //Insert new data into the provider with the given ContentValues.
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PHONES:
                return insertPhone(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a phone into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertPhone(Uri uri, ContentValues values) {
        // Check that the name is not null
        String name = values.getAsString(PhoneContract.PhoneEntry.COLUMN_PRODUCT_BRAND);
        if (name == null) {
            throw new IllegalArgumentException("Pet requires a name");
        }
        Integer quantity = values.getAsInteger(PhoneContract.PhoneEntry.COLUMN_QUANTITY);
        if (quantity != null && quantity < 0) {
            throw new IllegalArgumentException("Book requires valid quantity");
        }
        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        // Insert the new phone with the given values
        long id = database.insert(PhoneContract.PhoneEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }
        //Notify all the listeners that the data has changed for the phone content Uri
        getContext().getContentResolver().notifyChange(uri, null);
        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId(uri, id);
    }

    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    @Override
    public int update(@NonNull Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PHONES:
                return updatePet(uri, contentValues, selection, selectionArgs);
            case PHONES_ID:
                // For the PHONES_ID code, extract out the ID from the URI,
                selection = PhoneContract.PhoneEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updatePet(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update PHONES in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more PHONES).
     * Return the number of rows that were successfully updated.
     */
    private int updatePet(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // If the {@link PhoneEntry#COLUMN_PRODUCT_BRAND} key is present,
        // check that the name value is not null.
        if (values.containsKey(PhoneContract.PhoneEntry.COLUMN_PRODUCT_BRAND)) {
            String name = values.getAsString(PhoneContract.PhoneEntry.COLUMN_PRODUCT_BRAND);
            if (name == null) {
                throw new IllegalArgumentException("Book requires a name");
            }
        }
        // If the {@link PhoneEntry#COLUMN_QUANTITY} key is present,
        // check that the weight value is valid.
        if (values.containsKey(PhoneContract.PhoneEntry.COLUMN_QUANTITY)) {
            Integer quantity = values.getAsInteger(PhoneContract.PhoneEntry.COLUMN_QUANTITY);
            if (quantity != null && quantity < 0) {
                throw new IllegalArgumentException("Book requires valid weight");
            }
        }
        if (values.size() == 0) {
            return 0;
        }
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(PhoneContract.PhoneEntry.TABLE_NAME, values, selection, selectionArgs);
        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        // Return the number of rows updated
        return rowsUpdated;
    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        // Track the number of rows that were deleted
        int rowsDeleted;
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PHONES:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(PhoneContract.PhoneEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PHONES_ID:
                // Delete a single row given by the ID in the URI
                selection = PhoneContract.PhoneEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(PhoneContract.PhoneEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        // Return the number of rows deleted
        return rowsDeleted;
    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Override
    public String getType(@NonNull Uri uri) {
        /*s refers to static in sUriMatcher*/
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PHONES:
                return PhoneContract.PhoneEntry.CONTENT_LIST_TYPE;
            case PHONES_ID:
                return PhoneContract.PhoneEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}