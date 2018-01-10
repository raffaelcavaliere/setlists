package com.raffaelcavaliere.setlists.services;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.raffaelcavaliere.setlists.data.SetlistsDbContract;
import com.raffaelcavaliere.setlists.utils.Storage;

import java.io.File;
import java.util.ArrayList;

public class SyncService extends IntentService {
    private static final String ACTION_SYNC = "com.raffaelcavaliere.setlists.services.action.sync";
    static FirebaseAuth mAuth = FirebaseAuth.getInstance();

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
            final String action = intent.getAction();
            if (ACTION_SYNC.equals(action)) {
                sync();
            }
        }
    }

    private void sync() {

        ArrayList<ContentValues> documents = new ArrayList<ContentValues>();
        Cursor cursor = getContentResolver().query(SetlistsDbContract.SetlistsDbDocumentEntry.CONTENT_URI, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                String id = cursor.getString(cursor.getColumnIndex(SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_ID));
                String path = getFilesDir().getPath() + "/" + id;
                ContentValues values = new ContentValues();
                values.put("id", id);
                values.put("path", path);
                documents.add(values);
            } while (cursor.moveToNext());
        }
        cursor.close();
        if (!documents.isEmpty())
            upload(documents);
    }

    private void upload(final ArrayList<ContentValues> list) {
        ContentValues item = list.remove(0);
        String path = item.getAsString("path");
        String id = item.getAsString("id");
        Storage.uploadFile(new File(path), id, new Storage.FirebaseStorageListener() {
            @Override
            public void onFailure(Exception exception) {
                Log.d("UPLOAD FAILURE", exception.getMessage());
                if (!list.isEmpty())
                    upload(list);
            }

            @Override
            public void onComplete() {
                if (!list.isEmpty())
                    upload(list);
            }
        });
    }
}
