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
import android.util.Log;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.raffaelcavaliere.setlists.R;
import com.raffaelcavaliere.setlists.data.SetlistsDbArtistLoader;
import com.raffaelcavaliere.setlists.data.SetlistsDbContract;
import com.raffaelcavaliere.setlists.data.SetlistsDbDocumentLoader;
import com.raffaelcavaliere.setlists.ui.MainActivity;

import java.util.Date;
import java.util.UUID;

import static android.view.View.GONE;

public class SongEditActivity extends AppCompatActivity  implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private TextView txtTitle;
    private Spinner spinnerArtist;
    private ImageButton addArtistButton;
    private Spinner spinnerKey;
    private TextView textTempo;
    private TextView textMinutes;
    private TextView textSeconds;
    private Spinner spinnerDocument;
    private ImageButton addDocumentButton;
    private TextView textNotes;

    private Button saveButton;
    private Button cancelButton;
    private String id = null;
    private String artist = null;
    private String document = null;
    private int tempo = 0;
    private String title = "";

    private static final int REQUEST_ADD_ARTIST = 1000;
    private static final int REQUEST_ADD_DOCUMENT = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_edit);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle extras = getIntent().getExtras();

        if (extras != null)
            id = extras.getString("id", null);

        txtTitle = (TextView) findViewById(R.id.editSongTitle);
        if (extras != null) {
            title = extras.getString("title", "");
            txtTitle.setText(title);
        }

        spinnerArtist = (Spinner) findViewById(R.id.editSongArtist);
        if (extras != null)
            artist = extras.getString("artist", null);
        spinnerArtist.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                artist = ((MergeCursor)spinnerArtist.getAdapter().getItem(position)).getString(0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                artist = null;
            }
        });

        addArtistButton = (ImageButton) findViewById(R.id.editSongNewArtist);
        addArtistButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newArtistIntent = new Intent(v.getContext(), ArtistEditActivity.class);
                startActivityForResult(newArtistIntent, REQUEST_ADD_ARTIST);
            }
        });

        spinnerKey = (Spinner) findViewById(R.id.editSongKey);
        final String[] keys = getResources().getStringArray(R.array.keys);
        final ArrayAdapter<String> keysAdapter = new ArrayAdapter<String>(this, R.layout.spinner_element, keys);
        spinnerKey.setAdapter(keysAdapter);
        if (extras != null) {
            String key = extras.getString("key", "");
            spinnerKey.setSelection(keysAdapter.getPosition(key));
        }

        textTempo = (TextView) findViewById(R.id.editSongTempo);
        if (extras != null) {
            tempo = extras.getInt("tempo", 0);
            textTempo.setText(String.valueOf(tempo));
        }

        textMinutes = (TextView) findViewById(R.id.editSongDurationMinutes);
        textSeconds = (TextView) findViewById(R.id.editSongDurationSeconds);
        if (extras != null) {
            int duration = extras.getInt("duration", 0);
            int minutes = (int) (duration / 60);
            int seconds = (int) (duration % 60);
            textMinutes.setText(String.valueOf(minutes));
            textSeconds.setText((seconds < 10 ? "0" : "") + String.valueOf(seconds));
        }

        spinnerDocument = (Spinner) findViewById(R.id.editSongDocument);
        addDocumentButton = (ImageButton) findViewById(R.id.editSongNewDocument);

        if (id != null) {
            if (extras != null)
                document = extras.getString("document", null);

            spinnerDocument.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    //document = id;
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    document = null;
                }
            });

            addDocumentButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent newDocumentIntent = new Intent(v.getContext(), DocumentEditActivity.class);
                    newDocumentIntent.putExtra("song", id);
                    newDocumentIntent.putExtra("title", title);
                    newDocumentIntent.putExtra("tempo", tempo);
                    startActivityForResult(newDocumentIntent, REQUEST_ADD_DOCUMENT);
                }
            });
        }
        else {
            spinnerDocument.setVisibility(GONE);
            addDocumentButton.setVisibility(GONE);
            ((TextView)findViewById(R.id.editSongDocumentLabel)).setVisibility(GONE);
        }


        textNotes = (TextView) findViewById(R.id.editSongNotes);
        if (extras != null) {
            String notes = extras.getString("notes", "");
            textNotes.setText(notes);
        }

        saveButton = (Button) findViewById(R.id.editSongSave);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (txtTitle.getText().length() > 0) {
                    Intent data = new Intent();
                    String tempo = textTempo.getText().toString();
                    String minutes = textMinutes.getText().toString();
                    String seconds = textSeconds.getText().toString();
                    if (id != null) {
                        int result = updateSong(txtTitle.getText().toString(),
                                artist,
                                spinnerKey.getSelectedItem().toString(),
                                tempo.isEmpty() ? 0 : Integer.valueOf(tempo),
                                (minutes.isEmpty() ? 0 : Integer.valueOf(minutes)) * 60 + (seconds.isEmpty() ? 0 : Integer.valueOf(seconds)),
                                document,
                                textNotes.getText().toString());
                        setResult(RESULT_OK, data);
                    } else {
                        Uri result = addSong(txtTitle.getText().toString(),
                                artist,
                                spinnerKey.getSelectedItem().toString(),
                                tempo.isEmpty() ? 0 : Integer.valueOf(tempo),
                                (minutes.isEmpty() ? 0 : Integer.valueOf(minutes)) * 60 + (seconds.isEmpty() ? 0 : Integer.valueOf(seconds)),
                                document,
                                textNotes.getText().toString());
                        data.setData(result);
                        setResult(RESULT_OK, data);
                    }

                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), "Please provide a title", Toast.LENGTH_SHORT).show();
                }

            }
        });

        cancelButton = (Button) findViewById(R.id.editSongCancel);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        getSupportLoaderManager().initLoader(MainActivity.ARTISTS_LOADER, null, this);
        getSupportLoaderManager().initLoader(MainActivity.DOCUMENTS_LOADER, null, this);

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
            case REQUEST_ADD_ARTIST:
                if (resultCode == RESULT_OK) {
                    String returnedResult = data.getData().toString();
                    Log.d("RETURNED RESULT", returnedResult);
                    artist = data.getData().getLastPathSegment();
                    getSupportLoaderManager().restartLoader(MainActivity.ARTISTS_LOADER, null, this);
                }
                break;
            case REQUEST_ADD_DOCUMENT:
                if (resultCode == RESULT_OK) {
                    String returnedResult = data.getData().toString();
                    Log.d("RETURNED RESULT", returnedResult);
                    document = data.getData().getLastPathSegment();
                    getSupportLoaderManager().restartLoader(MainActivity.DOCUMENTS_LOADER, null, this);
                }
                break;
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        if (i == MainActivity.ARTISTS_LOADER)
            return SetlistsDbArtistLoader.newAllArtistsInstance(this);
        else {
            return SetlistsDbDocumentLoader.newInstanceForSongId(this, this.id);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (cursorLoader.getId() == MainActivity.ARTISTS_LOADER) {
            MatrixCursor extras = new MatrixCursor(new String[]{
                    SetlistsDbContract.SetlistsDbArtistEntry.COLUMN_ID,
                    SetlistsDbContract.SetlistsDbArtistEntry.COLUMN_NAME
            });
            extras.addRow(new Object[]{null, ""});
            Cursor[] cursors = {extras, cursor};
            Cursor extendedCursor = new MergeCursor(cursors);
            SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
                    R.layout.spinner_element,
                    extendedCursor,
                    new String[]{SetlistsDbContract.SetlistsDbArtistEntry.COLUMN_NAME},
                    new int[]{R.id.text1},
                    SimpleCursorAdapter.NO_SELECTION);
            spinnerArtist.setAdapter(adapter);
            for (int i = 1; i < adapter.getCount(); i++) {
                if (((MergeCursor) adapter.getItem(i)).getString(0).equals(artist))
                    spinnerArtist.setSelection(i);
            }
        }
        else {
            MatrixCursor extras = new MatrixCursor(new String[]{
                    SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_ID,
                    SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_DESCRIPTION
            });
            extras.addRow(new Object[]{0, ""});
            Cursor[] cursors = {extras, cursor};
            Cursor extendedCursor = new MergeCursor(cursors);
            SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
                    R.layout.spinner_element,
                    extendedCursor,
                    new String[]{SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_DESCRIPTION},
                    new int[]{R.id.text1},
                    SimpleCursorAdapter.NO_SELECTION);
            spinnerDocument.setAdapter(adapter);
            for (int i = 0; i < adapter.getCount(); i++) {
                if (((MergeCursor) adapter.getItem(i)).getString(0).equals(document))
                    spinnerDocument.setSelection(i);
            }
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == MainActivity.ARTISTS_LOADER)
            spinnerArtist.setAdapter(null);
        else
            spinnerDocument.setAdapter(null);
    }

    public Uri addSong(String title, String artist, String key, int tempo, int duration, String document, String notes) {
        ContentValues values = new ContentValues();
        values.put(SetlistsDbContract.SetlistsDbSongEntry.COLUMN_ID, UUID.randomUUID().toString());
        values.put(SetlistsDbContract.SetlistsDbSongEntry.COLUMN_TITLE, title);
        if (artist != null)
            values.put(SetlistsDbContract.SetlistsDbSongEntry.COLUMN_ARTIST, artist);
        values.put(SetlistsDbContract.SetlistsDbSongEntry.COLUMN_KEY, key.isEmpty() ? null : key);
        values.put(SetlistsDbContract.SetlistsDbSongEntry.COLUMN_TEMPO, tempo <= 0 ? null : tempo);
        values.put(SetlistsDbContract.SetlistsDbSongEntry.COLUMN_DURATION, duration <= 0 ? null : duration);
        values.put(SetlistsDbContract.SetlistsDbSongEntry.COLUMN_DOCUMENT, document);
        values.put(SetlistsDbContract.SetlistsDbSongEntry.COLUMN_NOTES, notes.isEmpty() ? null : notes);
        values.put(SetlistsDbContract.SetlistsDbSongEntry.COLUMN_DATE_ADDED, new Date().getTime() / 1000);
        values.put(SetlistsDbContract.SetlistsDbSongEntry.COLUMN_DATE_MODIFIED, new Date().getTime() / 1000);

        return getContentResolver().insert(SetlistsDbContract.SetlistsDbSongEntry.CONTENT_URI, values);
    }

    public int updateSong(String title, String artist, String key, int tempo, int duration, String document, String notes) {
        ContentValues values = new ContentValues();
        values.put(SetlistsDbContract.SetlistsDbSongEntry.COLUMN_TITLE, title);
        values.put(SetlistsDbContract.SetlistsDbSongEntry.COLUMN_ARTIST, artist);
        values.put(SetlistsDbContract.SetlistsDbSongEntry.COLUMN_KEY, key.isEmpty() ? null : key);
        values.put(SetlistsDbContract.SetlistsDbSongEntry.COLUMN_TEMPO, tempo <= 0 ? null : tempo);
        values.put(SetlistsDbContract.SetlistsDbSongEntry.COLUMN_DURATION, duration <= 0 ? null : duration);
        values.put(SetlistsDbContract.SetlistsDbSongEntry.COLUMN_DOCUMENT, document);
        values.put(SetlistsDbContract.SetlistsDbSongEntry.COLUMN_NOTES, notes.isEmpty() ? null : notes);
        values.put(SetlistsDbContract.SetlistsDbSongEntry.COLUMN_DATE_MODIFIED, new Date().getTime() / 1000);

        return getContentResolver().update(SetlistsDbContract.SetlistsDbSongEntry.buildSetlistsDbSongUri(id), values,
                SetlistsDbContract.SetlistsDbSongEntry.COLUMN_ID + "=?", new String[] {String.valueOf(id)});
    }
}
