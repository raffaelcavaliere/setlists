package com.raffaelcavaliere.setlists.data;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.CursorLoader;

/**
 * Created by raffaelcavaliere on 2016-11-20.
 */

public class SetlistsDbSongLoader extends CursorLoader {
    public static SetlistsDbSongLoader newAllSongsInstance(Context context) {
        return new SetlistsDbSongLoader(context, SetlistsDbContract.SetlistsDbSongEntry.CONTENT_URI);
    }

    public static SetlistsDbSongLoader newInstanceForItemId(Context context, String itemId) {
        return new SetlistsDbSongLoader(context, SetlistsDbContract.SetlistsDbSongEntry.buildSetlistsDbSongUri(itemId));
    }

    public static SetlistsDbSongLoader newInstanceForArtistId(Context context, String artistId) {
        return new SetlistsDbSongLoader(context, SetlistsDbContract.SetlistsDbSongEntry.buildSetlistsDbArtistSongsUri(artistId));
    }

    private SetlistsDbSongLoader(Context context, Uri uri) {
        super(context, uri, null, null, null, SetlistsDbContract.SetlistsDbSongEntry.DEFAULT_SORT);
    }
}
