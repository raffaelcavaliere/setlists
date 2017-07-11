package com.raffaelcavaliere.setlists.data;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.CursorLoader;

/**
 * Created by raffaelcavaliere on 2016-11-20.
 */

public class SetlistsDbArtistLoader extends CursorLoader {
    public static SetlistsDbArtistLoader newAllArtistsInstance(Context context) {
        return new SetlistsDbArtistLoader(context, SetlistsDbContract.SetlistsDbArtistEntry.CONTENT_URI);
    }

    public static SetlistsDbArtistLoader newInstanceForItemId(Context context, long itemId) {
        return new SetlistsDbArtistLoader(context, SetlistsDbContract.SetlistsDbArtistEntry.buildSetlistsDbArtistUri(itemId));
    }

    private SetlistsDbArtistLoader(Context context, Uri uri) {
        super(context, uri, null, null, null, SetlistsDbContract.SetlistsDbArtistEntry.DEFAULT_SORT);
    }
}
