package nc.instrumentum.recordatormunerum.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import nc.instrumentum.recordatormunerum.data.repository.RegistratioRepository;
import nc.instrumentum.recordatormunerum.util.WidgetRefresher;

public class MonitionesReceiver extends BroadcastReceiver {

    public static final String ACTION_EDIT =
            "nc.instrumentum.recordatormunerum.ACTION_NOTIFICATION_EDIT";

    public static final String ACTION_COMPLETE =
            "nc.instrumentum.recordatormunerum.ACTION_NOTIFICATION_COMPLETE";

    public static final String EXTRA_ID = "REGISTRATIO_ID";

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();
        int id = intent.getIntExtra(EXTRA_ID, -1);
        if (id == -1 || action == null) return;

        if (ACTION_COMPLETE.equals(action)) {
            new Thread(() -> {
                RegistratioRepository repo = new RegistratioRepository(context);
                repo.desactivar(id);

                // Quitar la notificación
                android.app.NotificationManager nm =
                        (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                if (nm != null) nm.cancel(id);

                // refresca widget
                WidgetRefresher.refresh(context);
            }).start();
        }
    }
}
