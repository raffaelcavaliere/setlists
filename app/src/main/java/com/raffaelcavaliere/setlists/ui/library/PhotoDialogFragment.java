package com.raffaelcavaliere.setlists.ui.library;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.raffaelcavaliere.setlists.R;
import com.raffaelcavaliere.setlists.ui.MainActivity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


/**
 * Created by raffaelcavaliere on 2017-04-06.
 */

public class PhotoDialogFragment extends DialogFragment implements AbsListView.OnItemClickListener {
    protected GridView mListView;
    protected PhotoArrayAdapter mAdapter;
    protected ArrayList<String> photoData =  new ArrayList<>();
    private String searchString;
    private TextView txtNoPictureFound;
    private TextView txtPrompt;
    private ProgressBar progressBar;


    @Override
    public void setArguments(Bundle args) {
        this.searchString = args.getString("search", "");
        super.setArguments(args);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("searchString", searchString);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null)
            searchString = savedInstanceState.getString("searchString");
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_photo_list, container, false);
        txtPrompt = (TextView)v.findViewById(R.id.fragmentPhotoListText);
        txtNoPictureFound = (TextView)v.findViewById(R.id.fragmentPhotoListTextNoPictureFound);
        txtNoPictureFound.setVisibility(View.GONE);
        progressBar = (ProgressBar)v.findViewById(R.id.searchPicturesProgress);
        mAdapter = new PhotoArrayAdapter(getActivity(), R.layout.fragment_photo_list_item, photoData);
        mListView = (GridView)v.findViewById(R.id.searchArtistPhotoGrid);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
        searchArtist(searchString);
        return v;
    }

    private void refreshLayout() {
        if (photoData.size() > 0) {
            if (txtPrompt != null)
                txtPrompt.setText(getText(R.string.dialog_text_select_picture));
            if (txtNoPictureFound != null)
                txtNoPictureFound.setVisibility(View.GONE);
            if (progressBar != null)
                progressBar.setVisibility(View.GONE);
        } else {
            if (txtPrompt != null)
                txtPrompt.setText(getText(R.string.dialog_text_searching_picture));
            if (txtNoPictureFound != null)
                txtNoPictureFound.setVisibility(View.VISIBLE);
            if (progressBar != null)
                progressBar.setVisibility(View.GONE);
        }
    }

    private void searchArtist(String name) {

        SpotifyApi api = new SpotifyApi();
        SpotifyService spotify = api.getService();
        api.setAccessToken(MainActivity.spotifyAccessToken);
        photoData.clear();

        spotify.searchArtists(name, new Callback<ArtistsPager>() {
            @Override
            public void success(ArtistsPager pager, Response response) {
                if (pager.artists.items.size() > 0) {
                    for (int i = 0; i < pager.artists.items.size(); i++)
                        if (pager.artists.items.get(i).images.size() > 0)
                            photoData.add(pager.artists.items.get(i).images.get(0).url);
                    mAdapter.notifyDataSetChanged();
                    refreshLayout();
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d("Artist search failure", error.toString());
                refreshLayout();
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String item = photoData.get(position);
        ArtistEditActivity activity = (ArtistEditActivity)this.getActivity();
        if (activity != null)
            activity.onPhotoDialogResult(item);
        this.dismiss();
    }

    public class PhotoArrayAdapter extends ArrayAdapter<String> {

        private Context context;
        private int layoutResourceId;
        private ArrayList<String> items;

        public PhotoArrayAdapter(Context context, int layoutResourceId, ArrayList<String> items) {
            super(context, layoutResourceId, items);
            this.layoutResourceId = layoutResourceId;
            this.context = context;
            this.items = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            PhotoItemHolder holder;

            if (row == null) {
                LayoutInflater inflater = ((Activity) context).getLayoutInflater();
                row = inflater.inflate(layoutResourceId, parent, false);

                holder = new PhotoItemHolder();
                holder.imgPoster = (ImageView) row.findViewById(R.id.searchArtistPhotoGridItem);


                row.setTag(holder);
            } else {
                holder = (PhotoItemHolder) row.getTag();
            }

            String item = items.get(position);
            Uri uri = Uri.parse(item);
            Picasso.with(context).load(uri.toString()).into(holder.imgPoster);

            return row;
        }

        class PhotoItemHolder {
            ImageView imgPoster;
        }
    }
}

