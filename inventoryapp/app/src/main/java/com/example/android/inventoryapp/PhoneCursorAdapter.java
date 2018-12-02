package com.example.android.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.PhoneContract;

public class PhoneCursorAdapter extends CursorAdapter {

    public PhoneCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        final int mQuantity;
        // Find individual views that we want to modify in the list item layout
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView quantityTextView = (TextView) view.findViewById(R.id.quantity);
        TextView priceTextView = (TextView) view.findViewById(R.id.price);
        TextView buyTextView = (TextView) view.findViewById(R.id.buy);
        // Find the columns of Book attributes that we're interested in
        int nameColumnIndex = cursor.getColumnIndex(PhoneContract.PhoneEntry.COLUMN_PRODUCT_BRAND);
        final int quantityColumnIndex = cursor.getColumnIndex(PhoneContract.PhoneEntry.COLUMN_QUANTITY);
        int priceColumnIndex = cursor.getColumnIndex(PhoneContract.PhoneEntry.COLUMN_PRICE);
        // Read the phone attributes from the Cursor for the current book
        final String PhoneName = cursor.getString(nameColumnIndex);
        final int PhoneQuantity = cursor.getInt(quantityColumnIndex);
        final String PhonePrice = cursor.getString(priceColumnIndex);
        // Update the TextViews with the attributes for the current phone
        nameTextView.setText(PhoneName);
        quantityTextView.setText(String.valueOf(PhoneQuantity));
        priceTextView.setText(PhonePrice);
        // OnClickListener for Sale button
        // When clicked it reduces the number in stock by 1.
        final String id = cursor.getString(cursor.getColumnIndex(PhoneContract.PhoneEntry._ID));
        buyTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (PhoneQuantity > 0) {
                    Uri currentPhoneUri = ContentUris.withAppendedId(PhoneContract.PhoneEntry.CONTENT_URI, Long.parseLong(id));
                    ContentValues values = new ContentValues();
                    values.put(PhoneContract.PhoneEntry.COLUMN_QUANTITY, PhoneQuantity - 1);
                    context.getContentResolver().update(currentPhoneUri, values, null, null);
                    swapCursor(cursor);
                    // Check if out of stock to display toast
                    if (PhoneQuantity == 1) {
                        Toast.makeText(context, R.string.toast_out_of_stock, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}