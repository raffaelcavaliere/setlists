package com.raffaelcavaliere.setlists.ui.band;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.raffaelcavaliere.setlists.R;
import com.raffaelcavaliere.setlists.data.SetlistsDbContract;
import com.raffaelcavaliere.setlists.data.SetlistsDbMusicianLoader;
import com.raffaelcavaliere.setlists.ui.MainActivity;

public class MusiciansActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private RecyclerView mRecyclerView;
    private TextView textNothingToShow;
    private String band;
    private int position = 0;

    private static final int REQUEST_ADD_MUSICIAN = 4000;
    private static final int REQUEST_EDIT_MUSICIAN = 4001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_musicians);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle extras = getIntent().getExtras();
        setTitle(extras.getString("name"));

        band = extras.getString("band");

        mRecyclerView = (RecyclerView) findViewById(R.id.musician_list);

        FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.fab_add_musician);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent newMusicianIntent = new Intent(view.getContext(), MusicianEditActivity.class);
                newMusicianIntent.putExtra(MusicianEditActivity.EXTRA_MODE, MusicianEditActivity.EXTRA_MODE_NEW);
                newMusicianIntent.putExtra("band", band);
                startActivityForResult(newMusicianIntent, REQUEST_ADD_MUSICIAN);
            }
        });
        textNothingToShow = (TextView) findViewById(R.id.text_no_musician);
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
        getSupportLoaderManager().restartLoader(MainActivity.MUSICIANS_LOADER, null, this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ADD_MUSICIAN:
                if (resultCode == RESULT_OK) {
                    String returnedResult = data.getData().toString();
                    Log.d("RETURNED RESULT", returnedResult);
                }
                break;
            case REQUEST_EDIT_MUSICIAN:
                if (resultCode == RESULT_OK) {
                    Log.d("RETURNED RESULT", "OK UPDATE VERSION");
                }
                break;
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return SetlistsDbMusicianLoader.newInstanceForBandId(this, band);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (cursorLoader.getId() == MainActivity.MUSICIANS_LOADER) {
            if (cursor.getCount() <= 0)
                textNothingToShow.setVisibility(View.VISIBLE);
            else
                textNothingToShow.setVisibility(View.GONE);
            MusiciansActivity.Adapter adapter = new MusiciansActivity.Adapter(cursor);
            mRecyclerView.setAdapter(adapter);
            final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            mRecyclerView.setLayoutManager(layoutManager);
            mRecyclerView.scrollToPosition(position);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == MainActivity.MUSICIANS_LOADER)
            mRecyclerView.setAdapter(null);
    }

    private class Adapter extends RecyclerView.Adapter<MusiciansActivity.ViewHolder> {
        private Cursor mCursor;
        public Adapter(Cursor cursor) {
            mCursor = cursor;
        }

        @Override
        public MusiciansActivity.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.list_item_details, parent, false);
            final MusiciansActivity.ViewHolder vh = new MusiciansActivity.ViewHolder(view);
            return vh;
        }

        @Override
        public void onBindViewHolder(MusiciansActivity.ViewHolder holder, int position) {
            mCursor.moveToPosition(position);
            holder.bindData(mCursor.getString(0), mCursor.getString(1), mCursor.getString(2), mCursor.getString(3), mCursor.getString(4), mCursor.getString(5));
        }

        @Override
        public int getItemCount() {
            return mCursor.getCount();
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView textName;
        private TextView textEmail;
        private TextView textInstrument;
        private ImageButton btnMenu;

        private String id;
        private String band;
        private String name;
        private String email;
        private String instrument;
        private String bandName;

        public void bindData(String id, String name, String email, String instrument, String band, String bandName) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.instrument = instrument;
            this.band = band;

            textName.setText(this.name);
            textEmail.setText(this.email);
            textInstrument.setText(this.instrument);
        }

        public ViewHolder(View view) {
            super(view);
            textName = (TextView) view.findViewById(R.id.item_details_title);
            textEmail = (TextView) view.findViewById(R.id.item_details_comment_left);
            textInstrument = (TextView) view.findViewById(R.id.item_details_subtitle);
            btnMenu = (ImageButton) view.findViewById(R.id.item_details_menu);

            ((TextView) view.findViewById(R.id.item_details_comment_right)).setVisibility(View.GONE);

            btnMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
                    popupMenu.getMenuInflater().inflate(R.menu.musician_context_menu, popupMenu.getMenu());
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.musician_context_menu_email:
                                    Intent emailMusicianIntent = new Intent();
                                    emailMusicianIntent.setAction(Intent.ACTION_SEND);
                                    emailMusicianIntent.setType("message/rfc822");
                                    emailMusicianIntent.putExtra(Intent.EXTRA_EMAIL, new String[] { email });
                                    startActivity(Intent.createChooser(emailMusicianIntent, "Send email"));
                                    return true;
                                case R.id.musician_context_menu_edit:
                                    position = getAdapterPosition();
                                    Intent editMusicianIntent = new Intent(v.getContext(), MusicianEditActivity.class);
                                    editMusicianIntent.putExtra(MusicianEditActivity.EXTRA_MODE, MusicianEditActivity.EXTRA_MODE_EDIT);
                                    editMusicianIntent.putExtra("id", id);
                                    editMusicianIntent.putExtra("name", name);
                                    editMusicianIntent.putExtra("email", email);
                                    editMusicianIntent.putExtra("instrument", instrument);
                                    editMusicianIntent.putExtra("band", band);
                                    startActivityForResult(editMusicianIntent, REQUEST_EDIT_MUSICIAN);
                                    return true;
                                case R.id.musician_context_menu_remove:
                                    position = getAdapterPosition();
                                    new AlertDialog.Builder(v.getContext())
                                            .setTitle(getResources().getString(R.string.menu_remove_musician))
                                            .setMessage(getResources().getString(R.string.dialog_remove_musician))
                                            .setIcon(android.R.drawable.ic_dialog_alert)
                                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int whichButton) {
                                                    if (getContentResolver().delete(SetlistsDbContract.SetlistsDbMusicianEntry.buildSetlistsDbMusicianUri(id),
                                                            SetlistsDbContract.SetlistsDbMusicianEntry.COLUMN_ID + "=?", new String[] { id }) > 0)
                                                        getSupportLoaderManager().restartLoader(MainActivity.MUSICIANS_LOADER, null, MusiciansActivity.this);
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
