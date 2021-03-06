package com.raffaelcavaliere.setlists.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.raffaelcavaliere.setlists.widget.SetlistsWidget;

import java.util.Date;
import java.util.UUID;

public class SetlistsContentProvider extends ContentProvider {

    static final int ARTIST = 100;
    static final int ARTIST_BY_ID = 101;
    static final int ARTIST_SONG = 102;
    static final int SONG = 200;
    static final int SONG_BY_ID = 201;
    static final int DOCUMENT = 300;
    static final int DOCUMENT_BY_ID = 301;
    static final int SONG_DOCUMENT = 302;
    static final int BAND = 400;
    static final int BAND_BY_ID = 401;
    static final int BAND_MUSICIAN = 402;
    static final int MUSICIAN = 500;
    static final int MUSICIAN_BY_ID = 501;
    static final int SET = 600;
    static final int SET_BY_ID = 601;
    static final int SET_SONG = 700;
    static final int SET_SONG_BY_ID = 701;
    static final int SET_SET_SONG = 702;
    static final int MIDI_MESSAGE = 800;
    static final int MIDI_MESSAGE_BY_ID = 801;
    static final int DOCUMENT_MIDI_MESSAGE = 802;
    static final int DOCUMENT_MIDI_MESSAGE_BY_ID = 803;
    static final int SET_DOCUMENT_MIDI_MESSAGE = 804;
    static final int DOCUMENT_DOCUMENT_MIDI_MESSAGE = 805;
    static final int DELETED_RECORD = 900;
    static final int DELETED_RECORD_BY_ID = 901;

    private SetlistsDatabase database;
    private final static UriMatcher uriMatcher = buildUriMatcher();
    static FirebaseAuth mAuth = FirebaseAuth.getInstance();

    public SetlistsContentProvider() {
    }

    static UriMatcher buildUriMatcher() {

        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = SetlistsDbContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, SetlistsDbContract.PATH_ARTIST, ARTIST);
        matcher.addURI(authority, SetlistsDbContract.PATH_ARTIST + "/*", ARTIST_BY_ID);
        matcher.addURI(authority, SetlistsDbContract.PATH_ARTIST + "/*/" + SetlistsDbContract.PATH_SONG, ARTIST_SONG);
        matcher.addURI(authority, SetlistsDbContract.PATH_SONG, SONG);
        matcher.addURI(authority, SetlistsDbContract.PATH_SONG + "/*", SONG_BY_ID);
        matcher.addURI(authority, SetlistsDbContract.PATH_SONG + "/*/" + SetlistsDbContract.PATH_DOCUMENT, SONG_DOCUMENT);
        matcher.addURI(authority, SetlistsDbContract.PATH_DOCUMENT, DOCUMENT);
        matcher.addURI(authority, SetlistsDbContract.PATH_DOCUMENT + "/*", DOCUMENT_BY_ID);
        matcher.addURI(authority, SetlistsDbContract.PATH_BAND, BAND);
        matcher.addURI(authority, SetlistsDbContract.PATH_BAND + "/*", BAND_BY_ID);
        matcher.addURI(authority, SetlistsDbContract.PATH_BAND + "/*/" + SetlistsDbContract.PATH_MUSICIAN, BAND_MUSICIAN);
        matcher.addURI(authority, SetlistsDbContract.PATH_MUSICIAN, MUSICIAN);
        matcher.addURI(authority, SetlistsDbContract.PATH_MUSICIAN + "/*", MUSICIAN_BY_ID);
        matcher.addURI(authority, SetlistsDbContract.PATH_SET, SET);
        matcher.addURI(authority, SetlistsDbContract.PATH_SET + "/*", SET_BY_ID);
        matcher.addURI(authority, SetlistsDbContract.PATH_SET_SONG, SET_SONG);
        matcher.addURI(authority, SetlistsDbContract.PATH_SET_SONG + "/*", SET_SONG_BY_ID);
        matcher.addURI(authority, SetlistsDbContract.PATH_SET + "/*/" + SetlistsDbContract.PATH_SET_SONG, SET_SET_SONG);
        matcher.addURI(authority, SetlistsDbContract.PATH_MIDI_MESSAGE, MIDI_MESSAGE);
        matcher.addURI(authority, SetlistsDbContract.PATH_MIDI_MESSAGE + "/*", MIDI_MESSAGE_BY_ID);
        matcher.addURI(authority, SetlistsDbContract.PATH_DOCUMENT_MIDI_MESSAGE, DOCUMENT_MIDI_MESSAGE);
        matcher.addURI(authority, SetlistsDbContract.PATH_DOCUMENT_MIDI_MESSAGE + "/*", DOCUMENT_MIDI_MESSAGE_BY_ID);
        matcher.addURI(authority, SetlistsDbContract.PATH_SET + "/*/" + SetlistsDbContract.PATH_DOCUMENT_MIDI_MESSAGE, SET_DOCUMENT_MIDI_MESSAGE);
        matcher.addURI(authority, SetlistsDbContract.PATH_DOCUMENT + "/*/" + SetlistsDbContract.PATH_DOCUMENT_MIDI_MESSAGE, DOCUMENT_DOCUMENT_MIDI_MESSAGE);
        matcher.addURI(authority, SetlistsDbContract.PATH_DELETED_RECORD, DELETED_RECORD);
        matcher.addURI(authority, SetlistsDbContract.PATH_DELETED_RECORD + "/*", DELETED_RECORD_BY_ID);

        return matcher;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        switch (uriMatcher.match(uri)) {
            case ARTIST:
                return SetlistsDbContract.SetlistsDbArtistEntry.CONTENT_TYPE;
            case ARTIST_BY_ID:
                return SetlistsDbContract.SetlistsDbArtistEntry.CONTENT_ITEM_TYPE;
            case ARTIST_SONG:
                return SetlistsDbContract.SetlistsDbSongEntry.CONTENT_TYPE;
            case SONG:
                return SetlistsDbContract.SetlistsDbSongEntry.CONTENT_TYPE;
            case SONG_BY_ID:
                return SetlistsDbContract.SetlistsDbSongEntry.CONTENT_ITEM_TYPE;
            case SONG_DOCUMENT:
                return SetlistsDbContract.SetlistsDbDocumentEntry.CONTENT_TYPE;
            case DOCUMENT:
                return SetlistsDbContract.SetlistsDbDocumentEntry.CONTENT_TYPE;
            case DOCUMENT_BY_ID:
                return SetlistsDbContract.SetlistsDbDocumentEntry.CONTENT_ITEM_TYPE;
            case BAND:
                return SetlistsDbContract.SetlistsDbBandEntry.CONTENT_TYPE;
            case BAND_BY_ID:
                return SetlistsDbContract.SetlistsDbBandEntry.CONTENT_ITEM_TYPE;
            case BAND_MUSICIAN:
                return SetlistsDbContract.SetlistsDbMusicianEntry.CONTENT_TYPE;
            case MUSICIAN:
                return SetlistsDbContract.SetlistsDbMusicianEntry.CONTENT_TYPE;
            case MUSICIAN_BY_ID:
                return SetlistsDbContract.SetlistsDbMusicianEntry.CONTENT_ITEM_TYPE;
            case SET:
                return SetlistsDbContract.SetlistsDbSetEntry.CONTENT_TYPE;
            case SET_BY_ID:
                return SetlistsDbContract.SetlistsDbSetEntry.CONTENT_ITEM_TYPE;
            case SET_SONG:
                return SetlistsDbContract.SetlistsDbSetSongEntry.CONTENT_TYPE;
            case SET_SONG_BY_ID:
                return SetlistsDbContract.SetlistsDbSetSongEntry.CONTENT_ITEM_TYPE;
            case SET_SET_SONG:
                return SetlistsDbContract.SetlistsDbSetSongEntry.CONTENT_TYPE;
            case MIDI_MESSAGE:
                return SetlistsDbContract.SetlistsDbSetEntry.CONTENT_TYPE;
            case MIDI_MESSAGE_BY_ID:
                return SetlistsDbContract.SetlistsDbSetEntry.CONTENT_ITEM_TYPE;
            case DOCUMENT_MIDI_MESSAGE:
                return SetlistsDbContract.SetlistsDbSetSongEntry.CONTENT_TYPE;
            case DOCUMENT_MIDI_MESSAGE_BY_ID:
                return SetlistsDbContract.SetlistsDbSetSongEntry.CONTENT_ITEM_TYPE;
            case DOCUMENT_DOCUMENT_MIDI_MESSAGE:
                return SetlistsDbContract.SetlistsDbSetSongEntry.CONTENT_TYPE;
            case DELETED_RECORD:
                return SetlistsDbContract.SetlistsDbDeletedRecordEntry.CONTENT_TYPE;
            case DELETED_RECORD_BY_ID:
                return SetlistsDbContract.SetlistsDbDeletedRecordEntry.CONTENT_ITEM_TYPE;
            default:
                return null;
        }
    }


    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = database.getWritableDatabase();
        int result;

        switch (uriMatcher.match(uri)) {
            case ARTIST_BY_ID: {
                result = db.delete(SetlistsDbContract.SetlistsDbArtistEntry.TABLE_NAME, selection, selectionArgs);
                if (result < 0)
                    throw new SQLException("Problem while deleting into uri: " + uri);
                else {
                    ContentValues values = new ContentValues();
                    values.put(SetlistsDbContract.SetlistsDbDeletedRecordEntry.COLUMN_ID, selectionArgs[0]);
                    values.put(SetlistsDbContract.SetlistsDbDeletedRecordEntry.COLUMN_TABLE, "artist");
                    values.put(SetlistsDbContract.SetlistsDbDeletedRecordEntry.COLUMN_DATE_DELETED, new Date().getTime() / 1000);

                    insert(SetlistsDbContract.SetlistsDbDeletedRecordEntry.CONTENT_URI, values);
                }
                break;
            }
            case SONG_BY_ID: {
                result = db.delete(SetlistsDbContract.SetlistsDbSongEntry.TABLE_NAME, selection, selectionArgs);
                if (result < 0)
                    throw new SQLException("Problem while deleting into uri: " + uri);
                else {
                    ContentValues values = new ContentValues();
                    values.put(SetlistsDbContract.SetlistsDbDeletedRecordEntry.COLUMN_ID, selectionArgs[0]);
                    values.put(SetlistsDbContract.SetlistsDbDeletedRecordEntry.COLUMN_TABLE, "song");
                    values.put(SetlistsDbContract.SetlistsDbDeletedRecordEntry.COLUMN_DATE_DELETED, new Date().getTime() / 1000);

                    insert(SetlistsDbContract.SetlistsDbDeletedRecordEntry.CONTENT_URI, values);
                }
                SetlistsWidget.sendRefreshBroadcast(getContext());
                break;
            }
            case DOCUMENT_BY_ID: {
                result = db.delete(SetlistsDbContract.SetlistsDbDocumentEntry.TABLE_NAME, selection, selectionArgs);
                if (result < 0)
                    throw new SQLException("Problem while deleting into uri: " + uri);
                else {
                    ContentValues values = new ContentValues();
                    values.put(SetlistsDbContract.SetlistsDbDeletedRecordEntry.COLUMN_ID, selectionArgs[0]);
                    values.put(SetlistsDbContract.SetlistsDbDeletedRecordEntry.COLUMN_TABLE, "document");
                    values.put(SetlistsDbContract.SetlistsDbDeletedRecordEntry.COLUMN_DATE_DELETED, new Date().getTime() / 1000);

                    insert(SetlistsDbContract.SetlistsDbDeletedRecordEntry.CONTENT_URI, values);
                }
                break;
            }
            case BAND_BY_ID: {
                result = db.delete(SetlistsDbContract.SetlistsDbBandEntry.TABLE_NAME, selection, selectionArgs);
                if (result < 0)
                    throw new SQLException("Problem while deleting into uri: " + uri);
                else {
                    ContentValues values = new ContentValues();
                    values.put(SetlistsDbContract.SetlistsDbDeletedRecordEntry.COLUMN_ID, selectionArgs[0]);
                    values.put(SetlistsDbContract.SetlistsDbDeletedRecordEntry.COLUMN_TABLE, "band");
                    values.put(SetlistsDbContract.SetlistsDbDeletedRecordEntry.COLUMN_DATE_DELETED, new Date().getTime() / 1000);

                    insert(SetlistsDbContract.SetlistsDbDeletedRecordEntry.CONTENT_URI, values);
                }
                break;
            }
            case MUSICIAN_BY_ID: {
                result = db.delete(SetlistsDbContract.SetlistsDbMusicianEntry.TABLE_NAME, selection, selectionArgs);
                if (result < 0)
                    throw new SQLException("Problem while deleting into uri: " + uri);
                else {
                    ContentValues values = new ContentValues();
                    values.put(SetlistsDbContract.SetlistsDbDeletedRecordEntry.COLUMN_ID, selectionArgs[0]);
                    values.put(SetlistsDbContract.SetlistsDbDeletedRecordEntry.COLUMN_TABLE, "musician");
                    values.put(SetlistsDbContract.SetlistsDbDeletedRecordEntry.COLUMN_DATE_DELETED, new Date().getTime() / 1000);

                    insert(SetlistsDbContract.SetlistsDbDeletedRecordEntry.CONTENT_URI, values);
                }
                break;
            }
            case SET_BY_ID: {
                result = db.delete(SetlistsDbContract.SetlistsDbSetEntry.TABLE_NAME, selection, selectionArgs);
                if (result < 0)
                    throw new SQLException("Problem while deleting into uri: " + uri);
                else {
                    ContentValues values = new ContentValues();
                    values.put(SetlistsDbContract.SetlistsDbDeletedRecordEntry.COLUMN_ID, selectionArgs[0]);
                    values.put(SetlistsDbContract.SetlistsDbDeletedRecordEntry.COLUMN_TABLE, "setlist");
                    values.put(SetlistsDbContract.SetlistsDbDeletedRecordEntry.COLUMN_DATE_DELETED, new Date().getTime() / 1000);

                    insert(SetlistsDbContract.SetlistsDbDeletedRecordEntry.CONTENT_URI, values);
                }
                break;
            }
            case SET_SONG_BY_ID: {
                result = db.delete(SetlistsDbContract.SetlistsDbSetSongEntry.TABLE_NAME, selection, selectionArgs);
                if (result < 0)
                    throw new SQLException("Problem while deleting into uri: " + uri);
                else {
                    ContentValues values = new ContentValues();
                    values.put(SetlistsDbContract.SetlistsDbDeletedRecordEntry.COLUMN_ID, selectionArgs[0]);
                    values.put(SetlistsDbContract.SetlistsDbDeletedRecordEntry.COLUMN_TABLE, "setlist_song");
                    values.put(SetlistsDbContract.SetlistsDbDeletedRecordEntry.COLUMN_DATE_DELETED, new Date().getTime() / 1000);

                    insert(SetlistsDbContract.SetlistsDbDeletedRecordEntry.CONTENT_URI, values);
                }
                break;
            }
            case MIDI_MESSAGE_BY_ID: {
                result = db.delete(SetlistsDbContract.SetlistsDbMidiMessageEntry.TABLE_NAME, selection, selectionArgs);
                if (result < 0)
                    throw new SQLException("Problem while deleting into uri: " + uri);
                else {
                    ContentValues values = new ContentValues();
                    values.put(SetlistsDbContract.SetlistsDbDeletedRecordEntry.COLUMN_ID, selectionArgs[0]);
                    values.put(SetlistsDbContract.SetlistsDbDeletedRecordEntry.COLUMN_TABLE, "message");
                    values.put(SetlistsDbContract.SetlistsDbDeletedRecordEntry.COLUMN_DATE_DELETED, new Date().getTime() / 1000);

                    insert(SetlistsDbContract.SetlistsDbDeletedRecordEntry.CONTENT_URI, values);
                }
                break;
            }
            case DOCUMENT_MIDI_MESSAGE_BY_ID: {
                result = db.delete(SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.TABLE_NAME, selection, selectionArgs);
                if (result < 0)
                    throw new SQLException("Problem while deleting into uri: " + uri);
                else {
                    ContentValues values = new ContentValues();
                    values.put(SetlistsDbContract.SetlistsDbDeletedRecordEntry.COLUMN_ID, selectionArgs[0]);
                    values.put(SetlistsDbContract.SetlistsDbDeletedRecordEntry.COLUMN_TABLE, "document_message");
                    values.put(SetlistsDbContract.SetlistsDbDeletedRecordEntry.COLUMN_DATE_DELETED, new Date().getTime() / 1000);

                    insert(SetlistsDbContract.SetlistsDbDeletedRecordEntry.CONTENT_URI, values);
                }
                break;
            }
            default:
                throw new UnsupportedOperationException("Not yet implemented");
        }

        if (getContext() != null)
            getContext().getContentResolver().notifyChange(uri, null);
        db.close();
        return result;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        SQLiteDatabase db = database.getWritableDatabase();
        Uri resultUri;

        switch (uriMatcher.match(uri)) {
            case ARTIST: {
                values.put("user", mAuth.getCurrentUser().getUid());
                long rowid = db.insert(SetlistsDbContract.SetlistsDbArtistEntry.TABLE_NAME, null, values);
                if (rowid >= 0)
                    resultUri = uri.buildUpon().appendPath(values.getAsString("_id")).build();
                else
                    throw new SQLException("Problem while inserting into uri: " + uri);
                break;
            }
            case SONG: {
                values.put("user", mAuth.getCurrentUser().getUid());
                if (!values.containsKey("author"))
                    values.put("author", mAuth.getCurrentUser().getEmail());
                long rowid = db.insert(SetlistsDbContract.SetlistsDbSongEntry.TABLE_NAME, null, values);
                if (rowid >= 0) {
                    resultUri = uri.buildUpon().appendPath(values.getAsString("_id")).build();
                    SetlistsWidget.sendRefreshBroadcast(getContext());
                }
                else
                    throw new SQLException("Problem while inserting into uri: " + uri);
                break;
            }
            case DOCUMENT: {
                values.put("user", mAuth.getCurrentUser().getUid());
                if (!values.containsKey("author"))
                    values.put("author", mAuth.getCurrentUser().getEmail());
                long rowid = db.insert(SetlistsDbContract.SetlistsDbDocumentEntry.TABLE_NAME, null, values);
                if (rowid >= 0)
                    resultUri = uri.buildUpon().appendPath(values.getAsString("_id")).build();
                else
                    throw new SQLException("Problem while inserting into uri: " + uri);
                break;
            }
            case BAND: {
                values.put("user", mAuth.getCurrentUser().getUid());
                long rowid = db.insert(SetlistsDbContract.SetlistsDbBandEntry.TABLE_NAME, null, values);
                if (rowid >= 0)
                    resultUri = uri.buildUpon().appendPath(values.getAsString("_id")).build();
                else
                    throw new SQLException("Problem while inserting into uri: " + uri);
                break;
            }
            case MUSICIAN: {
                values.put("user", mAuth.getCurrentUser().getUid());
                long rowid = db.insert(SetlistsDbContract.SetlistsDbMusicianEntry.TABLE_NAME, null, values);
                if (rowid >= 0)
                    resultUri = uri.buildUpon().appendPath(values.getAsString("_id")).build();
                else
                    throw new SQLException("Problem while inserting into uri: " + uri);
                break;
            }
            case SET: {
                values.put("user", mAuth.getCurrentUser().getUid());
                if (!values.containsKey("author"))
                    values.put("author", mAuth.getCurrentUser().getEmail());
                long rowid = db.insert(SetlistsDbContract.SetlistsDbSetEntry.TABLE_NAME, null, values);
                if (rowid >= 0)
                    resultUri = uri.buildUpon().appendPath(values.getAsString("_id")).build();
                else
                    throw new SQLException("Problem while inserting into uri: " + uri);
                break;
            }
            case SET_SONG: {
                values.put("user", mAuth.getCurrentUser().getUid());
                long rowid = db.insert(SetlistsDbContract.SetlistsDbSetSongEntry.TABLE_NAME, null, values);
                if (rowid >= 0)
                    resultUri = uri.buildUpon().appendPath(values.getAsString("_id")).build();
                else
                    throw new SQLException("Problem while inserting into uri: " + uri);
                break;
            }
            case MIDI_MESSAGE: {
                values.put("user", mAuth.getCurrentUser().getUid());
                long rowid = db.insert(SetlistsDbContract.SetlistsDbMidiMessageEntry.TABLE_NAME, null, values);
                if (rowid >= 0)
                    resultUri = uri.buildUpon().appendPath(values.getAsString("_id")).build();
                else
                    throw new SQLException("Problem while inserting into uri: " + uri);
                break;
            }
            case DOCUMENT_MIDI_MESSAGE: {
                values.put("user", mAuth.getCurrentUser().getUid());
                long rowid = db.insert(SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.TABLE_NAME, null, values);
                if (rowid >= 0)
                    resultUri = uri.buildUpon().appendPath(values.getAsString("_id")).build();
                else
                    throw new SQLException("Problem while inserting into uri: " + uri);
                break;
            }
            case DELETED_RECORD: {
                values.put("user", mAuth.getCurrentUser().getUid());
                long rowid = db.insert(SetlistsDbContract.SetlistsDbDeletedRecordEntry.TABLE_NAME, null, values);
                if (rowid >= 0)
                    resultUri = uri.buildUpon().appendPath(values.getAsString("_id")).build();
                else
                    throw new SQLException("Problem while inserting into uri: " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Not yet implemented");
        }

        if (getContext() != null)
            getContext().getContentResolver().notifyChange(uri, null);
        db.close();
        return resultUri;
    }

    @Override
    public boolean onCreate() {
        database = new SetlistsDatabase(getContext());
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = database.getReadableDatabase();
        Cursor resultCursor;

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null)
            return null;

        Log.d("QUERY", uri.toString());
        switch (uriMatcher.match(uri)) {
            case ARTIST: {
                if (sortOrder == null || sortOrder.isEmpty())
                    sortOrder = SetlistsDbContract.SetlistsDbArtistEntry.DEFAULT_SORT;
                resultCursor = db.rawQuery("SELECT " +
                        SetlistsDbContract.SetlistsDbArtistEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbArtistEntry.COLUMN_ID + ", " +
                        SetlistsDbContract.SetlistsDbArtistEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbArtistEntry.COLUMN_NAME + ", " +
                        SetlistsDbContract.SetlistsDbArtistEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbArtistEntry.COLUMN_PHOTO + ", " +
                        "COUNT(" + SetlistsDbContract.SetlistsDbSongEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbSongEntry.COLUMN_ID + ") AS songs, " +
                        SetlistsDbContract.SetlistsDbArtistEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbArtistEntry.COLUMN_DATE_ADDED + ", " +
                        SetlistsDbContract.SetlistsDbArtistEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbArtistEntry.COLUMN_DATE_MODIFIED + ", " +
                        SetlistsDbContract.SetlistsDbArtistEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbArtistEntry.COLUMN_USER + " FROM " +
                        SetlistsDbContract.SetlistsDbArtistEntry.TABLE_NAME + " LEFT JOIN " + SetlistsDbContract.SetlistsDbSongEntry.TABLE_NAME + " ON " +
                        SetlistsDbContract.SetlistsDbArtistEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbArtistEntry.COLUMN_ID + " = " +
                        SetlistsDbContract.SetlistsDbSongEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbSongEntry.COLUMN_ARTIST + " WHERE " +
                        SetlistsDbContract.SetlistsDbArtistEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbArtistEntry.COLUMN_USER + " = '" +
                        user.getUid() + "' GROUP BY 1, 2, 3 ORDER BY " + sortOrder, null, null);
                break;
            }
            case ARTIST_BY_ID: {
                String id = uri.getPathSegments().get(1);
                resultCursor = db.query(SetlistsDbContract.SetlistsDbArtistEntry.TABLE_NAME, null, "_id = ?", new String[] { id }, null, null, null);
                break;
            }
            case ARTIST_SONG: {
                String artistId = uri.getPathSegments().get(1);
                resultCursor = db.rawQuery("SELECT " +
                        SetlistsDbContract.SetlistsDbSongEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbSongEntry.COLUMN_ID + ", " +
                        SetlistsDbContract.SetlistsDbSongEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbSongEntry.COLUMN_TITLE + ", " +
                        SetlistsDbContract.SetlistsDbSongEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbSongEntry.COLUMN_ARTIST + ", " +
                        SetlistsDbContract.SetlistsDbArtistEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbArtistEntry.COLUMN_NAME + " AS artist_name, " +
                        SetlistsDbContract.SetlistsDbSongEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbSongEntry.COLUMN_KEY + ", " +
                        SetlistsDbContract.SetlistsDbSongEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbSongEntry.COLUMN_TEMPO + ", " +
                        SetlistsDbContract.SetlistsDbSongEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbSongEntry.COLUMN_DURATION + ", " +
                        SetlistsDbContract.SetlistsDbSongEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbSongEntry.COLUMN_NOTES + ", " +
                        SetlistsDbContract.SetlistsDbSongEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbSongEntry.COLUMN_DOCUMENT + ", " +
                        SetlistsDbContract.SetlistsDbSongEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbSongEntry.COLUMN_AUTHOR + ", " +
                        SetlistsDbContract.SetlistsDbSongEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbSongEntry.COLUMN_DATE_ADDED + ", " +
                        SetlistsDbContract.SetlistsDbSongEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbSongEntry.COLUMN_DATE_MODIFIED + " FROM " +
                        SetlistsDbContract.SetlistsDbSongEntry.TABLE_NAME + " LEFT JOIN " + SetlistsDbContract.SetlistsDbArtistEntry.TABLE_NAME + " ON " +
                        SetlistsDbContract.SetlistsDbSongEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbSongEntry.COLUMN_ARTIST + " = " +
                        SetlistsDbContract.SetlistsDbArtistEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbArtistEntry.COLUMN_ID + " WHERE " +
                        SetlistsDbContract.SetlistsDbSongEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbSongEntry.COLUMN_ARTIST + " = ? AND " +
                        SetlistsDbContract.SetlistsDbSongEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbSongEntry.COLUMN_USER + " = '" +
                        user.getUid() + "' ORDER BY " + SetlistsDbContract.SetlistsDbSongEntry.DEFAULT_SORT, new String[] { artistId }, null);
                break;
            }
            case SONG: {
                resultCursor = db.rawQuery("SELECT " +
                        SetlistsDbContract.SetlistsDbSongEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbSongEntry.COLUMN_ID + ", " +
                        SetlistsDbContract.SetlistsDbSongEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbSongEntry.COLUMN_TITLE + ", " +
                        SetlistsDbContract.SetlistsDbSongEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbSongEntry.COLUMN_ARTIST + ", " +
                        SetlistsDbContract.SetlistsDbArtistEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbArtistEntry.COLUMN_NAME + " AS artist_name, " +
                        SetlistsDbContract.SetlistsDbSongEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbSongEntry.COLUMN_KEY + ", " +
                        SetlistsDbContract.SetlistsDbSongEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbSongEntry.COLUMN_TEMPO + ", " +
                        SetlistsDbContract.SetlistsDbSongEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbSongEntry.COLUMN_DURATION + ", " +
                        SetlistsDbContract.SetlistsDbSongEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbSongEntry.COLUMN_NOTES + ", " +
                        SetlistsDbContract.SetlistsDbSongEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbSongEntry.COLUMN_DOCUMENT + ", " +
                        SetlistsDbContract.SetlistsDbSongEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbSongEntry.COLUMN_AUTHOR + ", " +
                        SetlistsDbContract.SetlistsDbSongEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbSongEntry.COLUMN_DATE_ADDED + ", " +
                        SetlistsDbContract.SetlistsDbSongEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbSongEntry.COLUMN_DATE_MODIFIED + ", " +
                        SetlistsDbContract.SetlistsDbSongEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbSongEntry.COLUMN_USER + " FROM " +
                        SetlistsDbContract.SetlistsDbSongEntry.TABLE_NAME + " LEFT JOIN " + SetlistsDbContract.SetlistsDbArtistEntry.TABLE_NAME + " ON " +
                        SetlistsDbContract.SetlistsDbSongEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbSongEntry.COLUMN_ARTIST + " = " +
                        SetlistsDbContract.SetlistsDbArtistEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbArtistEntry.COLUMN_ID + " WHERE " +
                        SetlistsDbContract.SetlistsDbSongEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbSongEntry.COLUMN_USER + " = '" +
                        user.getUid() + "' ORDER BY " + SetlistsDbContract.SetlistsDbSongEntry.DEFAULT_SORT, null, null);
                break;
            }
            case SONG_BY_ID: {
                String id = uri.getPathSegments().get(1);
                resultCursor = db.query(SetlistsDbContract.SetlistsDbSongEntry.TABLE_NAME, null, "_id = ?", new String[] { id }, null, null, null);
                break;
            }
            case DOCUMENT: {
                resultCursor = db.rawQuery("SELECT " +
                        SetlistsDbContract.SetlistsDbDocumentEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_ID + ", " +
                        SetlistsDbContract.SetlistsDbDocumentEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_DESCRIPTION + ", " +
                        SetlistsDbContract.SetlistsDbDocumentEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_AUTHOR + ", " +
                        SetlistsDbContract.SetlistsDbDocumentEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_SONG + ", " +
                        SetlistsDbContract.SetlistsDbDocumentEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_TYPE + ", " +
                        SetlistsDbContract.SetlistsDbDocumentEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_TRANSPOSE + ", " +
                        SetlistsDbContract.SetlistsDbDocumentEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_TRANSPOSE_MODE + ", " +
                        SetlistsDbContract.SetlistsDbDocumentEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_DATE_ADDED + ", " +
                        SetlistsDbContract.SetlistsDbDocumentEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_DATE_MODIFIED + ", " +
                        SetlistsDbContract.SetlistsDbDocumentEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_USER + " FROM " +
                        SetlistsDbContract.SetlistsDbDocumentEntry.TABLE_NAME + " WHERE " +
                        SetlistsDbContract.SetlistsDbDocumentEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_USER + " = '" +
                        user.getUid() + "' ORDER BY " + SetlistsDbContract.SetlistsDbDocumentEntry.DEFAULT_SORT, null, null);
                break;
            }
            case SONG_DOCUMENT: {
                String songId = uri.getPathSegments().get(1);
                resultCursor = db.rawQuery("SELECT " +
                        SetlistsDbContract.SetlistsDbDocumentEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_ID + ", " +
                        SetlistsDbContract.SetlistsDbDocumentEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_DESCRIPTION + ", " +
                        SetlistsDbContract.SetlistsDbDocumentEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_AUTHOR + ", " +
                        SetlistsDbContract.SetlistsDbDocumentEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_SONG + ", " +
                        SetlistsDbContract.SetlistsDbDocumentEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_TYPE + ", " +
                        SetlistsDbContract.SetlistsDbDocumentEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_TRANSPOSE + ", " +
                        SetlistsDbContract.SetlistsDbDocumentEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_TRANSPOSE_MODE + ", " +
                        SetlistsDbContract.SetlistsDbDocumentEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_DATE_ADDED + ", " +
                        SetlistsDbContract.SetlistsDbDocumentEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_DATE_MODIFIED + ", " +
                        SetlistsDbContract.SetlistsDbSongEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbSongEntry.COLUMN_TITLE + " AS song_title FROM " +
                        SetlistsDbContract.SetlistsDbDocumentEntry.TABLE_NAME + " LEFT JOIN " + SetlistsDbContract.SetlistsDbSongEntry.TABLE_NAME + " ON " +
                        SetlistsDbContract.SetlistsDbDocumentEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_SONG + " = " +
                        SetlistsDbContract.SetlistsDbSongEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbSongEntry.COLUMN_ID + " WHERE " +
                        SetlistsDbContract.SetlistsDbDocumentEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_SONG + " = ? AND " +
                        SetlistsDbContract.SetlistsDbDocumentEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_USER + " = '" +
                        user.getUid() + "' ORDER BY " + SetlistsDbContract.SetlistsDbDocumentEntry.DEFAULT_SORT, new String[] { songId }, null);
                break;
            }
            case BAND: {
                resultCursor = db.rawQuery("SELECT " +
                        SetlistsDbContract.SetlistsDbBandEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbBandEntry.COLUMN_ID + ", " +
                        SetlistsDbContract.SetlistsDbBandEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbBandEntry.COLUMN_NAME + ", " +
                        "COUNT (DISTINCT " + SetlistsDbContract.SetlistsDbMusicianEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbMusicianEntry.COLUMN_ID + ") AS musician_count, " +
                        "COUNT (DISTINCT " + SetlistsDbContract.SetlistsDbSetEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbSetEntry.COLUMN_ID + ") AS set_count, " +
                        SetlistsDbContract.SetlistsDbBandEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbBandEntry.COLUMN_DATE_ADDED + ", " +
                        SetlistsDbContract.SetlistsDbBandEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbBandEntry.COLUMN_DATE_MODIFIED + ", " +
                        SetlistsDbContract.SetlistsDbBandEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbBandEntry.COLUMN_USER + " FROM " +
                        SetlistsDbContract.SetlistsDbBandEntry.TABLE_NAME + " LEFT JOIN " + SetlistsDbContract.SetlistsDbSetEntry.TABLE_NAME + " ON " +
                        SetlistsDbContract.SetlistsDbBandEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbBandEntry.COLUMN_ID + " = " +
                        SetlistsDbContract.SetlistsDbSetEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbSetEntry.COLUMN_BAND + " LEFT JOIN " + SetlistsDbContract.SetlistsDbMusicianEntry.TABLE_NAME + " ON " +
                        SetlistsDbContract.SetlistsDbBandEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbBandEntry.COLUMN_ID + " = " +
                        SetlistsDbContract.SetlistsDbMusicianEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbMusicianEntry.COLUMN_BAND + " WHERE " +
                        SetlistsDbContract.SetlistsDbBandEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbBandEntry.COLUMN_USER + " = '" +
                        user.getUid() + "' GROUP BY 1,2 ORDER BY " + SetlistsDbContract.SetlistsDbBandEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbBandEntry.DEFAULT_SORT, null, null);
                break;
            }
            case BAND_BY_ID: {
                String id = uri.getPathSegments().get(1);
                resultCursor = db.query(SetlistsDbContract.SetlistsDbBandEntry.TABLE_NAME, null, "_id = ?", new String[] { id }, null, null, null);
                break;
            }
            case BAND_MUSICIAN: {
                String bandId = uri.getPathSegments().get(1);
                resultCursor = db.rawQuery("SELECT " +
                        SetlistsDbContract.SetlistsDbMusicianEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbMusicianEntry.COLUMN_ID + ", " +
                        SetlistsDbContract.SetlistsDbMusicianEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbMusicianEntry.COLUMN_NAME + ", " +
                        SetlistsDbContract.SetlistsDbMusicianEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbMusicianEntry.COLUMN_EMAIL + ", " +
                        SetlistsDbContract.SetlistsDbMusicianEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbMusicianEntry.COLUMN_INSTRUMENT + ", " +
                        SetlistsDbContract.SetlistsDbMusicianEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbMusicianEntry.COLUMN_BAND + ", " +
                        SetlistsDbContract.SetlistsDbBandEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbBandEntry.COLUMN_NAME + " AS band_name FROM " +
                        SetlistsDbContract.SetlistsDbMusicianEntry.TABLE_NAME + " LEFT JOIN " + SetlistsDbContract.SetlistsDbBandEntry.TABLE_NAME + " ON " +
                        SetlistsDbContract.SetlistsDbMusicianEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbMusicianEntry.COLUMN_BAND + " = " +
                        SetlistsDbContract.SetlistsDbBandEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbBandEntry.COLUMN_ID + " WHERE " +
                        SetlistsDbContract.SetlistsDbMusicianEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbMusicianEntry.COLUMN_BAND + " = ? AND " +
                        SetlistsDbContract.SetlistsDbMusicianEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbMusicianEntry.COLUMN_USER + " = '" +
                        user.getUid() + "' ORDER BY " + SetlistsDbContract.SetlistsDbMusicianEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbMusicianEntry.DEFAULT_SORT, new String[] { bandId }, null);
                break;
            }
            case MUSICIAN: {
                resultCursor = db.rawQuery("SELECT " +
                        SetlistsDbContract.SetlistsDbMusicianEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbMusicianEntry.COLUMN_ID + ", " +
                        SetlistsDbContract.SetlistsDbMusicianEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbMusicianEntry.COLUMN_NAME + ", " +
                        SetlistsDbContract.SetlistsDbMusicianEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbMusicianEntry.COLUMN_EMAIL + ", " +
                        SetlistsDbContract.SetlistsDbMusicianEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbMusicianEntry.COLUMN_INSTRUMENT + ", " +
                        SetlistsDbContract.SetlistsDbMusicianEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbMusicianEntry.COLUMN_BAND + ", " +
                        SetlistsDbContract.SetlistsDbMusicianEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbMusicianEntry.COLUMN_DATE_ADDED + ", " +
                        SetlistsDbContract.SetlistsDbMusicianEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbMusicianEntry.COLUMN_DATE_MODIFIED + ", " +
                        SetlistsDbContract.SetlistsDbMusicianEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbMusicianEntry.COLUMN_USER + " FROM " +
                        SetlistsDbContract.SetlistsDbMusicianEntry.TABLE_NAME  + " WHERE " +
                        SetlistsDbContract.SetlistsDbMusicianEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbMusicianEntry.COLUMN_USER + " = '" +
                        user.getUid() + "' ORDER BY " + SetlistsDbContract.SetlistsDbMusicianEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbMusicianEntry.DEFAULT_SORT, null, null);
                break;
            }
            case MUSICIAN_BY_ID: {
                String id = uri.getPathSegments().get(1);
                resultCursor = db.query(SetlistsDbContract.SetlistsDbMusicianEntry.TABLE_NAME, null, "_id = ?", new String[] { id }, null, null, null);
                break;
            }
            case SET: {
                resultCursor = db.rawQuery("SELECT " +
                        SetlistsDbContract.SetlistsDbSetEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbSetEntry.COLUMN_ID + ", " +
                        SetlistsDbContract.SetlistsDbSetEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbSetEntry.COLUMN_NAME + ", " +
                        SetlistsDbContract.SetlistsDbSetEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbSetEntry.COLUMN_LOCATION + ", " +
                        SetlistsDbContract.SetlistsDbSetEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbSetEntry.COLUMN_BAND + ", " +
                        SetlistsDbContract.SetlistsDbSetEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbSetEntry.COLUMN_DATE_ADDED + ", " +
                        SetlistsDbContract.SetlistsDbSetEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbSetEntry.COLUMN_DATE_MODIFIED + ", " +
                        SetlistsDbContract.SetlistsDbSetEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbSetEntry.COLUMN_AUTHOR + ", " +
                        SetlistsDbContract.SetlistsDbBandEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbBandEntry.COLUMN_NAME + " AS band_name, " +
                        "COUNT (" + SetlistsDbContract.SetlistsDbSetSongEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbSetSongEntry.COLUMN_ID + ") as song_count, " +
                        "SUM (" + SetlistsDbContract.SetlistsDbSongEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbSongEntry.COLUMN_DURATION + ") as duration_sum FROM " +
                        SetlistsDbContract.SetlistsDbSetEntry.TABLE_NAME + " LEFT JOIN " + SetlistsDbContract.SetlistsDbBandEntry.TABLE_NAME + " ON " +
                        SetlistsDbContract.SetlistsDbSetEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbSetEntry.COLUMN_BAND + " = " +
                        SetlistsDbContract.SetlistsDbBandEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbBandEntry.COLUMN_ID + " LEFT JOIN " + SetlistsDbContract.SetlistsDbSetSongEntry.TABLE_NAME + " ON " +
                        SetlistsDbContract.SetlistsDbSetEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbSetEntry.COLUMN_ID + " = " +
                        SetlistsDbContract.SetlistsDbSetSongEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbSetSongEntry.COLUMN_SETLIST + " LEFT JOIN " + SetlistsDbContract.SetlistsDbSongEntry.TABLE_NAME + " ON " +
                        SetlistsDbContract.SetlistsDbSetSongEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbSetSongEntry.COLUMN_SONG + " = " +
                        SetlistsDbContract.SetlistsDbSongEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbSongEntry.COLUMN_ID + " WHERE " +
                        SetlistsDbContract.SetlistsDbSetEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbSetEntry.COLUMN_USER + " = '" +
                        user.getUid() + "' GROUP BY 1,2,3,4,5,6,7,8 ORDER BY " + SetlistsDbContract.SetlistsDbSetEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbSetEntry.DEFAULT_SORT, null, null);
                break;
            }
            case SET_BY_ID: {
                String id = uri.getPathSegments().get(1);
                resultCursor = db.query(SetlistsDbContract.SetlistsDbSetEntry.TABLE_NAME, null, "_id = ?", new String[] { id }, null, null, null);
                break;
            }
            case SET_SONG: {
                resultCursor = db.rawQuery("SELECT " +
                        SetlistsDbContract.SetlistsDbSetSongEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbSetSongEntry.COLUMN_ID + ", " +
                        SetlistsDbContract.SetlistsDbSetSongEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbSetSongEntry.COLUMN_SETLIST + ", " +
                        SetlistsDbContract.SetlistsDbSetSongEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbSetSongEntry.COLUMN_SONG + ", " +
                        SetlistsDbContract.SetlistsDbSetSongEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbSetSongEntry.COLUMN_SEQUENCE + ", " +
                        SetlistsDbContract.SetlistsDbSetSongEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbSetSongEntry.COLUMN_DATE_ADDED + ", " +
                        SetlistsDbContract.SetlistsDbSetSongEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbSetSongEntry.COLUMN_DATE_MODIFIED + ", " +
                        SetlistsDbContract.SetlistsDbSetSongEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbSetSongEntry.COLUMN_USER + " FROM " +
                        SetlistsDbContract.SetlistsDbSetSongEntry.TABLE_NAME + " WHERE " +
                        SetlistsDbContract.SetlistsDbSetSongEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbSetSongEntry.COLUMN_USER + " = '" +
                        user.getUid() + "' ORDER BY " + SetlistsDbContract.SetlistsDbSetSongEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbSetSongEntry.DEFAULT_SORT, null, null);
                break;
            }
            case SET_SONG_BY_ID: {
                String id = uri.getPathSegments().get(1);
                resultCursor = db.query(SetlistsDbContract.SetlistsDbSetSongEntry.TABLE_NAME, null, "_id = ?", new String[] { id }, null, null, null);
                break;
            }
            case SET_SET_SONG: {
                String setId = uri.getPathSegments().get(1);
                resultCursor = db.rawQuery("SELECT " +
                        SetlistsDbContract.SetlistsDbSetSongEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbSetSongEntry.COLUMN_ID + ", " +
                        SetlistsDbContract.SetlistsDbSetSongEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbSetSongEntry.COLUMN_SETLIST + ", " +
                        SetlistsDbContract.SetlistsDbSetSongEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbSetSongEntry.COLUMN_SONG + ", " +
                        SetlistsDbContract.SetlistsDbSetSongEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbSetSongEntry.COLUMN_SEQUENCE + ", " +
                        SetlistsDbContract.SetlistsDbSongEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbSongEntry.COLUMN_DOCUMENT + ", " +
                        SetlistsDbContract.SetlistsDbDocumentEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_TYPE + " AS type, " +
                        SetlistsDbContract.SetlistsDbSongEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbSongEntry.COLUMN_ARTIST + ", " +
                        SetlistsDbContract.SetlistsDbSongEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbSongEntry.COLUMN_TITLE + " AS title, " +
                        SetlistsDbContract.SetlistsDbDocumentEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_DESCRIPTION + " AS description, " +
                        SetlistsDbContract.SetlistsDbSongEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbSongEntry.COLUMN_KEY + " AS key, " +
                        SetlistsDbContract.SetlistsDbSongEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbSongEntry.COLUMN_TEMPO + " AS tempo, " +
                        SetlistsDbContract.SetlistsDbSongEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbSongEntry.COLUMN_DURATION + " AS duration, " +
                        SetlistsDbContract.SetlistsDbDocumentEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_TRANSPOSE + " AS transpose, " +
                        SetlistsDbContract.SetlistsDbDocumentEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_TRANSPOSE_MODE + " AS transpose_mode, " +
                        SetlistsDbContract.SetlistsDbSongEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbSongEntry.COLUMN_NOTES + " FROM " +
                        SetlistsDbContract.SetlistsDbSetSongEntry.TABLE_NAME + " LEFT JOIN " + SetlistsDbContract.SetlistsDbSongEntry.TABLE_NAME + " ON " +
                        SetlistsDbContract.SetlistsDbSetSongEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbSetSongEntry.COLUMN_SONG + " = " +
                        SetlistsDbContract.SetlistsDbSongEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbSongEntry.COLUMN_ID + " LEFT JOIN " + SetlistsDbContract.SetlistsDbDocumentEntry.TABLE_NAME + " ON " +
                        SetlistsDbContract.SetlistsDbSongEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbSongEntry.COLUMN_DOCUMENT + " = " +
                        SetlistsDbContract.SetlistsDbDocumentEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_ID + " WHERE " +
                        SetlistsDbContract.SetlistsDbSetSongEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbSetSongEntry.COLUMN_SETLIST + " = ? AND " +
                        SetlistsDbContract.SetlistsDbSetSongEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbSetSongEntry.COLUMN_USER + " = '" +
                        user.getUid() + "' ORDER BY " + SetlistsDbContract.SetlistsDbSetSongEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbSetSongEntry.DEFAULT_SORT, new String[] { setId }, null);
                break;
            }
            case MIDI_MESSAGE: {
                resultCursor = db.rawQuery("SELECT " +
                        SetlistsDbContract.SetlistsDbMidiMessageEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbMidiMessageEntry.COLUMN_ID + ", " +
                        SetlistsDbContract.SetlistsDbMidiMessageEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbMidiMessageEntry.COLUMN_NAME + ", " +
                        SetlistsDbContract.SetlistsDbMidiMessageEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbMidiMessageEntry.COLUMN_CHANNEL + ", " +
                        SetlistsDbContract.SetlistsDbMidiMessageEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbMidiMessageEntry.COLUMN_STATUS + ", " +
                        SetlistsDbContract.SetlistsDbMidiMessageEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbMidiMessageEntry.COLUMN_DATA1 + ", " +
                        SetlistsDbContract.SetlistsDbMidiMessageEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbMidiMessageEntry.COLUMN_DATA2 + ", " +
                        SetlistsDbContract.SetlistsDbMidiMessageEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbMidiMessageEntry.COLUMN_DATE_ADDED + ", " +
                        SetlistsDbContract.SetlistsDbMidiMessageEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbMidiMessageEntry.COLUMN_DATE_MODIFIED + ", " +
                        SetlistsDbContract.SetlistsDbMidiMessageEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbMidiMessageEntry.COLUMN_USER + " FROM " +
                        SetlistsDbContract.SetlistsDbMidiMessageEntry.TABLE_NAME + " WHERE " +
                        SetlistsDbContract.SetlistsDbMidiMessageEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbMidiMessageEntry.COLUMN_USER + " = '" +
                        user.getUid() + "' ORDER BY " + SetlistsDbContract.SetlistsDbMidiMessageEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbMidiMessageEntry.DEFAULT_SORT, null, null);
                break;
            }
            case MIDI_MESSAGE_BY_ID: {
                String id = uri.getPathSegments().get(1);
                resultCursor = db.query(SetlistsDbContract.SetlistsDbMidiMessageEntry.TABLE_NAME, null, "_id = ?", new String[] { id }, null, null, null);
                break;
            }
            case DOCUMENT_MIDI_MESSAGE: {
                resultCursor = db.rawQuery("SELECT " +
                        SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.COLUMN_ID + ", " +
                        SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.COLUMN_DOCUMENT + ", " +
                        SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.COLUMN_MESSAGE + ", " +
                        SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.COLUMN_AUTOSEND + ", " +
                        SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.COLUMN_DATE_ADDED + ", " +
                        SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.COLUMN_DATE_MODIFIED + ", " +
                        SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.COLUMN_USER + " FROM " +
                        SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.TABLE_NAME + " WHERE " +
                        SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.COLUMN_USER + " = '" +
                        user.getUid() + "' ORDER BY " + SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.DEFAULT_SORT, null, null);
                break;
            }
            case DOCUMENT_MIDI_MESSAGE_BY_ID: {
                String id = uri.getPathSegments().get(1);
                resultCursor = db.query(SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.TABLE_NAME, null, "_id = ?", new String[] { id }, null, null, null);
                break;
            }
            case SET_DOCUMENT_MIDI_MESSAGE: {
                String setId = uri.getPathSegments().get(1);
                resultCursor = db.rawQuery("SELECT " +
                        SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.COLUMN_ID + ", " +
                        SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.COLUMN_DOCUMENT + ", " +
                        SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.COLUMN_MESSAGE + ", " +
                        SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.COLUMN_AUTOSEND + ", " +
                        SetlistsDbContract.SetlistsDbMidiMessageEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbMidiMessageEntry.COLUMN_NAME + ", " +
                        SetlistsDbContract.SetlistsDbMidiMessageEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbMidiMessageEntry.COLUMN_CHANNEL + ", " +
                        SetlistsDbContract.SetlistsDbMidiMessageEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbMidiMessageEntry.COLUMN_STATUS + ", " +
                        SetlistsDbContract.SetlistsDbMidiMessageEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbMidiMessageEntry.COLUMN_DATA1 + ", " +
                        SetlistsDbContract.SetlistsDbMidiMessageEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbMidiMessageEntry.COLUMN_DATA2 + " FROM " +
                        SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.TABLE_NAME + " LEFT JOIN " + SetlistsDbContract.SetlistsDbDocumentEntry.TABLE_NAME + " ON " +
                        SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.COLUMN_DOCUMENT + " = " +
                        SetlistsDbContract.SetlistsDbDocumentEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_ID + " LEFT JOIN " + SetlistsDbContract.SetlistsDbMidiMessageEntry.TABLE_NAME + " ON " +
                        SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.COLUMN_MESSAGE + " = " +
                        SetlistsDbContract.SetlistsDbMidiMessageEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbMidiMessageEntry.COLUMN_ID + " LEFT JOIN " + SetlistsDbContract.SetlistsDbSongEntry.TABLE_NAME + " ON " +
                        SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.COLUMN_DOCUMENT + " = " +
                        SetlistsDbContract.SetlistsDbSongEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbSongEntry.COLUMN_DOCUMENT + " LEFT JOIN " + SetlistsDbContract.SetlistsDbSetSongEntry.TABLE_NAME + " ON " +
                        SetlistsDbContract.SetlistsDbSongEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbSongEntry.COLUMN_ID + " = " +
                        SetlistsDbContract.SetlistsDbSetSongEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbSetSongEntry.COLUMN_SONG + " WHERE " +
                        SetlistsDbContract.SetlistsDbSetSongEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbSetSongEntry.COLUMN_SETLIST + " = ? AND " +
                        SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.COLUMN_USER + " = '" +
                        user.getUid() + "' ORDER BY " + SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.DEFAULT_SORT, new String[] { setId }, null);
                break;
            }
            case DOCUMENT_DOCUMENT_MIDI_MESSAGE: {
                String documentId = uri.getPathSegments().get(1);
                resultCursor = db.rawQuery("SELECT " +
                        SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.COLUMN_ID + ", " +
                        SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.COLUMN_DOCUMENT + ", " +
                        SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.COLUMN_MESSAGE + ", " +
                        SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.COLUMN_AUTOSEND + ", " +
                        SetlistsDbContract.SetlistsDbMidiMessageEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbMidiMessageEntry.COLUMN_NAME + ", " +
                        SetlistsDbContract.SetlistsDbMidiMessageEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbMidiMessageEntry.COLUMN_CHANNEL + ", " +
                        SetlistsDbContract.SetlistsDbMidiMessageEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbMidiMessageEntry.COLUMN_STATUS + ", " +
                        SetlistsDbContract.SetlistsDbMidiMessageEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbMidiMessageEntry.COLUMN_DATA1 + ", " +
                        SetlistsDbContract.SetlistsDbMidiMessageEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbMidiMessageEntry.COLUMN_DATA2 + " FROM " +
                        SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.TABLE_NAME + " LEFT JOIN " + SetlistsDbContract.SetlistsDbDocumentEntry.TABLE_NAME + " ON " +
                        SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.COLUMN_DOCUMENT + " = " +
                        SetlistsDbContract.SetlistsDbDocumentEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_ID + " LEFT JOIN " + SetlistsDbContract.SetlistsDbMidiMessageEntry.TABLE_NAME + " ON " +
                        SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.COLUMN_MESSAGE + " = " +
                        SetlistsDbContract.SetlistsDbMidiMessageEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbMidiMessageEntry.COLUMN_ID + " WHERE " +
                        SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.COLUMN_DOCUMENT + " = ? AND " +
                        SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.COLUMN_USER + " = '" +
                        user.getUid() + "' ORDER BY " + SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.DEFAULT_SORT, new String[] { documentId }, null);
                break;
            }
            case DELETED_RECORD: {
                resultCursor = db.rawQuery("SELECT " +
                        SetlistsDbContract.SetlistsDbDeletedRecordEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbDeletedRecordEntry.COLUMN_ID + ", " +
                        SetlistsDbContract.SetlistsDbDeletedRecordEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbDeletedRecordEntry.COLUMN_TABLE + ", " +
                        SetlistsDbContract.SetlistsDbDeletedRecordEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbDeletedRecordEntry.COLUMN_DESCRIPTION + ", " +
                        SetlistsDbContract.SetlistsDbDeletedRecordEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbDeletedRecordEntry.COLUMN_DATE_DELETED + " FROM " +
                        SetlistsDbContract.SetlistsDbDeletedRecordEntry.TABLE_NAME + " WHERE " +
                        SetlistsDbContract.SetlistsDbDeletedRecordEntry.TABLE_NAME + "." + SetlistsDbContract.SetlistsDbDeletedRecordEntry.COLUMN_USER + " = '" +
                        user.getUid() + "' ORDER BY " + SetlistsDbContract.SetlistsDbDeletedRecordEntry.DEFAULT_SORT, null, null);
                break;
            }
            case DELETED_RECORD_BY_ID: {
                String id = uri.getPathSegments().get(1);
                resultCursor = db.query(SetlistsDbContract.SetlistsDbDeletedRecordEntry.TABLE_NAME, null, "_id = ?", new String[] { id }, null, null, null);
                break;
            }
            default:
                throw new UnsupportedOperationException("Not yet implemented");
        }
        return resultCursor;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        SQLiteDatabase db = database.getWritableDatabase();
        int result;

        Log.d("UPDATE", uri.toString());
        switch (uriMatcher.match(uri)) {
            case ARTIST_BY_ID: {
                result = db.update(SetlistsDbContract.SetlistsDbArtistEntry.TABLE_NAME, values, selection, selectionArgs);
                if (result < 0)
                    throw new SQLException("Problem while updating into uri: " + uri);
                break;
            }
            case SONG_BY_ID: {
                result = db.update(SetlistsDbContract.SetlistsDbSongEntry.TABLE_NAME, values, selection, selectionArgs);
                if (result < 0)
                    throw new SQLException("Problem while updating into uri: " + uri);
                SetlistsWidget.sendRefreshBroadcast(getContext());
                break;
            }
            case DOCUMENT_BY_ID: {
                result = db.update(SetlistsDbContract.SetlistsDbDocumentEntry.TABLE_NAME, values, selection, selectionArgs);
                if (result < 0)
                    throw new SQLException("Problem while updating into uri: " + uri);
                break;
            }
            case BAND_BY_ID: {
                result = db.update(SetlistsDbContract.SetlistsDbBandEntry.TABLE_NAME, values, selection, selectionArgs);
                if (result < 0)
                    throw new SQLException("Problem while updating into uri: " + uri);
                break;
            }
            case MUSICIAN_BY_ID: {
                result = db.update(SetlistsDbContract.SetlistsDbMusicianEntry.TABLE_NAME, values, selection, selectionArgs);
                if (result < 0)
                    throw new SQLException("Problem while updating into uri: " + uri);
                break;
            }
            case SET_BY_ID: {
                result = db.update(SetlistsDbContract.SetlistsDbSetEntry.TABLE_NAME, values, selection, selectionArgs);
                if (result < 0)
                    throw new SQLException("Problem while updating into uri: " + uri);
                break;
            }
            case SET_SONG_BY_ID: {
                result = db.update(SetlistsDbContract.SetlistsDbSetSongEntry.TABLE_NAME, values, selection, selectionArgs);
                if (result < 0)
                    throw new SQLException("Problem while updating into uri: " + uri);
                break;
            }
            case MIDI_MESSAGE_BY_ID: {
                result = db.update(SetlistsDbContract.SetlistsDbMidiMessageEntry.TABLE_NAME, values, selection, selectionArgs);
                if (result < 0)
                    throw new SQLException("Problem while updating into uri: " + uri);
                break;
            }
            case DOCUMENT_MIDI_MESSAGE_BY_ID: {
                result = db.update(SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.TABLE_NAME, values, selection, selectionArgs);
                if (result < 0)
                    throw new SQLException("Problem while updating into uri: " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Not yet implemented");
        }

        if (getContext() != null)
            getContext().getContentResolver().notifyChange(uri, null);
        db.close();
        return result;
    }
}
