package nc.instrumentum.recordatormunerum.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import nc.instrumentum.recordatormunerum.R;
import nc.instrumentum.recordatormunerum.model.Registratio;

public final class NotificationHelper {

    private static final String CHANNEL_ID = "registratio_channel";

    public static void show(Context context, Registratio r) {

        NotificationManager nm =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Recordatorios",
                    NotificationManager.IMPORTANCE_HIGH
            );
            nm.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(android.R.drawable.ic_dialog_info)
                        .setContentTitle("Recordatorio")
                        .setContentText(r.getTitle())
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_HIGH);

        nm.notify(r.getId(), builder.build());
    }
}
