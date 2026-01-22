package nc.instrumentum.recordatormunerum.data.room.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "registratio")
public class RegistratioEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String titulus;

    public String dies;

    public int hebdomadae;

    public String diesMenses;

    public String menses;

    public String modus;

    public int hora;

    public int minuta;

    public boolean activa;

    public long initium;

    public Long finis;
}
