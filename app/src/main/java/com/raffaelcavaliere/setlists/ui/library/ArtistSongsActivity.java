package com.raffaelcavaliere.setlists.ui.library;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.raffaelcavaliere.setlists.R;

public class ArtistSongsActivity extends AppCompatActivity
        implements SongsFragment.OnFragmentInteractionListener {

    private RecyclerView mRecyclerView;
    private Toolbar toolbar;
    private TextView textNothingToShow;
    private String artist;
    FragmentManager mFragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_songs);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle extras = getIntent().getExtras();
        setTitle(extras.getString("name"));
        artist = extras.getString("artist");
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
    protected void onResume() {
        super.onResume();
        mFragmentManager = getSupportFragmentManager();
        FragmentTransaction xfragmentTransaction = mFragmentManager.beginTransaction();
        xfragmentTransaction.replace(R.id.fragmentContainerView, SongsFragment.newInstance(artist)).commit();
    }

    public void setSubtitle(String text) {
        this.toolbar.setSubtitle(text);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        Log.d("FRAGMENT INTERACTION", uri.toString());
    }
}
