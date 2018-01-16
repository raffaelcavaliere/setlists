package com.raffaelcavaliere.setlists.ui.library;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.raffaelcavaliere.setlists.R;
import com.raffaelcavaliere.setlists.data.SetlistsDbContract;
import com.raffaelcavaliere.setlists.data.SetlistsDbItemSetSong;
import com.raffaelcavaliere.setlists.data.SetlistsDbSetSongLoader;
import com.raffaelcavaliere.setlists.ui.MainActivity;
import com.raffaelcavaliere.setlists.ui.document.SwipeViewerActivity;
import com.raffaelcavaliere.setlists.utils.ItemTouchHelperAdapter;
import com.raffaelcavaliere.setlists.utils.ItemTouchHelperViewHolder;
import com.raffaelcavaliere.setlists.utils.OnStartDragListener;
import com.raffaelcavaliere.setlists.utils.SimpleItemTouchHelperCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import static java.security.AccessController.getContext;

public class SetSongsActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>, OnStartDragListener {

    private Toolbar toolbar;
    private RecyclerView mRecyclerView;
    private SetSongsActivity.Adapter mAdapter;
    private ItemTouchHelper mItemTouchHelper;
    private TextView textNothingToShow;
    private FloatingActionButton fab;
    private String set;
    private Menu menu;
    private String name;
    private int position = 0;

    private static final int REQUEST_ADD_SONG = 4000;
    private static final int REQUEST_EDIT_SONG = 4001;

    private static final int LIST_MODE_NORMAL = 0;
    private static final int LIST_MODE_REORDER = 1;
    private int listMode = LIST_MODE_NORMAL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_songs);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle extras = getIntent().getExtras();
        name = extras.getString("name");
        setTitle(name);

        set = extras.getString("set");

        mRecyclerView = (RecyclerView) findViewById(R.id.set_song_list);
        mAdapter = new SetSongsActivity.Adapter(this);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mAdapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);

        fab = (FloatingActionButton)findViewById(R.id.fab_add_set_song);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent setSongPickerIntent = new Intent(view.getContext(), SetSongPickerActivity.class);
                setSongPickerIntent.putExtra("set", set);
                setSongPickerIntent.putExtra("sequence", mAdapter.getItemCount() + 1);
                startActivityForResult(setSongPickerIntent, REQUEST_ADD_SONG);
            }
        });
        textNothingToShow = (TextView) findViewById(R.id.text_no_song);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.set_songs_menu, menu);
        this.menu = menu;
        if (this.listMode == LIST_MODE_NORMAL) {
            menu.getItem(0).setVisible(true);
            menu.getItem(1).setVisible(false);
        }
        else if (this.listMode == LIST_MODE_REORDER) {
            menu.getItem(0).setVisible(false);
            menu.getItem(1).setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.set_songs_menu_reorder) {
            listMode = LIST_MODE_REORDER;
        } else if (id == R.id.set_songs_menu_done) {
            for (int i = 0; i < mAdapter.getItemCount(); i++) {
                ContentValues values = new ContentValues();
                values.put(SetlistsDbContract.SetlistsDbSetSongEntry.COLUMN_SEQUENCE, i+1);
                values.put(SetlistsDbContract.SetlistsDbSetSongEntry.COLUMN_DATE_MODIFIED, new Date().getTime() / 1000);
                int result = getContentResolver().update(
                        SetlistsDbContract.SetlistsDbSetSongEntry.buildSetlistsDbSetSongUri(mAdapter.getItemUuid(i)),
                        values,
                        SetlistsDbContract.SetlistsDbSetSongEntry.COLUMN_ID + "=?",
                        new String[] {String.valueOf(mAdapter.getItemId(i))});
            }
            getSupportLoaderManager().restartLoader(MainActivity.SET_SONGS_LOADER, null, this);
            listMode = LIST_MODE_NORMAL;
        }
        if (this.listMode == LIST_MODE_NORMAL) {
            menu.getItem(0).setVisible(true);
            menu.getItem(1).setVisible(false);
            fab.setVisibility(View.VISIBLE);
        }
        else if (this.listMode == LIST_MODE_REORDER) {
            menu.getItem(0).setVisible(false);
            menu.getItem(1).setVisible(true);
            fab.setVisibility(View.INVISIBLE);
        }
        mAdapter.notifyDataSetChanged();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }

    @Override
    public void onResume() {
        super.onResume();
        getSupportLoaderManager().restartLoader(MainActivity.SET_SONGS_LOADER, null, this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ADD_SONG:
                if (resultCode == RESULT_OK) {
                    Log.d("RETURNED RESULT", "OK ADD SONG");
                }
                break;
            case REQUEST_EDIT_SONG:
                if (resultCode == RESULT_OK) {
                    Log.d("RETURNED RESULT", "OK UPDATE VERSION");
                }
                break;
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return SetlistsDbSetSongLoader.newInstanceForSetId(this, set);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (cursorLoader.getId() == MainActivity.SET_SONGS_LOADER) {
            if (cursor.getCount() <= 0)
                textNothingToShow.setVisibility(View.VISIBLE);
            else
                textNothingToShow.setVisibility(View.GONE);
            toolbar.setSubtitle(cursor.getCount() <= 0 ? "No song" : String.valueOf(cursor.getCount()) + " song" + (cursor.getCount() > 1 ? "s" : ""));

            ArrayList<SetlistsDbItemSetSong> items = new ArrayList<>();
            int sequence = 1;
            if (cursor.moveToFirst()) {
                do {
                    items.add(new SetlistsDbItemSetSong(cursor.getString(0), cursor.getString(1), cursor.getString(2), sequence,
                            cursor.getString(4), cursor.getInt(5), cursor.getString(6), cursor.getString(7),
                            cursor.getString(8), cursor.getString(9), cursor.getInt(10), cursor.getInt(11), cursor.getInt(12),
                            cursor.getInt(13), cursor.getString(14)));
                    sequence++;
                }
                while (cursor.moveToNext());
            }
            this.mAdapter.setData(items);
            mRecyclerView.setAdapter(mAdapter);
            mRecyclerView.scrollToPosition(position);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == MainActivity.SET_SONGS_LOADER)
            mRecyclerView.setAdapter(null);
    }

    private class Adapter extends RecyclerView.Adapter<ViewHolder>  implements ItemTouchHelperAdapter {

        private final ArrayList<SetlistsDbItemSetSong> items = new ArrayList<>();
        private final OnStartDragListener mDragStartListener;

        public Adapter(OnStartDragListener dragStartListener) {
            this.mDragStartListener = dragStartListener;
        }

        public void setData(ArrayList<SetlistsDbItemSetSong> items) {
            this.items.clear();
            this.items.addAll(items);
        }

        public String getItemUuid(int position) {
            return items.get(position).getId();
        }

        public SetlistsDbItemSetSong get(int position) {
            return items.get(position);
        }

        @Override
        public SetSongsActivity.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.list_item_details_numbered, parent, false);
            final SetSongsActivity.ViewHolder vh = new SetSongsActivity.ViewHolder(view);
            return vh;
        }

        @Override
        public void onBindViewHolder(final SetSongsActivity.ViewHolder holder, int position) {
            Log.d("POSITION", String.valueOf(position));
            SetlistsDbItemSetSong item = items.get(position);
            holder.bindData(item.getId(), item.getSong(), item.getSequence(), item.getDocument(), item.getType(),
                    item.getTitle(), item.getDescription(), item.getKey(), item.getDuration(), item.getTempo(),
                    item.getTranspose(), item.getTranposeMode(), item.getArtist(), item.getNotes());
            holder.handleView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                        mDragStartListener.onStartDrag(holder);
                    }
                    return false;
                }
            });
            if (menu != null)
                holder.setListMode(listMode);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        @Override
        public void onItemClear(int position) {
            notifyDataSetChanged();
        }

        @Override
        public boolean onItemMove(int fromPosition, int toPosition) {
            Collections.swap(items, fromPosition, toPosition);
            notifyItemMoved(fromPosition, toPosition);
            for (int i = 0; i < items.size(); i++) {
                items.get(i).setSequence(i + 1);
            }
            return true;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements
            ItemTouchHelperViewHolder {

        private final TextView textTitle;
        private final TextView textDescription;
        private final TextView textKey;
        private final TextView textTempo;
        private final TextView textDuration;
        private final TextView textSequence;
        private final ImageView handleView;
        private final ImageButton btnMenu;

        private String id;
        private String song;
        private String document;
        private String artist;
        private String title;
        private String description;
        private String key;
        private String notes;
        private int sequence;
        private int tempo;
        private int duration;
        private int type;
        private int transpose;
        private int transposeMode;

        public void bindData(String id, String song, int sequence, String document, int type, String title, String description, String key, int duration, int tempo, int transpose, int transposeMode, String artist, String notes) {
            this.id = id;
            this.song = song;
            this.document = document;
            this.artist = artist;
            this.title = title == null ? "" : title;
            this.description = description == null ? "" : description;
            this.key = key == null ? "C" : key;
            this.duration = duration;
            this.tempo = tempo;
            this.sequence = sequence;
            this.type = type;
            this.transpose = transpose;
            this.transposeMode = transposeMode;
            this.notes = notes == null ? "" : notes;

            textTitle.setText(this.title);

            if (this.document != null) {
                textDescription.setText(this.description);
                textDescription.setTextColor(getColor(R.color.black));
                textDescription.setTypeface(null, Typeface.NORMAL);
            } else {
                textDescription.setText(getResources().getText(R.string.no_document));
                textDescription.setTextColor(getColor(R.color.colorAccent));
                textDescription.setTypeface(null, Typeface.ITALIC);
            }

            textTempo.setText(getResources().getString(R.string.bpm_note) + " " + String.valueOf(this.tempo));
            int minutes = (int) (this.duration / 60);
            int seconds = (int) (this.duration % 60);
            textDuration.setText(String.valueOf(minutes) + ":" + (seconds < 10 ? "0" : "") + String.valueOf(seconds));
            textDuration.setVisibility(View.VISIBLE);

            if (this.key != null) {
                textKey.setText(this.key);
                textKey.setVisibility(View.VISIBLE);
            }

            textSequence.setText(String.valueOf(this.sequence));
        }

        @Override
        public void onItemSelected() {
            TypedValue outValue = new TypedValue();
            getTheme().resolveAttribute(android.R.attr.colorBackgroundFloating, outValue, true);
            itemView.setBackgroundResource(outValue.resourceId);
        }

        @Override
        public void onItemClear() {
            TypedValue outValue = new TypedValue();
            getTheme().resolveAttribute(android.R.attr.colorBackground, outValue, true);
            itemView.setBackgroundResource(outValue.resourceId);
        }

        public void setListMode(int mode) {
            this.handleView.setVisibility(mode == LIST_MODE_REORDER ? View.VISIBLE : View.INVISIBLE);
            this.btnMenu.setVisibility(mode == LIST_MODE_NORMAL ? View.VISIBLE : View.INVISIBLE);
        }

        public ViewHolder(View view) {
            super(view);
            textTitle = (TextView) view.findViewById(R.id.item_details_numbered_title);
            textDescription = (TextView) view.findViewById(R.id.item_details_numbered_subtitle);
            textTempo = (TextView) view.findViewById(R.id.item_details_numbered_comment_left);
            textKey = (TextView) view.findViewById(R.id.item_details_numbered_comment_center_left);
            textDuration = (TextView) view.findViewById(R.id.item_details_numbered_comment_center_right);
            textSequence = (TextView) view.findViewById(R.id.item_details_numbered_sequence);
            handleView = (ImageView) view.findViewById(R.id.item_details_numbered_handle);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listMode == LIST_MODE_NORMAL) {
                        position = getAdapterPosition();
                        Intent swipeIntent = new Intent(v.getContext(), SwipeViewerActivity.class);
                        swipeIntent.putExtra("song", song);
                        swipeIntent.putExtra("title", name);
                        swipeIntent.putExtra("set", set);
                        swipeIntent.putExtra("position", getAdapterPosition());
                        startActivity(swipeIntent);
                    }
                }
            });

            btnMenu = (ImageButton) view.findViewById(R.id.item_details_numbered_menu);

            btnMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
                    popupMenu.getMenuInflater().inflate(R.menu.set_song_context_menu, popupMenu.getMenu());
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.set_song_context_menu_edit:
                                    position = getAdapterPosition();
                                    Intent editSongIntent = new Intent(v.getContext(), SongEditActivity.class);
                                    editSongIntent.putExtra("id", song);
                                    editSongIntent.putExtra("title", title);
                                    editSongIntent.putExtra("artist", artist);
                                    editSongIntent.putExtra("key", key);
                                    editSongIntent.putExtra("tempo", tempo);
                                    editSongIntent.putExtra("duration", duration);
                                    editSongIntent.putExtra("document", document);
                                    editSongIntent.putExtra("notes", notes);
                                    startActivityForResult(editSongIntent, REQUEST_EDIT_SONG);
                                    return true;
                                case R.id.set_song_context_menu_remove:
                                    position = getAdapterPosition();
                                    new AlertDialog.Builder(v.getContext())
                                            .setTitle(getResources().getString(R.string.menu_remove_set_song))
                                            .setMessage(getResources().getString(R.string.dialog_remove_set_song))
                                            .setIcon(android.R.drawable.ic_dialog_alert)
                                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int whichButton) {
                                                    if (getContentResolver().delete(SetlistsDbContract.SetlistsDbSetSongEntry.buildSetlistsDbSetSongUri(id),
                                                            SetlistsDbContract.SetlistsDbSetSongEntry.COLUMN_ID + "=?", new String[] {String.valueOf(id)}) > 0)
                                                        getSupportLoaderManager().restartLoader(MainActivity.SET_SONGS_LOADER, null, SetSongsActivity.this);
                                                }})
                                            .setNegativeButton(android.R.string.no, null)
                                            .show();
                                    return true;
                                default:
                                    return false;
                            }
                        }
                    });
                    popupMenu.show();
                }
            });
        }
    }
}
