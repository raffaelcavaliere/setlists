package com.raffaelcavaliere.setlists.data;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.CursorLoader;

/**
 * Created by raffaelcavaliere on 2016-11-20.
 */

public class SetlistsDbDeletedRecordLoader extends CursorLoader {
    public static SetlistsDbDeletedRecordLoader newAllDeletedRecordsInstance(Context context) {
        return new SetlistsDbDeletedRecordLoader(context, SetlistsDbContract.SetlistsDbDeletedRecordEntry.CONTENT_URI);
    }

    public static SetlistsDbDeletedRecordLoader newInstanceForItemId(Context context, String itemId) {
        return new SetlistsDbDeletedRecordLoader(context, SetlistsDbContract.SetlistsDbDeletedRecordEntry.buildSetlistsDbDeletedRecordUri(itemId));
    }

    private SetlistsDbDeletedRecordLoader(Context context, Uri uri) {
        super(context, uri, null, null, null, SetlistsDbContract.SetlistsDbDeletedRecordEntry.DEFAULT_SORT);
    }
}
