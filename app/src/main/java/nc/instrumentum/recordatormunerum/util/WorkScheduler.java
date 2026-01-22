package nc.instrumentum.recordatormunerum.util;

import android.content.Context;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

import nc.instrumentum.recordatormunerum.notification.MonitionesWorker;

public class WorkScheduler {

    public static void scheduleDaily(Context context) {

        PeriodicWorkRequest work =
                new PeriodicWorkRequest.Builder(
                        MonitionesWorker.class,
                        1,
                        TimeUnit.DAYS
                ).build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "daily_notifications",
                ExistingPeriodicWorkPolicy.UPDATE,
                work
        );
    }
}
