package com.raffaelcavaliere.setlists.data;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.CursorLoader;

/**
 * Created by raffaelcavaliere on 2016-11-20.
 */

public class SetlistsDbMusicianLoader extends CursorLoader {
    public static SetlistsDbMusicianLoader newAllMusiciansInstance(Context context) {
        return new SetlistsDbMusicianLoader(context, SetlistsDbContract.SetlistsDbMusicianEntry.CONTENT_URI);
    }

    public static SetlistsDbMusicianLoader newInstanceForItemId(Context context, String itemId) {
        return new SetlistsDbMusicianLoader(context, SetlistsDbContract.SetlistsDbMusicianEntry.buildSetlistsDbMusicianUri(itemId));
    }

    public static SetlistsDbMusicianLoader newInstanceForBandId(Context context, String bandId) {
        return new SetlistsDbMusicianLoader(context, SetlistsDbContract.SetlistsDbMusicianEntry.buildSetlistsDbBandMusiciansUri(bandId));
    }

    private SetlistsDbMusicianLoader(Context context, Uri uri) {
        super(context, uri, null, null, null, SetlistsDbContract.SetlistsDbMusicianEntry.DEFAULT_SORT);
    }
}
