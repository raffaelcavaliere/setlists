package com.raffaelcavaliere.setlists.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by raffaelcavaliere on 2016-11-15.
 */

public class SetlistsDbContract {
    public static final String CONTENT_AUTHORITY = "com.raffaelcavaliere.setlists";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_SONG = "songs";
    public static final String PATH_DOCUMENT = "documents";
    public static final String PATH_ARTIST = "artists";
    public static final String PATH_AUTHOR = "authors";
    public static final String PATH_BAND = "bands";
    public static final String PATH_MUSICIAN = "musicians";
    public static final String PATH_SET = "sets";
    public static final String PATH_SET_SONG = "setsongs";
    public static final String PATH_MIDI_MESSAGE = "messages";
    public static final String PATH_DOCUMENT_MIDI_MESSAGE = "documentmessages";

    //SONG
    public static final class SetlistsDbSongEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_SONG).build();
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SONG;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SONG;

        public static final String TABLE_NAME = "song";
        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_ARTIST = "artist";
        public static final String COLUMN_KEY = "key";
        public static final String COLUMN_TEMPO = "tempo";
        public static final String COLUMN_DURATION = "duration";
        public static final String COLUMN_NOTES = "notes";
        public static final String COLUMN_DOCUMENT = "document";
        public static final String COLUMN_DATE_ADDED = "added";
        public static final String COLUMN_DATE_MODIFIED = "modified";

        public static final String DEFAULT_SORT = COLUMN_TITLE + " ASC";

        public static Uri buildSetlistsDbSongUri(long id) {
            return CONTENT_URI.buildUpon().appendPath(Long.toString(id)).build();
        }

        public static Uri buildSetlistsDbArtistSongsUri(long artistId) {
            return SetlistsDbArtistEntry.CONTENT_URI.buildUpon().appendPath(Long.toString(artistId)).appendPath(PATH_SONG).build();
        }
    }

    //SONG
    public static final class SetlistsDbArtistEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_ARTIST).build();
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ARTIST;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ARTIST;

        public static final String TABLE_NAME = "artist";
        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_PHOTO = "photo";

        public static final String DEFAULT_SORT = COLUMN_NAME + " COLLATE NOCASE ASC";

        public static Uri buildSetlistsDbArtistUri(long id) {
            return CONTENT_URI.buildUpon().appendPath(Long.toString(id)).build();
        }
    }

    //DOCUMENT
    public static final class SetlistsDbDocumentEntry implements BaseColumns {

        public static final int DOCUMENT_TYPE_PDF = 1;
        public static final int DOCUMENT_TYPE_IMAGE = 2;
        public static final int DOCUMENT_TYPE_CHORDPRO = 3;
        public static final int DOCUMENT_TYPE_TEXT = 4;

        public static final int TRANSPOSE_USE_SHARPS = 0;
        public static final int TRANSPOSE_USE_FLATS = 1;

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_DOCUMENT).build();
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_DOCUMENT;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_DOCUMENT;

        public static final String TABLE_NAME = "document";
        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_AUTHOR = "author";
        public static final String COLUMN_SONG = "song";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_TYPE = "type";
        public static final String COLUMN_TRANSPOSE = "transpose";
        public static final String COLUMN_TRANSPOSE_MODE = "transpose_mode";
        public static final String COLUMN_DATE_ADDED = "added";
        public static final String COLUMN_DATE_MODIFIED = "modified";

        public static final String DEFAULT_SORT = COLUMN_DESCRIPTION + " COLLATE NOCASE ASC";

        public static Uri buildSetlistsDbDocumentUri(long id) {
            return CONTENT_URI.buildUpon().appendPath(Long.toString(id)).build();
        }

        public static Uri buildSetlistsDbSongDocumentsUri(long songId) {
            return SetlistsDbSongEntry.CONTENT_URI.buildUpon().appendPath(Long.toString(songId)).appendPath(PATH_DOCUMENT).build();
        }
    }

    //AUTHOR
    public static final class SetlistsDbAuthorEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_AUTHOR).build();
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_AUTHOR;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_AUTHOR;

        public static final String TABLE_NAME = "author";
        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_EMAIL = "email";

        public static final String DEFAULT_SORT = COLUMN_NAME + " COLLATE NOCASE ASC";

        public static Uri buildSetlistsDbAuthorUri(long id) {
            return CONTENT_URI.buildUpon().appendPath(PATH_AUTHOR).appendPath(Long.toString(id)).build();
        }
    }

    //BAND
    public static final class SetlistsDbBandEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_BAND).build();
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BAND;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BAND;

        public static final String TABLE_NAME = "band";
        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_NAME = "name";

        public static final String DEFAULT_SORT = COLUMN_NAME + " COLLATE NOCASE ASC";

        public static Uri buildSetlistsDbBandUri(long id) {
            return CONTENT_URI.buildUpon().appendPath(Long.toString(id)).build();
        }
    }

    //MUSICIAN
    public static final class SetlistsDbMusicianEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_MUSICIAN).build();
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MUSICIAN;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MUSICIAN;

        public static final String TABLE_NAME = "musician";
        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_EMAIL = "email";
        public static final String COLUMN_INSTRUMENT = "instrument";
        public static final String COLUMN_BAND = "band";

        public static final String DEFAULT_SORT = COLUMN_NAME + " COLLATE NOCASE ASC";

        public static Uri buildSetlistsDbMusicianUri(long id) {
            return CONTENT_URI.buildUpon().appendPath(Long.toString(id)).build();
        }

        public static Uri buildSetlistsDbBandMusiciansUri(long bandId) {
            return SetlistsDbBandEntry.CONTENT_URI.buildUpon().appendPath(Long.toString(bandId)).appendPath(PATH_MUSICIAN).build();
        }
    }

    //SETLIST
    public static final class SetlistsDbSetEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_SET).build();
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SET;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SET;

        public static final String TABLE_NAME = "setlist";
        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_LOCATION = "location";
        public static final String COLUMN_BAND = "band";
        public static final String COLUMN_DATE_ADDED = "added";
        public static final String COLUMN_DATE_MODIFIED = "modified";
        public static final String COLUMN_AUTHOR = "author";

        public static final String DEFAULT_SORT = COLUMN_DATE_MODIFIED + " DESC";

        public static Uri buildSetlistsDbSetUri(long id) {
            return CONTENT_URI.buildUpon().appendPath(Long.toString(id)).build();
        }

        public static Uri buildSetlistsDbBandSetsUri(long bandId) {
            return SetlistsDbBandEntry.CONTENT_URI.buildUpon().appendPath(Long.toString(bandId)).appendPath(PATH_SET).build();
        }
    }

    //SETLIST SONG
    public static final class SetlistsDbSetSongEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_SET_SONG).build();
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SET_SONG;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SET_SONG;

        public static final String TABLE_NAME = "setlist_song";
        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_SETLIST = "setlist";
        public static final String COLUMN_SONG = "song";
        public static final String COLUMN_SEQUENCE = "sequence";

        public static final String DEFAULT_SORT = COLUMN_SEQUENCE + " ASC";

        public static Uri buildSetlistsDbSetSongUri(long id) {
            return CONTENT_URI.buildUpon().appendPath(Long.toString(id)).build();
        }

        public static Uri buildSetlistsDbSetSongForSetUri(long setId) {
            return SetlistsDbSetEntry.CONTENT_URI.buildUpon().appendPath(Long.toString(setId)).appendPath(PATH_SET_SONG).build();
        }
    }

    //MIDI MESSAGE
    public static final class SetlistsDbMidiMessageEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_MIDI_MESSAGE).build();
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MIDI_MESSAGE;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MIDI_MESSAGE;

        public static final String TABLE_NAME = "message";
        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_CHANNEL = "channel";
        public static final String COLUMN_STATUS = "status";
        public static final String COLUMN_DATA1 = "data1";
        public static final String COLUMN_DATA2 = "data2";

        public static final String DEFAULT_SORT = COLUMN_NAME + " COLLATE NOCASE ASC";

        public static Uri buildSetlistsDbMidiMessageUri(long id) {
            return CONTENT_URI.buildUpon().appendPath(Long.toString(id)).build();
        }
    }

    //DOCUMENT MESSAGES
    public static final class SetlistsDbDocumentMidiMessageEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_DOCUMENT_MIDI_MESSAGE).build();
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_DOCUMENT_MIDI_MESSAGE;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_DOCUMENT_MIDI_MESSAGE;

        public static final String TABLE_NAME = "document_message";
        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_DOCUMENT = "document";
        public static final String COLUMN_MESSAGE = "message";
        public static final String COLUMN_AUTOSEND = "autosend";

        public static final String DEFAULT_SORT = COLUMN_ID + " ASC";

        public static Uri buildSetlistsDbDocumentMidiMessageUri(long id) {
            return CONTENT_URI.buildUpon().appendPath(Long.toString(id)).build();
        }

        public static Uri buildSetlistsDbDocumentMidiMessagesForDocumentUri(long documentId) {
            return SetlistsDbDocumentEntry.CONTENT_URI.buildUpon().appendPath(Long.toString(documentId)).appendPath(PATH_DOCUMENT_MIDI_MESSAGE).build();
        }

        public static Uri buildSetlistsDbDocumentMidiMessagesForSetUri(long setId) {
            return SetlistsDbSetEntry.CONTENT_URI.buildUpon().appendPath(Long.toString(setId)).appendPath(PATH_DOCUMENT_MIDI_MESSAGE).build();
        }
    }
}
