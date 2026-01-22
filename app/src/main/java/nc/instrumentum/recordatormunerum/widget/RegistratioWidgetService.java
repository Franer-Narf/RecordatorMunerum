package nc.instrumentum.recordatormunerum.widget;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class RegistratioWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RegistratioWidgetFactory(getApplicationContext());
    }
}
