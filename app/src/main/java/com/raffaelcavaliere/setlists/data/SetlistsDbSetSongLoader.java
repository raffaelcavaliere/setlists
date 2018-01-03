package com.raffaelcavaliere.setlists.data;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.CursorLoader;

/**
 * Created by raffaelcavaliere on 2016-11-20.
 */

public class SetlistsDbSetSongLoader extends CursorLoader {
    public static SetlistsDbSetSongLoader newAllSetSongsInstance(Context context) {
        return new SetlistsDbSetSongLoader(context, SetlistsDbContract.SetlistsDbSetSongEntry.CONTENT_URI);
    }

    public static SetlistsDbSetSongLoader newInstanceForItemId(Context context, String itemId) {
        return new SetlistsDbSetSongLoader(context, SetlistsDbContract.SetlistsDbSetSongEntry.buildSetlistsDbSetSongUri(itemId));
    }

    public static SetlistsDbSetSongLoader newInstanceForSetId(Context context, String setId) {
        return new SetlistsDbSetSongLoader(context, SetlistsDbContract.SetlistsDbSetSongEntry.buildSetlistsDbSetSongForSetUri(setId));
    }

    private SetlistsDbSetSongLoader(Context context, Uri uri) {
        super(context, uri, null, null, null, SetlistsDbContract.SetlistsDbSetSongEntry.DEFAULT_SORT);
    }
}
