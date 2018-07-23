package com.raffaelcavaliere.setlists.services;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.util.Pair;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.server.response.FastJsonResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.raffaelcavaliere.setlists.R;
import com.raffaelcavaliere.setlists.data.SetlistsDbArtistLoader;
import com.raffaelcavaliere.setlists.data.SetlistsDbContract;
import com.raffaelcavaliere.setlists.utils.Storage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

public class SyncService extends IntentService {
    private static final String ACTION_SYNC = "com.raffaelcavaliere.setlists.services.action.sync";
    private static final String BASE_API_URL = "138.197.155.207";
    static FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private RequestQueue queue;

    public SyncService() {
        super("StorageSyncService");
    }

    public static void startSync(Context context) {
        Intent intent = new Intent(context, SyncService.class);
        intent.setAction(ACTION_SYNC);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                CharSequence name = "Chordiste";
                String description = "Chordiste notification channel";
                int importance = NotificationManager.IMPORTANCE_DEFAULT;
                NotificationChannel channel = new NotificationChannel("Chordiste", name, importance);
                channel.setDescription(description);
                // Register the channel with the system; you can't change the importance
                // or other notification behaviors after this
                NotificationManager notificationManager = getSystemService(NotificationManager.class);
                notificationManager.createNotificationChannel(channel);
            }
            final String action = intent.getAction();
            if (ACTION_SYNC.equals(action)) {
                syncAll();
            }
        }
    }

    private void syncAll() {

        final FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Handler mHandler = new Handler(getMainLooper());
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "Unknown user", Toast.LENGTH_LONG).show();
                }
            });
            return;
        }

        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo ni = cm.getActiveNetworkInfo();
            if (ni == null || !ni.isConnected()) {
                Handler mHandler = new Handler(getMainLooper());
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Network is unavailable", Toast.LENGTH_LONG).show();
                    }
                });
                return;
            }
        } else {
            Handler mHandler = new Handler(getMainLooper());
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "Network is unavailable", Toast.LENGTH_LONG).show();
                }
            });
            return;
        }

        Handler mHandler = new Handler(getMainLooper());
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "Synchronization has started.  You will receive a notification when it is completed.", Toast.LENGTH_LONG).show();
            }
        });

        queue = Volley.newRequestQueue(this);

        // sync deleted records
        syncDeletedRecords(user, new SyncTableListener() {
            @Override
            public void onFailure(Exception exception) {

            }

            @Override
            public void onComplete() {
                Log.d("SYNC", "Sync deleted records completed.");
                // sync bands
                syncBands(user, new SyncTableListener() {
                    @Override
                    public void onFailure(Exception exception) {

                    }

                    @Override
                    public void onComplete() {
                        Log.d("SYNC", "Sync bands completed.");
                        // syncs musicians
                        syncMusicians(user, new SyncTableListener() {
                            @Override
                            public void onFailure(Exception exception) {

                            }

                            @Override
                            public void onComplete() {
                                Log.d("SYNC", "Sync musicians completed.");
                            }
                        });

                        // sync artists
                        syncArtists(user, new SyncTableListener() {
                            @Override
                            public void onFailure(Exception exception) {

                            }

                            @Override
                            public void onComplete() {
                                Log.d("SYNC", "Sync artists completed.");
                                // sync songs
                                syncSongs(user, new SyncTableListener() {
                                    @Override
                                    public void onFailure(Exception exception) {

                                    }

                                    @Override
                                    public void onComplete() {
                                        Log.d("SYNC", "Sync songs completed.");
                                        // sync documents
                                        syncDocuments(user, new SyncTableListener() {
                                            @Override
                                            public void onFailure(Exception exception) {

                                            }

                                            @Override
                                            public void onComplete() {
                                                Log.d("SYNC", "Sync documents completed.");
                                                // sync midi messages
                                                syncMidiMessages(user, new SyncTableListener() {
                                                    @Override
                                                    public void onFailure(Exception exception) {

                                                    }

                                                    @Override
                                                    public void onComplete() {
                                                        Log.d("SYNC", "Sync midi messages completed.");
                                                        // sync document midi messages
                                                        syncDocumentMidiMessages(user, new SyncTableListener() {
                                                            @Override
                                                            public void onFailure(Exception exception) {

                                                            }

                                                            @Override
                                                            public void onComplete() {
                                                                Log.d("SYNC", "Sync document midi messages completed.");
                                                                Handler mHandler = new Handler(getMainLooper());
                                                                mHandler.post(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        sendNotification();
                                                                        Toast.makeText(getApplicationContext(), "Sync completed", Toast.LENGTH_LONG).show();
                                                                    }
                                                                });
                                                            }
                                                        });
                                                    }
                                                });
                                            }
                                        });
                                        // sync sets
                                        syncSets(user, new SyncTableListener() {
                                            @Override
                                            public void onFailure(Exception exception) {

                                            }

                                            @Override
                                            public void onComplete() {
                                                Log.d("SYNC", "Sync sets completed.");
                                                // sync setlist songs
                                                syncSetSongs(user, new SyncTableListener() {
                                                    @Override
                                                    public void onFailure(Exception exception) {

                                                    }

                                                    @Override
                                                    public void onComplete() {
                                                        Log.d("SYNC", "Sync set songs completed.");
                                                    }
                                                });
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });

    }

    private void syncBands(FirebaseUser user, final SyncTableListener callback) {
        syncTable(SetlistsDbContract.SetlistsDbBandEntry.CONTENT_URI, SetlistsDbContract.SetlistsDbBandEntry.TABLE_NAME,
                new String[]{SetlistsDbContract.SetlistsDbBandEntry.COLUMN_ID,
                        SetlistsDbContract.SetlistsDbBandEntry.COLUMN_NAME,
                        SetlistsDbContract.SetlistsDbBandEntry.COLUMN_DATE_ADDED,
                        SetlistsDbContract.SetlistsDbBandEntry.COLUMN_DATE_MODIFIED,
                        SetlistsDbContract.SetlistsDbBandEntry.COLUMN_USER},
                new int[]{Cursor.FIELD_TYPE_STRING,
                        Cursor.FIELD_TYPE_STRING,
                        Cursor.FIELD_TYPE_INTEGER,
                        Cursor.FIELD_TYPE_INTEGER,
                        Cursor.FIELD_TYPE_STRING}, user, new SyncTableListener() {
                    @Override
                    public void onFailure(Exception exception) {
                        callback.onFailure(exception);
                    }

                    @Override
                    public void onComplete() {
                        callback.onComplete();
                    }
                });
    }

    private void syncMusicians(FirebaseUser user, final SyncTableListener callback) {
        syncTable(SetlistsDbContract.SetlistsDbMusicianEntry.CONTENT_URI, SetlistsDbContract.SetlistsDbMusicianEntry.TABLE_NAME,
                new String[]{SetlistsDbContract.SetlistsDbMusicianEntry.COLUMN_ID,
                        SetlistsDbContract.SetlistsDbMusicianEntry.COLUMN_NAME,
                        SetlistsDbContract.SetlistsDbMusicianEntry.COLUMN_EMAIL,
                        SetlistsDbContract.SetlistsDbMusicianEntry.COLUMN_INSTRUMENT,
                        SetlistsDbContract.SetlistsDbMusicianEntry.COLUMN_BAND,
                        SetlistsDbContract.SetlistsDbMusicianEntry.COLUMN_DATE_ADDED,
                        SetlistsDbContract.SetlistsDbMusicianEntry.COLUMN_DATE_MODIFIED,
                        SetlistsDbContract.SetlistsDbMusicianEntry.COLUMN_USER},
                new int[]{Cursor.FIELD_TYPE_STRING,
                        Cursor.FIELD_TYPE_STRING,
                        Cursor.FIELD_TYPE_STRING,
                        Cursor.FIELD_TYPE_STRING,
                        Cursor.FIELD_TYPE_STRING,
                        Cursor.FIELD_TYPE_INTEGER,
                        Cursor.FIELD_TYPE_INTEGER,
                        Cursor.FIELD_TYPE_STRING}, user, new SyncTableListener() {
                    @Override
                    public void onFailure(Exception exception) {
                        callback.onFailure(exception);
                    }

                    @Override
                    public void onComplete() {
                        callback.onComplete();
                    }
                });
    }

    private void syncArtists(FirebaseUser user, final SyncTableListener callback) {
        syncTable(SetlistsDbContract.SetlistsDbArtistEntry.CONTENT_URI, SetlistsDbContract.SetlistsDbArtistEntry.TABLE_NAME,
                new String[]{SetlistsDbContract.SetlistsDbArtistEntry.COLUMN_ID,
                        SetlistsDbContract.SetlistsDbArtistEntry.COLUMN_NAME,
                        SetlistsDbContract.SetlistsDbArtistEntry.COLUMN_PHOTO,
                        SetlistsDbContract.SetlistsDbArtistEntry.COLUMN_DATE_ADDED,
                        SetlistsDbContract.SetlistsDbArtistEntry.COLUMN_DATE_MODIFIED,
                        SetlistsDbContract.SetlistsDbArtistEntry.COLUMN_USER},
                new int[]{Cursor.FIELD_TYPE_STRING,
                        Cursor.FIELD_TYPE_STRING,
                        Cursor.FIELD_TYPE_STRING,
                        Cursor.FIELD_TYPE_INTEGER,
                        Cursor.FIELD_TYPE_INTEGER,
                        Cursor.FIELD_TYPE_STRING}, user, new SyncTableListener() {
                    @Override
                    public void onFailure(Exception exception) {
                        callback.onFailure(exception);
                    }

                    @Override
                    public void onComplete() {
                        callback.onComplete();
                    }
                });
    }

    private void syncSongs(FirebaseUser user, final SyncTableListener callback) {
        syncTable(SetlistsDbContract.SetlistsDbSongEntry.CONTENT_URI, SetlistsDbContract.SetlistsDbSongEntry.TABLE_NAME,
                new String[]{SetlistsDbContract.SetlistsDbSongEntry.COLUMN_ID,
                        SetlistsDbContract.SetlistsDbSongEntry.COLUMN_TITLE,
                        SetlistsDbContract.SetlistsDbSongEntry.COLUMN_AUTHOR,
                        SetlistsDbContract.SetlistsDbSongEntry.COLUMN_ARTIST,
                        SetlistsDbContract.SetlistsDbSongEntry.COLUMN_DOCUMENT,
                        SetlistsDbContract.SetlistsDbSongEntry.COLUMN_KEY,
                        SetlistsDbContract.SetlistsDbSongEntry.COLUMN_TEMPO,
                        SetlistsDbContract.SetlistsDbSongEntry.COLUMN_DURATION,
                        SetlistsDbContract.SetlistsDbSongEntry.COLUMN_NOTES,
                        SetlistsDbContract.SetlistsDbSongEntry.COLUMN_DATE_ADDED,
                        SetlistsDbContract.SetlistsDbSongEntry.COLUMN_DATE_MODIFIED,
                        SetlistsDbContract.SetlistsDbSongEntry.COLUMN_USER},
                new int[]{Cursor.FIELD_TYPE_STRING,
                        Cursor.FIELD_TYPE_STRING,
                        Cursor.FIELD_TYPE_STRING,
                        Cursor.FIELD_TYPE_STRING,
                        Cursor.FIELD_TYPE_STRING,
                        Cursor.FIELD_TYPE_STRING,
                        Cursor.FIELD_TYPE_INTEGER,
                        Cursor.FIELD_TYPE_INTEGER,
                        Cursor.FIELD_TYPE_STRING,
                        Cursor.FIELD_TYPE_INTEGER,
                        Cursor.FIELD_TYPE_INTEGER,
                        Cursor.FIELD_TYPE_STRING}, user, new SyncTableListener() {
                    @Override
                    public void onFailure(Exception exception) {
                        callback.onFailure(exception);
                    }

                    @Override
                    public void onComplete() {
                        callback.onComplete();
                    }
                });
    }

    private void syncDocuments(final FirebaseUser user, final SyncTableListener callback) {
        syncTable(SetlistsDbContract.SetlistsDbDocumentEntry.CONTENT_URI, SetlistsDbContract.SetlistsDbDocumentEntry.TABLE_NAME,
                new String[]{SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_ID,
                        SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_DESCRIPTION,
                        SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_AUTHOR,
                        SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_SONG,
                        SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_TRANSPOSE,
                        SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_TRANSPOSE_MODE,
                        SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_TYPE,
                        SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_DATE_ADDED,
                        SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_DATE_MODIFIED,
                        SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_USER},
                new int[]{Cursor.FIELD_TYPE_STRING,
                        Cursor.FIELD_TYPE_STRING,
                        Cursor.FIELD_TYPE_STRING,
                        Cursor.FIELD_TYPE_STRING,
                        Cursor.FIELD_TYPE_INTEGER,
                        Cursor.FIELD_TYPE_INTEGER,
                        Cursor.FIELD_TYPE_INTEGER,
                        Cursor.FIELD_TYPE_INTEGER,
                        Cursor.FIELD_TYPE_INTEGER,
                        Cursor.FIELD_TYPE_STRING}, user, new SyncTableListener() {
                    @Override
                    public void onFailure(Exception exception) {
                        callback.onFailure(exception);
                    }

                    @Override
                    public void onComplete() {
                        ArrayList<ContentValues> documents = new ArrayList<>();
                        Cursor cursor = getContentResolver().query(SetlistsDbContract.SetlistsDbDocumentEntry.CONTENT_URI, null, null, null, null);
                        if (cursor != null && cursor.moveToFirst()) {
                            do {
                                String id = cursor.getString(cursor.getColumnIndex(SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_ID));
                                String path = getFilesDir().getPath() + "/" + id;
                                String description = cursor.getString(cursor.getColumnIndex(SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_DESCRIPTION));
                                String type = "";
                                switch (cursor.getInt(cursor.getColumnIndex(SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_TYPE))) {
                                    case SetlistsDbContract.SetlistsDbDocumentEntry.DOCUMENT_TYPE_IMAGE:
                                        type = "image";
                                        break;
                                    case SetlistsDbContract.SetlistsDbDocumentEntry.DOCUMENT_TYPE_PDF:
                                        type = "application/pdf";
                                        break;
                                    default:
                                        type = "text/plain";
                                }
                                int modified = cursor.getInt(cursor.getColumnIndex(SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_DATE_MODIFIED));

                                ContentValues values = new ContentValues();
                                values.put("id", id);
                                values.put("path", path);
                                values.put("description", description);
                                values.put("type", type);
                                values.put("modified", modified);
                                documents.add(values);
                            } while (cursor.moveToNext());
                        }
                        if (cursor != null)
                            cursor.close();
                        if (!documents.isEmpty()) {
                            syncFiles(user, documents, new SyncTableListener() {
                                @Override
                                public void onFailure(Exception exception) {

                                }

                                @Override
                                public void onComplete() {
                                    callback.onComplete();
                                }
                            });
                        } else {
                            callback.onComplete();
                        }
                    }
                });
    }

    private void syncSets(FirebaseUser user, final SyncTableListener callback) {
        syncTable(SetlistsDbContract.SetlistsDbSetEntry.CONTENT_URI, SetlistsDbContract.SetlistsDbSetEntry.TABLE_NAME,
                new String[]{SetlistsDbContract.SetlistsDbSetEntry.COLUMN_ID,
                        SetlistsDbContract.SetlistsDbSetEntry.COLUMN_NAME,
                        SetlistsDbContract.SetlistsDbSetEntry.COLUMN_BAND,
                        SetlistsDbContract.SetlistsDbSetEntry.COLUMN_LOCATION,
                        SetlistsDbContract.SetlistsDbSetEntry.COLUMN_DATE_ADDED,
                        SetlistsDbContract.SetlistsDbSetEntry.COLUMN_DATE_MODIFIED,
                        SetlistsDbContract.SetlistsDbSetEntry.COLUMN_USER},
                new int[]{Cursor.FIELD_TYPE_STRING,
                        Cursor.FIELD_TYPE_STRING,
                        Cursor.FIELD_TYPE_STRING,
                        Cursor.FIELD_TYPE_STRING,
                        Cursor.FIELD_TYPE_INTEGER,
                        Cursor.FIELD_TYPE_INTEGER,
                        Cursor.FIELD_TYPE_STRING}, user, new SyncTableListener() {
                    @Override
                    public void onFailure(Exception exception) {
                        callback.onFailure(exception);
                    }

                    @Override
                    public void onComplete() {
                        callback.onComplete();
                    }
                });
    }

    private void syncSetSongs(FirebaseUser user, final SyncTableListener callback) {
        syncTable(SetlistsDbContract.SetlistsDbSetSongEntry.CONTENT_URI, SetlistsDbContract.SetlistsDbSetSongEntry.TABLE_NAME,
                new String[]{SetlistsDbContract.SetlistsDbSetSongEntry.COLUMN_ID,
                        SetlistsDbContract.SetlistsDbSetSongEntry.COLUMN_SETLIST,
                        SetlistsDbContract.SetlistsDbSetSongEntry.COLUMN_SONG,
                        SetlistsDbContract.SetlistsDbSetSongEntry.COLUMN_SEQUENCE,
                        SetlistsDbContract.SetlistsDbSetSongEntry.COLUMN_DATE_ADDED,
                        SetlistsDbContract.SetlistsDbSetSongEntry.COLUMN_DATE_MODIFIED,
                        SetlistsDbContract.SetlistsDbSetSongEntry.COLUMN_USER},
                new int[]{Cursor.FIELD_TYPE_STRING,
                        Cursor.FIELD_TYPE_STRING,
                        Cursor.FIELD_TYPE_STRING,
                        Cursor.FIELD_TYPE_INTEGER,
                        Cursor.FIELD_TYPE_INTEGER,
                        Cursor.FIELD_TYPE_INTEGER,
                        Cursor.FIELD_TYPE_STRING}, user, new SyncTableListener() {
                    @Override
                    public void onFailure(Exception exception) {
                        callback.onFailure(exception);
                    }

                    @Override
                    public void onComplete() {
                        callback.onComplete();
                    }
                });
    }

    private void syncMidiMessages(FirebaseUser user, final SyncTableListener callback) {
        syncTable(SetlistsDbContract.SetlistsDbMidiMessageEntry.CONTENT_URI, SetlistsDbContract.SetlistsDbMidiMessageEntry.TABLE_NAME,
                new String[]{SetlistsDbContract.SetlistsDbMidiMessageEntry.COLUMN_ID,
                        SetlistsDbContract.SetlistsDbMidiMessageEntry.COLUMN_NAME,
                        SetlistsDbContract.SetlistsDbMidiMessageEntry.COLUMN_CHANNEL,
                        SetlistsDbContract.SetlistsDbMidiMessageEntry.COLUMN_STATUS,
                        SetlistsDbContract.SetlistsDbMidiMessageEntry.COLUMN_DATA1,
                        SetlistsDbContract.SetlistsDbMidiMessageEntry.COLUMN_DATA2,
                        SetlistsDbContract.SetlistsDbMidiMessageEntry.COLUMN_DATE_ADDED,
                        SetlistsDbContract.SetlistsDbMidiMessageEntry.COLUMN_DATE_MODIFIED,
                        SetlistsDbContract.SetlistsDbMidiMessageEntry.COLUMN_USER},
                new int[]{Cursor.FIELD_TYPE_STRING,
                        Cursor.FIELD_TYPE_STRING,
                        Cursor.FIELD_TYPE_INTEGER,
                        Cursor.FIELD_TYPE_INTEGER,
                        Cursor.FIELD_TYPE_INTEGER,
                        Cursor.FIELD_TYPE_INTEGER,
                        Cursor.FIELD_TYPE_INTEGER,
                        Cursor.FIELD_TYPE_INTEGER,
                        Cursor.FIELD_TYPE_STRING}, user, new SyncTableListener() {
                    @Override
                    public void onFailure(Exception exception) {
                        callback.onFailure(exception);
                    }

                    @Override
                    public void onComplete() {
                        callback.onComplete();
                    }
                });
    }

    private void syncDocumentMidiMessages(FirebaseUser user, final SyncTableListener callback) {
        syncTable(SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.CONTENT_URI, SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.TABLE_NAME,
                new String[]{SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.COLUMN_ID,
                        SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.COLUMN_DOCUMENT,
                        SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.COLUMN_MESSAGE,
                        SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.COLUMN_AUTOSEND,
                        SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.COLUMN_DATE_ADDED,
                        SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.COLUMN_DATE_MODIFIED,
                        SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.COLUMN_USER},
                new int[]{Cursor.FIELD_TYPE_STRING,
                        Cursor.FIELD_TYPE_STRING,
                        Cursor.FIELD_TYPE_STRING,
                        Cursor.FIELD_TYPE_INTEGER,
                        Cursor.FIELD_TYPE_INTEGER,
                        Cursor.FIELD_TYPE_INTEGER,
                        Cursor.FIELD_TYPE_STRING}, user, new SyncTableListener() {
                    @Override
                    public void onFailure(Exception exception) {
                        callback.onFailure(exception);
                    }

                    @Override
                    public void onComplete() {
                        callback.onComplete();
                    }
                });
    }

    private void syncFiles(final FirebaseUser user, final ArrayList<ContentValues> list, final SyncTableListener callback) {
        ContentValues item = list.remove(0);
        String path = item.getAsString("path");
        String id = item.getAsString("id");
        String description = item.getAsString("description");
        String contentType = item.getAsString("type");
        int modified = item.getAsInteger("modified");
        Storage.syncFile(user, new File(path), id, description, contentType, modified, new Storage.FirebaseStorageListener() {
            @Override
            public void onFailure(Exception exception) {
                Log.d("UPLOAD FAILURE", exception.getMessage());
                if (!list.isEmpty())
                    syncFiles(user, list, callback);
            }

            @Override
            public void onComplete() {
                if (!list.isEmpty())
                    syncFiles(user, list, callback);
                else {
                    callback.onComplete();
                }
            }
        });
    }

    private JSONObject findRecord(JSONArray array, String key, String value) {
        for (int i = 0; i < array.length(); i++) {
            try {
                JSONObject o = array.getJSONObject(i);
                if (o.optString(key).equals(value)) {
                    return o;
                }
            } catch (JSONException ex) { Log.d("JSON EXCEPTION", ex.getMessage()); }
        }
        return null;
    }

    private void syncTable(final Uri uri, final String table, final String[] columns, final int[] types, final FirebaseUser user, final SyncTableListener callback) {
        Uri.Builder builder = new Uri.Builder();
        Uri requestUri = builder.scheme("http").path(BASE_API_URL).appendPath(table).appendQueryParameter("uid", user.getUid()).build();
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, requestUri.toString(), null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d("JSON RESPONSE", response.toString());
                        Cursor local =  getContentResolver().query(uri,null,null,null, null);
                        if (local != null && local.moveToFirst()) {
                            do {
                                String id = local.getString(local.getColumnIndex("_id"));
                                JSONObject remote = findRecord(response, "_id", id);
                                if (remote != null) {
                                    int remoteDate = remote.optInt("modified");
                                    int localDate = local.getInt(local.getColumnIndex("modified"));
                                    if (remoteDate > localDate) {
                                        Log.d("UPDATING LOCAL RECORD", id);
                                        ContentValues values = new ContentValues();
                                        for (int i = 0; i < columns.length; i++) {
                                            if (remote.isNull(columns[i])) {
                                                values.putNull(columns[i]);
                                            } else {
                                                switch (types[i]) {
                                                    case Cursor.FIELD_TYPE_STRING:
                                                        values.put(columns[i], remote.optString(columns[i]));
                                                        break;
                                                    case Cursor.FIELD_TYPE_INTEGER:
                                                        values.put(columns[i], remote.optInt(columns[i]));
                                                        break;
                                                    case Cursor.FIELD_TYPE_FLOAT:
                                                        values.put(columns[i], remote.optDouble(columns[i]));
                                                        break;
                                                }
                                            }
                                        }
                                        getContentResolver().update(uri.buildUpon().appendPath(id).build(), values,"_id=?", new String[] {String.valueOf(id)});
                                    } else if (remoteDate < localDate) {
                                        Log.d("UPDATING REMOTE RECORD", id);
                                        final JSONObject json = new JSONObject();
                                        try {
                                            for (int i = 0; i < columns.length; i++) {
                                                if (local.getType(local.getColumnIndex(columns[i])) == Cursor.FIELD_TYPE_NULL)
                                                    json.put(columns[i], null);
                                                else {
                                                    switch (types[i]) {
                                                        case Cursor.FIELD_TYPE_STRING:
                                                            json.put(columns[i], local.getString(local.getColumnIndex(columns[i])));
                                                            break;
                                                        case Cursor.FIELD_TYPE_INTEGER:
                                                            json.put(columns[i], local.getInt(local.getColumnIndex(columns[i])));
                                                            break;
                                                        case Cursor.FIELD_TYPE_FLOAT:
                                                            json.put(columns[i], local.getDouble(local.getColumnIndex(columns[i])));
                                                            break;
                                                    }
                                                }
                                            }
                                            updateRemoteRecord(table, id, json, user);
                                        } catch (Exception ex) { Log.d("EXCEPTION", ex.getMessage()); }
                                    }
                                } else {
                                    Log.d("INSERTING REMOTE RECORD", id);
                                    final JSONObject json = new JSONObject();
                                    try {
                                        for (int i = 0; i < columns.length; i++) {
                                            if (local.getType(local.getColumnIndex(columns[i])) == Cursor.FIELD_TYPE_NULL)
                                                json.put(columns[i], null);
                                            else {
                                                switch (types[i]) {
                                                    case Cursor.FIELD_TYPE_STRING:
                                                        json.put(columns[i], local.getString(local.getColumnIndex(columns[i])));
                                                        break;
                                                    case Cursor.FIELD_TYPE_INTEGER:
                                                        json.put(columns[i], local.getInt(local.getColumnIndex(columns[i])));
                                                        break;
                                                    case Cursor.FIELD_TYPE_FLOAT:
                                                        json.put(columns[i], local.getDouble(local.getColumnIndex(columns[i])));
                                                        break;
                                                }
                                            }
                                        }
                                        insertRemoteRecord(table, json, user);
                                    } catch (JSONException ex) { Log.d("JSON EXCEPTION", ex.getMessage()); }
                                }
                            } while (local.moveToNext());
                        }
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject remote = response.optJSONObject(i);
                            String id = remote.optString("_id");
                            boolean found = false;
                            if (local != null && local.moveToFirst()) {
                                do {
                                    if (local.getString(local.getColumnIndex("_id")).equals(id)) {
                                        found = true;
                                        break;
                                    }
                                } while (local.moveToNext());
                            }
                            if (!found) {
                                Log.d("INSERTING LOCAL RECORD", id);
                                ContentValues values = new ContentValues();
                                for (int j = 0; j < columns.length; j++) {
                                    if (remote.isNull(columns[j]))
                                        values.putNull(columns[j]);
                                    else {
                                        switch (types[j]) {
                                            case Cursor.FIELD_TYPE_STRING:
                                                values.put(columns[j], remote.optString(columns[j]));
                                                break;
                                            case Cursor.FIELD_TYPE_INTEGER:
                                                values.put(columns[j], remote.optInt(columns[j]));
                                                break;
                                            case Cursor.FIELD_TYPE_FLOAT:
                                                values.put(columns[j], remote.optDouble(columns[j]));
                                                break;
                                        }
                                    }
                                }
                                getContentResolver().insert(uri, values);
                            }
                        }
                        if (local != null)
                            local.close();

                        callback.onComplete();

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("ERROR RESPONSE", error.getMessage());
                callback.onFailure(error);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json; charset=UTF-8");
                params.put("Authorization", "Token 1fa3a854e1f7dac367f57fd81ef91198fe365d63");
                return params;
            }
        };

        queue.add(jsonArrayRequest);
    }

    private void syncDeletedRecords(final FirebaseUser user, final SyncTableListener callback) {
        Uri.Builder builder = new Uri.Builder();
        Uri requestUri = builder.scheme("http").path(BASE_API_URL)
                .appendPath(SetlistsDbContract.SetlistsDbDeletedRecordEntry.TABLE_NAME)
                .appendQueryParameter("uid", user.getUid()).build();
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, requestUri.toString(), null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d("JSON RESPONSE", response.toString());
                        Cursor local =  getContentResolver().query(SetlistsDbContract.SetlistsDbDeletedRecordEntry.CONTENT_URI,null,null,null, null);
                        if (local != null && local.moveToFirst()) {
                            do {
                                String id = local.getString(local.getColumnIndex("_id"));
                                JSONObject remote = findRecord(response, "_id", id);
                                if (remote == null) {
                                    deleteRemoteRecord(local.getString(local.getColumnIndex(SetlistsDbContract.SetlistsDbDeletedRecordEntry.COLUMN_TABLE)), id, user);
                                }
                            } while (local.moveToNext());
                        }
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject remote = response.optJSONObject(i);
                            String id = remote.optString("_id");
                            boolean found = false;
                            if (local != null && local.moveToFirst()) {
                                do {
                                    if (local.getString(local.getColumnIndex("_id")).equals(id)) {
                                        found = true;
                                        break;
                                    }
                                } while (local.moveToNext());
                            }
                            if (!found) {
                                switch (remote.optString(SetlistsDbContract.SetlistsDbDeletedRecordEntry.COLUMN_TABLE)) {
                                    case SetlistsDbContract.SetlistsDbBandEntry.TABLE_NAME:
                                        getContentResolver().delete(SetlistsDbContract.SetlistsDbBandEntry.buildSetlistsDbBandUri(id),
                                            SetlistsDbContract.SetlistsDbBandEntry.COLUMN_ID + "=?", new String[] {String.valueOf(id)});
                                        break;
                                    case SetlistsDbContract.SetlistsDbMusicianEntry.TABLE_NAME:
                                        getContentResolver().delete(SetlistsDbContract.SetlistsDbMusicianEntry.buildSetlistsDbMusicianUri(id),
                                                SetlistsDbContract.SetlistsDbMusicianEntry.COLUMN_ID + "=?", new String[] {String.valueOf(id)});
                                        break;
                                    case SetlistsDbContract.SetlistsDbArtistEntry.TABLE_NAME:
                                        getContentResolver().delete(SetlistsDbContract.SetlistsDbArtistEntry.buildSetlistsDbArtistUri(id),
                                                SetlistsDbContract.SetlistsDbArtistEntry.COLUMN_ID + "=?", new String[] {String.valueOf(id)});
                                        break;
                                    case SetlistsDbContract.SetlistsDbSongEntry.TABLE_NAME:
                                        getContentResolver().delete(SetlistsDbContract.SetlistsDbSongEntry.buildSetlistsDbSongUri(id),
                                                SetlistsDbContract.SetlistsDbSongEntry.COLUMN_ID + "=?", new String[] {String.valueOf(id)});
                                        break;
                                    case SetlistsDbContract.SetlistsDbDocumentEntry.TABLE_NAME:
                                        getContentResolver().delete(SetlistsDbContract.SetlistsDbDocumentEntry.buildSetlistsDbDocumentUri(id),
                                                SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_ID + "=?", new String[] {String.valueOf(id)});
                                        break;
                                    case SetlistsDbContract.SetlistsDbMidiMessageEntry.TABLE_NAME:
                                        getContentResolver().delete(SetlistsDbContract.SetlistsDbMidiMessageEntry.buildSetlistsDbMidiMessageUri(id),
                                                SetlistsDbContract.SetlistsDbMidiMessageEntry.COLUMN_ID + "=?", new String[] {String.valueOf(id)});
                                        break;
                                    case SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.TABLE_NAME:
                                        getContentResolver().delete(SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.buildSetlistsDbDocumentMidiMessageUri(id),
                                                SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.COLUMN_ID + "=?", new String[] {String.valueOf(id)});
                                        break;
                                    case SetlistsDbContract.SetlistsDbSetEntry.TABLE_NAME:
                                        getContentResolver().delete(SetlistsDbContract.SetlistsDbSetEntry.buildSetlistsDbSetUri(id),
                                                SetlistsDbContract.SetlistsDbSetEntry.COLUMN_ID + "=?", new String[] {String.valueOf(id)});
                                        break;
                                    case SetlistsDbContract.SetlistsDbSetSongEntry.TABLE_NAME:
                                        getContentResolver().delete(SetlistsDbContract.SetlistsDbSetSongEntry.buildSetlistsDbSetSongUri(id),
                                                SetlistsDbContract.SetlistsDbSetSongEntry.COLUMN_ID + "=?", new String[] {String.valueOf(id)});
                                        break;
                                }
                            }
                        }
                        if (local != null)
                            local.close();

                        callback.onComplete();

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("ERROR RESPONSE", error.getMessage());
                callback.onFailure(error);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json; charset=UTF-8");
                params.put("Authorization", "Token 1fa3a854e1f7dac367f57fd81ef91198fe365d63");
                return params;
            }
        };

        queue.add(jsonArrayRequest);
    }


    private void updateRemoteRecord(String table, String id, JSONObject jsonObject, FirebaseUser user) {
        Uri.Builder builder = new Uri.Builder();
        Uri uri = builder.scheme("http").path(BASE_API_URL).appendPath(table).appendPath(id).appendPath("").appendQueryParameter("uid", user.getUid()).build();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, uri.toString(), jsonObject,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response) {
                        // response
                        Log.d("UPDATE REMOTE", response.toString());
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("UPDATE REMOTE ERROR", error.toString());
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json; charset=UTF-8");
                params.put("Authorization", "Token 1fa3a854e1f7dac367f57fd81ef91198fe365d63");
                return params;
            }
        };
        queue.add(jsonObjectRequest);
    }

    private void insertRemoteRecord(String table, JSONObject jsonObject, FirebaseUser user) {
        Uri.Builder builder = new Uri.Builder();
        Uri uri = builder.scheme("http").path(BASE_API_URL).appendPath(table).appendPath("").appendQueryParameter("uid", user.getUid()).build();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, uri.toString(), jsonObject,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response) {
                        // response
                        Log.d("INSERT REMOTE", response.toString());
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("INSERT REMOTE ERROR", error.toString());
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json; charset=UTF-8");
                params.put("Authorization", "Token 1fa3a854e1f7dac367f57fd81ef91198fe365d63");
                return params;
            }
        };
        queue.add(jsonObjectRequest);
    }

    private void deleteRemoteRecord(String table, String id, FirebaseUser user) {
        Uri.Builder builder = new Uri.Builder();
        Uri uri = builder.scheme("http").path(BASE_API_URL).appendPath(table).appendPath(id).appendPath("").appendQueryParameter("uid", user.getUid()).build();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.DELETE, uri.toString(), null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response) {
                        // response
                        Log.d("UPDATE REMOTE", response.toString());
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("UPDATE REMOTE ERROR", error.toString());
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json; charset=UTF-8");
                params.put("Authorization", "Token 1fa3a854e1f7dac367f57fd81ef91198fe365d63");
                return params;
            }
        };
        queue.add(jsonObjectRequest);
    }

    private void sendNotification() {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, "Chordiste")
                .setSmallIcon(R.drawable.midi_icon)
                .setContentTitle("Chordiste Sync Service")
                .setContentText("Synchronization completed successfully.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(0, mBuilder.build());
    }

    public interface SyncTableListener {
        void onFailure(Exception exception);
        void onComplete();
    }
}
