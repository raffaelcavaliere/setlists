package com.raffaelcavaliere.setlists.ui.library;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.raffaelcavaliere.setlists.R;
import com.raffaelcavaliere.setlists.data.SetlistsDbContract;

public class ArtistEditActivity extends AppCompatActivity {

    private TextView txtName, txtUrl;
    private Button saveButton;
    private Button cancelButton;
    private long id = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_edit);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle extras = getIntent().getExtras();

        if (extras != null)
            id = getIntent().getExtras().getLong("id", 0);

        txtName = (TextView) findViewById(R.id.editArtistName);
        if (extras != null) {
            String name = extras.getString("name", "");
            txtName.setText(name);
        }

        txtUrl = (TextView) findViewById(R.id.editArtistPhotoUrl);
        if (extras != null) {
            String photo = extras.getString("photo", "");
            txtUrl.setText(photo);
        }

        saveButton = (Button) findViewById(R.id.editArtistSave);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (txtName.getText().length() > 0) {
                    Intent data = new Intent();
                    if (id > 0) {
                        int result = updateArtist(txtName.getText().toString(), txtUrl.getText().toString());
                        setResult(RESULT_OK, data);
                    } else {
                        Uri result = addArtist(txtName.getText().toString(), txtUrl.getText().toString());
                        data.setData(result);
                        setResult(RESULT_OK, data);
                    }

                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), "Please provide a name", Toast.LENGTH_SHORT).show();
                }

            }
        });

        cancelButton = (Button) findViewById(R.id.editArtistCancel);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ImageButton searchPhotoButton = (ImageButton)findViewById(R.id.editArtistSearchPhoto);
        searchPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment dialog = new PhotoDialogFragment();
                Bundle args = new Bundle();
                args.putString("search", txtName.getText().toString());
                dialog.setArguments(args);
                dialog.show(getSupportFragmentManager(), "dialog");
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


    public void onPhotoDialogResult(String url) {
        txtUrl.setText(url.toString());
    }

    public Uri addArtist(String name, String photo) {
        ContentValues values = new ContentValues();
        values.put(SetlistsDbContract.SetlistsDbArtistEntry.COLUMN_NAME, name);
        values.put(SetlistsDbContract.SetlistsDbArtistEntry.COLUMN_PHOTO, photo);

        return getContentResolver().insert(SetlistsDbContract.SetlistsDbArtistEntry.CONTENT_URI, values);
    }

    public int updateArtist(String name, String photo) {
        ContentValues values = new ContentValues();
        values.put(SetlistsDbContract.SetlistsDbArtistEntry.COLUMN_NAME, name);
        values.put(SetlistsDbContract.SetlistsDbArtistEntry.COLUMN_PHOTO, photo);

        return getContentResolver().update(SetlistsDbContract.SetlistsDbArtistEntry.buildSetlistsDbArtistUri(id), values,
                SetlistsDbContract.SetlistsDbArtistEntry.COLUMN_ID + "=?", new String[] {String.valueOf(id)});
    }
}
