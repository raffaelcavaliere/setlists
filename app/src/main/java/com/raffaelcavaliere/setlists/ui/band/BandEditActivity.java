package com.raffaelcavaliere.setlists.ui.band;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.raffaelcavaliere.setlists.R;
import com.raffaelcavaliere.setlists.data.SetlistsDbContract;

import java.util.Date;
import java.util.UUID;

public class BandEditActivity extends AppCompatActivity {

    private TextView txtName;
    private Button saveButton;
    private Button cancelButton;
    private String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_band_edit);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle extras = getIntent().getExtras();

        if (extras != null)
            id = getIntent().getExtras().getString("id", null);

        txtName = (TextView) findViewById(R.id.editBandName);
        if (extras != null) {
            String name = extras.getString("name", "");
            txtName.setText(name);
        }

        saveButton = (Button) findViewById(R.id.editBandSave);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (txtName.getText().length() > 0) {
                    Intent data = new Intent();
                    if (id != null) {
                        int result = updateBand(txtName.getText().toString());
                        setResult(RESULT_OK, data);
                    } else {
                        Uri result = addBand(txtName.getText().toString());
                        data.setData(result);
                        setResult(RESULT_OK, data);
                    }

                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), "Please provide a name", Toast.LENGTH_SHORT).show();
                }

            }
        });

        cancelButton = (Button) findViewById(R.id.editBandCancel);

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

    public Uri addBand(String name) {
        ContentValues values = new ContentValues();
        values.put(SetlistsDbContract.SetlistsDbBandEntry.COLUMN_ID, UUID.randomUUID().toString());
        values.put(SetlistsDbContract.SetlistsDbBandEntry.COLUMN_NAME, name);
        values.put(SetlistsDbContract.SetlistsDbBandEntry.COLUMN_DATE_ADDED, new Date().getTime() / 1000);
        values.put(SetlistsDbContract.SetlistsDbBandEntry.COLUMN_DATE_MODIFIED, new Date().getTime() / 1000);

        return getContentResolver().insert(SetlistsDbContract.SetlistsDbBandEntry.CONTENT_URI, values);
    }

    public int updateBand(String name) {
        ContentValues values = new ContentValues();
        values.put(SetlistsDbContract.SetlistsDbBandEntry.COLUMN_NAME, name);
        values.put(SetlistsDbContract.SetlistsDbBandEntry.COLUMN_DATE_MODIFIED, new Date().getTime() / 1000);

        return getContentResolver().update(SetlistsDbContract.SetlistsDbBandEntry.buildSetlistsDbBandUri(id), values,
                SetlistsDbContract.SetlistsDbBandEntry.COLUMN_ID + "=?", new String[] {String.valueOf(id)});
    }
}
