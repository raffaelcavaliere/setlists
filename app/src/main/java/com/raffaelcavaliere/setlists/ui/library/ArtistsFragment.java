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
import android.widget.ImageView;
import android.widget.TextView;

import com.raffaelcavaliere.setlists.R;
import com.raffaelcavaliere.setlists.data.SetlistsDbArtistLoader;
import com.raffaelcavaliere.setlists.data.SetlistsDbContract;
import com.raffaelcavaliere.setlists.ui.MainActivity;
import com.raffaelcavaliere.setlists.utils.CircleTransform;
import com.squareup.picasso.Picasso;

import static android.app.Activity.RESULT_OK;

public class ArtistsFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>, LibraryFragment.LibraryUpdateListener {

    private static final int REQUEST_ADD_ARTIST = 1000;
    private static final int REQUEST_EDIT_ARTIST = 1001;

    private OnFragmentInteractionListener mListener;
    private RecyclerView mRecyclerView;
    private TextView textNothingToShow;
    private int selectedArtist = 0;

    public ArtistsFragment() {
        // Required empty public constructor
    }

    public static ArtistsFragment newInstance() {
        ArtistsFragment fragment = new ArtistsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            ;
        }
    }

    @Override
    public void onLibraryUpdate() {
        getActivity().getSupportLoaderManager().restartLoader(MainActivity.ARTISTS_LOADER, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_artists, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.artist_list);

        FloatingActionButton fab = (FloatingActionButton)view.findViewById(R.id.fab_add_artist);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent newArtistIntent = new Intent(getActivity(), ArtistEditActivity.class);
                startActivityForResult(newArtistIntent, REQUEST_ADD_ARTIST);
            }
        });
        textNothingToShow = (TextView) view.findViewById(R.id.text_no_artist);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().getSupportLoaderManager().restartLoader(MainActivity.ARTISTS_LOADER, null, this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ADD_ARTIST:
                if (resultCode == RESULT_OK) {
                    String returnedResult = data.getData().toString();
                    Log.d("RETURNED RESULT", returnedResult);
                }
                break;
            case REQUEST_EDIT_ARTIST:
                if (resultCode == RESULT_OK) {
                    Log.d("RETURNED RESULT", "OK UPDATE ARTIST");
                }
                break;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (getParentFragment() instanceof LibraryFragment)
            ((LibraryFragment)getParentFragment()).registerLibraryUpdateListener(this);
        if (context instanceof ArtistsFragment.OnFragmentInteractionListener) {
            mListener = (ArtistsFragment.OnFragmentInteractionListener) context;
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
        return SetlistsDbArtistLoader.newAllArtistsInstance(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        Adapter adapter = new Adapter(cursor);
        if (cursor.getCount() <= 0)
            textNothingToShow.setVisibility(View.VISIBLE);
        else
            textNothingToShow.setVisibility(View.GONE);
        adapter.setHasStableIds(true);
        mRecyclerView.setAdapter(adapter);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.scrollToPosition(selectedArtist);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mRecyclerView.setAdapter(null);
    }

    private class Adapter extends RecyclerView.Adapter<ViewHolder> {
        private Cursor mCursor;
        public Adapter(Cursor cursor) {
            mCursor = cursor;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getActivity().getLayoutInflater().inflate(R.layout.list_item_picture, parent, false);
            final ViewHolder vh = new ViewHolder(view);
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            mCursor.moveToPosition(position);
            holder.bindData(mCursor.getString(0), mCursor.getString(1), mCursor.getString(2), mCursor.getInt(3));
        }

        @Override
        public int getItemCount() {
            return mCursor.getCount();
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView textName;
        private TextView textSongCount;
        private ImageView imgPhoto;
        private ImageButton btnMenu;

        private String id;
        private String name;
        private String photoUrl;
        private int songCount;

        public void bindData(String id, String name, String photoUrl, int songCount) {
            this.id = id;
            this.name = name;
            this.photoUrl = photoUrl == null ? "" : photoUrl;
            this.songCount = songCount;
            textName.setText(name);
            textSongCount.setText(songCount <= 0 ? "No song" : songCount + " song" + (songCount > 1 ? "s" : ""));
            if (this.photoUrl != null && !this.photoUrl.isEmpty()) {
                Picasso.with(getContext()).load(this.photoUrl).transform(new CircleTransform()).into(imgPhoto);
            }
        }

        public ViewHolder(View view) {
            super(view);
            textName = (TextView) view.findViewById(R.id.item_picture_title);
            textSongCount = (TextView) view.findViewById(R.id.item_picture_subtitle);
            imgPhoto = (ImageView) view.findViewById(R.id.item_picture_photo);
            btnMenu = (ImageButton) view.findViewById(R.id.item_picture_menu);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedArtist = getAdapterPosition();
                    Intent artistSongsIntent = new Intent(v.getContext(), ArtistSongsActivity.class);
                    artistSongsIntent.putExtra("artist", id);
                    artistSongsIntent.putExtra("name", name);
                    startActivity(artistSongsIntent);
                }
            });

            btnMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
                    popupMenu.getMenuInflater().inflate(R.menu.artist_context_menu, popupMenu.getMenu());
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.artist_context_menu_edit:
                                    selectedArtist = getAdapterPosition();
                                    Intent editArtistIntent = new Intent(v.getContext(), ArtistEditActivity.class);
                                    editArtistIntent.putExtra("id", id);
                                    editArtistIntent.putExtra("name", name);
                                    editArtistIntent.putExtra("photo", photoUrl);
                                    startActivityForResult(editArtistIntent, REQUEST_EDIT_ARTIST);
                                    return true;
                                case R.id.artist_context_menu_remove:
                                    selectedArtist = getAdapterPosition();
                                    new AlertDialog.Builder(v.getContext())
                                            .setTitle(getResources().getString(R.string.menu_remove_artist))
                                            .setMessage(getResources().getString(R.string.dialog_remove_artist))
                                            .setIcon(android.R.drawable.ic_dialog_alert)
                                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int whichButton) {
                                                    if (getActivity().getContentResolver().delete(SetlistsDbContract.SetlistsDbArtistEntry.buildSetlistsDbArtistUri(id),
                                                            SetlistsDbContract.SetlistsDbArtistEntry.COLUMN_ID + "=?", new String[] {String.valueOf(id)}) > 0) {
                                                        if (getParentFragment() instanceof LibraryFragment)
                                                            ((LibraryFragment)getParentFragment()).libraryUpdated();
                                                        else
                                                            getActivity().getSupportLoaderManager().restartLoader(MainActivity.ARTISTS_LOADER, null, ArtistsFragment.this);
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
