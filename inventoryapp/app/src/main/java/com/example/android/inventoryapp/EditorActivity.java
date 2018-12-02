package com.example.android.inventoryapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.android.inventoryapp.data.PhoneContract;
import com.example.android.inventoryapp.data.PhoneContract.PhoneEntry;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Identifier for the phone data loader
     **/
    private static final int EXISTING_PHONE_LOADER = 0;
    /**
     * content URI for the existing phone(null if its a new phone)
     */
    private Uri mCurrentPhoneUri;
    /**
     * EditText field to enter the phone's brand
     */
    private EditText mProductBrandEditText;
    /**
     * EditText field to enter the phone's model
     */
    private EditText mProductModelEditText;
    /**
     * EditText field to enter the phone's supplier
     */
    private Spinner mSupplierSpinner;
    /**
     * EditText field to enter the supplier's phone
     */
    private EditText mSupplierPhoneEditText;
    /**
     * EditText field to enter the phone's quantity
     */
    private EditText mQuantityEditText;
    /**
     * EditText field to enter the phone's price
     */
    private EditText mPriceEditText;
    /**
     * Values for validation
     */
    private String productBrandString;
    private String productModelString;
    private String supplierPhoneString;
    private int quantityInt;
    private int quantity;
    private int priceInt;
    private int price;
    private Button plusButton;
    private Button minusButton;
    private Button callButton;
    private int mSupplierName = PhoneContract.PhoneEntry.SUPPLIER_SELECT;
    /**
     * Boolean flag that keeps track of whether the phone has been edited (true) or not (false)
     */
    private boolean mPhoneChanged = false;
    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mPhoneChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mPhoneChanged = true;
            return false;
        }
    };
    /**
     * OnTouchListener that listens for when a user touches the spinner
     * to close the soft keyboard if it is open.
     */
    private View.OnTouchListener mSpinnerTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mPhoneChanged = true;
            hideSoftKeyboard(view);
            view.performClick();
            return false;
        }
    };
    /**
     * OnFocusChangeListener that listens for any click outside the EditText field
     * so we can hide the keyboard.
     */
    private View.OnFocusChangeListener mFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View view, boolean hasFocus) {
            if (!hasFocus) {
                hideSoftKeyboard(view);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        //Examine the intent that was used to launch the activity
        //inorder to figure out if we are adding a new phone
        Intent intent = getIntent();
        mCurrentPhoneUri = intent.getData();
        // Find all relevant views that we will need to read user input from
        mProductBrandEditText = findViewById(R.id.edit_product_brand);
        mProductModelEditText = findViewById(R.id.edit_product_model);
        mSupplierSpinner = findViewById(R.id.spinner_supplier);
        mSupplierPhoneEditText = findViewById(R.id.edit_supplier_phone);
        mQuantityEditText = findViewById(R.id.edit_product_quantity);
        mPriceEditText = findViewById(R.id.edit_product_price);
        plusButton = findViewById(R.id.button_plus);
        minusButton = findViewById(R.id.button_minus);
        callButton = findViewById(R.id.button_call);
        //if the intent does not contain a phone contentURI , then we know that we are adding a new phone
        if (mCurrentPhoneUri == null) {
            //this is a new phone so change the appbar to say "add a Phone"
            setTitle("Add a Phone");
            plusButton.setVisibility(View.GONE);
            minusButton.setVisibility(View.GONE);
            callButton.setVisibility(View.GONE);
            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a entry that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            //say "edit phone"
            setTitle(getString(R.string.editor_activity_title_edit_phone));
            // Initialize a loader to read the phone data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_PHONE_LOADER, null, this);
        }
        callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", mSupplierPhoneEditText.getText().toString().trim(), null));
                startActivity(intent);
            }
        });
        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mProductBrandEditText.setOnTouchListener(mTouchListener);
        mProductModelEditText.setOnTouchListener(mTouchListener);
        mSupplierPhoneEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        minusButton.setOnTouchListener(mTouchListener);
        plusButton.setOnTouchListener(mTouchListener);
        callButton.setOnTouchListener(mTouchListener);
        // Setup OnFocusChangeListeners on all the input fields, so we can hide the
        // soft keyboard and get it out of the way
        mProductBrandEditText.setOnFocusChangeListener(mFocusChangeListener);
        mProductModelEditText.setOnFocusChangeListener(mFocusChangeListener);
        mQuantityEditText.setOnFocusChangeListener(mFocusChangeListener);
        mPriceEditText.setOnFocusChangeListener(mFocusChangeListener);
        // Setup OnTouchListener on the spinner so we can hide the soft keyboard
        mSupplierSpinner.setOnTouchListener(mSpinnerTouchListener);
        setupSpinner();
    }

    /**
     * Setup the dropdown spinner that allows the user to select the supplier of the phone.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter supplierSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_supplier_options, android.R.layout.simple_spinner_item);
        // Specify dropdown layout style - simple list view with 1 item per line
        supplierSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        // Apply the adapter to the spinner
        mSupplierSpinner.setAdapter(supplierSpinnerAdapter);
        // Set the integer mSelected to the constant values
        mSupplierSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.supplier_1))) {
                        mSupplierName = PhoneContract.PhoneEntry.SUPPLIER_1;
                    } else if (selection.equals(getString(R.string.supplier_2))) {
                        mSupplierName = PhoneContract.PhoneEntry.SUPPLIER_2;
                    } else if (selection.equals(getString(R.string.supplier_3))) {
                        mSupplierName = PhoneContract.PhoneEntry.SUPPLIER_3;
                    } else if (selection.equals(getString(R.string.supplier_4))) {
                        mSupplierName = PhoneContract.PhoneEntry.SUPPLIER_4;
                    } else {
                        mSupplierName = PhoneContract.PhoneEntry.SUPPLIER_SELECT;
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mSupplierName = PhoneContract.PhoneEntry.SUPPLIER_SELECT;
            }
        });
    }

    /**
     * Get user input from editor and save phone into the database
     */
    private void savePhone() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String productNameString = mProductBrandEditText.getText().toString().trim();
        String productAuthorString = mProductModelEditText.getText().toString().trim();
        String supplierPhoneString = mSupplierPhoneEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        int quantityInt = Integer.parseInt(quantityString);
        String priceString = mPriceEditText.getText().toString().trim();
        int priceInt = Integer.parseInt(priceString);
        // Check if this is supposed to be a new phone
        // and check if all the fields in the editor are blank
        if (mCurrentPhoneUri == null &&
                TextUtils.isEmpty(productNameString) && TextUtils.isEmpty(productAuthorString) &&
                TextUtils.isEmpty(supplierPhoneString) && TextUtils.isEmpty(quantityString) &&
                TextUtils.isEmpty(priceString) && mSupplierName == PhoneContract.PhoneEntry.SUPPLIER_SELECT) {
            // Since no fields were modified, we can return early without creating a new phone.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            return;
        }
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(PhoneContract.PhoneEntry.COLUMN_PRODUCT_BRAND, productNameString);
        values.put(PhoneContract.PhoneEntry.COLUMN_PRODUCT_MODEL, productAuthorString);
        values.put(PhoneContract.PhoneEntry.COLUMN_SUPPLIER_NAME, mSupplierName);
        values.put(PhoneEntry.COLUMN_SUPPLIER_PHONE, supplierPhoneString);
        values.put(PhoneContract.PhoneEntry.COLUMN_QUANTITY, quantityInt);
        values.put(PhoneEntry.COLUMN_PRICE, priceInt);
        // If the price is not provided by the user, don't try to parse the string into an
        // integer value. Use 0 by default.
        int price = 0;
        if (!TextUtils.isEmpty(priceString)) {
            price = Integer.parseInt(priceString);
        }
        values.put(PhoneEntry.COLUMN_PRICE, price);
        int quantity = 0;
        if (!TextUtils.isEmpty(quantityString)) {
            quantity = Integer.parseInt(quantityString);
        }
        values.put(PhoneContract.PhoneEntry.COLUMN_QUANTITY, quantity);
        // Determine if this is a new or existing phone by checking if mCurrentPhoneUri is null or not
        if (mCurrentPhoneUri == null) {
            // This is a NEW phone, so insert a new phone into the provider,
            // returning the content URI for the new phone.
            Uri newUri = getContentResolver().insert(PhoneEntry.CONTENT_URI, values);
            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_phone_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_phone_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            int rowsAffected = getContentResolver().update(mCurrentPhoneUri, values, null, null);
            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_phone_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_phone_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new pet, hide the "Delete" menu item.
        if (mCurrentPhoneUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // save with field validation
                if (validation()) {
                    Toast.makeText(getApplicationContext(), R.string.toast_saving_product_full, Toast.LENGTH_LONG).show();
                    // Save phone to database
                    savePhone();
                    // Exit activity
                    finish();
                    return true;
                } else {
                    // Update toast with error message
                    Toast.makeText(getApplicationContext(), R.string.toast_error_saving_product_full, Toast.LENGTH_LONG).show();
                }
                break;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the pet hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mPhoneChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }
                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };
                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
        }
        return true;
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the phone hasn't changed, continue with handling back button press
        if (!mPhoneChanged) {
            super.onBackPressed();
            return;
        }
        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };
        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all pet attributes, define a projection that contains
        // all columns from the table
        String[] projection = {
                PhoneContract.PhoneEntry._ID,
                PhoneContract.PhoneEntry.COLUMN_PRODUCT_BRAND,
                PhoneContract.PhoneEntry.COLUMN_PRODUCT_MODEL,
                PhoneContract.PhoneEntry.COLUMN_SUPPLIER_NAME,
                PhoneContract.PhoneEntry.COLUMN_SUPPLIER_PHONE,
                PhoneContract.PhoneEntry.COLUMN_QUANTITY,
                PhoneContract.PhoneEntry.COLUMN_PRICE
        };
        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,
                mCurrentPhoneUri,
                projection,
                null,
                null,
                null);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of phone attributes that we're interested in
            int productBrandColumnIndex = cursor.getColumnIndex(PhoneContract.PhoneEntry.COLUMN_PRODUCT_BRAND);
            int productModelColumnIndex = cursor.getColumnIndex(PhoneContract.PhoneEntry.COLUMN_PRODUCT_MODEL);
            int suppliernameColumnIndex = cursor.getColumnIndex(PhoneContract.PhoneEntry.COLUMN_SUPPLIER_NAME);
            int supplierphoneColumnIndex = cursor.getColumnIndex(PhoneContract.PhoneEntry.COLUMN_SUPPLIER_PHONE);
            int quantityColumnIndex = cursor.getColumnIndex(PhoneContract.PhoneEntry.COLUMN_QUANTITY);
            int priceColumnIndex = cursor.getColumnIndex(PhoneContract.PhoneEntry.COLUMN_PRICE);
            // Extract out the value from the Cursor for the given column index
            String brand = cursor.getString(productBrandColumnIndex);
            String model = cursor.getString(productModelColumnIndex);
            int suppliername = cursor.getInt(suppliernameColumnIndex);
            int supplierphone = cursor.getInt(supplierphoneColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            // Update the views on the screen with the values from the database
            mProductBrandEditText.setText(brand);
            mProductModelEditText.setText(model);
            mSupplierPhoneEditText.setText(Integer.toString(supplierphone));
            mQuantityEditText.setText(Integer.toString(quantity));
            mPriceEditText.setText(Integer.toString(price));
            // Gender is a dropdown spinner, so map the constant value from the database
            switch (suppliername) {
                case PhoneContract.PhoneEntry.SUPPLIER_1:
                    mSupplierSpinner.setSelection(1);
                    break;
                case PhoneContract.PhoneEntry.SUPPLIER_2:
                    mSupplierSpinner.setSelection(2);
                    break;
                case PhoneEntry.SUPPLIER_3:
                    mSupplierSpinner.setSelection(3);
                    break;
                default:
                    mSupplierSpinner.setSelection(0);
                    break;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mProductBrandEditText.setText("");
        mProductModelEditText.setText("");
        mSupplierPhoneEditText.setText("");
        mQuantityEditText.setText("");
        mPriceEditText.setText("");
        mSupplierSpinner.setSelection(0);
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Prompt the user to confirm that they want to delete this entry.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the phone.
                deletePhone();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the book in the database.
     */
    private void deletePhone() {
        // Only perform the delete if this is an existing phome.
        if (mCurrentPhoneUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentPhoneUri, null, null);
            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_phone_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_phone_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        // Close the activity
        finish();
    }

    public boolean validation() {
        // Get text from editor and trim off any leading white space
        productBrandString = mProductBrandEditText.getText().toString().trim();
        productModelString = mProductModelEditText.getText().toString().trim();
        supplierPhoneString = mSupplierPhoneEditText.getText().toString().trim();
        // check if Quantity has invalid ""
        try {
            quantityInt = Integer.parseInt(mQuantityEditText.getText().toString().trim());
        } catch (NumberFormatException ex) {
            quantityInt = -1;
        }
        // Check if price has invalid ""
        try {
            priceInt = Integer.parseInt(mPriceEditText.getText().toString().trim());
        } catch (NumberFormatException ex) {
            priceInt = -1;
        }
        // Check required fields are filled in
        return !productBrandString.equals("") &&
                // No brand specified
                !productModelString.equals("") &&
                // No quantity specified
                quantityInt != -1 &&
                // no price specified
                priceInt != -1 &&
                // no supplier selected
                mSupplierName != 0;
    }

    public void increment(View view) {
        quantity++;
        displayQuantity();
    }

    public void decrement(View view) {
        if (quantity == 0) {
            Toast.makeText(this, R.string.noLessQuantity, Toast.LENGTH_SHORT).show();
        } else {
            quantity--;
            displayQuantity();
        }
    }

    public void displayQuantity() {
        mQuantityEditText.setText(String.valueOf(quantity));
    }

    // Hide the software keyboard when necessary
    public void hideSoftKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        assert inputMethodManager != null;
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}