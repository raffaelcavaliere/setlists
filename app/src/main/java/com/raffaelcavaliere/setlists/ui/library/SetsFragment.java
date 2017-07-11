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
import com.raffaelcavaliere.setlists.data.SetlistsDbSetLoader;
import com.raffaelcavaliere.setlists.ui.MainActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;

public class SetsFragment extends Fragment implements
    LoaderManager.LoaderCallbacks<Cursor>, LibraryFragment.LibraryUpdateListener {

    private static final int REQUEST_ADD_SET = 1000;
    private static final int REQUEST_EDIT_SET = 1001;

    private SetsFragment.OnFragmentInteractionListener mListener;
    private RecyclerView mRecyclerView;
    private TextView textNothingToShow;
    private int selectedSet = 0;

    public SetsFragment() {
    }

    public static SetsFragment newInstance() {
        SetsFragment fragment = new SetsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onLibraryUpdate() {
        getActivity().getSupportLoaderManager().restartLoader(MainActivity.SETS_LOADER, null, this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            ;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sets, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.set_list);

        FloatingActionButton fab = (FloatingActionButton)view.findViewById(R.id.fab_add_set);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent newSetIntent = new Intent(getActivity(), SetEditActivity.class);
                startActivityForResult(newSetIntent, REQUEST_ADD_SET);
            }
        });
        textNothingToShow = (TextView) view.findViewById(R.id.text_no_set);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().getSupportLoaderManager().restartLoader(MainActivity.SETS_LOADER, null, this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ADD_SET:
                if (resultCode == RESULT_OK) {
                    String returnedResult = data.getData().toString();
                    Log.d("RETURNED RESULT", returnedResult);
                }
                break;
            case REQUEST_EDIT_SET:
                if (resultCode == RESULT_OK) {
                    Log.d("RETURNED RESULT", "OK UPDATE SET");
                }
                break;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (getParentFragment() instanceof LibraryFragment)
            ((LibraryFragment)getParentFragment()).registerLibraryUpdateListener(this);
        if (context instanceof SetsFragment.OnFragmentInteractionListener) {
            mListener = (SetsFragment.OnFragmentInteractionListener) context;
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
        return SetlistsDbSetLoader.newAllSetsInstance(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (cursor.getCount() <= 0)
            textNothingToShow.setVisibility(View.VISIBLE);
        else {
            textNothingToShow.setVisibility(View.GONE);
        }
        SetsFragment.Adapter adapter = new SetsFragment.Adapter(cursor);
        adapter.setHasStableIds(true);
        mRecyclerView.setAdapter(adapter);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.scrollToPosition(selectedSet);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mRecyclerView.setAdapter(null);
    }

    private class Adapter extends RecyclerView.Adapter<SetsFragment.ViewHolder> {
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
        public SetsFragment.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getActivity().getLayoutInflater().inflate(R.layout.list_item_details, parent, false);
            final SetsFragment.ViewHolder vh = new SetsFragment.ViewHolder(view);
            return vh;
        }

        @Override
        public void onBindViewHolder(SetsFragment.ViewHolder holder, int position) {
            mCursor.moveToPosition(position);
            holder.bindData(mCursor.getLong(0), mCursor.getString(6), mCursor.getString(1), mCursor.getString(2), mCursor.getLong(3), mCursor.getString(7), mCursor.getInt(8), mCursor.getInt(9), new Date(mCursor.getLong(5) * 1000));
        }

        @Override
        public int getItemCount() {
            return mCursor.getCount();
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView textName;
        private TextView textSongCount;
        private TextView textBand;
        private TextView textDuration;
        private TextView textDateModified;
        private ImageButton btnMenu;

        private long id;
        private String name;
        private int songCount;
        private int duration;
        private long band;
        private String location;
        private String author;
        private String bandName;
        private Date dateModified;

        public void bindData(long id, String author, String name, String location, long band, String bandName, int songCount, int duration, Date dateModified) {
            this.id = id;
            this.name = name == null ? "" : name;
            this.author = author == null ? "" : author;
            this.location = location == null ? "" : location;
            this.band = band;
            this.bandName = bandName == null ? "" : bandName;
            this.songCount = songCount;
            this.duration = duration;
            this.dateModified = dateModified;
            textName.setText(name);
            textSongCount.setText(songCount <= 0 ? "No song" : songCount + " song" + (songCount > 1 ? "s" : ""));
            textBand.setText(bandName);

            int minutes = (int)(this.duration / 60);
            int seconds = (int)(this.duration % 60);
            textDuration.setText(String.valueOf(minutes) + ":" + (seconds < 10 ? "0" : "") + String.valueOf(seconds));
            textDuration.setVisibility(View.VISIBLE);

            if (dateModified != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
                String dateModifiedFormatted = sdf.format(dateModified);
                textDateModified.setText(getResources().getString(R.string.modified_on) + " " + dateModifiedFormatted);
            }
        }

        public ViewHolder(View view) {
            super(view);
            textName = (TextView) view.findViewById(R.id.item_details_title);
            textBand = (TextView) view.findViewById(R.id.item_details_subtitle);
            textSongCount = (TextView) view.findViewById(R.id.item_details_comment_left);
            textDuration = (TextView) view.findViewById(R.id.item_details_comment_center_left);
            textDateModified = ((TextView) view.findViewById(R.id.item_details_comment_right));

            btnMenu = (ImageButton) view.findViewById(R.id.item_details_menu);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedSet = getAdapterPosition();
                    Intent setSongsIntent = new Intent(v.getContext(), SetSongsActivity.class);
                    setSongsIntent.putExtra("set", id);
                    setSongsIntent.putExtra("name", name);
                    startActivity(setSongsIntent);
                }
            });

            btnMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
                    popupMenu.getMenuInflater().inflate(R.menu.set_context_menu, popupMenu.getMenu());
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.set_context_menu_share:
                                    ArrayList<String> emailList = new ArrayList<>();
                                    Cursor musicianCursor = getActivity().getContentResolver().query(
                                            SetlistsDbContract.SetlistsDbMusicianEntry.buildSetlistsDbBandMusiciansUri(band), null, null, null, null);
                                    if (musicianCursor != null && musicianCursor.getCount() > 0) {
                                        musicianCursor.moveToFirst();
                                        do {
                                            if (musicianCursor.getString(2).length() > 0)
                                                emailList.add(musicianCursor.getString(2));
                                        } while (musicianCursor.moveToNext());
                                    }
                                    if (musicianCursor != null)
                                        musicianCursor.close();

                                    String[] emails = new String[emailList.size()];
                                    for (int i = 0; i < emailList.size(); i++)
                                        emails[i] = emailList.get(i);

                                    ArrayList<String> songs = new ArrayList<>();
                                    Cursor songCursor = getActivity().getContentResolver().query(
                                            SetlistsDbContract.SetlistsDbSetSongEntry.buildSetlistsDbSetSongForSetUri(id), null, null, null, null);
                                    if (songCursor != null && songCursor.getCount() > 0) {
                                        songCursor.moveToFirst();
                                        do {
                                            songs.add(songCursor.getString(7) + " - " + songCursor.getString(9));
                                        } while (songCursor.moveToNext());
                                    }
                                    if (songCursor != null)
                                        songCursor.close();

                                    String body = "";
                                    for (int i = 0; i < songs.size(); i++)
                                        body += String.valueOf(i + 1) + " - " + songs.get(i) + (i < songs.size() - 1 ? "\n" : "");

                                    Intent emailBandIntent = new Intent();
                                    emailBandIntent.setAction(Intent.ACTION_SEND);
                                    emailBandIntent.setType("message/rfc822");
                                    emailBandIntent.putExtra(Intent.EXTRA_EMAIL, emails);
                                    emailBandIntent.putExtra(Intent.EXTRA_SUBJECT, name);
                                    emailBandIntent.putExtra(Intent.EXTRA_TEXT, body);
                                    startActivity(Intent.createChooser(emailBandIntent, "Send email"));
                                    return true;
                                case R.id.set_context_menu_edit:
                                    selectedSet = getAdapterPosition();
                                    Intent editSetIntent = new Intent(v.getContext(), SetEditActivity.class);
                                    editSetIntent.putExtra("id", id);
                                    editSetIntent.putExtra("name", name);
                                    editSetIntent.putExtra("location", location);
                                    editSetIntent.putExtra("band", band);
                                    startActivityForResult(editSetIntent, REQUEST_EDIT_SET);
                                    return true;
                                case R.id.set_context_menu_remove:
                                    selectedSet = getAdapterPosition();
                                    new AlertDialog.Builder(v.getContext())
                                            .setTitle(getResources().getString(R.string.menu_remove_set))
                                            .setMessage(getResources().getString(R.string.dialog_remove_set))
                                            .setIcon(android.R.drawable.ic_dialog_alert)
                                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int whichButton) {
                                                    if (getActivity().getContentResolver().delete(SetlistsDbContract.SetlistsDbSetEntry.buildSetlistsDbSetUri(id),
                                                            SetlistsDbContract.SetlistsDbSetEntry.COLUMN_ID + "=?", new String[] {String.valueOf(id)}) > 0) {
                                                        if (getParentFragment() instanceof LibraryFragment)
                                                            ((LibraryFragment)getParentFragment()).libraryUpdated();
                                                        else
                                                            getActivity().getSupportLoaderManager().restartLoader(MainActivity.SETS_LOADER, null, SetsFragment.this);
                                                    }
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
