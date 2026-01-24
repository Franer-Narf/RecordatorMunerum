package nc.instrumentum.recordatormunerum.util;

import java.util.Calendar;

import nc.instrumentum.recordatormunerum.model.Registratio;

public final class Horologium {

    private Horologium() {
    }

    public static boolean tocaHoy(Registratio r) {

        if (!r.getActive()) return false;

        Calendar now = Calendar.getInstance();

        if (!dentroDeRango(r, now)) return false;
        if (!pasaFiltroSemanas(r, now)) return false;
        if (!pasaFiltroDiasSemana(r, now)) return false;
        if (!pasaFiltroMensual(r, now)) return false;
        if (!pasaFiltroAnual(r, now)) return false;

        return true;
    }

    public static boolean tocaHoy(Registratio r, long millis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);

        if (!dentroDeRango(r, cal)) return false;
        if (!pasaFiltroSemanas(r, cal)) return false;
        if (!pasaFiltroDiasSemana(r, cal)) return false;
        if (!pasaFiltroMensual(r, cal)) return false;
        if (!pasaFiltroAnual(r, cal)) return false;

        return true;
    }


    private static boolean dentroDeRango(Registratio r, Calendar now) {

        if (now.getTimeInMillis() < r.getStartDateMillis()) {
            return false;
        }

        if (r.getEndDateMillis() != null &&
                now.getTimeInMillis() > r.getEndDateMillis()) {
            return false;
        }

        return true;
    }

    private static boolean pasaFiltroSemanas(Registratio r, Calendar now) {

        int repeat = r.getRepeatEveryWeeks();
        if (repeat == 0) return true;

        Calendar start = Calendar.getInstance();
        start.setTimeInMillis(r.getStartDateMillis());

        long diffMillis = now.getTimeInMillis() - start.getTimeInMillis();
        long weeks = diffMillis / (7L * 24 * 60 * 60 * 1000);

        if (repeat == 4) {
            // semanas alternas: 0,2,4...
            return weeks % 2 == 0;
        }

        return weeks % repeat == 0;
    }

    private static boolean pasaFiltroDiasSemana(Registratio r, Calendar now) {

        String days = r.getWeekDays();

        if (days == null || days.equals("0")) return true;
        if (days.equals("8")) return true;

        int today = now.get(Calendar.DAY_OF_WEEK);
        // Calendar.SUNDAY = 1 ... SATURDAY = 7

        return contieneValor(days, today);
    }

    private static boolean pasaFiltroMensual(Registratio r, Calendar now) {

        // Patrón mensual tiene prioridad
        if (r.getMonthlyPattern() != null && !r.getMonthlyPattern().isEmpty()) {
            return cumplePatronMensual(r.getMonthlyPattern(), now);
        }

        String monthDays = r.getMonthDays();

        if (monthDays == null || monthDays.equals("0")) return true;
        if (monthDays.equals("32")) return true;

        int dayOfMonth = now.get(Calendar.DAY_OF_MONTH);

        return contieneValor(monthDays, dayOfMonth);
    }

    private static boolean pasaFiltroAnual(Registratio r, Calendar now) {

        String months = r.getYearMonths();

        if (months == null || months.equals("0")) return true;
        if (months.equals("13")) return true;

        int month = now.get(Calendar.MONTH) + 1;

        return contieneValor(months, month);
    }

    private static boolean contieneValor(String csv, int value) {

        if (csv == null || csv.trim().isEmpty()) {
            return false;
        }

        String[] parts = csv.split(",");

        for (String p : parts) {
            String trimmed = p.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            if (Integer.parseInt(trimmed) == value) {
                return true;
            }
        }
        return false;
    }

    private static boolean cumplePatronMensual(String pattern, Calendar now) {

        if (pattern == null || pattern.isEmpty()) {
            return false;
        }

        String[] texto = pattern.split("_");                          // FORMATO: ORDEN_DIA (FIRST_MONDAY)
        if (texto.length != 2) {
            return false;
        }

        int diaActual = now.get(Calendar.DAY_OF_WEEK);                     // Get the today's day name (number).
        int diaMesActual = now.get(Calendar.DAY_OF_MONTH);                 // Get today's month number.
        int ultimoDia = now.getActualMaximum(Calendar.DAY_OF_MONTH);       // Get last number of the day in this month.                          // Get th prefix and suffix of pattern.

        int textoFinal = resolverDia(texto[1]);

        switch (texto[0]) {
            case "FIRST":		//1 - 7
                if(diaMesActual <= 7 && diaActual == textoFinal) {
                    return true;}
                break;
            case "SECOND":	//8-14
                if(diaMesActual >= 8 && diaMesActual <= 14 && diaActual == textoFinal) {
                    return true;}
                break;
            case "THIRD":		//15 - 21
                if(diaMesActual >= 15 && diaMesActual <= 21 && diaActual == textoFinal) {
                    return true;}
                break;
            case "FOURTH":	//22 - 28
                if(diaMesActual >= 22 && diaMesActual <= 28 && diaActual == textoFinal) {
                    return true;}
                break;
            case "LAST":		//maxDiasMes-7 - maxDiasMes
                for (int i = 0; (ultimoDia-i) > (ultimoDia-7); i++) {
                if(diaMesActual == ultimoDia-i && diaActual == textoFinal) {
                    return true;}}
            break;
        }
        return false;
    }

    private static int resolverDia(String subfix) {
        int aux = 0;

        switch (subfix) {
            case "MONDAY":
                aux = Calendar.MONDAY;
            break;
            case "TUESDAY":
                aux = Calendar.TUESDAY;
           break;
            case "WEDNESDAY":
                aux = Calendar.WEDNESDAY;
            break;
            case "THURSDAY":
                aux = Calendar.THURSDAY;
            break;
            case "FRIDAY":
                aux = Calendar.FRIDAY;
            break;
            case "SATURDAY":
                aux = Calendar.SATURDAY;
            break;
            case "SUNDAY":
                aux = Calendar.SUNDAY;
            break;
        }
        return aux;
    }
}