package nc.instrumentum.recordatormunerum.data.room.mapper;

import nc.instrumentum.recordatormunerum.model.Registratio;
import nc.instrumentum.recordatormunerum.data.room.entity.RegistratioEntity;

public class RegistratioMapper {

    // Modelo → Entity (guardar en BD)
    public static RegistratioEntity toEntity(Registratio model) {
        RegistratioEntity entity = new RegistratioEntity();
        entity.id = model.getId();
        entity.titulus = model.getTitle();
        entity.activa = model.getActive();
        entity.dies = model.getWeekDays();
        entity.hebdomadae = model.getRepeatEveryWeeks();
        entity.diesMenses = model.getMonthDays();
        entity.menses = model.getYearMonths();
        entity.modus = model.getMonthlyPattern();
        entity.hora = model.getHour();
        entity.minuta = model.getMinute();
        entity.initium = model.getStartDateMillis();
        entity.finis = model.getEndDateMillis();
        return entity;
    }

    // Entity → Modelo (usar en la app)
    public static Registratio toModel(RegistratioEntity entity) {
        return new Registratio(
                entity.id,
                entity.titulus,
                entity.dies,
                entity.hebdomadae,
                entity.diesMenses,
                entity.menses,
                entity.modus,
                entity.hora,
                entity.minuta,
                entity.activa,
                entity.initium,
                entity.finis
        );
    }
}
