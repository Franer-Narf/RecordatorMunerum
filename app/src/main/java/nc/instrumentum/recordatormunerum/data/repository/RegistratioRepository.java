package nc.instrumentum.recordatormunerum.data.repository;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import nc.instrumentum.recordatormunerum.data.room.entity.RegistratioEntity;
import nc.instrumentum.recordatormunerum.data.room.mapper.RegistratioMapper;
import nc.instrumentum.recordatormunerum.data.room.dao.RegistratioDao;
import nc.instrumentum.recordatormunerum.data.room.database.AppDatabase;
import nc.instrumentum.recordatormunerum.model.Registratio;

public class RegistratioRepository {

    private final RegistratioDao registratioDao;

    public RegistratioRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        registratioDao = db.registratioDao();
    }

    // Insertar tarea
    public void insert(Registratio registratio) {
        long id = registratioDao.insert(
                RegistratioMapper.toEntity(registratio)
        );
        registratio.setId((int) id);
    }

    // Obtener tareas activas
    public List<Registratio> getActivas() {
        List<Registratio> result = new ArrayList<>();

        for (RegistratioEntity entity : registratioDao.getActivas()) {
            result.add(RegistratioMapper.toModel(entity));
        }

        return result;
    }

    public void desactivar(int id) {
        registratioDao.desactivar(id);
    }

    public void save(Registratio r) {
        if (r.getId() == 0) {
            long newId = registratioDao.insert(
                    RegistratioMapper.toEntity(r)
            );
            r.setId((int) newId);
        } else {
            registratioDao.update(
                    RegistratioMapper.toEntity(r)
            );
        }
    }

    public Registratio getById(int id) {
        return RegistratioMapper.toModel(
                registratioDao.getById(id)
        );
    }

    public List<Registratio> getAll() {

        List<Registratio> result = new ArrayList<>();

        for (RegistratioEntity entity : registratioDao.getAll()) {
            result.add(
                    RegistratioMapper.toModel(entity)
            );
        }

        return result;
    }

    public List<Registratio> getAllActivas() {
        List<Registratio> result = new ArrayList<>();

        for (RegistratioEntity entity : registratioDao.getAllActivas()) {
            result.add(RegistratioMapper.toModel(entity));
        }

        return result;
    }

}

