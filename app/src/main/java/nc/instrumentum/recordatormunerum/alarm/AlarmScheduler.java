package nc.instrumentum.recordatormunerum.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import nc.instrumentum.recordatormunerum.model.Registratio;

public final class AlarmScheduler {

    public static void schedule(Context context, Registratio r) {

        Intent intent = new Intent(context, RegistratioAlarmReceiver.class);
        intent.putExtra("REGISTRATIO_ID", r.getId());

        PendingIntent pi = PendingIntent.getBroadcast(
                context,
                r.getId(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager am =
                (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        am.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                r.getNextTriggerMillis(),
                pi
        );
    }
}