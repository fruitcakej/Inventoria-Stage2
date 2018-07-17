package com.example.android.inventoriastage2.adapter;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoriastage2.R;
import com.example.android.inventoriastage2.data.ShipContract;

import java.text.NumberFormat;

/**
 * ShipCursor Adapter is an adapter for a list that uses a cursor of ships data as its data source
 */

public class ShipCursorAdapter extends CursorAdapter {

    public ShipCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the ship data (current row pointed by the cursor) to the given list item layout
     */
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {

        TextView starshipNameTV = view.findViewById(R.id.pName);
        TextView quantityTV = view.findViewById(R.id.quantity);
        TextView priceTV = view.findViewById(R.id.price);
        ImageButton sellBTN = view.findViewById(R.id.sale);

        final int id = cursor.getInt(cursor.getColumnIndex(ShipContract.ShipsEntry._ID));
        int namePosition = cursor.getInt(cursor.getColumnIndex(ShipContract.ShipsEntry.COLUMN_STARSHIP_NAME));
        String[] shipsArray = context.getResources().getStringArray(R.array.array_ships);

        // Set the ship name based on data from the array at the retrieved value index
        String name = shipsArray[namePosition];

        final int quantityValue = cursor.getInt(cursor.getColumnIndex(ShipContract.ShipsEntry.COLUMN_STARSHIP_QUANTITY));
        double priceValue = cursor.getDouble(cursor.getColumnIndex(ShipContract.ShipsEntry.COLUMN_STARSHIP_PRICE));

        // need to add some currency stuff here
        NumberFormat currency = NumberFormat.getCurrencyInstance();

        starshipNameTV.setText(name);
        quantityTV.setText(String.valueOf(quantityValue));
        priceTV.setText(currency.format(priceValue));

        sellBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Cannot allow to go below zero stock!
                if (quantityValue > 0) {
                    int newStock = quantityValue - 1;
                    // Retrieve the URI
                    Uri quantityURI = ContentUris.withAppendedId(ShipContract.ShipsEntry.CONTENT_URI, id);

                    // Now need to update new quantity value
                    ContentValues values = new ContentValues();
                    values.put(ShipContract.ShipsEntry.COLUMN_STARSHIP_QUANTITY, newStock);
                    context.getContentResolver().update(quantityURI, values, null, null);
                } else {
                    Toast.makeText(context,R.string.no_stock, Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
