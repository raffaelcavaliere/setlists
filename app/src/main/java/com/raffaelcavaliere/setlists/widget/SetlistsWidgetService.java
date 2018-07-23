package com.raffaelcavaliere.setlists.widget;

import android.content.ContentProvider;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.raffaelcavaliere.setlists.R;
import com.raffaelcavaliere.setlists.data.SetlistsDbContract;
import com.raffaelcavaliere.setlists.data.SetlistsDbItemSong;
import com.raffaelcavaliere.setlists.ui.library.DocumentsActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * Created by raffaelcavaliere on 2018-01-07.
 */

public class SetlistsWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new SetlistsWidgetDataFactory(this, intent);
    }
}

class SetlistsWidgetDataFactory implements RemoteViewsService.RemoteViewsFactory {

    List<SetlistsDbItemSong> mCollection = new ArrayList<>();
    Context mContext = null;

    public SetlistsWidgetDataFactory(Context context, Intent intent) {
        mContext = context;
    }

    @Override
    public void onCreate() {
        loadData();
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public void onDataSetChanged() {
        Thread thread = new Thread() {
            public void run() {
                loadData();
            }
        };
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
        }
    }

    @Override
    public int getCount() {
        return mCollection.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {

        SetlistsDbItemSong song = mCollection.get(position);
        RemoteViews view = new RemoteViews(mContext.getPackageName(), R.layout.list_item_details);
        view.setTextViewText(R.id.item_details_title, song.getTitle());
        view.setTextViewText(R.id.item_details_subtitle, song.getArtistName());
        view.setTextViewText(R.id.item_details_comment_left, mContext.getResources().getString(R.string.bpm_note) + " " + String.valueOf(song.getTempo()));
        int duration = song.getDuration();
        int minutes = (int)(duration / 60);
        int seconds = (int)(duration % 60);
        view.setTextViewText(R.id.item_details_comment_center_right, String.valueOf(minutes) + ":" + (seconds < 10 ? "0" : "") + String.valueOf(seconds));
        view.setViewVisibility(R.id.item_details_comment_center_right, VISIBLE);
        String key = song.getKey();
        if (key != null) {
            view.setTextViewText(R.id.item_details_comment_center_left, key);
            view.setViewVisibility(R.id.item_details_comment_center_left, VISIBLE);
        }
        view.setViewVisibility(R.id.item_details_comment_right, GONE);
        view.setViewVisibility(R.id.item_details_menu, GONE);

        Intent fillInIntent = new Intent();
        fillInIntent.putExtra(SetlistsWidget.WIDGET_ACTION, SetlistsWidget.WIDGET_ACTION_SHOW_SONG);
        fillInIntent.putExtra("song", song.getId());
        fillInIntent.putExtra("title", song.getTitle());
        fillInIntent.putExtra("preferred", song.getDocument());
        fillInIntent.putExtra("tempo", song.getTempo());
        view.setOnClickFillInIntent(R.id.item_details_title, fillInIntent);

        return view;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    private void loadData() {
        mCollection.clear();
        Cursor c = mContext.getContentResolver().query(
                SetlistsDbContract.SetlistsDbSongEntry.CONTENT_URI,
                null, null, null, null
        );
        if (c != null && c.moveToFirst()) {
            do {
                mCollection.add(new SetlistsDbItemSong(c.getString(0), c.getString(8), c.getString(2), c.getString(3),
                        c.getString(1), c.getString(4), c.getInt(5), c.getInt(6), c.getString(7),
                        c.getString(9), new Date(c.getLong(11) * 1000)));
            } while (c.moveToNext());
        }
        if (c != null)
            c.close();
    }
}
