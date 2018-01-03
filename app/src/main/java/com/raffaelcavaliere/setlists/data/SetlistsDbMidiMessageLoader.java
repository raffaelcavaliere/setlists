package com.raffaelcavaliere.setlists.data;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.CursorLoader;

/**
 * Created by raffaelcavaliere on 2016-11-20.
 */

public class SetlistsDbMidiMessageLoader extends CursorLoader {
    public static SetlistsDbMidiMessageLoader newAllMidiMessagesInstance(Context context) {
        return new SetlistsDbMidiMessageLoader(context, SetlistsDbContract.SetlistsDbMidiMessageEntry.CONTENT_URI);
    }

    public static SetlistsDbMidiMessageLoader newInstanceForItemId(Context context, String itemId) {
        return new SetlistsDbMidiMessageLoader(context, SetlistsDbContract.SetlistsDbMidiMessageEntry.buildSetlistsDbMidiMessageUri(itemId));
    }

    private SetlistsDbMidiMessageLoader(Context context, Uri uri) {
        super(context, uri, null, null, null, SetlistsDbContract.SetlistsDbMidiMessageEntry.DEFAULT_SORT);
    }
}
