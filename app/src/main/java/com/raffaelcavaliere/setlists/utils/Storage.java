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
import com.google.firebase.storage.FirebaseStorage;
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
    static FirebaseAuth mAuth = FirebaseAuth.getInstance();

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

    public static void uploadFile(File file, String name, final FirebaseStorageListener callback) {

        FirebaseUser currentUser = mAuth.getCurrentUser();
        StorageReference storageRef = firebaseStorage.getReference();
        StorageReference documentRef = storageRef.child("documents/" + currentUser.getUid() + "/" + name);

        Uri uri = Uri.fromFile(file);
        UploadTask uploadTask = documentRef.putFile(uri);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                callback.onFailure(exception);
                Log.d("EXCEPTION FIREBASE", exception.getMessage());
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                Log.d("FIREBASE SUCCESS", downloadUrl.toString());
            }
        }).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                Uri downloadUrl = task.getResult().getDownloadUrl();
                callback.onComplete();
                Log.d("FIREBASE FINISH", downloadUrl.toString());
            }
        });
    }

    public interface FirebaseStorageListener {
        void onFailure(Exception exception);
        void onComplete();
    }
}
