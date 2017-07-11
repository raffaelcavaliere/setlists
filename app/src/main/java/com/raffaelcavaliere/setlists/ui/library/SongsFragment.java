package com.raffaelcavaliere.setlists.ui.library;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.raffaelcavaliere.setlists.R;
import com.raffaelcavaliere.setlists.data.SetlistsDbContract;
import com.raffaelcavaliere.setlists.data.SetlistsDbSongLoader;
import com.raffaelcavaliere.setlists.ui.MainActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;

public class SongsFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>, LibraryFragment.LibraryUpdateListener {

    private static final int REQUEST_ADD_SONG = 2000;
    private static final int REQUEST_EDIT_SONG = 2001;

    static final String ARG_ARTIST_ID = "artist";

    private OnFragmentInteractionListener mListener;
    private RecyclerView mRecyclerView;
    private TextView textNothingToShow;

    private long artistId = 0;
    private int selectedSong = 0;

    public SongsFragment() {
        // Required empty public constructor
    }

    public static SongsFragment newInstance() {
        SongsFragment fragment = new SongsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public static SongsFragment newInstance(long artistId) {
        SongsFragment fragment = new SongsFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_ARTIST_ID, artistId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            artistId = getArguments().getLong(ARG_ARTIST_ID);
        }
    }

    @Override
    public void onLibraryUpdate() {
        getActivity().getSupportLoaderManager().restartLoader(MainActivity.SONGS_LOADER, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_songs, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.song_list);
        FloatingActionButton fab = (FloatingActionButton)view.findViewById(R.id.fab_add_song);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent newSongIntent = new Intent(getActivity(), SongEditActivity.class);
                if (artistId > 0)
                    newSongIntent.putExtra("artist", artistId);
                startActivityForResult(newSongIntent, REQUEST_ADD_SONG);
            }
        });
        textNothingToShow = (TextView) view.findViewById(R.id.text_no_song);
        return view;
}

    @Override
    public void onResume() {
        super.onResume();
        getActivity().getSupportLoaderManager().restartLoader(MainActivity.SONGS_LOADER, null, this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ADD_SONG:
                if (resultCode == RESULT_OK) {
                    String returnedResult = data.getData().toString();
                    Log.d("RETURNED RESULT", returnedResult);
                }
                break;
            case REQUEST_EDIT_SONG:
                if (resultCode == RESULT_OK) {
                    Log.d("RETURNED RESULT", "OK UPDATE SONG");
                }
                break;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (getParentFragment() instanceof LibraryFragment)
            ((LibraryFragment)getParentFragment()).registerLibraryUpdateListener(this);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (getParentFragment() instanceof LibraryFragment)
            ((LibraryFragment)getParentFragment()).unregisterLibraryUpdateListener(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        if (artistId > 0)
            return SetlistsDbSongLoader.newInstanceForArtistId(getActivity(), artistId);
        return SetlistsDbSongLoader.newAllSongsInstance(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (cursorLoader.getId() == MainActivity.SONGS_LOADER) {
            if (cursor.getCount() <= 0)
                textNothingToShow.setVisibility(View.VISIBLE);
            else
                textNothingToShow.setVisibility(View.GONE);

            if (getActivity() instanceof ArtistSongsActivity)
                ((ArtistSongsActivity)getActivity()).setSubtitle(cursor.getCount() <= 0 ? "No song" : String.valueOf(cursor.getCount()) + " song" + (cursor.getCount() > 1 ? "s" : ""));

            SongsFragment.Adapter adapter = new SongsFragment.Adapter(cursor);
            adapter.setHasStableIds(true);
            mRecyclerView.setAdapter(adapter);
            final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            mRecyclerView.setLayoutManager(layoutManager);
            mRecyclerView.scrollToPosition(selectedSong);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == MainActivity.SONGS_LOADER)
            mRecyclerView.setAdapter(null);
    }

    public class Adapter extends RecyclerView.Adapter<SongsFragment.ViewHolder> {
        private Cursor mCursor;
        public Adapter(Cursor cursor) {
            mCursor = cursor;
        }

        @Override
        public long getItemId(int position) {
            mCursor.moveToPosition(position);
            return mCursor.getLong(0);
        }

        @Override
        public SongsFragment.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getActivity().getLayoutInflater().inflate(R.layout.list_item_details, parent, false);
            final SongsFragment.ViewHolder vh = new SongsFragment.ViewHolder(view);
            return vh;
        }

        @Override
        public void onBindViewHolder(SongsFragment.ViewHolder holder, int position) {
            mCursor.moveToPosition(position);
            holder.bindData(mCursor.getLong(0), mCursor.getString(1), mCursor.getLong(2), mCursor.getString(3),
                    mCursor.getString(4), mCursor.getInt(5), mCursor.getInt(6), mCursor.getString(7), mCursor.getLong(8),
                    new Date(mCursor.getLong(10) * 1000));
        }

        @Override
        public int getItemCount() {
            return mCursor.getCount();
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView textTitle;
        private TextView textArtist;
        private TextView textKey;
        private TextView textTempo;
        private TextView textDuration;
        private TextView textDateModified;
        private ImageButton btnMenu;

        private long id;
        private String title;
        private String artistName;
        private long artist;
        private String key;
        private int tempo;
        private int duration;
        private long document;
        private String notes;
        private Date dateModified;

        public void bindData(long id, String title, long artist, String artistName, String key, int tempo, int duration, String notes, long document, Date dateModified) {
            this.id = id;
            this.title = title == null ? "" : title;
            this.artist = artist;
            this.artistName = artistName == null ? "" : artistName;
            this.key = key == null ? "C" : key;
            this.tempo = tempo;
            this.duration = duration;
            this.document = document;
            this.notes = notes == null ? "" : notes;
            this.dateModified = dateModified;

            textTitle.setText(title);
            textArtist.setText(artistName);
            textTempo.setText(getResources().getString(R.string.bpm_note) + " " + String.valueOf(this.tempo));

            int minutes = (int)(this.duration / 60);
            int seconds = (int)(this.duration % 60);
            textDuration.setText(String.valueOf(minutes) + ":" + (seconds < 10 ? "0" : "") + String.valueOf(seconds));
            textDuration.setVisibility(View.VISIBLE);

            if (this.key != null) {
                textKey.setText(this.key);
                textKey.setVisibility(View.VISIBLE);
            }

            if (dateModified != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
                String dateModifiedFormatted = sdf.format(dateModified);
                textDateModified.setText(getResources().getString(R.string.modified_on) + " " + dateModifiedFormatted);
            }
        }

        public ViewHolder(View view) {
            super(view);
            textTitle = (TextView) view.findViewById(R.id.item_details_title);
            textArtist = (TextView) view.findViewById(R.id.item_details_subtitle);
            textTempo = (TextView) view.findViewById(R.id.item_details_comment_left);
            textKey = (TextView) view.findViewById(R.id.item_details_comment_center_left);
            textDuration = (TextView) view.findViewById(R.id.item_details_comment_center_right);
            textDateModified = (TextView) view.findViewById(R.id.item_details_comment_right);
            btnMenu = (ImageButton) view.findViewById(R.id.item_details_menu);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedSong = getAdapterPosition();
                    Intent documentsIntent = new Intent(v.getContext(), DocumentsActivity.class);
                    documentsIntent.putExtra("song", id);
                    documentsIntent.putExtra("title", title);
                    documentsIntent.putExtra("preferred", document);
                    documentsIntent.putExtra("tempo", tempo);
                    startActivity(documentsIntent);
                }
            });

            btnMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
                    popupMenu.getMenuInflater().inflate(R.menu.song_context_menu, popupMenu.getMenu());
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.song_context_menu_edit:
                                    selectedSong = getAdapterPosition();
                                    Intent editSongIntent = new Intent(v.getContext(), SongEditActivity.class);
                                    editSongIntent.putExtra("id", id);
                                    editSongIntent.putExtra("title", title);
                                    editSongIntent.putExtra("artist", artist);
                                    editSongIntent.putExtra("key", key);
                                    editSongIntent.putExtra("tempo", tempo);
                                    editSongIntent.putExtra("duration", duration);
                                    editSongIntent.putExtra("document", document);
                                    editSongIntent.putExtra("notes", notes);
                                    startActivityForResult(editSongIntent, REQUEST_EDIT_SONG);
                                    return true;
                                case R.id.song_context_menu_remove:
                                    selectedSong = getAdapterPosition();
                                    new AlertDialog.Builder(v.getContext())
                                            .setTitle(getResources().getString(R.string.menu_remove_song))
                                            .setMessage(getResources().getString(R.string.dialog_remove_song))
                                            .setIcon(android.R.drawable.ic_dialog_alert)
                                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int whichButton) {
                                                    if (getActivity().getContentResolver().delete(SetlistsDbContract.SetlistsDbSongEntry.buildSetlistsDbSongUri(id),
                                                            SetlistsDbContract.SetlistsDbSongEntry.COLUMN_ID + "=?", new String[] {String.valueOf(id)}) > 0)
                                                        if (getParentFragment() instanceof LibraryFragment)
                                                            ((LibraryFragment)getParentFragment()).libraryUpdated();
                                                        else
                                                            getActivity().getSupportLoaderManager().restartLoader(MainActivity.SONGS_LOADER, null, SongsFragment.this);
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
    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}