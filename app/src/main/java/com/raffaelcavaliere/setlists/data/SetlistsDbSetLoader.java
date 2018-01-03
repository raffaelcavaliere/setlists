package com.raffaelcavaliere.setlists.data;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.CursorLoader;

/**
 * Created by raffaelcavaliere on 2016-11-20.
 */

public class SetlistsDbSetLoader extends CursorLoader {
    public static SetlistsDbSetLoader newAllSetsInstance(Context context) {
        return new SetlistsDbSetLoader(context, SetlistsDbContract.SetlistsDbSetEntry.CONTENT_URI);
    }

    public static SetlistsDbSetLoader newInstanceForItemId(Context context, String itemId) {
        return new SetlistsDbSetLoader(context, SetlistsDbContract.SetlistsDbSetEntry.buildSetlistsDbSetUri(itemId));
    }

    public static SetlistsDbSetLoader newInstanceForBandId(Context context, String bandId) {
        return new SetlistsDbSetLoader(context, SetlistsDbContract.SetlistsDbSetEntry.buildSetlistsDbBandSetsUri(bandId));
    }

    private SetlistsDbSetLoader(Context context, Uri uri) {
        super(context, uri, null, null, null, SetlistsDbContract.SetlistsDbSetEntry.DEFAULT_SORT);
    }
}
