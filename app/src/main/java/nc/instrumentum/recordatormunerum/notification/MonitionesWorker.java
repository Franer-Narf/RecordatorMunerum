package nc.instrumentum.recordatormunerum.notification;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.List;

import nc.instrumentum.recordatormunerum.data.repository.RegistratioRepository;
import nc.instrumentum.recordatormunerum.model.Registratio;
import nc.instrumentum.recordatormunerum.util.Horologium;

public class MonitionesWorker extends Worker {

    private final RegistratioRepository repository;

    public MonitionesWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        repository = new RegistratioRepository(context);
    }

    @NonNull
    @Override
    public Result doWork() {

        List<Registratio> activas = repository.getActivas();
        long now = System.currentTimeMillis();

        for (Registratio r : activas) {
            if (Horologium.tocaHoy(r, now)) {
                Monitiones.mostrar(getApplicationContext(), r);
            }
        }

        return Result.success();
    }
}

