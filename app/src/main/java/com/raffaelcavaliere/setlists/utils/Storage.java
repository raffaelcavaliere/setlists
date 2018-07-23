package com.raffaelcavaliere.setlists.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.raffaelcavaliere.setlists.data.SetlistsDbDocumentLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * Created by raffaelcavaliere on 2017-06-10.
 */

public class Storage {

    static FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();

    public static void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        try {
            OutputStream out = new FileOutputStream(dst);
            try {
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            } finally {
                out.close();
            }
        } finally {
            in.close();
        }
    }

    public static void replace(File src, String data) throws IOException {
        OutputStream out = new FileOutputStream(src, false);
        try {
            out.write(data.getBytes(Charset.defaultCharset()));
        } finally {
            out.close();
        }
    }

    public static void syncFile(FirebaseUser user, final File file, final String name, final String description,
                                  final String contentType, final int modified, final FirebaseStorageListener callback) {

        StorageReference storageRef = firebaseStorage.getReference();
        final StorageReference documentRef = storageRef.child("documents/" + user.getUid() + "/" + name);

        documentRef.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
            @Override
            public void onSuccess(StorageMetadata storageMetadata) {
                if (!file.exists()) {
                    downloadFile(file, documentRef, callback);
                }
                else {
                    int modified_server = 0;
                    try {
                        modified_server = Integer.valueOf(storageMetadata.getCustomMetadata("Modified"));
                    } catch (Exception ex) {}
                    if (modified > modified_server) {
                        uploadFile(file, name, description, contentType, modified, documentRef, callback);
                    } else {
                        callback.onComplete();
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d("FIREBASE EXCEPTION", exception.getMessage());
                uploadFile(file, name, description, contentType, modified, documentRef, callback);
            }
        });
    }

    private static void uploadFile(File file, String name, String description, String contentType,
                                   int modified, StorageReference documentRef, final FirebaseStorageListener callback) {

        StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType(contentType)
                .setCustomMetadata("Description", description)
                .setCustomMetadata("Modified", String.valueOf(modified))
                .build();

        Uri uri = Uri.fromFile(file);
        documentRef.putFile(uri, metadata).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                callback.onFailure(exception);
                Log.d("FIREBASE EXCEPTION", exception.getMessage());
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                Uri uploadUri = taskSnapshot.getUploadSessionUri();
                Log.d("FIREBASE SUCCESS", uploadUri.toString());
            }
        }).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                Uri uploadUri = task.getResult().getUploadSessionUri();
                callback.onComplete();
                Log.d("FIREBASE FINISH", uploadUri.toString());
            }
        });
    }

    private static void downloadFile(File file, StorageReference documentRef, final FirebaseStorageListener callback) {

        documentRef.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                long bytes = taskSnapshot.getTotalByteCount();
                callback.onComplete();
                Log.d("FIREBASE SUCCESS", String.valueOf(bytes) + " bytes downloaded.");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                callback.onFailure(exception);
                Log.d("FIREBASE EXCEPTION", exception.getMessage());
            }
        });
    }

    public interface FirebaseStorageListener {
        void onFailure(Exception exception);
        void onComplete();
    }
}
