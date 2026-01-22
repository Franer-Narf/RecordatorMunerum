package nc.instrumentum.recordatormunerum.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import nc.instrumentum.recordatormunerum.data.repository.RegistratioRepository;
import nc.instrumentum.recordatormunerum.model.Registratio;
import nc.instrumentum.recordatormunerum.notification.Monitiones;

public class RegistratioAlarmReceiver extends BroadcastReceiver {

    public static final String ACTION_ALARM =
            "nc.instrumentum.recordatormunerum.ACTION_ALARM";

    @Override
    public void onReceive(Context context, Intent intent) {

        if (!ACTION_ALARM.equals(intent.getAction())) return;

        int id = intent.getIntExtra("REGISTRATIO_ID", -1);
        if (id == -1) return;


        new Thread(() -> {
            RegistratioRepository repo = new RegistratioRepository(context);
            Registratio r = repo.getById(id);
            if (r != null && r.getActive()) {
                Monitiones.mostrar(context, r);
                RegistratioAlarmScheduler.scheduleOne(context, r);
            }
        }).start();

    }
}
