package nc.instrumentum.recordatormunerum.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import nc.instrumentum.recordatormunerum.R;
import nc.instrumentum.recordatormunerum.ui.EditRegistratioActivity;

public class RegistratioWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(
            Context context,
            AppWidgetManager appWidgetManager,
            int[] appWidgetIds
    ) {
        for (int widgetId : appWidgetIds) {

            RemoteViews views =
                    new RemoteViews(
                            context.getPackageName(),
                            R.layout.widget_registratio
                    );

            // Adapter del ListView
            Intent serviceIntent =
                    new Intent(context, RegistratioWidgetService.class);

            views.setRemoteAdapter(
                    R.id.widget_list,
                    serviceIntent
            );

            // 🔑 TEMPLATE DE CLICK (ESTO FALTABA)
            Intent clickIntent =
                    new Intent(context, EditRegistratioActivity.class);

            PendingIntent clickPending =
                    PendingIntent.getActivity(
                            context,
                            0,
                            clickIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                                    | PendingIntent.FLAG_IMMUTABLE
                    );

            views.setPendingIntentTemplate(
                    R.id.widget_list,
                    clickPending
            );

            appWidgetManager.updateAppWidget(widgetId, views);
        }
    }

}
