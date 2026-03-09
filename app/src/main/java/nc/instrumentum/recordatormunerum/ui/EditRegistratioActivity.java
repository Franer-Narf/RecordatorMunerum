package nc.instrumentum.recordatormunerum.ui;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import nc.instrumentum.recordatormunerum.R;
import nc.instrumentum.recordatormunerum.alarm.RegistratioAlarmScheduler;
import nc.instrumentum.recordatormunerum.data.repository.RegistratioRepository;
import nc.instrumentum.recordatormunerum.model.Registratio;
import nc.instrumentum.recordatormunerum.util.WidgetRefresher;
import nc.instrumentum.recordatormunerum.widget.RegistratioWidgetProvider;

public class EditRegistratioActivity extends AppCompatActivity {

    private RegistratioRepository repository;
    private Registratio registratio;

    private EditText editTitle;
    private TextView tvSelectedTime;
    private TextView tvMonthDaysValue;
    private TextView tvYearMonthsValue;
    private TextView tvStartDateValue;
    private TextView tvEndDateValue;

    private Button btnPickTime;
    private Button btnSelectMonthDays;
    private Button btnSelectYearMonths;
    private Button btnPickStartDate;
    private Button btnPickEndDate;
    private Button btnClearEndDate;
    private Button btnSave;
    private Button btnDelete;

    private CheckBox cbMon;
    private CheckBox cbTue;
    private CheckBox cbWed;
    private CheckBox cbThu;
    private CheckBox cbFri;
    private CheckBox cbSat;
    private CheckBox cbSun;

    private Spinner spinnerWeeks;
    private Spinner spinnerMonthlyPattern;

    private int selectedHour;
    private int selectedMinute;
    private long selectedStartDateMillis;
    private Long selectedEndDateMillis;

    private final boolean[] selectedMonthDays = new boolean[31];
    private final boolean[] selectedYearMonths = new boolean[12];

    private String legacyMonthDaysRaw;
    private String legacyYearMonthsRaw;
    private boolean monthDaysChangedByUser;
    private boolean yearMonthsChangedByUser;

    private final List<String> monthlyPatternValues = new ArrayList<>();

    private static final int REPEAT_EVERY_WEEK_INDEX = 1;
    private static final String MONTHLY_PATTERN_LAST_FRIDAY = "LAST_FRIDAY";
    private boolean recurrenceFilterUpdating;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_registratio);

        repository = new RegistratioRepository(this);

        initViews();
        setupSpinners();
        setupActions();
        loadIfEdit();

        btnSave.setOnClickListener(v -> onSave());
    }

    private void initViews() {
        editTitle = findViewById(R.id.editTitle);

        tvSelectedTime = findViewById(R.id.tvSelectedTime);
        tvMonthDaysValue = findViewById(R.id.tvMonthDaysValue);
        tvYearMonthsValue = findViewById(R.id.tvYearMonthsValue);
        tvStartDateValue = findViewById(R.id.tvStartDateValue);
        tvEndDateValue = findViewById(R.id.tvEndDateValue);

        btnPickTime = findViewById(R.id.btnPickTime);
        btnSelectMonthDays = findViewById(R.id.btnSelectMonthDays);
        btnSelectYearMonths = findViewById(R.id.btnSelectYearMonths);
        btnPickStartDate = findViewById(R.id.btnPickStartDate);
        btnPickEndDate = findViewById(R.id.btnPickEndDate);
        btnClearEndDate = findViewById(R.id.btnClearEndDate);
        btnSave = findViewById(R.id.btnSave);
        btnDelete = findViewById(R.id.btnDelete);

        cbMon = findViewById(R.id.cbMon);
        cbTue = findViewById(R.id.cbTue);
        cbWed = findViewById(R.id.cbWed);
        cbThu = findViewById(R.id.cbThu);
        cbFri = findViewById(R.id.cbFri);
        cbSat = findViewById(R.id.cbSat);
        cbSun = findViewById(R.id.cbSun);

        spinnerWeeks = findViewById(R.id.spinnerWeeks);
        spinnerMonthlyPattern = findViewById(R.id.spinnerMonthlyPattern);
    }

    private void setupSpinners() {
        ArrayAdapter<CharSequence> weeksAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.repeat_weeks_options,
                android.R.layout.simple_spinner_item
        );
        weeksAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerWeeks.setAdapter(weeksAdapter);

        String[] labels = getResources().getStringArray(R.array.monthly_pattern_labels);
        String[] values = getResources().getStringArray(R.array.monthly_pattern_values);

        monthlyPatternValues.clear();
        List<String> effectiveLabels = new ArrayList<>();

        int count = Math.min(labels.length, values.length);
        for (int i = 0; i < count; i++) {
            effectiveLabels.add(labels[i]);
            monthlyPatternValues.add(values[i]);
        }

        if (count == 0) {
            effectiveLabels.add("Sin patron");
            monthlyPatternValues.add("");
        }

        ArrayAdapter<String> patternAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                effectiveLabels
        );
        patternAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonthlyPattern.setAdapter(patternAdapter);
    }

    private void setupActions() {
        btnPickTime.setOnClickListener(v -> showTimePicker());
        btnSelectMonthDays.setOnClickListener(v -> showMonthDaysDialog());
        btnSelectYearMonths.setOnClickListener(v -> showYearMonthsDialog());
        btnPickStartDate.setOnClickListener(v -> showDatePicker(true));
        btnPickEndDate.setOnClickListener(v -> showDatePicker(false));
        btnClearEndDate.setOnClickListener(v -> {
            selectedEndDateMillis = null;
            updateDateViews();
        });
        btnDelete.setOnClickListener(v -> confirmDeleteCurrentAlarm());

        setupRecurrenceFilter();
    }

    private void setupRecurrenceFilter() {
        spinnerWeeks.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!recurrenceFilterUpdating) {
                    enforceLastFridayRepeatRuleFromWeeks();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No-op.
            }
        });

        spinnerMonthlyPattern.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!recurrenceFilterUpdating) {
                    enforceLastFridayRepeatRuleFromMonthlyPattern();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No-op.
            }
        });
    }

    private void enforceLastFridayRepeatRuleFromWeeks() {
        if (spinnerWeeks.getSelectedItemPosition() == REPEAT_EVERY_WEEK_INDEX && isLastFridaySelected()) {
            recurrenceFilterUpdating = true;
            spinnerMonthlyPattern.setSelection(0);
            recurrenceFilterUpdating = false;
            showError(getString(R.string.error_last_friday_with_weekly_repeat));
        }
    }

    private void enforceLastFridayRepeatRuleFromMonthlyPattern() {
        if (isLastFridaySelected() && spinnerWeeks.getSelectedItemPosition() == REPEAT_EVERY_WEEK_INDEX) {
            recurrenceFilterUpdating = true;
            spinnerWeeks.setSelection(0);
            recurrenceFilterUpdating = false;
            showError(getString(R.string.error_last_friday_with_weekly_repeat));
        }
    }

    private boolean isLastFridaySelected() {
        int selectedIndex = spinnerMonthlyPattern.getSelectedItemPosition();
        if (selectedIndex < 0 || selectedIndex >= monthlyPatternValues.size()) {
            return false;
        }
        return MONTHLY_PATTERN_LAST_FRIDAY.equals(monthlyPatternValues.get(selectedIndex));
    }
    private void loadIfEdit() {
        int id = getIntent().getIntExtra("id", -1);

        if (id != -1) {
            registratio = repository.getById(id);
            if (registratio != null) {
                btnDelete.setVisibility(View.VISIBLE);
                fillUI(registratio);
                return;
            }
        }

        btnDelete.setVisibility(View.GONE);

        registratio = new Registratio();
        Calendar now = Calendar.getInstance();
        selectedHour = now.get(Calendar.HOUR_OF_DAY);
        selectedMinute = now.get(Calendar.MINUTE);
        selectedStartDateMillis = normalizeToDayStart(now.getTimeInMillis());
        selectedEndDateMillis = null;

        updateTimeView();
        updateMonthDaysSummary();
        updateYearMonthsSummary();
        updateDateViews();
    }

    private void fillUI(Registratio r) {
        editTitle.setText(r.getTitle() == null ? "" : r.getTitle());

        selectedHour = r.getHour();
        selectedMinute = r.getMinute();
        updateTimeView();

        String weekDays = r.getWeekDays();
        cbMon.setChecked(containsDay(weekDays, Calendar.MONDAY));
        cbTue.setChecked(containsDay(weekDays, Calendar.TUESDAY));
        cbWed.setChecked(containsDay(weekDays, Calendar.WEDNESDAY));
        cbThu.setChecked(containsDay(weekDays, Calendar.THURSDAY));
        cbFri.setChecked(containsDay(weekDays, Calendar.FRIDAY));
        cbSat.setChecked(containsDay(weekDays, Calendar.SATURDAY));
        cbSun.setChecked(containsDay(weekDays, Calendar.SUNDAY));

        int weeks = r.getRepeatEveryWeeks();
        if (weeks < 0) {
            weeks = 0;
        }
        int max = spinnerWeeks.getAdapter() != null ? spinnerWeeks.getAdapter().getCount() - 1 : 0;
        spinnerWeeks.setSelection(Math.min(weeks, max));

        applyLegacyAwareSelection(r.getMonthDays(), selectedMonthDays, 1, 31, "32", true);
        applyLegacyAwareSelection(r.getYearMonths(), selectedYearMonths, 1, 12, "13", false);
        applyMonthlyPattern(r.getMonthlyPattern());

        long start = r.getStartDateMillis();
        selectedStartDateMillis = start > 0
                ? normalizeToDayStart(start)
                : normalizeToDayStart(System.currentTimeMillis());

        selectedEndDateMillis = r.getEndDateMillis();
        updateDateViews();
    }

    private void applyMonthlyPattern(String rawPattern) {
        String pattern = TextUtils.isEmpty(rawPattern) ? "" : rawPattern.trim();

        int selectedIndex = monthlyPatternValues.indexOf(pattern);
        if (selectedIndex >= 0) {
            spinnerMonthlyPattern.setSelection(selectedIndex);
            return;
        }

        if (TextUtils.isEmpty(pattern)) {
            spinnerMonthlyPattern.setSelection(0);
            return;
        }

        @SuppressWarnings("unchecked")
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerMonthlyPattern.getAdapter();
        if (adapter != null) {
            adapter.add(getString(R.string.label_legacy_pattern, pattern));
            monthlyPatternValues.add(pattern);
            spinnerMonthlyPattern.setSelection(adapter.getCount() - 1);
        }
    }

    private void applyLegacyAwareSelection(
            String rawValue,
            boolean[] destination,
            int min,
            int max,
            String allValue,
            boolean monthDays
    ) {
        Arrays.fill(destination, false);

        if (TextUtils.isEmpty(rawValue) || allValue.equals(rawValue.trim())) {
            if (monthDays) {
                legacyMonthDaysRaw = null;
                updateMonthDaysSummary();
            } else {
                legacyYearMonthsRaw = null;
                updateYearMonthsSummary();
            }
            return;
        }

        List<Integer> parsed = new ArrayList<>();
        boolean legacyDetected = false;

        for (String token : rawValue.split(",")) {
            String trimmed = token.trim();
            if (trimmed.isEmpty()) {
                continue;
            }

            try {
                int value = Integer.parseInt(trimmed);
                if (value >= min && value <= max) {
                    parsed.add(value);
                } else {
                    legacyDetected = true;
                }
            } catch (NumberFormatException ignored) {
                legacyDetected = true;
            }
        }

        if (parsed.isEmpty() || legacyDetected) {
            if (monthDays) {
                legacyMonthDaysRaw = rawValue;
                updateMonthDaysSummary();
            } else {
                legacyYearMonthsRaw = rawValue;
                updateYearMonthsSummary();
            }
            return;
        }

        for (Integer value : parsed) {
            destination[value - min] = true;
        }

        if (monthDays) {
            legacyMonthDaysRaw = null;
            updateMonthDaysSummary();
        } else {
            legacyYearMonthsRaw = null;
            updateYearMonthsSummary();
        }
    }

    private void showTimePicker() {
        TimePickerDialog dialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    selectedHour = hourOfDay;
                    selectedMinute = minute;
                    updateTimeView();
                },
                selectedHour,
                selectedMinute,
                true
        );
        dialog.show();
    }

    private void showDatePicker(boolean startDate) {
        Calendar seed = Calendar.getInstance();
        long seedMillis;

        if (startDate) {
            seedMillis = selectedStartDateMillis > 0
                    ? selectedStartDateMillis
                    : System.currentTimeMillis();
        } else {
            seedMillis = (selectedEndDateMillis != null && selectedEndDateMillis > 0)
                    ? selectedEndDateMillis
                    : (selectedStartDateMillis > 0
                    ? selectedStartDateMillis
                    : System.currentTimeMillis());
        }

        seed.setTimeInMillis(seedMillis);

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(Calendar.YEAR, year);
                    selected.set(Calendar.MONTH, month);
                    selected.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    if (startDate) {
                        selectedStartDateMillis = normalizeToDayStart(selected.getTimeInMillis());
                    } else {
                        selectedEndDateMillis = normalizeToDayEnd(selected.getTimeInMillis());
                    }

                    updateDateViews();
                },
                seed.get(Calendar.YEAR),
                seed.get(Calendar.MONTH),
                seed.get(Calendar.DAY_OF_MONTH)
        );

        dialog.show();
    }

    private void showMonthDaysDialog() {
        boolean[] checked = Arrays.copyOf(selectedMonthDays, selectedMonthDays.length);
        String[] labels = new String[31];

        for (int i = 0; i < 31; i++) {
            labels[i] = String.valueOf(i + 1);
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_select_month_days_title)
                .setMultiChoiceItems(labels, checked, (dialog, which, isChecked) -> checked[which] = isChecked)
                .setPositiveButton(R.string.dialog_accept, (dialog, which) -> {
                    monthDaysChangedByUser = true;
                    System.arraycopy(checked, 0, selectedMonthDays, 0, checked.length);
                    legacyMonthDaysRaw = null;
                    updateMonthDaysSummary();
                })
                .setNeutralButton(R.string.dialog_select_all, (dialog, which) -> {
                    monthDaysChangedByUser = true;
                    Arrays.fill(selectedMonthDays, false);
                    legacyMonthDaysRaw = null;
                    updateMonthDaysSummary();
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .show();
    }

    private void showYearMonthsDialog() {
        boolean[] checked = Arrays.copyOf(selectedYearMonths, selectedYearMonths.length);
        String[] labels = new String[12];

        for (int i = 0; i < 12; i++) {
            labels[i] = getString(R.string.value_month_format, i + 1);
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_select_year_months_title)
                .setMultiChoiceItems(labels, checked, (dialog, which, isChecked) -> checked[which] = isChecked)
                .setPositiveButton(R.string.dialog_accept, (dialog, which) -> {
                    yearMonthsChangedByUser = true;
                    System.arraycopy(checked, 0, selectedYearMonths, 0, checked.length);
                    legacyYearMonthsRaw = null;
                    updateYearMonthsSummary();
                })
                .setNeutralButton(R.string.dialog_select_all, (dialog, which) -> {
                    yearMonthsChangedByUser = true;
                    Arrays.fill(selectedYearMonths, false);
                    legacyYearMonthsRaw = null;
                    updateYearMonthsSummary();
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .show();
    }

    private void updateTimeView() {
        tvSelectedTime.setText(formatTime(selectedHour, selectedMinute));
    }

    private void updateMonthDaysSummary() {
        if (!monthDaysChangedByUser && !TextUtils.isEmpty(legacyMonthDaysRaw)) {
            tvMonthDaysValue.setText(getString(R.string.value_legacy_kept, legacyMonthDaysRaw));
            return;
        }

        String csv = toCsvOrAll(selectedMonthDays, 1, "32");
        if ("32".equals(csv)) {
            tvMonthDaysValue.setText(R.string.value_all_month_days);
        } else {
            tvMonthDaysValue.setText(getString(R.string.value_month_days_selected, csv));
        }
    }

    private void updateYearMonthsSummary() {
        if (!yearMonthsChangedByUser && !TextUtils.isEmpty(legacyYearMonthsRaw)) {
            tvYearMonthsValue.setText(getString(R.string.value_legacy_kept, legacyYearMonthsRaw));
            return;
        }

        String csv = toCsvOrAll(selectedYearMonths, 1, "13");
        if ("13".equals(csv)) {
            tvYearMonthsValue.setText(R.string.value_all_year_months);
        } else {
            tvYearMonthsValue.setText(getString(R.string.value_year_months_selected, csv));
        }
    }

    private void updateDateViews() {
        if (selectedStartDateMillis > 0) {
            tvStartDateValue.setText(formatDate(selectedStartDateMillis));
        } else {
            tvStartDateValue.setText(R.string.value_not_set);
        }

        if (selectedEndDateMillis != null && selectedEndDateMillis > 0) {
            tvEndDateValue.setText(formatDate(selectedEndDateMillis));
        } else {
            tvEndDateValue.setText(R.string.value_no_end_date);
        }
    }

    private void onSave() {
        if (!readAndValidate()) {
            return;
        }

        new Thread(() -> {
            repository.save(registratio);

            if (registratio.getActive()) {
                nc.instrumentum.recordatormunerum.alarm.RegistratioAlarmScheduler
                        .scheduleOne(EditRegistratioActivity.this, registratio);
            } else {
                nc.instrumentum.recordatormunerum.alarm.RegistratioAlarmScheduler
                        .cancelOne(EditRegistratioActivity.this, registratio.getId());
            }

            runOnUiThread(() -> {
                updateWidget();
                finish();
            });
        }).start();
    }

    private void confirmDeleteCurrentAlarm() {
        if (registratio == null || registratio.getId() == 0) {
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_delete_alarm_title)
                .setMessage(getString(R.string.dialog_delete_alarm_message, registratio.getTitle()))
                .setPositiveButton(R.string.dialog_delete_alarm_confirm, (dialog, which) -> deleteCurrentAlarm())
                .setNegativeButton(R.string.dialog_cancel, null)
                .show();
    }

    private void deleteCurrentAlarm() {
        if (registratio == null || registratio.getId() == 0) {
            return;
        }

        new Thread(() -> {
            repository.deleteById(registratio.getId());
            RegistratioAlarmScheduler.cancelOne(EditRegistratioActivity.this, registratio.getId());

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
                .getAppWidgetIds(new ComponentName(this, RegistratioWidgetProvider.class));

        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        sendBroadcast(intent);
        WidgetRefresher.refresh(this);
    }

    private boolean readAndValidate() {
        String title = editTitle.getText().toString().trim();
        if (title.isEmpty()) {
            showError(getString(R.string.error_title_empty));
            return false;
        }

        registratio.setTitle(title);
        registratio.setHour(selectedHour);
        registratio.setMinute(selectedMinute);
        registratio.setActive(true);

        registratio.setWeekDays(buildWeekDays());
        registratio.setRepeatEveryWeeks(spinnerWeeks.getSelectedItemPosition());

        String monthDays = (!monthDaysChangedByUser && !TextUtils.isEmpty(legacyMonthDaysRaw))
                ? legacyMonthDaysRaw
                : toCsvOrAll(selectedMonthDays, 1, "32");
        registratio.setMonthDays(monthDays);

        String yearMonths = (!yearMonthsChangedByUser && !TextUtils.isEmpty(legacyYearMonthsRaw))
                ? legacyYearMonthsRaw
                : toCsvOrAll(selectedYearMonths, 1, "13");
        registratio.setYearMonths(yearMonths);

        String selectedPattern = "";
        int patternIndex = spinnerMonthlyPattern.getSelectedItemPosition();
        if (patternIndex >= 0 && patternIndex < monthlyPatternValues.size()) {
            selectedPattern = monthlyPatternValues.get(patternIndex);
        }

        if (spinnerWeeks.getSelectedItemPosition() == REPEAT_EVERY_WEEK_INDEX
                && MONTHLY_PATTERN_LAST_FRIDAY.equals(selectedPattern)) {
            showError(getString(R.string.error_last_friday_with_weekly_repeat));
            return false;
        }

        registratio.setMonthlyPattern(TextUtils.isEmpty(selectedPattern) ? null : selectedPattern);

        long startDateMillis = selectedStartDateMillis > 0
                ? selectedStartDateMillis
                : normalizeToDayStart(System.currentTimeMillis());

        if (selectedEndDateMillis != null
                && selectedEndDateMillis > 0
                && selectedEndDateMillis < startDateMillis) {
            showError(getString(R.string.error_end_before_start));
            return false;
        }

        registratio.setStartDateMillis(startDateMillis);
        registratio.setEndDateMillis(selectedEndDateMillis);

        return true;
    }

    private String toCsvOrAll(boolean[] selected, int baseValue, String allValue) {
        List<String> values = new ArrayList<>();

        for (int i = 0; i < selected.length; i++) {
            if (selected[i]) {
                values.add(String.valueOf(i + baseValue));
            }
        }

        return values.isEmpty() ? allValue : TextUtils.join(",", values);
    }

    private String formatDate(long millis) {
        DateFormat format = android.text.format.DateFormat.getMediumDateFormat(this);
        return getString(R.string.value_date_format, format.format(millis));
    }

    private String formatTime(int hour, int minute) {
        return getString(R.string.value_time_format, hour, minute);
    }

    private String buildWeekDays() {
        List<String> days = new ArrayList<>();

        if (cbMon.isChecked()) {
            days.add(String.valueOf(Calendar.MONDAY));
        }
        if (cbTue.isChecked()) {
            days.add(String.valueOf(Calendar.TUESDAY));
        }
        if (cbWed.isChecked()) {
            days.add(String.valueOf(Calendar.WEDNESDAY));
        }
        if (cbThu.isChecked()) {
            days.add(String.valueOf(Calendar.THURSDAY));
        }
        if (cbFri.isChecked()) {
            days.add(String.valueOf(Calendar.FRIDAY));
        }
        if (cbSat.isChecked()) {
            days.add(String.valueOf(Calendar.SATURDAY));
        }
        if (cbSun.isChecked()) {
            days.add(String.valueOf(Calendar.SUNDAY));
        }

        return TextUtils.join(",", days);
    }

    private boolean containsDay(String csvDays, int calendarDay) {
        return containsToken(csvDays, String.valueOf(calendarDay))
                || containsToken(csvDays, String.valueOf(calendarDayToLegacy(calendarDay)));
    }

    private boolean containsToken(String csv, String token) {
        if (TextUtils.isEmpty(csv)) {
            return false;
        }

        for (String part : csv.split(",")) {
            if (token.equals(part.trim())) {
                return true;
            }
        }

        return false;
    }

    private int calendarDayToLegacy(int calendarDay) {
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

    private long normalizeToDayStart(long millis) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(millis);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    private long normalizeToDayEnd(long millis) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(millis);
        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        c.set(Calendar.MILLISECOND, 999);
        return c.getTimeInMillis();
    }

    private void showError(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
