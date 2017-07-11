package com.raffaelcavaliere.setlists.data;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.CursorLoader;

/**
 * Created by raffaelcavaliere on 2016-11-20.
 */

public class SetlistsDbDocumentMidiMessageLoader extends CursorLoader {
    public static SetlistsDbDocumentMidiMessageLoader newAllDocumentMidiMessagesInstance(Context context) {
        return new SetlistsDbDocumentMidiMessageLoader(context, SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.CONTENT_URI);
    }

    public static SetlistsDbDocumentMidiMessageLoader newInstanceForItemId(Context context, long itemId) {
        return new SetlistsDbDocumentMidiMessageLoader(context, SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.buildSetlistsDbDocumentMidiMessageUri(itemId));
    }

    public static SetlistsDbDocumentMidiMessageLoader newInstanceForDocumentId(Context context, long documentId) {
        return new SetlistsDbDocumentMidiMessageLoader(context, SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.buildSetlistsDbDocumentMidiMessagesForDocumentUri(documentId));
    }

    public static SetlistsDbDocumentMidiMessageLoader newInstanceForSetId(Context context, long setId) {
        return new SetlistsDbDocumentMidiMessageLoader(context, SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.buildSetlistsDbDocumentMidiMessagesForSetUri(setId));
    }

    private SetlistsDbDocumentMidiMessageLoader(Context context, Uri uri) {
        super(context, uri, null, null, null, SetlistsDbContract.SetlistsDbDocumentMidiMessageEntry.DEFAULT_SORT);
    }
}
