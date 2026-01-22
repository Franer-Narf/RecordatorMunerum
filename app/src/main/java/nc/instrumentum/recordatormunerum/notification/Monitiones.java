package nc.instrumentum.recordatormunerum.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import nc.instrumentum.recordatormunerum.R;
import nc.instrumentum.recordatormunerum.model.Registratio;
import nc.instrumentum.recordatormunerum.ui.EditRegistratioActivity;
import nc.instrumentum.recordatormunerum.ui.MainActivity;

public class Monitiones {

    private static final String CHANNEL_ID = "recordator_munerum";

    public static void mostrar(Context context, Registratio r) {
        crearCanal(context);

        // 1) Tap en notificación => abre app
        PendingIntent openAppPending = PendingIntent.getActivity(
                context,
                r.getId(),
                new Intent(context, MainActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // 2) Acción editar => abre EditRegistratioActivity
        Intent editIntent = new Intent(context, EditRegistratioActivity.class);
        editIntent.putExtra("id", r.getId()); // IMPORTANTE: tú lees "id" en loadIfEdit()
        PendingIntent editPending = PendingIntent.getActivity(
                context,
                r.getId() + 1000,
                editIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // 3) Acción completar => broadcast
        Intent completeIntent = new Intent(context, MonitionesReceiver.class);
        completeIntent.setAction(MonitionesReceiver.ACTION_COMPLETE);
        completeIntent.putExtra(MonitionesReceiver.EXTRA_ID, r.getId());

        PendingIntent completePending = PendingIntent.getBroadcast(
                context,
                r.getId() + 2000,
                completeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(r.getTitle())
                        .setContentText("Tarea pendiente")
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true)
                        .setContentIntent(openAppPending)
                        .addAction(0, "Editar", editPending)
                        .addAction(0, "Completar", completePending);

        NotificationManager nm =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        nm.notify(r.getId(), builder.build());
    }

    private static void crearCanal(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel =
                    new NotificationChannel(
                            CHANNEL_ID,
                            "Recordatorios",
                            NotificationManager.IMPORTANCE_HIGH
                    );

            NotificationManager nm =
                    context.getSystemService(NotificationManager.class);

            nm.createNotificationChannel(channel);
        }
    }
}
