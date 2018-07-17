package com.example.android.inventoriastage2;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.inventoriastage2.adapter.ShipCursorAdapter;
import com.example.android.inventoriastage2.data.ShipContract;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    ShipCursorAdapter shipCursorAdapter;
    public static final int URL_LOADER = 0;
    public static final String LOG_TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        ListView lvItems = findViewById(R.id.lvItems);

        View emptyView = findViewById(R.id.empty_view);
        lvItems.setEmptyView(emptyView);

        // Setup adapter to create a list item for each row of ship data in the Cursor
        shipCursorAdapter = new ShipCursorAdapter(this, null);
        lvItems.setAdapter(shipCursorAdapter);

        lvItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);

                // Build URI for specific item that was clicked and append the id
                // Passed as input to this method
                Uri currentDataUri = ContentUris.withAppendedId(ShipContract.ShipsEntry.CONTENT_URI, id);
                intent.setData(currentDataUri);
                Log.d(LOG_TAG, "LOG URI " + currentDataUri);
                startActivity(intent);
            }
        });

        /**
         * Initialises the Cursor Loader. The URL_LOADER value is eventually passed to
         * onCreateLoader()
         */
        getLoaderManager().initLoader(URL_LOADER, null, this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_delete_all_entries:
                deleteAll();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void deleteAll() {

        int rowsDeleted = getContentResolver().delete(
                ShipContract.ShipsEntry.CONTENT_URI,
                null,
                null
        );
        Log.v(LOG_TAG, rowsDeleted + " rows deleted from database");
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String[] projection = {
                ShipContract.ShipsEntry._ID,
                ShipContract.ShipsEntry.COLUMN_STARSHIP_NAME,
                ShipContract.ShipsEntry.COLUMN_STARSHIP_QUANTITY,
                ShipContract.ShipsEntry.COLUMN_STARSHIP_PRICE
        };

        // This loader to execute the Content providers query on a background thread
        return new CursorLoader(
                this,
                ShipContract.ShipsEntry.CONTENT_URI,
                projection,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Swap the new cursor in. (The framework will take care of closing the old cursor
        // once we return.)
        shipCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        shipCursorAdapter.swapCursor(null);
    }
}
