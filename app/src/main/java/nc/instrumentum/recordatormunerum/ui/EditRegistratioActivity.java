package nc.instrumentum.recordatormunerum.ui;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


import nc.instrumentum.recordatormunerum.R;
import nc.instrumentum.recordatormunerum.data.repository.RegistratioRepository;
import nc.instrumentum.recordatormunerum.model.Registratio;
import nc.instrumentum.recordatormunerum.util.WidgetRefresher;
import nc.instrumentum.recordatormunerum.widget.RegistratioWidgetProvider;

public class EditRegistratioActivity extends AppCompatActivity {

    private static final int[] LEGACY_MON_TO_SUN = {
            1, 2, 3, 4, 5, 6, 7
    };
    private static final int[] CALENDAR_MON_TO_SUN = {
            Calendar.MONDAY,
            Calendar.TUESDAY,
            Calendar.WEDNESDAY,
            Calendar.THURSDAY,
            Calendar.FRIDAY,
            Calendar.SATURDAY,
            Calendar.SUNDAY
    };


    private RegistratioRepository repository;
    private Registratio registratio;

    private EditText editTitle;
    private EditText editHour;
    private EditText editMinute;
    private EditText editMonthDays;
    private EditText editYearMonths;
    private EditText editMonthlyPattern;
    private EditText editStartDateMillis;
    private EditText editEndDateMillis;

    private Button btnSave;
    private CheckBox cbMon, cbTue, cbWed, cbThu, cbFri, cbSat, cbSun;
    private Spinner spinnerWeeks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_registratio);

        repository = new RegistratioRepository(this);

        initViews();

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.repeat_weeks_options,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerWeeks.setAdapter(adapter);

        loadIfEdit();

        btnSave.setOnClickListener(v -> onSave());
    }

    private void initViews() {
        editTitle = findViewById(R.id.editTitle);
        editHour = findViewById(R.id.editHour);
        editMinute = findViewById(R.id.editMinute);
        editMonthDays = findViewById(R.id.editMonthDays);
        editYearMonths = findViewById(R.id.editYearMonths);
        editMonthlyPattern = findViewById(R.id.editMonthlyPattern);
        editStartDateMillis = findViewById(R.id.editStartDateMillis);
        editEndDateMillis = findViewById(R.id.editEndDateMillis);

        btnSave = findViewById(R.id.btnSave);

        cbMon = findViewById(R.id.cbMon);
        cbTue = findViewById(R.id.cbTue);
        cbWed = findViewById(R.id.cbWed);
        cbThu = findViewById(R.id.cbThu);
        cbFri = findViewById(R.id.cbFri);
        cbSat = findViewById(R.id.cbSat);
        cbSun = findViewById(R.id.cbSun);

        spinnerWeeks = findViewById(R.id.spinnerWeeks);
    }

    private void loadIfEdit() {
        int id = getIntent().getIntExtra("id", -1);

        if (id != -1) {
            registratio = repository.getById(id);
            fillUI(registratio);
        } else {
            registratio = new Registratio();
        }
    }

    private void fillUI(Registratio r) {
        editTitle.setText(r.getTitle());
        editHour.setText(String.valueOf(r.getHour()));
        editMinute.setText(String.valueOf(r.getMinute()));

        String normalizedDays = normalizeWeekDays(r.getWeekDays());

        cbMon.setChecked(containsDay(normalizedDays, Calendar.MONDAY));
        cbTue.setChecked(containsDay(normalizedDays, Calendar.TUESDAY));
        cbWed.setChecked(containsDay(normalizedDays, Calendar.WEDNESDAY));
        cbThu.setChecked(containsDay(normalizedDays, Calendar.THURSDAY));
        cbFri.setChecked(containsDay(normalizedDays, Calendar.FRIDAY));
        cbSat.setChecked(containsDay(normalizedDays, Calendar.SATURDAY));
        cbSun.setChecked(containsDay(normalizedDays, Calendar.SUNDAY));


        int v = r.getRepeatEveryWeeks();
        if (v < 0) v = 0;

        int max = spinnerWeeks.getAdapter() != null
                ? spinnerWeeks.getAdapter().getCount() - 1
                : 0;

        if (v > max) v = max;
        spinnerWeeks.setSelection(v);

        String monthDays = r.getMonthDays();
        editMonthDays.setText(monthDays == null ? "" : monthDays);

        String yearMonths = r.getYearMonths();
        editYearMonths.setText(yearMonths == null ? "" : yearMonths);

        String monthlyPattern = r.getMonthlyPattern();
        editMonthlyPattern.setText(monthlyPattern == null ? "" : monthlyPattern);

        long startMillis = r.getStartDateMillis();
        editStartDateMillis.setText(startMillis > 0 ? String.valueOf(startMillis) : "");

        Long endMillis = r.getEndDateMillis();
        editEndDateMillis.setText(endMillis != null && endMillis > 0
                ? String.valueOf(endMillis)
                : "");

    }

    private void onSave() {
        if (!readAndValidate()) return;

        new Thread(() -> {
            // 1) Guardar
            repository.save(registratio);

            // 2) Programar / cancelar alarma
            if (registratio.getActive()) {
                nc.instrumentum.recordatormunerum.alarm.RegistratioAlarmScheduler
                        .scheduleOne(EditRegistratioActivity.this, registratio);
            } else {
                nc.instrumentum.recordatormunerum.alarm.RegistratioAlarmScheduler
                        .cancelOne(EditRegistratioActivity.this, registratio.getId());
            }

            // 3) Widget + cerrar en UI thread
            runOnUiThread(() -> {
                updateWidget();
                finish();
            });
        }).start();
    }


    private void updateWidget() {
        Intent intent = new Intent(this, RegistratioWidgetProvider.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);

        int[] ids = AppWidgetManager.getInstance(this)
                .getAppWidgetIds(
                        new ComponentName(this, RegistratioWidgetProvider.class)
                );

        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        sendBroadcast(intent);
        WidgetRefresher.refresh(this);
    }

    private boolean readAndValidate() {
        String title = editTitle.getText().toString().trim();

        if (title.isEmpty()) {
            showError("The title cannot be empty");
            return false;
        }

        registratio.setTitle(title);
        registratio.setHour(parseIntSafe(editHour));
        registratio.setMinute(parseIntSafe(editMinute));
        registratio.setActive(true);

        registratio.setWeekDays(buildWeekDays());

        registratio.setRepeatEveryWeeks(spinnerWeeks.getSelectedItemPosition());

        registratio.setMonthDays(normalizeCsvAll(editMonthDays, "32"));
        registratio.setYearMonths(normalizeCsvAll(editYearMonths, "13"));

        String monthlyPattern = editMonthlyPattern.getText().toString().trim();
        registratio.setMonthlyPattern(monthlyPattern.isEmpty() ? null : monthlyPattern);

        registratio.setStartDateMillis(parseLongSafe(editStartDateMillis));
        registratio.setEndDateMillis(parseNullableLong(editEndDateMillis));

        return true;
    }

    private String buildWeekDays() {
        List<String> days = new ArrayList<>();

        if (cbMon.isChecked()) days.add(String.valueOf(Calendar.MONDAY));
        if (cbTue.isChecked()) days.add(String.valueOf(Calendar.TUESDAY));
        if (cbWed.isChecked()) days.add(String.valueOf(Calendar.WEDNESDAY));
        if (cbThu.isChecked()) days.add(String.valueOf(Calendar.THURSDAY));
        if (cbFri.isChecked()) days.add(String.valueOf(Calendar.FRIDAY));
        if (cbSat.isChecked()) days.add(String.valueOf(Calendar.SATURDAY));
        if (cbSun.isChecked()) days.add(String.valueOf(Calendar.SUNDAY));


        return TextUtils.join(",", days);
    }

    private boolean containsDay(String days, int dayOfWeek) {
        return containsToken(days, String.valueOf(dayOfWeek));
    }

    private boolean containsToken(String csv, String token) {
        if (TextUtils.isEmpty(csv)) return false;
        for (String part : csv.split(",")) {
            if (token.equals(part.trim())) {
                return true;
            }
        }
        return false;
    }

    private String normalizeWeekDays(String weekDays) {
        if (TextUtils.isEmpty(weekDays)) return "";

        Set<String> normalized = new LinkedHashSet<>();
        boolean remappedLegacy = false;

        for (String part : weekDays.split(",")) {
            String trimmed = part.trim();
            if (trimmed.isEmpty()) continue;

            int parsed;
            try {
                parsed = Integer.parseInt(trimmed);
            } catch (NumberFormatException ignored) {
                normalized.add(trimmed);
                continue;
            }

            int calendarDay = mapLegacyToCalendar(parsed);
            if (calendarDay != parsed) {
                remappedLegacy = true;
            }
            normalized.add(String.valueOf(calendarDay));
        }

        String normalizedCsv = TextUtils.join(",", normalized);
        if (remappedLegacy && registratio != null) {
            registratio.setWeekDays(normalizedCsv);
            new Thread(() -> repository.save(registratio)).start();
        }
        return normalizedCsv;
    }

    private int mapLegacyToCalendar(int legacyDay) {
        for (int i = 0; i < LEGACY_MON_TO_SUN.length; i++) {
            if (LEGACY_MON_TO_SUN[i] == legacyDay) {
                return CALENDAR_MON_TO_SUN[i];
            }
        }
        return legacyDay;
    }


    private int parseIntSafe(EditText et) {
        try {
            return Integer.parseInt(et.getText().toString());
        } catch (Exception e) {
            return 0;
        }
    }

    private long parseLongSafe(EditText et) {
        try {
            return Long.parseLong(et.getText().toString().trim());
        } catch (Exception e) {
            return 0L;
        }
    }

    private Long parseNullableLong(EditText et) {
        String value = et.getText().toString().trim();
        if (value.isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (Exception e) {
            return null;
        }
    }

    private String normalizeCsvAll(EditText et, String allValue) {
        String value = et.getText().toString().trim();
        return value.isEmpty() ? allValue : value;
    }

    private void showError(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
