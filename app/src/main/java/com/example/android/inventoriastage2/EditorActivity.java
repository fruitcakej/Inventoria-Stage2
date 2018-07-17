package com.example.android.inventoriastage2;

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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.android.inventoriastage2.data.ShipContract;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private Spinner shipSpinner;
    private EditText priceEditText, quantityEditText, supplierEditText, phoneEditText;
    private String currentStock;
    private int newQuantity;
    private int ship = 0;  // int value for different starships


    public static final int EXISTING_LOADER = 0;
    private static final String LOG_TAG = EditorActivity.class.getSimpleName();

    // Content URI for existing data
    private Uri currentDataUri;

    private boolean dataHasChanged = false;

    // OnTouchListener that listens for any user touches on a View, implying that they are modifying
    // the view, and we change the dataHasChanged boolean to true.

    private View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            dataHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Button minusButton, plusButton;
        ImageButton phoneIButton;

        // Get Uri from main activity
        currentDataUri = getIntent().getData();

        // If intent has no data (ie clicked on via fab button, then we are creating a new entry
        if (currentDataUri == null) {
            setTitle(R.string.add_ship);
            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            invalidateOptionsMenu();
        } else {
            setTitle(R.string.edit_entry);

            /**
             * Initialises the Cursor Loader. The ..LOADER value is eventually passed to
             * onCreateLoader()
             */
            getLoaderManager().initLoader(EXISTING_LOADER, null, this);
        }

        shipSpinner = findViewById(R.id.spinner_name);
        priceEditText = findViewById(R.id.edit_price);
        quantityEditText = findViewById(R.id.edit_quantity);
        supplierEditText = findViewById(R.id.edit_supplier);
        phoneEditText = findViewById(R.id.edit_phone);
        plusButton = findViewById(R.id.plus);
        minusButton = findViewById(R.id.minus);
        phoneIButton = findViewById(R.id.ib_phone);

        shipSpinner.setOnTouchListener(touchListener);
        priceEditText.setOnTouchListener(touchListener);
        quantityEditText.setOnTouchListener(touchListener);
        supplierEditText.setOnTouchListener(touchListener);
        phoneEditText.setOnTouchListener(touchListener);
        plusButton.setOnTouchListener(touchListener);
        minusButton.setOnTouchListener(touchListener);
        phoneIButton.setOnTouchListener(touchListener);

        setupSpinner();

        plusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                currentStock = quantityEditText.getText().toString().trim();
                if (TextUtils.isEmpty(currentStock)) { // need to protect against "" input when parsing

                    try {
                        newQuantity = Integer.parseInt(currentStock);
                    } catch (NumberFormatException e) {
                        Log.e(LOG_TAG, "quantity error on plus button " + e);
                        newQuantity = 0;
                        quantityEditText.setText(String.valueOf(newQuantity + 1));
                    }
                } else {
                    newQuantity = Integer.parseInt(currentStock);
                    quantityEditText.setText(String.valueOf(newQuantity + 1));
                }
            }
        });

        minusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                currentStock = quantityEditText.getText().toString().trim();
                if (TextUtils.isEmpty(currentStock)) {
                    Toast.makeText(getApplicationContext(), getString(R.string.no_stock), Toast.LENGTH_SHORT).show();
                } else {
                    newQuantity = Integer.parseInt(currentStock);
                    if (newQuantity >= 1) {
                        quantityEditText.setText(String.valueOf(newQuantity - 1));
                    } else {  // i.e a zero value but not a null value (empty)
                        Toast.makeText(getApplicationContext(), getString(R.string.no_stock), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        // Listener & Intent for dialing supplier of item
        phoneIButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String phoneToDial = phoneEditText.getText().toString().trim();
                if (!TextUtils.isEmpty(phoneToDial)) {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + phoneToDial));
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.no_phone), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Setup the dropdown spinner that allows the user to select the type of starship.
     */
    private void setupSpinner() {
        ArrayAdapter shipSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_ships, android.R.layout.simple_spinner_item);
        shipSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        shipSpinner.setAdapter(shipSpinnerAdapter);

        shipSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.uss_enterprise))) {
                        ship = ShipContract.ShipsEntry.STARSHIP_USS_ENTERPRISE;
                    } else if (selection.equals(getString(R.string.romulan_warbird))) {
                        ship = ShipContract.ShipsEntry.STARSHIP_ROMULAN_WARBIRD;
                    } else if (selection.equals(getString(R.string.klingon_bird_of_prey))) {
                        ship = ShipContract.ShipsEntry.STARSHIP_KLINGON_BIRD_OF_PREY;
                    } else if (selection.equals(getString(R.string.borg_cube))) {
                        ship = ShipContract.ShipsEntry.STARSHIP_BORG_CUBE;
                    } else if (selection.equals(getString(R.string.uss_prometheus))) {
                        ship = ShipContract.ShipsEntry.STARSHIP_USS_PROMETHEUS;
                    } else {
                        ship = ShipContract.ShipsEntry.STARSHIP_UNKNOWN;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                ship = 0;  // default unknown
            }
        });
    }

    private void saveData() {

        String priceString = priceEditText.getText().toString().trim();
        String quantityString = quantityEditText.getText().toString().trim();
        String supplier = supplierEditText.getText().toString().trim();
        String phoneString = phoneEditText.getText().toString().trim();
        double price;
        int quantity;
        int phone;

        // Check inputs
        if (ship == ShipContract.ShipsEntry.STARSHIP_UNKNOWN || TextUtils.isEmpty(priceString)
                || TextUtils.isEmpty(quantityString) || TextUtils.isEmpty(supplier) || TextUtils.isEmpty(phoneString)) {
            Toast.makeText(this, R.string.enter_details, Toast.LENGTH_LONG).show();
            return;
        }

        // if you try to parse to an integer value when the xxxxxString value is empty, which means "" (No user input).
        // This throws an NumberFormatException because we are trying to convert a empty string to an integer.

        try {
            price = Double.parseDouble(priceString);
        } catch (NumberFormatException e) {
            Log.e(LOG_TAG, "price error " + e);
            price = 58000000;
        }

        try {
            quantity = Integer.parseInt(quantityString);
        } catch (NumberFormatException e) {
            Log.e(LOG_TAG, "quantity error " + e);
            quantity = 1; // Min value
        }

        try {
            phone = Integer.parseInt(phoneString);
        } catch (NumberFormatException e) {
            Log.e(LOG_TAG, "phone number error " + e);
            phone = 441323334;
        }

        ContentValues values = new ContentValues();
        values.put(ShipContract.ShipsEntry.COLUMN_STARSHIP_NAME, ship);
        values.put(ShipContract.ShipsEntry.COLUMN_STARSHIP_PRICE, price);
        values.put(ShipContract.ShipsEntry.COLUMN_STARSHIP_QUANTITY, quantity);
        values.put(ShipContract.ShipsEntry.COLUMN_STARSHIP_SUPPLIER, supplier);
        values.put(ShipContract.ShipsEntry.COLUMN_STARSHIP_PHONE, phone);

        // Check to see if this is a new entry or an edit

        if (currentDataUri == null) {
            Uri newUri = getContentResolver().insert(
                    ShipContract.ShipsEntry.CONTENT_URI, values
            );

            if (newUri == null) {
                Toast.makeText(this, R.string.error_saving, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.saved, Toast.LENGTH_SHORT).show();
            }
        } else {

            int rowsUpdated = getContentResolver().update(
                    currentDataUri,
                    values,
                    null,
                    null
            );

            if (rowsUpdated == 0) {
                Toast.makeText(this, R.string.editor_update_item_failed, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.editor_update_item_successful, Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    // As menu is invalidated for new item we can call this method to hide delete option
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new item, hide the "Delete" menu item.
        if (currentDataUri == null) {
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
                saveData();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the entry hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!dataHasChanged) {
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
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the current entry.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {   // back button
        // If the entry hasn't changed, continue with handling back button press
        if (!dataHasChanged) {
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

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the entry.
                deleteEntry();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the entry.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteEntry() {

        int rowsDeleted = getContentResolver().delete(
                currentDataUri,
                null,
                null
        );
        if (rowsDeleted == 0) {
            Toast.makeText(this, getString(R.string.editor_delete_item_failed),
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.editor_delete_item_successful),
                    Toast.LENGTH_SHORT).show();
        }
        finish();
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String[] projection = {
                ShipContract.ShipsEntry._ID,
                ShipContract.ShipsEntry.COLUMN_STARSHIP_NAME,
                ShipContract.ShipsEntry.COLUMN_STARSHIP_PRICE,
                ShipContract.ShipsEntry.COLUMN_STARSHIP_QUANTITY,
                ShipContract.ShipsEntry.COLUMN_STARSHIP_SUPPLIER,
                ShipContract.ShipsEntry.COLUMN_STARSHIP_PHONE
        };

        return new CursorLoader(
                this,
                currentDataUri,
                projection,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        // No need to process on if cursor is null or less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()) {

            int namePosition = cursor.getInt(cursor.getColumnIndex(ShipContract.ShipsEntry.COLUMN_STARSHIP_NAME));
            double priceValue = cursor.getDouble(cursor.getColumnIndex(ShipContract.ShipsEntry.COLUMN_STARSHIP_PRICE));
            int quantityValue = cursor.getInt(cursor.getColumnIndex(ShipContract.ShipsEntry.COLUMN_STARSHIP_QUANTITY));
            String supplierName = cursor.getString(cursor.getColumnIndex(ShipContract.ShipsEntry.COLUMN_STARSHIP_SUPPLIER));
            int phone = cursor.getInt(cursor.getColumnIndex(ShipContract.ShipsEntry.COLUMN_STARSHIP_PHONE));

            priceEditText.setText(Double.toString(priceValue));
            quantityEditText.setText(Integer.toString(quantityValue));
            supplierEditText.setText(supplierName);
            phoneEditText.setText(Integer.toString(phone));

            switch (namePosition) {
                case ShipContract.ShipsEntry.STARSHIP_USS_ENTERPRISE:
                    shipSpinner.setSelection(1);
                    break;
                case ShipContract.ShipsEntry.STARSHIP_ROMULAN_WARBIRD:
                    shipSpinner.setSelection(2);
                    break;
                case ShipContract.ShipsEntry.STARSHIP_KLINGON_BIRD_OF_PREY:
                    shipSpinner.setSelection(3);
                    break;
                case ShipContract.ShipsEntry.STARSHIP_BORG_CUBE:
                    shipSpinner.setSelection(4);
                    break;
                case ShipContract.ShipsEntry.STARSHIP_USS_PROMETHEUS:
                    shipSpinner.setSelection(5);
                    break;
                default:
                    shipSpinner.setSelection(0);
                    break;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        shipSpinner.setSelection(0);
        priceEditText.getText().clear();
        quantityEditText.getText().clear();
        supplierEditText.getText().clear();
        phoneEditText.getText().clear();
    }
}

