package com.example.android.inventoryapp;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.inventoryapp.data.PhoneContract;
import com.example.android.inventoryapp.data.PhoneContract.PhoneEntry;

public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int PHONE_LOADER = 0;
    PhoneCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });
        // Find the ListView which will be populated with the pet data
        ListView phoneListView = (ListView) findViewById(R.id.list);
        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        phoneListView.setEmptyView(emptyView);
        //setup up an adapter to create a list item for each row of Phone data in the Cursor.
        //There is no Phone data yet(until the loader finishes) so pass in null for the Cursor.
        mCursorAdapter = new PhoneCursorAdapter(this, null);
        phoneListView.setAdapter(mCursorAdapter);
        //setup item click listener
        phoneListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //create a new intent to go to {@Link EditorActivity}
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                Uri currentPhoneUri = ContentUris.withAppendedId(PhoneContract.PhoneEntry.CONTENT_URI, id);
                //set the uri on the data field of the intent
                intent.setData(currentPhoneUri);
                //Launch the @link(EditorActivity) to display the data for the current phone
                startActivity(intent);
            }
        });
        //kick off the loader
        getLoaderManager().initLoader(PHONE_LOADER, null, this);
    }

    // After the user has clicked Save in the Activity
    private void insertPhone() {
        // Create a content values object where column names are the keys
        //and phone's attributes are the values
        ContentValues values = new ContentValues();
        values.put(PhoneContract.PhoneEntry.COLUMN_PRODUCT_BRAND, "Samsung");
        values.put(PhoneEntry.COLUMN_PRODUCT_MODEL, "J6");
        values.put(PhoneEntry.COLUMN_SUPPLIER_NAME, PhoneEntry.SUPPLIER_1);
        values.put(PhoneEntry.COLUMN_SUPPLIER_PHONE, "555-777-7788");
        values.put(PhoneEntry.COLUMN_QUANTITY, 1);
        values.put(PhoneEntry.COLUMN_PRICE, 17000);
        Uri newUri = getContentResolver().insert(PhoneEntry.CONTENT_URI, values);
    }

    /**
     * Helper method to delete all entries in the database.
     */
    private void deleteAllPhones() {
        int rowsDeleted = getContentResolver().delete(PhoneContract.PhoneEntry.CONTENT_URI, null, null);
        Log.v("CatalogActivity", rowsDeleted + " rows deleted from pet database");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertPhone();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteAllPhones();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                PhoneContract.PhoneEntry._ID,
                PhoneEntry.COLUMN_PRODUCT_BRAND,
                PhoneEntry.COLUMN_QUANTITY,
                PhoneEntry.COLUMN_PRICE
        };
        //This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,
                PhoneEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //{update @link PhoneCursorAdapter} with this new Cursor containing updated phones data
        mCursorAdapter.swapCursor(data);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //callback called when the data is need to be deleted
        mCursorAdapter.swapCursor(null);
    }
}