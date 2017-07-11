package com.raffaelcavaliere.setlists.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by raffaelcavaliere on 2016-11-15.
 */

public class SetlistsDatabase extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 47;
    public static final String DATABASE_NAME = "setlists.db";

    public SetlistsDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            // Enable foreign key constraints
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String SQL_CREATE_SONG_TABLE = "CREATE TABLE " + SetlistsDbContract.SetlistsDbSongEntry.TABLE_NAME + " (" +
                SetlistsDbContract.SetlistsDbSongEntry.COLUMN_ID + " INTEGER NOT NULL, " +
                SetlistsDbContract.SetlistsDbSongEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                SetlistsDbContract.SetlistsDbSongEntry.COLUMN_ARTIST + " INTEGER NULL, " +
                SetlistsDbContract.SetlistsDbSongEntry.COLUMN_KEY + " TEXT NULL, " +
                SetlistsDbContract.SetlistsDbSongEntry.COLUMN_TEMPO + " INTEGER NULL, " +
                SetlistsDbContract.SetlistsDbSongEntry.COLUMN_DURATION + " INTEGER NULL, " +
                SetlistsDbContract.SetlistsDbSongEntry.COLUMN_NOTES + " TEXT NULL, " +
                SetlistsDbContract.SetlistsDbSongEntry.COLUMN_DOCUMENT + " INTEGER NULL, " +
                SetlistsDbContract.SetlistsDbSongEntry.COLUMN_DATE_ADDED + " INTEGER NOT NULL, " +
                SetlistsDbContract.SetlistsDbSongEntry.COLUMN_DATE_MODIFIED + " INTEGER NOT NULL, " +
                " PRIMARY KEY (" + SetlistsDbContract.SetlistsDbSongEntry.COLUMN_ID + ")" +
                " FOREIGN KEY (" + SetlistsDbContract.SetlistsDbSongEntry.COLUMN_ARTIST +
                ") REFERENCES " + SetlistsDbContract.SetlistsDbArtistEntry.TABLE_NAME + "(" + SetlistsDbContract.SetlistsDbArtistEntry.COLUMN_ID + ") ON DELETE SET NULL" +
                " FOREIGN KEY (" + SetlistsDbContract.SetlistsDbSongEntry.COLUMN_DOCUMENT +
                ") REFERENCES " + SetlistsDbContract.SetlistsDbDocumentEntry.TABLE_NAME + "(" + SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_ID + ") ON DELETE SET NULL" +
                ");";

        final String SQL_CREATE_ARTIST_TABLE = "CREATE TABLE " + SetlistsDbContract.SetlistsDbArtistEntry.TABLE_NAME + " (" +
                SetlistsDbContract.SetlistsDbArtistEntry.COLUMN_ID + " INTEGER NOT NULL, " +
                SetlistsDbContract.SetlistsDbArtistEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                SetlistsDbContract.SetlistsDbArtistEntry.COLUMN_PHOTO + " TEXT NULL, " +
                " PRIMARY KEY (" + SetlistsDbContract.SetlistsDbArtistEntry.COLUMN_ID + ")" +
                ");";

        final String SQL_CREATE_AUTHOR_TABLE = "CREATE TABLE " + SetlistsDbContract.SetlistsDbAuthorEntry.TABLE_NAME + " (" +
                SetlistsDbContract.SetlistsDbAuthorEntry.COLUMN_ID + " TEXT NOT NULL, " +
                SetlistsDbContract.SetlistsDbAuthorEntry.COLUMN_EMAIL + " TEXT NOT NULL, " +
                SetlistsDbContract.SetlistsDbAuthorEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                " PRIMARY KEY (" + SetlistsDbContract.SetlistsDbAuthorEntry.COLUMN_ID + ")" +
                ");";

        final String SQL_CREATE_DOCUMENT_TABLE = "CREATE TABLE " + SetlistsDbContract.SetlistsDbDocumentEntry.TABLE_NAME + " (" +
                SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_ID + " INTEGER NOT NULL, " +
                SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_SONG + " INTEGER NOT NULL, " +
                SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_AUTHOR + " TEXT NOT NULL, " +
                SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_DESCRIPTION + " TEXT NOT NULL, " +
                SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_TYPE + " INTEGER NOT NULL, " +
                SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_TRANSPOSE + " INTEGER NULL, " +
                SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_TRANSPOSE_MODE + " INTEGER NULL, " +
                SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_DATE_ADDED + " INTEGER NOT NULL, " +
                SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_DATE_MODIFIED + " INTEGER NOT NULL, " +
                " PRIMARY KEY (" + SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_ID + ")" +
                " FOREIGN KEY (" + SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_SONG +
                ") REFERENCES " + SetlistsDbContract.SetlistsDbSongEntry.TABLE_NAME + "(" + SetlistsDbContract.SetlistsDbSongEntry.COLUMN_ID + ") ON DELETE CASCADE" +
                ");";

        final String SQL_CREATE_BAND_TABLE = "CREATE TABLE " + SetlistsDbContract.SetlistsDbBandEntry.TABLE_NAME + " (" +
                SetlistsDbContract.SetlistsDbBandEntry.COLUMN_ID + " INTEGER NOT NULL, " +
                SetlistsDbContract.SetlistsDbBandEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                " PRIMARY KEY (" + SetlistsDbContract.SetlistsDbBandEntry.COLUMN_ID + ")" +
                ");";

        final String SQL_CREATE_MUSICIAN_TABLE = "CREATE TABLE " + SetlistsDbContract.SetlistsDbMusicianEntry.TABLE_NAME + " (" +
                SetlistsDbContract.SetlistsDbMusicianEntry.COLUMN_ID + " INTEGER NOT NULL, " +
                SetlistsDbContract.SetlistsDbMusicianEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                SetlistsDbContract.SetlistsDbMusicianEntry.COLUMN_BAND + " INTEGER NOT NULL, " +
                SetlistsDbContract.SetlistsDbMusicianEntry.COLUMN_EMAIL + " TEXT NULL, " +
                SetlistsDbContract.SetlistsDbMusicianEntry.COLUMN_INSTRUMENT + " TEXT NULL, " +
                " PRIMARY KEY (" + SetlistsDbContract.SetlistsDbMusicianEntry.COLUMN_ID + ")" +
                " FOREIGN KEY (" + SetlistsDbContract.SetlistsDbMusicianEntry.COLUMN_BAND +
                ") REFERENCES " + SetlistsDbContract.SetlistsDbBandEntry.TABLE_NAME + "(" + SetlistsDbContract.SetlistsDbBandEntry.COLUMN_ID + ") ON DELETE CASCADE" +
                ");";

        final String SQL_CREATE_SETLIST_TABLE = "CREATE TABLE " + SetlistsDbContract.SetlistsDbSetEntry.TABLE_NAME + " (" +
                SetlistsDbContract.SetlistsDbSetEntry.COLUMN_ID + " INTEGER NOT NULL, " +
                SetlistsDbContract.SetlistsDbSetEntry.COLUMN_AUTHOR + " INTEGER NOT NULL, " +
                SetlistsDbContract.SetlistsDbSetEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                SetlistsDbContract.SetlistsDbSetEntry.COLUMN_LOCATION + " TEXT NULL, " +
                SetlistsDbContract.SetlistsDbSetEntry.COLUMN_BAND + " INTEGER NULL, " +
                SetlistsDbContract.SetlistsDbSetEntry.COLUMN_DATE_ADDED + " INTEGER NOT NULL, " +
                SetlistsDbContract.SetlistsDbSetEntry.COLUMN_DATE_MODIFIED + " INTEGER NOT NULL, " +
                " PRIMARY KEY (" + SetlistsDbContract.SetlistsDbSetEntry.COLUMN_ID + ")" +
                " FOREIGN KEY (" + SetlistsDbContract.SetlistsDbSetEntry.COLUMN_BAND +
                ") REFERENCES " + SetlistsDbContract.SetlistsDbBandEntry.TABLE_NAME + "(" + SetlistsDbContract.SetlistsDbBandEntry.COLUMN_ID + ") ON DELETE SET NULL" +
                ");";

        final String SQL_CREATE_SET_SONG_TABLE = "CREATE TABLE " + SetlistsDbContract.SetlistsDbSetSongEntry.TABLE_NAME + " (" +
                SetlistsDbContract.SetlistsDbSetSongEntry.COLUMN_ID + " INTEGER NOT NULL, " +
                SetlistsDbContract.SetlistsDbSetSongEntry.COLUMN_SETLIST + " INTEGER NOT NULL, " +
                SetlistsDbContract.SetlistsDbSetSongEntry.COLUMN_SONG + " INTEGER NULL, " +
                SetlistsDbContract.SetlistsDbSetSongEntry.COLUMN_SEQUENCE + " INTEGER NOT NULL, " +
                " PRIMARY KEY (" + SetlistsDbContract.SetlistsDbSetSongEntry.COLUMN_ID + ")"  +
                " FOREIGN KEY (" + SetlistsDbContract.SetlistsDbSetSongEntry.COLUMN_SETLIST +
                ") REFERENCES " + SetlistsDbContract.SetlistsDbSetEntry.TABLE_NAME + "(" + SetlistsDbContract.SetlistsDbSetEntry.COLUMN_ID + ") ON DELETE CASCADE" +
                " FOREIGN KEY (" + SetlistsDbContract.SetlistsDbSetSongEntry.COLUMN_SONG +
                ") REFERENCES " + SetlistsDbContract.SetlistsDbSongEntry.TABLE_NAME + "(" + SetlistsDbContract.SetlistsDbSongEntry.COLUMN_ID + ") ON DELETE CASCADE" +
                ");";

        final String SQL_CREATE_MIDI_MESSAGE_TABLE = "CREATE TABLE " + SetlistsDbContract.SetlistsDbMidiMessageEntry.TABLE_NAME + " (" +
                SetlistsDbContract.SetlistsDbMidiMessageEntry.COLUMN_ID + " INTEGER NOT NULL, " +
                SetlistsDbContract.SetlistsDbMidiMessageEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                SetlistsDbContract.SetlistsDbMidiMessageEntry.COLUMN_CHANNEL + " INTEGER NULL, " +
                SetlistsDbContract.SetlistsDbMidiMessageEntry.COLUMN_STATUS + " INTEGER NULL, " +
                SetlistsDbContract.SetlistsDbMidiMessageEntry.COLUMN_DATA1 + " INTEGER NULL, " +
                SetlistsDbContract.SetlistsDbMidiMessageEntry.COLUMN_DATA2 + " INTEGER NULL, " +
                " PRIMARY KEY (" + SetlistsDbContract.SetlistsDbMidiMessageEntry.COLUMN_ID + ")" +
                ");";

        final String SQL_CREATE_DOCUMENT_MIDI_MESSAGE_TABLE = "CREATE TABLE " + SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.TABLE_NAME + " (" +
                SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.COLUMN_ID + " INTEGER NOT NULL, " +
                SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.COLUMN_DOCUMENT + " INTEGER NOT NULL, " +
                SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.COLUMN_MESSAGE + " INTEGER NOT NULL, " +
                SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.COLUMN_AUTOSEND + " INTEGER NOT NULL, " +
                " PRIMARY KEY (" + SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.COLUMN_ID + ")"  +
                " FOREIGN KEY (" + SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.COLUMN_DOCUMENT +
                ") REFERENCES " + SetlistsDbContract.SetlistsDbDocumentEntry.TABLE_NAME + "(" + SetlistsDbContract.SetlistsDbDocumentEntry.COLUMN_ID + ") ON DELETE CASCADE" +
                " FOREIGN KEY (" + SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.COLUMN_MESSAGE +
                ") REFERENCES " + SetlistsDbContract.SetlistsDbMidiMessageEntry.TABLE_NAME + "(" + SetlistsDbContract.SetlistsDbMidiMessageEntry.COLUMN_ID + ") ON DELETE CASCADE" +
                ");";

        sqLiteDatabase.execSQL(SQL_CREATE_ARTIST_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_SONG_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_AUTHOR_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_DOCUMENT_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_BAND_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_MUSICIAN_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_SETLIST_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_SET_SONG_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_MIDI_MESSAGE_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_DOCUMENT_MIDI_MESSAGE_TABLE);

        final String SQL_INSERT_ARTIST = "INSERT INTO artist (_id, name, photo) VALUES (1, 'Elvis Presley', 'https://i.scdn.co/image/e3e5fd6d8a70dc5f94438fbe880fb1a8844026c4');";
        sqLiteDatabase.execSQL(SQL_INSERT_ARTIST);

        final String SQL_INSERT_AUTHOR = "INSERT INTO author (_id, email, name) VALUES ('raffaelcavaliere', 'raffaelcavaliere@gmail.com', 'Raffael Cavaliere');";
        sqLiteDatabase.execSQL(SQL_INSERT_AUTHOR);

        final String SQL_INSERT_SONG = "INSERT INTO song (_id, title, artist, added, modified) VALUES (1, 'Blue Suede Shoes', 1, 1496589295, 1496589295);";
        sqLiteDatabase.execSQL(SQL_INSERT_SONG);

        final String SQL_INSERT_SONG_2 = "INSERT INTO song (_id, title, artist, added, modified) VALUES (2, 'Little Sister', 1, 1496589295, 1496589295);";
        sqLiteDatabase.execSQL(SQL_INSERT_SONG_2);

        final String SQL_INSERT_DOCUMENT = "INSERT INTO document (_id, author, song, description, type, added, modified) " +
                "VALUES (1, 'raffaelcavaliere', 1, 'Version pdf', 1, 1496589295, 1496589295);";
        sqLiteDatabase.execSQL(SQL_INSERT_DOCUMENT);

        final String SQL_INSERT_BAND = "INSERT INTO band (_id, name) VALUES (1, 'The Neptunians');";
        sqLiteDatabase.execSQL(SQL_INSERT_BAND);

        final String SQL_INSERT_MUSICIAN = "INSERT INTO musician (_id, name, band, email, instrument) VALUES (1, 'Raffael Cavaliere', 1, 'raffaelcavaliere@gmail.com', 'Piano');";
        sqLiteDatabase.execSQL(SQL_INSERT_MUSICIAN);

        final String SQL_INSERT_SETLIST = "INSERT INTO setlist (_id, author, name, band, added, modified) VALUES (1, 1, 'Gig ABC', 1, 1496589295, 1496589295);";
        sqLiteDatabase.execSQL(SQL_INSERT_SETLIST);

        final String SQL_INSERT_SETLIST_SONG = "INSERT INTO setlist_song (_id, setlist, song, sequence) VALUES (1, 1, 1, 1);";
        sqLiteDatabase.execSQL(SQL_INSERT_SETLIST_SONG);

        final String SQL_INSERT_MIDI_MESSAGE = "INSERT INTO message (_id, name, channel, status, data1) VALUES (1, 'Program 1A', 1, 192, 1);";
        sqLiteDatabase.execSQL(SQL_INSERT_MIDI_MESSAGE);

        final String SQL_INSERT_DOCUMENT_MIDI_MESSAGE = "INSERT INTO document_message (_id, document, message, autosend) VALUES (1, 1, 1, 1);";
        sqLiteDatabase.execSQL(SQL_INSERT_DOCUMENT_MIDI_MESSAGE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // Note that this only fires if you change the version number for your database.
        // It does NOT depend on the version number for your application.
        // If you want to update the schema without wiping data, commenting out the next 2 lines
        // should be your top priority before modifying this method.
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + SetlistsDbContract.SetlistsDbSongEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + SetlistsDbContract.SetlistsDbArtistEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + SetlistsDbContract.SetlistsDbDocumentEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + SetlistsDbContract.SetlistsDbAuthorEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + SetlistsDbContract.SetlistsDbBandEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + SetlistsDbContract.SetlistsDbMusicianEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + SetlistsDbContract.SetlistsDbSetEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + SetlistsDbContract.SetlistsDbSetSongEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + SetlistsDbContract.SetlistsDbMidiMessageEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
