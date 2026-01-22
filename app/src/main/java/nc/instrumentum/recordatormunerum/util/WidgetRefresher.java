package nc.instrumentum.recordatormunerum.util;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;

import nc.instrumentum.recordatormunerum.R;
import nc.instrumentum.recordatormunerum.widget.RegistratioWidgetProvider;

public class WidgetRefresher {

    public static void refresh(Context context) {
        AppWidgetManager manager =
                AppWidgetManager.getInstance(context);

        ComponentName widget =
                new ComponentName(
                        context,
                        RegistratioWidgetProvider.class
                );

        manager.notifyAppWidgetViewDataChanged(
                manager.getAppWidgetIds(widget),
                R.id.widget_list
        );
    }
}
