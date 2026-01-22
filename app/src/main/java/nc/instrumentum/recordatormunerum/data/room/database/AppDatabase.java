package nc.instrumentum.recordatormunerum.data.room.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import nc.instrumentum.recordatormunerum.data.room.dao.RegistratioDao;
import nc.instrumentum.recordatormunerum.data.room.entity.RegistratioEntity;

@Database(entities = {RegistratioEntity.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract RegistratioDao registratioDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "registratio_db"
                    ).allowMainThreadQueries() // Temporal solution
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}