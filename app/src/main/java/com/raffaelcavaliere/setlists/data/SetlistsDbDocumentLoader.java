package com.raffaelcavaliere.setlists.data;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.CursorLoader;

/**
 * Created by raffaelcavaliere on 2016-11-20.
 */

public class SetlistsDbDocumentLoader extends CursorLoader {
    public static SetlistsDbDocumentLoader newAllDocumentsInstance(Context context) {
        return new SetlistsDbDocumentLoader(context, SetlistsDbContract.SetlistsDbDocumentEntry.CONTENT_URI);
    }

    public static SetlistsDbDocumentLoader newInstanceForSongId(Context context, String songId) {
        return new SetlistsDbDocumentLoader(context, SetlistsDbContract.SetlistsDbDocumentEntry.buildSetlistsDbSongDocumentsUri(songId));
    }

    public static SetlistsDbDocumentLoader newInstanceForItemId(Context context, String itemId) {
        return new SetlistsDbDocumentLoader(context, SetlistsDbContract.SetlistsDbDocumentEntry.buildSetlistsDbDocumentUri(itemId));
    }

    private SetlistsDbDocumentLoader(Context context, Uri uri) {
        super(context, uri, null, null, null, SetlistsDbContract.SetlistsDbDocumentEntry.DEFAULT_SORT);
    }
}
