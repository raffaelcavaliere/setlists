package com.raffaelcavaliere.setlists.data;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.CursorLoader;

/**
 * Created by raffaelcavaliere on 2016-11-20.
 */

public class SetlistsDbBandLoader extends CursorLoader {
    public static SetlistsDbBandLoader newAllBandsInstance(Context context) {
        return new SetlistsDbBandLoader(context, SetlistsDbContract.SetlistsDbBandEntry.CONTENT_URI);
    }

    public static SetlistsDbBandLoader newInstanceForItemId(Context context, String itemId) {
        return new SetlistsDbBandLoader(context, SetlistsDbContract.SetlistsDbBandEntry.buildSetlistsDbBandUri(itemId));
    }

    private SetlistsDbBandLoader(Context context, Uri uri) {
        super(context, uri, null, null, null, SetlistsDbContract.SetlistsDbBandEntry.DEFAULT_SORT);
    }
}
