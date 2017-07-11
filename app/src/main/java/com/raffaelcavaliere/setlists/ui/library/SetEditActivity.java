package com.raffaelcavaliere.setlists.ui.library;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.raffaelcavaliere.setlists.R;
import com.raffaelcavaliere.setlists.data.SetlistsDbBandLoader;
import com.raffaelcavaliere.setlists.data.SetlistsDbContract;
import com.raffaelcavaliere.setlists.ui.band.BandEditActivity;
import com.raffaelcavaliere.setlists.ui.MainActivity;

import java.util.Date;

public class SetEditActivity extends AppCompatActivity  implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private TextView textName;
    private TextView textLocation;
    private Spinner spinnerBand;
    private ImageButton addBandButton;
    private Button saveButton;
    private Button cancelButton;
    private long id = 0;
    private long band = 0;

    private static final int REQUEST_ADD_BAND = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_edit);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportLoaderManager().initLoader(MainActivity.BANDS_LOADER, null, this);

        Bundle extras = getIntent().getExtras();

        if (extras != null)
            id = extras.getLong("id", 0);

        textName = (TextView) findViewById(R.id.editSetName);
        if (extras != null) {
            String name = extras.getString("name", "");
            textName.setText(name);
        } else {
            textName.setText("");
        }

        textLocation = (TextView) findViewById(R.id.editSetLocation);
        if (extras != null) {
            String location = extras.getString("location", "");
            textLocation.setText(location);
        } else {
            textLocation.setText("");
        }

        spinnerBand = (Spinner) findViewById(R.id.editSetBand);

        if (extras != null)
            band = extras.getLong("band", 0);
        spinnerBand.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                band = id;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        addBandButton = (ImageButton) findViewById(R.id.editSetNewBand);
        addBandButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newBandIntent = new Intent(v.getContext(), BandEditActivity.class);
                startActivityForResult(newBandIntent, REQUEST_ADD_BAND);
            }
        });

        saveButton = (Button) findViewById(R.id.editSetSave);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (textName.getText().length() > 0) {
                    Intent data = new Intent();
                    if (id > 0) {
                        int result = updateSet(textName.getText().toString(), textLocation.getText().toString(), band);
                        setResult(RESULT_OK, data);
                    } else {
                        Uri result = addSet(textName.getText().toString(), textLocation.getText().toString(), band);
                        data.setData(result);
                        setResult(RESULT_OK, data);
                    }

                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), "Please provide a name", Toast.LENGTH_SHORT).show();
                }

            }
        });

        cancelButton = (Button) findViewById(R.id.editSetCancel);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ADD_BAND:
                if (resultCode == RESULT_OK) {
                    String returnedResult = data.getData().toString();
                    Log.d("RETURNED RESULT", returnedResult);
                    band = Long.valueOf(data.getData().getLastPathSegment());
                    getSupportLoaderManager().restartLoader(MainActivity.BANDS_LOADER, null, this);
                }
                break;
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return SetlistsDbBandLoader.newAllBandsInstance(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        MatrixCursor extras = new MatrixCursor(new String[] {
                SetlistsDbContract.SetlistsDbBandEntry.COLUMN_ID,
                SetlistsDbContract.SetlistsDbBandEntry.COLUMN_NAME
        });
        extras.addRow(new Object[] { 0, "" });
        Cursor[] cursors = { extras, cursor };
        Cursor extendedCursor = new MergeCursor(cursors);
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
                R.layout.spinner_element,
                extendedCursor,
                new String[] { SetlistsDbContract.SetlistsDbBandEntry.COLUMN_NAME },
                new int[] { R.id.text1 },
                SimpleCursorAdapter.NO_SELECTION);
        spinnerBand.setAdapter(adapter);
        for (int i = 0; i < adapter.getCount(); i++) {
            if (((MergeCursor)adapter.getItem(i)).getLong(0) == band)
                spinnerBand.setSelection(i);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        spinnerBand.setAdapter(null);
    }

    public Uri addSet(String name, String location, long band) {
        ContentValues values = new ContentValues();
        values.put(SetlistsDbContract.SetlistsDbSetEntry.COLUMN_AUTHOR, "raffaelcavaliere");
        values.put(SetlistsDbContract.SetlistsDbSetEntry.COLUMN_NAME, name);
        values.put(SetlistsDbContract.SetlistsDbSetEntry.COLUMN_LOCATION, location);
        if (band > 0)
            values.put(SetlistsDbContract.SetlistsDbSetEntry.COLUMN_BAND, band);
        values.put(SetlistsDbContract.SetlistsDbSetEntry.COLUMN_DATE_ADDED, new Date().getTime() / 1000);
        values.put(SetlistsDbContract.SetlistsDbSetEntry.COLUMN_DATE_MODIFIED, new Date().getTime() / 1000);

        return getContentResolver().insert(SetlistsDbContract.SetlistsDbSetEntry.CONTENT_URI, values);
    }

    public int updateSet(String name, String location, long band) {
        ContentValues values = new ContentValues();
        values.put(SetlistsDbContract.SetlistsDbSetEntry.COLUMN_AUTHOR, "raffaelcavaliere");
        values.put(SetlistsDbContract.SetlistsDbSetEntry.COLUMN_NAME, name);
        values.put(SetlistsDbContract.SetlistsDbSetEntry.COLUMN_LOCATION, location);
        values.put(SetlistsDbContract.SetlistsDbSetEntry.COLUMN_BAND, band > 0 ? band : null);
        values.put(SetlistsDbContract.SetlistsDbSetEntry.COLUMN_DATE_MODIFIED, new Date().getTime() / 1000);

        return getContentResolver().update(SetlistsDbContract.SetlistsDbSetEntry.buildSetlistsDbSetUri(id), values,
                SetlistsDbContract.SetlistsDbSetEntry.COLUMN_ID + "=?", new String[] {String.valueOf(id)});
    }
}
