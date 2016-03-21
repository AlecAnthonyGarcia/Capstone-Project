package im.stillalive;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class StillAliveWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new StillAliveRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}