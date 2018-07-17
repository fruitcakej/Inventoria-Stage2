package com.example.android.inventoriastage2.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

public class ShipProvider extends ContentProvider {

    private ShipDbHelper shipDbHelper;

    public static final String LOG_TAG = ShipProvider.class.getSimpleName();

    /**
     * URI matcher code for the content URI for the ships table
     */
    private static final int SHIPS = 100;

    /**
     * URI matcher code for the content URI for a single ship in the ships table
     */
    private static final int SHIP_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        sUriMatcher.addURI(ShipContract.CONTENT_AUTHORITY, ShipContract.PATH_SHIPS, SHIPS);
        sUriMatcher.addURI(ShipContract.CONTENT_AUTHORITY, ShipContract.PATH_SHIPS + "/#", SHIP_ID);
    }

    @Override
    public boolean onCreate() {
        shipDbHelper = new ShipDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs,
                        @Nullable String sortOrder) {
        SQLiteDatabase database = shipDbHelper.getReadableDatabase();

        // To hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case SHIPS:
                cursor = database.query(ShipContract.ShipsEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case SHIP_ID:
                selection = ShipContract.ShipsEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(ShipContract.ShipsEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case SHIPS:
                return ShipContract.ShipsEntry.CONTENT_LIST_TYPE;
            case SHIP_ID:
                return ShipContract.ShipsEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI" + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case SHIPS:
                return insertIntoDB(uri, values);
            default:
                throw new IllegalArgumentException("Inseration is not supported for " + uri);
        }
    }

    private Uri insertIntoDB(Uri uri, ContentValues values) {

        // Check input data
        Integer name = values.getAsInteger(ShipContract.ShipsEntry.COLUMN_STARSHIP_NAME);
        Double price = values.getAsDouble(ShipContract.ShipsEntry.COLUMN_STARSHIP_PRICE);
        Integer quantity = values.getAsInteger(ShipContract.ShipsEntry.COLUMN_STARSHIP_QUANTITY);
        Integer phone = values.getAsInteger(ShipContract.ShipsEntry.COLUMN_STARSHIP_PHONE);

        if (name == null || !ShipContract.ShipsEntry.isValidStarship(name)) {
            throw new IllegalArgumentException("Valid ship type required");
        }

        if (price != null && price < 0) {
            throw new IllegalArgumentException("Please enter a valid price");
        }

        if (quantity != null && quantity < 0) {
            throw new IllegalArgumentException("Please enter a valid quantity");
        }

        if (phone == null) {
            throw new IllegalArgumentException("Please enter a valid phone number");
        }

        SQLiteDatabase db = shipDbHelper.getWritableDatabase();

        // Insert the row, returning the primary key of the new row
        long id = db.insert(ShipContract.ShipsEntry.TABLE_NAME, ShipContract.ShipsEntry.NULL, values);
        Log.v("Mainactivity", "New row ID" + id);

        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {

        SQLiteDatabase db = shipDbHelper.getWritableDatabase();
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case SHIPS:
                rowsDeleted = db.delete(ShipContract.ShipsEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case SHIP_ID:
                selection = ShipContract.ShipsEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = db.delete(ShipContract.ShipsEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case SHIPS:
                return updateDB(uri, values, selection, selectionArgs);
            case SHIP_ID:
                selection = ShipContract.ShipsEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateDB(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateDB (Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        // ContentValues containsKey() method to check if each attribute is present or not.
        // If the key is present, then we can proceed with extracting the value from it, and then checking if it’s valid.
        if (values.containsKey(ShipContract.ShipsEntry.COLUMN_STARSHIP_NAME)) {
            Integer name = values.getAsInteger(ShipContract.ShipsEntry.COLUMN_STARSHIP_NAME);
            if (name == null || !ShipContract.ShipsEntry.isValidStarship(name)) {
                throw new IllegalArgumentException("Valid ship type required");
            }
        }
        if (values.containsKey(ShipContract.ShipsEntry.COLUMN_STARSHIP_PRICE)) {
            Double price = values.getAsDouble(ShipContract.ShipsEntry.COLUMN_STARSHIP_PRICE);
            if (price != null && price < 0) {
                throw new IllegalArgumentException("Please enter a valid price");
            }
        }
        if (values.containsKey(ShipContract.ShipsEntry.COLUMN_STARSHIP_QUANTITY)) {
            Integer quantity = values.getAsInteger(ShipContract.ShipsEntry.COLUMN_STARSHIP_QUANTITY);
            if (quantity != null && quantity < 0) {
                throw new IllegalArgumentException("Please enter a valid quantity");
            }
        }
        if (values.containsKey(ShipContract.ShipsEntry.COLUMN_STARSHIP_PHONE)) {
            Integer phone = values.getAsInteger(ShipContract.ShipsEntry.COLUMN_STARSHIP_PHONE);
            if (phone == null) {
                throw new IllegalArgumentException("Please enter a valid phone number");
            }
        }
        if (values.size() == 0) {
            return 0;
        }
        SQLiteDatabase db = shipDbHelper.getWritableDatabase();
        int rowsUpdated = db.update(ShipContract.ShipsEntry.TABLE_NAME, values, selection, selectionArgs);
        if (rowsUpdated !=0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }
}
