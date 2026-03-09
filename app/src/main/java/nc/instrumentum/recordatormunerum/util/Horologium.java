package nc.instrumentum.recordatormunerum.util;

import java.util.Calendar;

import nc.instrumentum.recordatormunerum.model.Registratio;

public final class Horologium {

    private static final long ONE_WEEK_MILLIS = 7L * 24L * 60L * 60L * 1000L;

    private Horologium() {
    }

    public static boolean tocaHoy(Registratio r) {
        if (r == null || !r.getActive()) {
            return false;
        }

        Calendar now = Calendar.getInstance();
        return tocaEnFecha(r, now);
    }

    public static boolean tocaHoy(Registratio r, long millis) {
        if (r == null || !r.getActive()) {
            return false;
        }

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);
        return tocaEnFecha(r, cal);
    }

    private static boolean tocaEnFecha(Registratio r, Calendar now) {
        if (!dentroDeRango(r, now)) {
            return false;
        }
        if (!pasaFiltroSemanas(r, now)) {
            return false;
        }
        if (!pasaFiltroDiasSemana(r, now)) {
            return false;
        }
        if (!pasaFiltroMensual(r, now)) {
            return false;
        }
        return pasaFiltroAnual(r, now);
    }

    private static boolean dentroDeRango(Registratio r, Calendar now) {
        long nowMillis = now.getTimeInMillis();

        if (r.getStartDateMillis() > 0 && nowMillis < r.getStartDateMillis()) {
            return false;
        }

        Long endDateMillis = r.getEndDateMillis();
        if (endDateMillis != null && endDateMillis > 0 && nowMillis > endDateMillis) {
            return false;
        }

        return true;
    }

    private static boolean pasaFiltroSemanas(Registratio r, Calendar now) {
        int repeat = r.getRepeatEveryWeeks();
        if (repeat <= 0) {
            return true;
        }

        long startMillis = r.getStartDateMillis();
        if (startMillis <= 0) {
            return true;
        }

        Calendar start = Calendar.getInstance();
        start.setTimeInMillis(startMillis);
        normalizeToDayStart(start);

        Calendar current = (Calendar) now.clone();
        normalizeToDayStart(current);

        long diffMillis = current.getTimeInMillis() - start.getTimeInMillis();
        if (diffMillis < 0) {
            return false;
        }

        long weeks = diffMillis / ONE_WEEK_MILLIS;

        if (repeat == 4) {
            return weeks % 2 == 0;
        }

        return weeks % repeat == 0;
    }

    private static boolean pasaFiltroDiasSemana(Registratio r, Calendar now) {
        String days = r.getWeekDays();

        if (days == null || days.trim().isEmpty() || "0".equals(days) || "8".equals(days)) {
            return true;
        }

        int todayCalendar = now.get(Calendar.DAY_OF_WEEK);
        int todayLegacy = calendarDayToLegacy(todayCalendar);

        return contieneValor(days, todayCalendar) || contieneValor(days, todayLegacy);
    }

    private static boolean pasaFiltroMensual(Registratio r, Calendar now) {
        String monthlyPattern = r.getMonthlyPattern();
        if (monthlyPattern != null && !monthlyPattern.trim().isEmpty()) {
            return cumplePatronMensual(monthlyPattern.trim(), now);
        }

        String monthDays = r.getMonthDays();
        if (monthDays == null || monthDays.trim().isEmpty() || "0".equals(monthDays) || "32".equals(monthDays)) {
            return true;
        }

        int dayOfMonth = now.get(Calendar.DAY_OF_MONTH);
        return contieneValor(monthDays, dayOfMonth);
    }

    private static boolean pasaFiltroAnual(Registratio r, Calendar now) {
        String months = r.getYearMonths();

        if (months == null || months.trim().isEmpty() || "0".equals(months) || "13".equals(months)) {
            return true;
        }

        int month = now.get(Calendar.MONTH) + 1;
        return contieneValor(months, month);
    }

    private static boolean contieneValor(String csv, int value) {
        if (csv == null || csv.trim().isEmpty()) {
            return false;
        }

        for (String p : csv.split(",")) {
            String trimmed = p.trim();
            if (trimmed.isEmpty()) {
                continue;
            }

            try {
                if (Integer.parseInt(trimmed) == value) {
                    return true;
                }
            } catch (NumberFormatException ignored) {
                // Ignore malformed legacy tokens.
            }
        }

        return false;
    }

    private static boolean cumplePatronMensual(String pattern, Calendar now) {
        String[] parts = pattern.split("_");
        if (parts.length != 2) {
            return false;
        }

        int expectedDay = resolverDia(parts[1]);
        if (expectedDay == 0) {
            return false;
        }

        int dayOfMonth = now.get(Calendar.DAY_OF_MONTH);
        int dayOfWeek = now.get(Calendar.DAY_OF_WEEK);

        if (dayOfWeek != expectedDay) {
            return false;
        }

        switch (parts[0]) {
            case "FIRST":
                return dayOfMonth <= 7;
            case "SECOND":
                return dayOfMonth >= 8 && dayOfMonth <= 14;
            case "THIRD":
                return dayOfMonth >= 15 && dayOfMonth <= 21;
            case "FOURTH":
                return dayOfMonth >= 22 && dayOfMonth <= 28;
            case "LAST":
                Calendar last = (Calendar) now.clone();
                last.set(Calendar.DAY_OF_MONTH, last.getActualMaximum(Calendar.DAY_OF_MONTH));
                while (last.get(Calendar.DAY_OF_WEEK) != expectedDay) {
                    last.add(Calendar.DAY_OF_MONTH, -1);
                }
                return dayOfMonth == last.get(Calendar.DAY_OF_MONTH);
            default:
                return false;
        }
    }

    private static int resolverDia(String suffix) {
        switch (suffix) {
            case "MONDAY":
                return Calendar.MONDAY;
            case "TUESDAY":
                return Calendar.TUESDAY;
            case "WEDNESDAY":
                return Calendar.WEDNESDAY;
            case "THURSDAY":
                return Calendar.THURSDAY;
            case "FRIDAY":
                return Calendar.FRIDAY;
            case "SATURDAY":
                return Calendar.SATURDAY;
            case "SUNDAY":
                return Calendar.SUNDAY;
            default:
                return 0;
        }
    }

    private static int calendarDayToLegacy(int calendarDay) {
        switch (calendarDay) {
            case Calendar.MONDAY:
                return 1;
            case Calendar.TUESDAY:
                return 2;
            case Calendar.WEDNESDAY:
                return 3;
            case Calendar.THURSDAY:
                return 4;
            case Calendar.FRIDAY:
                return 5;
            case Calendar.SATURDAY:
                return 6;
            case Calendar.SUNDAY:
                return 7;
            default:
                return calendarDay;
        }
    }

    private static void normalizeToDayStart(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }
}
