package com.raffaelcavaliere.setlists.ui;

import android.content.ContentValues;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.nononsenseapps.filepicker.FilePickerActivity;
import com.raffaelcavaliere.setlists.R;
import com.raffaelcavaliere.setlists.data.SetlistsDatabase;
import com.raffaelcavaliere.setlists.data.SetlistsDbContract;
import com.raffaelcavaliere.setlists.services.SyncService;
import com.raffaelcavaliere.setlists.ui.band.BandsFragment;
import com.raffaelcavaliere.setlists.ui.library.ArtistsFragment;
import com.raffaelcavaliere.setlists.ui.library.DocumentsActivity;
import com.raffaelcavaliere.setlists.ui.library.LibraryFragment;
import com.raffaelcavaliere.setlists.ui.library.SetsFragment;
import com.raffaelcavaliere.setlists.ui.library.SongEditActivity;
import com.raffaelcavaliere.setlists.ui.library.SongsFragment;
import com.raffaelcavaliere.setlists.ui.midi.MidiMessagesFragment;
import com.raffaelcavaliere.setlists.utils.FilteredFilePickerFragment;
import com.raffaelcavaliere.setlists.utils.Storage;
import com.raffaelcavaliere.setlists.widget.SetlistsWidget;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        LibraryFragment.OnFragmentInteractionListener,
        ArtistsFragment.OnFragmentInteractionListener,
        SongsFragment.OnFragmentInteractionListener,
        BandsFragment.OnFragmentInteractionListener,
        SetsFragment.OnFragmentInteractionListener,
        MidiMessagesFragment.OnFragmentInteractionListener {

    FragmentManager mFragmentManager;
    FragmentTransaction mFragmentTransaction;
    Toolbar toolbar;
    public static final int SONGS_LOADER = 0;
    public static final int ARTISTS_LOADER = 1;
    public static final int BANDS_LOADER = 2;
    public static final int MUSICIANS_LOADER = 3;
    public static final int SETS_LOADER = 4;
    public static final int SET_SONGS_LOADER = 5;
    public static final int DOCUMENTS_LOADER = 6;
    public static final int MIDI_MESSAGES_LOADER = 7;
    public static final int DOCUMENT_MIDI_MESSAGES_LOADER = 8;

    public static String spotifyAccessToken;

    public static final int REQUEST_RESTORE_DATABASE = 6000;
    public static final int REQUEST_BACKUP_DATABASE = 6001;
    public static final int REQUEST_BATCH_IMPORT = 6002;

    private Fragment libraryFragment = new LibraryFragment();
    private Fragment bandsFragment = new BandsFragment();
    private Fragment midiFragment = new MidiMessagesFragment();
    private int selectedNav = R.id.nav_library;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d("FIREBASE", "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d("FIREBASE", "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };

        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.library_toolbar);
        setSupportActionBar(toolbar);
        mFragmentManager = getSupportFragmentManager();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View header = navigationView.getHeaderView(0);
        TextView textName = (TextView) header.findViewById(R.id.nav_header_name);
        textName.setText(mAuth.getCurrentUser().getDisplayName());

        TextView textEmail = (TextView) header.findViewById(R.id.nav_header_email);
        textEmail.setText(mAuth.getCurrentUser().getEmail());

        Button logoutButton = (Button) header.findViewById(R.id.nav_header_logout);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                finish();
            }
        });


        if (savedInstanceState != null) {
            selectedNav = savedInstanceState.getInt("fragment", R.id.nav_library);
            switch (selectedNav) {
                case R.id.nav_library:
                    setTitle(R.string.nav_library);
                    break;
                case R.id.nav_bands:
                    setTitle(R.string.nav_bands);
                    break;
                case R.id.nav_midi:
                    setTitle(R.string.nav_midi);
                    break;
            }
        } else {
            navigationView.setCheckedItem(R.id.nav_library);
            FragmentTransaction xfragmentTransaction = mFragmentManager.beginTransaction();
            xfragmentTransaction.replace(R.id.mainContainerView, libraryFragment).commit();
            setTitle(R.string.nav_library);
        }

        // Spotify
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni != null && ni.isConnected())
            new AuthenticateOnSpotify().execute();

        // Temp folder
        File tempDir = new File(getFilesDir().getPath() + "/temp");
        if (!tempDir.exists())
            tempDir.mkdir();

        // Load specific activity on top of main activity if requested
        int action = this.getIntent().getIntExtra(SetlistsWidget.WIDGET_ACTION, 0);
        if (action > 0) {
            if (action == SetlistsWidget.WIDGET_ACTION_NEW_SONG) {

                Intent newSongIntent = new Intent(this, SongEditActivity.class);
                startActivityForResult(newSongIntent, SongsFragment.REQUEST_ADD_SONG);
            } else if (action == SetlistsWidget.WIDGET_ACTION_SHOW_SONG) {
                Intent documentsIntent = new Intent(this, DocumentsActivity.class);
                documentsIntent.putExtra("song", this.getIntent().getStringExtra("song"));
                documentsIntent.putExtra("title", this.getIntent().getStringExtra("title"));
                documentsIntent.putExtra("preferred", this.getIntent().getStringExtra("document"));
                documentsIntent.putExtra("tempo", this.getIntent().getIntExtra("tempo", 0));
                startActivity(documentsIntent);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("fragment", selectedNav);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            ;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private void importDB(String path) {
        try {
            File data = Environment.getDataDirectory();
            String dest = "//data//" + getPackageName()
                    + "//databases//" + SetlistsDatabase.DATABASE_NAME;
            File destDB = new File(data, dest);
            File srcDB = new File(path);

            FileChannel src = new FileInputStream(srcDB).getChannel();
            FileChannel dst = new FileOutputStream(destDB).getChannel();
            dst.transferFrom(src, 0, src.size());
            src.close();
            dst.close();
            Toast.makeText(getApplicationContext(), "Import Successful!",
                    Toast.LENGTH_SHORT).show();

        } catch (Exception e) {

            Log.d("EXCEPTION", e.getMessage());
            Toast.makeText(getApplicationContext(), "Import Failed!", Toast.LENGTH_SHORT)
                    .show();

        }
    }

    private void exportDB(String path) {
        try {
            File data = Environment.getDataDirectory();
            String dataPath = "//data//" + getPackageName()
                    + "//databases//" + SetlistsDatabase.DATABASE_NAME;
            File srcDB = new File(data, dataPath);
            File destDB = new File(path);

            FileChannel src = new FileInputStream(srcDB).getChannel();
            FileChannel dst = new FileOutputStream(destDB).getChannel();
            dst.transferFrom(src, 0, src.size());
            src.close();
            dst.close();
            Toast.makeText(getApplicationContext(), "Backup Successful!",
                    Toast.LENGTH_SHORT).show();

        } catch (Exception e) {

            Toast.makeText(getApplicationContext(), "Backup Failed!", Toast.LENGTH_SHORT)
                    .show();

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.main_menu_preferences) {
            return true;
        } else if (id == R.id.main_menu_restore_database) {
            Intent filePickerIntent = new Intent(this, FilePickerActivity.class);
            filePickerIntent.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
            filePickerIntent.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false);
            filePickerIntent.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE);
            filePickerIntent.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().toString());

            startActivityForResult(filePickerIntent, REQUEST_RESTORE_DATABASE);
            return true;
        } else if (id == R.id.main_menu_backup_database) {
            Intent filePickerIntent = new Intent(this, FilePickerActivity.class);
            filePickerIntent.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
            filePickerIntent.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, true);
            filePickerIntent.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_NEW_FILE);
            filePickerIntent.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().toString());

            startActivityForResult(filePickerIntent, REQUEST_BACKUP_DATABASE);
            return true;
        } else if (id == R.id.main_menu_batch_import) {
            Intent filePickerIntent = new Intent(this, FilePickerActivity.class);
            filePickerIntent.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, true);
            filePickerIntent.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false);
            filePickerIntent.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE);
            filePickerIntent.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().toString());

            startActivityForResult(filePickerIntent, REQUEST_BATCH_IMPORT);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_RESTORE_DATABASE:
                if (resultCode == RESULT_OK) {
                    Uri uri = data.getData();
                    Log.d("RETURNED RESULT", uri.toString());
                    if (uri.getPath().length() > 0) {
                        String path = uri.getPath().replace("/root", "");
                        importDB(path);
                    }
                }
                break;
            case REQUEST_BACKUP_DATABASE:
                if (resultCode == RESULT_OK) {
                    Uri uri = data.getData();
                    Log.d("RETURNED RESULT", uri.toString());
                    if (uri.getPath().length() > 0) {
                        String path = uri.getPath().replace("/root", "");
                        exportDB(path);
                    }
                }
                break;
            case REQUEST_BATCH_IMPORT:
                if (resultCode == RESULT_OK) {
                    if (data.getExtras().getBoolean(FilePickerActivity.EXTRA_ALLOW_MULTIPLE)) {
                        ArrayList<String> src = data.getExtras().getStringArrayList(FilePickerActivity.EXTRA_PATHS);
                        if (src != null) {
                            for (String s : src) {
                                File f = new File(s);
                                importSong(f);
                            }

                        }
                    }
                    Log.d("RETURNED RESULT",data.getExtras().toString());
                }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void importSong(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children)
                    if (child.isFile())
                        importSong(child);
            }
        } else {
            Uri uri = Uri.fromFile(file);
            int dot = uri.getLastPathSegment().lastIndexOf(".");
            if (dot < 0) {
                return;
            }
            int type = 0;
            String extension = uri.getLastPathSegment().substring(dot);
            if (extension.equalsIgnoreCase(FilteredFilePickerFragment.EXTENSION_PDF))
                type = SetlistsDbContract.SetlistsDbDocumentEntry.DOCUMENT_TYPE_PDF;
            else if (extension.equalsIgnoreCase(FilteredFilePickerFragment.EXTENSION_JPG) ||
                    extension.equalsIgnoreCase(FilteredFilePickerFragment.EXTENSION_BMP) ||
                    extension.equalsIgnoreCase(FilteredFilePickerFragment.EXTENSION_PNG) ||
                    extension.equalsIgnoreCase(FilteredFilePickerFragment.EXTENSION_GIF))
                type = SetlistsDbContract.SetlistsDbDocumentEntry.DOCUMENT_TYPE_IMAGE;
            else if (extension.equalsIgnoreCase(FilteredFilePickerFragment.EXTENSION_PRO))
                type = SetlistsDbContract.SetlistsDbDocumentEntry.DOCUMENT_TYPE_CHORDPRO;
            else if (extension.equalsIgnoreCase(FilteredFilePickerFragment.EXTENSION_TXT))
                type = SetlistsDbContract.SetlistsDbDocumentEntry.DOCUMENT_TYPE_TEXT;
            else
                return;

            ContentValues values = new ContentValues();
            String song = UUID.randomUUID().toString();
            values.put(SetlistsDbContract.SetlistsDbSongEntry.COLUMN_ID, song);
            values.put(SetlistsDbContract.SetlistsDbSongEntry.COLUMN_TITLE, uri.getLastPathSegment().substring(0, dot));
            values.put(SetlistsDbContract.SetlistsDbSongEntry.COLUMN_KEY, "C");
            values.put(SetlistsDbContract.SetlistsDbSongEntry.COLUMN_DATE_ADDED, new Date().getTime() / 1000);
            values.put(SetlistsDbContract.SetlistsDbSongEntry.COLUMN_DATE_MODIFIED, new Date().getTime() / 1000);
            Uri songResult = getContentResolver().insert(SetlistsDbContract.SetlistsDbSongEntry.CONTENT_URI, values);

            if (songResult.getLastPathSegment() != null) {
                values = new ContentValues();
                String document = UUID.randomUUID().toString();
                values.put(SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_ID, document);
                values.put(SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_DESCRIPTION, uri.getLastPathSegment().substring(0, dot));
                values.put(SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_SONG, song);
                values.put(SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_AUTHOR, "raffaelcavaliere");
                values.put(SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_TYPE, type);
                values.put(SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_DATE_ADDED, new Date().getTime() / 1000);
                values.put(SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_DATE_MODIFIED, new Date().getTime() / 1000);
                Uri documentResult = getContentResolver().insert(SetlistsDbContract.SetlistsDbDocumentEntry.CONTENT_URI, values);

                if (documentResult.getLastPathSegment() != null) {
                    File fileToImport = new File(file.getPath().replace("/root", ""));
                    try {
                        Storage.copy(fileToImport, new File(getFilesDir().getPath() + "/" + String.valueOf(document)));
                        Log.d("IMPORT", fileToImport.getPath() + " > " + getFilesDir().getPath() + "/" + String.valueOf(document));
                    } catch (IOException ex) {
                        Log.d("EXCEPTION", ex.toString());
                    }

                    values = new ContentValues();
                    values.put(SetlistsDbContract.SetlistsDbSongEntry.COLUMN_DOCUMENT, document);
                    values.put(SetlistsDbContract.SetlistsDbSongEntry.COLUMN_DATE_MODIFIED, new Date().getTime() / 1000);

                    getContentResolver().update(SetlistsDbContract.SetlistsDbSongEntry.buildSetlistsDbSongUri(song), values,
                            SetlistsDbContract.SetlistsDbSongEntry.COLUMN_ID + "=?", new String[]{String.valueOf(song)});
                }
            }
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        selectedNav = item.getItemId();

        if (selectedNav == R.id.nav_library) {
            FragmentTransaction xfragmentTransaction = mFragmentManager.beginTransaction();
            xfragmentTransaction.replace(R.id.mainContainerView, libraryFragment).commit();
            toolbar.setTitle(R.string.nav_library);

        } else if (selectedNav == R.id.nav_midi) {
            FragmentTransaction xfragmentTransaction = mFragmentManager.beginTransaction();
            xfragmentTransaction.replace(R.id.mainContainerView, midiFragment).commit();
            toolbar.setTitle(R.string.nav_midi);

        } else if (selectedNav == R.id.nav_bands) {
            FragmentTransaction xfragmentTransaction = mFragmentManager.beginTransaction();
            xfragmentTransaction.replace(R.id.mainContainerView, bandsFragment).commit();
            toolbar.setTitle(R.string.nav_bands);

        } else if (selectedNav == R.id.nav_community) {

        } else if (selectedNav == R.id.nav_search) {

        } else if (selectedNav == R.id.nav_sync) {
            SyncService.startSync(this);

        } else if (selectedNav == R.id.nav_settings) {

        } else if (selectedNav == R.id.nav_help) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        Log.d("FRAGMENT INTERACTION", uri.toString());
    }


    private class AuthenticateOnSpotify extends AsyncTask<Void, Void, String> {
        protected String doInBackground(Void... args) {
            try {
                URL url = new URL(getResources().getString(R.string.spotify_token_url));
                Map<String,Object> params = new LinkedHashMap<>();
                params.put("grant_type", "client_credentials");
                StringBuilder postData = new StringBuilder();
                for (Map.Entry<String,Object> param : params.entrySet()) {
                    if (postData.length() != 0) postData.append('&');
                    postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                    postData.append('=');
                    postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
                }
                byte[] postDataBytes = postData.toString().getBytes("UTF-8");

                HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestProperty("Authorization",
                        getB64Auth(getResources().getString(R.string.spotify_client_id), getResources().getString(R.string.spotify_client_secret)));
                conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
                conn.setDoOutput(true);
                conn.getOutputStream().write(postDataBytes);

                Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                StringBuilder response = new StringBuilder();
                for (int c; (c = in.read()) >= 0;)
                    response.append((char)c);
                JSONObject data = new JSONObject(response.toString());
                return data.getString("access_token");

            } catch (Exception ex) {
                Log.d("EXCEPTION", ex.toString());
            }
            return null;
        }

        protected void onPostExecute(String result) {
            Log.d("SPOTIFY", result == null ? "NO RESULT FROM SPOTIFY" : result);
            spotifyAccessToken = result;
        }

        private String getB64Auth (String clientId, String clientSecret) {
            String source = clientId + ":" + clientSecret;
            String ret = "Basic " + Base64.encodeToString(source.getBytes(), Base64.URL_SAFE | Base64.NO_WRAP);
            return ret;
        }
    }
}
