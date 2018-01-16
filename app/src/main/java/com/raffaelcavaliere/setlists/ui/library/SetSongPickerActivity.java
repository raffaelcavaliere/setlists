package com.raffaelcavaliere.setlists.ui.library;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.raffaelcavaliere.setlists.R;
import com.raffaelcavaliere.setlists.data.SetlistsDbContract;
import com.raffaelcavaliere.setlists.data.SetlistsDbSetSongLoader;
import com.raffaelcavaliere.setlists.data.SetlistsDbSongLoader;
import com.raffaelcavaliere.setlists.ui.MainActivity;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

public class SetSongPickerActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private RecyclerView mRecyclerView;

    private String set;
    private int sequence;
    private ArrayList<String> selectedSongs = new ArrayList<String>();
    private ArrayList<String> excludedSongs = new ArrayList<String>();
    private Button addButton;
    private Button cancelButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_song_picker);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRecyclerView = (RecyclerView) findViewById(R.id.set_song_picker_list);

        set = getIntent().getExtras().getString("set");
        sequence = getIntent().getExtras().getInt("sequence", 1);

        addButton = (Button) findViewById(R.id.song_picker_add);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent data = new Intent();
                    if (set != null) {
                        int result = addSetSongs();
                        setResult(RESULT_OK, data);
                    }
                    finish();
            }
        });

        cancelButton = (Button) findViewById(R.id.song_picker_cancel);
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
    public void onResume() {
        super.onResume();
        getSupportLoaderManager().restartLoader(MainActivity.SET_SONGS_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        if (i == MainActivity.SONGS_LOADER)
            return SetlistsDbSongLoader.newAllSongsInstance(this);
        else
            return SetlistsDbSetSongLoader.newInstanceForSetId(this, set);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (cursorLoader.getId() == MainActivity.SONGS_LOADER) {
            SetSongPickerActivity.Adapter adapter = new SetSongPickerActivity.Adapter(cursor);
            mRecyclerView.setAdapter(adapter);
            final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            mRecyclerView.setLayoutManager(layoutManager);
        }
        else if (cursorLoader.getId() == MainActivity.SET_SONGS_LOADER) {
            if (cursor.moveToFirst()) {
                do {
                    excludedSongs.add(cursor.getString(2));
                } while (cursor.moveToNext());
            }
            getSupportLoaderManager().restartLoader(MainActivity.SONGS_LOADER, null, this);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == MainActivity.SONGS_LOADER)
            mRecyclerView.setAdapter(null);
    }

    private class Adapter extends RecyclerView.Adapter<SetSongPickerActivity.ViewHolder> {
        private Cursor mCursor;

        public Adapter(Cursor cursor) {
            mCursor = cursor;
        }

        @Override
        public SetSongPickerActivity.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.list_item_checkable, parent, false);
            final SetSongPickerActivity.ViewHolder vh = new SetSongPickerActivity.ViewHolder(view);
            return vh;
        }

        @Override
        public void onBindViewHolder(SetSongPickerActivity.ViewHolder holder, int position) {
            mCursor.moveToPosition(position);
            holder.bindData(mCursor.getString(0), mCursor.getString(1), mCursor.getString(2), mCursor.getString(3),
                    new Date(mCursor.getLong(4) * 1000), mCursor.getInt(5), excludedSongs.contains(mCursor.getLong(0)));
        }

        @Override
        public int getItemCount() {
            return mCursor.getCount();
        }
    }

    private class ViewHolder extends RecyclerView.ViewHolder {
        private TextView textTitle;
        private TextView textArtist;
        private CheckBox checkSelect;

        private String id;
        private String title;
        private String artistName;
        private String artist;
        private boolean disabled = false;

        public void bindData(String id, String title, String artist, String artistName, Date dateModified, int versionCount, boolean disabled) {
            this.id = id;
            this.title = title == null ? "" : title;
            this.artist = artist;
            this.artistName = artistName == null ? "" : artistName;
            this.disabled = disabled;

            textTitle.setText(title);
            textArtist.setText(artistName);
            checkSelect.setChecked(selectedSongs.contains(id));
            checkSelect.setEnabled(!this.disabled);
        }

        public ViewHolder(View view) {
            super(view);
            textTitle = (TextView) view.findViewById(R.id.item_checkable_title);
            textArtist = (TextView) view.findViewById(R.id.item_checkable_subtitle);
            checkSelect = (CheckBox) view.findViewById(R.id.item_checkable_checkbox);

            checkSelect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked && !selectedSongs.contains(id))
                        selectedSongs.add(id);
                    else if (!isChecked && selectedSongs.contains(id))
                        selectedSongs.remove(id);
                }
            });
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!disabled)
                        checkSelect.toggle();
                }
            });
        }
    }

    private int addSetSongs() {
        for (int i = 0; i < selectedSongs.size(); i++) {
            ContentValues values = new ContentValues();
            values.put(SetlistsDbContract.SetlistsDbSetSongEntry.COLUMN_ID, UUID.randomUUID().toString());
            values.put(SetlistsDbContract.SetlistsDbSetSongEntry.COLUMN_SEQUENCE, sequence);
            values.put(SetlistsDbContract.SetlistsDbSetSongEntry.COLUMN_SETLIST, set);
            values.put(SetlistsDbContract.SetlistsDbSetSongEntry.COLUMN_SONG, selectedSongs.get(i));
            values.put(SetlistsDbContract.SetlistsDbSetSongEntry.COLUMN_DATE_ADDED, new Date().getTime() / 1000);
            values.put(SetlistsDbContract.SetlistsDbSetSongEntry.COLUMN_DATE_MODIFIED, new Date().getTime() / 1000);

            Uri uri = getContentResolver().insert(SetlistsDbContract.SetlistsDbSetSongEntry.CONTENT_URI, values);
            sequence++;
        }
        return 0;
    }
}
