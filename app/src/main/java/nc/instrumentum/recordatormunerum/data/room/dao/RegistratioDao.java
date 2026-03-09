package nc.instrumentum.recordatormunerum.data.room.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import nc.instrumentum.recordatormunerum.data.room.entity.RegistratioEntity;

@Dao
public interface RegistratioDao {

    @Insert
    long insert(RegistratioEntity entity);

    @Query("SELECT * FROM registratio WHERE activa = 1")
    List<RegistratioEntity> getActivas();

    @Query("UPDATE registratio SET activa = 0 WHERE id = :id")
    void desactivar(int id);

    @Query("DELETE FROM registratio WHERE id = :id")
    void deleteById(int id);

    @Query("DELETE FROM registratio WHERE activa = 1")
    void deleteAllActivas();

    @Update
    void update(RegistratioEntity entity);

    @Query("SELECT * FROM registratio WHERE id = :id")
    RegistratioEntity getById(int id);

    @Query("SELECT * FROM registratio ORDER BY hora, minuta")
    List<RegistratioEntity> getAll();

    @Query("SELECT * FROM registratio WHERE activa = 1")
    List<RegistratioEntity> getAllActivas();
}