package nc.instrumentum.recordatormunerum.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;
import java.util.List;

import nc.instrumentum.recordatormunerum.data.repository.RegistratioRepository;
import nc.instrumentum.recordatormunerum.model.Registratio;
import nc.instrumentum.recordatormunerum.util.Horologium;

public final class RegistratioAlarmScheduler {

    private RegistratioAlarmScheduler() {}

    // ✅ Para tu EditRegistratioActivity
    public static void scheduleOne(Context context, Registratio r) {
        if (r == null) return;
        if (!r.getActive()) return;

        long triggerAt = computeNextTriggerMillis(r);
        if (triggerAt <= 0) return;

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;

        Intent i = new Intent(context, RegistratioAlarmReceiver.class);
        i.setAction(RegistratioAlarmReceiver.ACTION_ALARM);
        i.putExtra("REGISTRATIO_ID", r.getId());

        PendingIntent pi = PendingIntent.getBroadcast(
                context,
                r.getId(),
                i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        try {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi);
        } catch (SecurityException se) {
            // Si el sistema no permite exact alarms, degradamos a una no-exacta:
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi);
        }
    }

    public static void cancelOne(Context context, int registratioId) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;

        Intent i = new Intent(context, RegistratioAlarmReceiver.class);
        i.setAction(RegistratioAlarmReceiver.ACTION_ALARM);

        PendingIntent pi = PendingIntent.getBroadcast(
                context,
                registratioId,
                i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        am.cancel(pi);
    }

    //BootReceiver
    public static void scheduleAll(Context context) {
        RegistratioRepository repo = new RegistratioRepository(context);

        new Thread(() -> {
            List<Registratio> actives = repo.getAllActivas();
            for (Registratio r : actives) {
                scheduleOne(context, r);
            }
        }).start();
    }

    // (Opcional) compat con tu scheduler viejo
    public static void schedule(Context context, Registratio r) {
        scheduleOne(context, r);
    }

    public static void cancel(Context context, int registratioId) {
        cancelOne(context, registratioId);
    }

    private static long computeNextTriggerMillis(Registratio r) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.HOUR_OF_DAY, r.getHour());
        cal.set(Calendar.MINUTE, r.getMinute());

        if (cal.getTimeInMillis() <= System.currentTimeMillis()) {
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }

        for (int i = 0; i < 366; i++) {
            long candidate = cal.getTimeInMillis();

            if (Horologium.tocaHoy(r, candidate)) {
                return candidate;
            }

            cal.add(Calendar.DAY_OF_MONTH, 1);
        }

        return -1;
    }
}
