package nc.instrumentum.recordatormunerum.widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import nc.instrumentum.recordatormunerum.data.repository.RegistratioRepository;
import nc.instrumentum.recordatormunerum.util.WidgetRefresher;

public class RegistratioWidgetReceiver extends BroadcastReceiver {

    public static final String ACTION_COMPLETE =
            "nc.instrumentum.recordatormunerum.ACTION_COMPLETE";

    @Override
    public void onReceive(Context context, Intent intent) {

        if (ACTION_COMPLETE.equals(intent.getAction())) {

            int id = intent.getIntExtra("REGISTRATIO_ID", -1);
            if (id == -1) return;

            RegistratioRepository repo =
                    new RegistratioRepository(context);

            repo.desactivar(id);

            WidgetRefresher.refresh(context);
        }
    }

}
