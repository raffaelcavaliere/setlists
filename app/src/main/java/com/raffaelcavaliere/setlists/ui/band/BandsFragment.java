package com.raffaelcavaliere.setlists.ui.band;

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
import com.raffaelcavaliere.setlists.data.SetlistsDbBandLoader;
import com.raffaelcavaliere.setlists.data.SetlistsDbContract;
import com.raffaelcavaliere.setlists.data.SetlistsDbMusicianLoader;
import com.raffaelcavaliere.setlists.ui.MainActivity;

import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;

public class BandsFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final int REQUEST_ADD_BAND = 1000;
    private static final int REQUEST_EDIT_BAND = 1001;

    private OnFragmentInteractionListener mListener;
    private RecyclerView mRecyclerView;
    private TextView textNothingToShow;
    private int position = 0;

    public BandsFragment() {
        // Required empty public constructor
    }

    public static BandsFragment newInstance() {
        BandsFragment fragment = new BandsFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bands, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.band_list);

        FloatingActionButton fab = (FloatingActionButton)view.findViewById(R.id.fab_add_band);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent newBandIntent = new Intent(getActivity(), BandEditActivity.class);
                startActivityForResult(newBandIntent, REQUEST_ADD_BAND);
            }
        });
        textNothingToShow = (TextView) view.findViewById(R.id.text_no_band);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().getSupportLoaderManager().restartLoader(MainActivity.BANDS_LOADER, null, this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ADD_BAND:
                if (resultCode == RESULT_OK) {
                    String returnedResult = data.getData().toString();
                    Log.d("RETURNED RESULT", returnedResult);
                }
                break;
            case REQUEST_EDIT_BAND:
                if (resultCode == RESULT_OK) {
                    Log.d("RETURNED RESULT", "OK UPDATE BAND");
                }
                break;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof BandsFragment.OnFragmentInteractionListener) {
            mListener = (BandsFragment.OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return SetlistsDbBandLoader.newAllBandsInstance(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        BandsFragment.Adapter adapter = new BandsFragment.Adapter(cursor);
        if (cursor.getCount() <= 0)
            textNothingToShow.setVisibility(View.VISIBLE);
        else
            textNothingToShow.setVisibility(View.GONE);
        adapter.setHasStableIds(true);
        mRecyclerView.setAdapter(adapter);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.scrollToPosition(position);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mRecyclerView.setAdapter(null);
    }

    private class Adapter extends RecyclerView.Adapter<BandsFragment.ViewHolder> {
        private Cursor mCursor;
        public Adapter(Cursor cursor) {
            mCursor = cursor;
        }

        @Override
        public BandsFragment.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getActivity().getLayoutInflater().inflate(R.layout.list_item_details, parent, false);
            final BandsFragment.ViewHolder vh = new BandsFragment.ViewHolder(view);
            return vh;
        }

        @Override
        public void onBindViewHolder(BandsFragment.ViewHolder holder, int position) {
            mCursor.moveToPosition(position);
            holder.bindData(mCursor.getString(0), mCursor.getString(1), mCursor.getInt(2), mCursor.getInt(3));
        }

        @Override
        public int getItemCount() {
            return mCursor.getCount();
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView textName;
        private TextView textMemberCount;
        private TextView textSetCount;
        private ImageButton btnMenu;

        private String id;
        private String name;
        private int memberCount;
        private int setCount;

        public void bindData(String id, String name, int memberCount, int setCount) {
            this.id = id;
            this.name = name;
            this.memberCount = memberCount;
            this.setCount = setCount;
            textName.setText(name);
            textMemberCount.setText(memberCount <= 0 ? "No member" : memberCount + " member" + (memberCount > 1 ? "s" : ""));
            textSetCount.setText(setCount <= 0 ? "No set" : setCount + " set" + (setCount > 1 ? "s" : ""));
        }

        public ViewHolder(View view) {
            super(view);
            textName = (TextView) view.findViewById(R.id.item_details_title);
            textMemberCount = (TextView) view.findViewById(R.id.item_details_subtitle);
            textSetCount = (TextView) view.findViewById(R.id.item_details_comment_left);
            btnMenu = (ImageButton) view.findViewById(R.id.item_details_menu);
            ((TextView) view.findViewById(R.id.item_details_comment_right)).setVisibility(View.GONE);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    position = getAdapterPosition();
                    Intent bandMusiciansIntent = new Intent(v.getContext(), MusiciansActivity.class);
                    bandMusiciansIntent.putExtra("name", name);
                    bandMusiciansIntent.putExtra("band", id);
                    startActivity(bandMusiciansIntent);
                }
            });

            btnMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
                    popupMenu.getMenuInflater().inflate(R.menu.band_context_menu, popupMenu.getMenu());
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.band_context_menu_email:
                                    ArrayList<String> emails = new ArrayList<>();
                                    Cursor cursor = getActivity().getContentResolver().query(
                                            SetlistsDbContract.SetlistsDbMusicianEntry.buildSetlistsDbBandMusiciansUri(id), null, null, null, null);
                                    if (cursor != null && cursor.getCount() > 0) {
                                        cursor.moveToFirst();
                                        do {
                                            if (cursor.getString(2).length() > 0)
                                                emails.add(cursor.getString(2));
                                        } while (cursor.moveToNext());
                                    }
                                    if (cursor != null)
                                        cursor.close();

                                    String[] list = new String[emails.size()];
                                    for (int i = 0; i < emails.size(); i++)
                                        list[i] = emails.get(i);

                                    Intent emailBandIntent = new Intent();
                                    emailBandIntent.setAction(Intent.ACTION_SEND);
                                    emailBandIntent.setType("message/rfc822");
                                    emailBandIntent.putExtra(Intent.EXTRA_EMAIL, list);
                                    startActivity(Intent.createChooser(emailBandIntent, "Send email"));
                                    return true;
                                case R.id.band_context_menu_edit:
                                    position = getAdapterPosition();
                                    Intent editBandIntent = new Intent(v.getContext(), BandEditActivity.class);
                                    editBandIntent.putExtra("id", id);
                                    editBandIntent.putExtra("name", name);
                                    startActivityForResult(editBandIntent, REQUEST_EDIT_BAND);
                                    return true;
                                case R.id.band_context_menu_remove:
                                    position = getAdapterPosition();
                                    new AlertDialog.Builder(v.getContext())
                                            .setTitle(getResources().getString(R.string.menu_remove_band))
                                            .setMessage(getResources().getString(R.string.dialog_remove_band))
                                            .setIcon(android.R.drawable.ic_dialog_alert)
                                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int whichButton) {
                                                    if (getActivity().getContentResolver().delete(SetlistsDbContract.SetlistsDbBandEntry.buildSetlistsDbBandUri(id),
                                                            SetlistsDbContract.SetlistsDbBandEntry.COLUMN_ID + "=?", new String[] {String.valueOf(id)}) > 0) {
                                                        getActivity().getSupportLoaderManager().restartLoader(MainActivity.BANDS_LOADER, null, BandsFragment.this);
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
