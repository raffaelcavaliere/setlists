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

public class MusicianEditActivity extends AppCompatActivity {

    private TextView textName;
    private TextView textEmail;
    private TextView textInstrument;
    private Button saveButton;
    private Button cancelButton;
    private String id = null;
    private String band;

    private int mode;
    final static String EXTRA_MODE = "mode";
    final static int EXTRA_MODE_NEW = 0;
    final static int EXTRA_MODE_EDIT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_musician_edit);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle extras = getIntent().getExtras();
        mode = extras.getInt(EXTRA_MODE, 0);

        band = extras.getString("band", null);
        id = extras.getString("id", null);

        String name = extras.getString("name", "");
        textName = (TextView) findViewById(R.id.editMusicianName);
        textName.setText(name);

        String email = extras.getString("email", "");
        textEmail = (TextView) findViewById(R.id.editMusicianEmail);
        textEmail.setText(email);

        String instrument = extras.getString("instrument", "");
        textInstrument = (TextView) findViewById(R.id.editMusicianInstrument);
        textInstrument.setText(instrument);

        saveButton = (Button) findViewById(R.id.editMusicianSave);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (textName.getText().length() > 0) {
                    Intent data = new Intent();
                    String name = textName.getText().toString();
                    String email = textEmail.getText().toString();
                    String instrument = textInstrument.getText().toString();

                    if (mode == EXTRA_MODE_EDIT) {
                        int result = updateMusician(name, email, instrument);
                        setResult(RESULT_OK, data);

                    } else {
                        Uri result = addMusician(name, email, instrument);
                        data.setData(result);
                        id = result.getLastPathSegment();
                        setResult(RESULT_OK, data);
                    }
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), "Please provide a name", Toast.LENGTH_SHORT).show();
                }

            }
        });

        cancelButton = (Button) findViewById(R.id.editMusicianCancel);
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

    private Uri addMusician(String name, String email, String instrument) {
        ContentValues values = new ContentValues();
        values.put(SetlistsDbContract.SetlistsDbMusicianEntry.COLUMN_ID, UUID.randomUUID().toString());
        values.put(SetlistsDbContract.SetlistsDbMusicianEntry.COLUMN_NAME, name);
        values.put(SetlistsDbContract.SetlistsDbMusicianEntry.COLUMN_EMAIL, email);
        values.put(SetlistsDbContract.SetlistsDbMusicianEntry.COLUMN_INSTRUMENT, instrument);
        values.put(SetlistsDbContract.SetlistsDbMusicianEntry.COLUMN_BAND, band);
        values.put(SetlistsDbContract.SetlistsDbMusicianEntry.COLUMN_DATE_ADDED, new Date().getTime() / 1000);
        values.put(SetlistsDbContract.SetlistsDbMusicianEntry.COLUMN_DATE_MODIFIED, new Date().getTime() / 1000);

        return getContentResolver().insert(SetlistsDbContract.SetlistsDbMusicianEntry.CONTENT_URI, values);
    }

    private int updateMusician(String name, String email, String instrument) {
        ContentValues values = new ContentValues();
        values.put(SetlistsDbContract.SetlistsDbMusicianEntry.COLUMN_NAME, name);
        values.put(SetlistsDbContract.SetlistsDbMusicianEntry.COLUMN_EMAIL, email);
        values.put(SetlistsDbContract.SetlistsDbMusicianEntry.COLUMN_INSTRUMENT, instrument);
        values.put(SetlistsDbContract.SetlistsDbMusicianEntry.COLUMN_BAND, band);
        values.put(SetlistsDbContract.SetlistsDbMusicianEntry.COLUMN_DATE_MODIFIED, new Date().getTime() / 1000);

        return getContentResolver().update(SetlistsDbContract.SetlistsDbMusicianEntry.buildSetlistsDbMusicianUri(id), values,
                SetlistsDbContract.SetlistsDbMusicianEntry.COLUMN_ID + "=?", new String[] {String.valueOf(id)});
    }


}
