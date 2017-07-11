package com.raffaelcavaliere.setlists.utils;

import android.support.v7.widget.RecyclerView;

/**
 * Created by raffaelcavaliere on 2017-07-01.
 */

public interface OnStartDragListener {

    /**
     * Called when a view is requesting a start of a drag.
     *
     * @param viewHolder The holder of the view to drag.
     */
    void onStartDrag(RecyclerView.ViewHolder viewHolder);

}