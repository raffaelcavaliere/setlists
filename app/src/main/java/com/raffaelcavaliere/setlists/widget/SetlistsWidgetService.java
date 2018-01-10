package com.raffaelcavaliere.setlists.widget;

import android.content.Intent;
import android.widget.RemoteViewsService;

/**
 * Created by raffaelcavaliere on 2018-01-07.
 */

public class ç extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ListRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}

class ListRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

//... include adapter-like methods here. See the StackView Widget sample.

}
