package nc.instrumentum.recordatormunerum.ui;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
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
import java.util.List;

import nc.instrumentum.recordatormunerum.R;
import nc.instrumentum.recordatormunerum.alarm.AlarmScheduler;
import nc.instrumentum.recordatormunerum.data.repository.RegistratioRepository;
import nc.instrumentum.recordatormunerum.model.Registratio;
import nc.instrumentum.recordatormunerum.widget.RegistratioWidgetProvider;

public class EditRegistratioActivity extends AppCompatActivity {

    private RegistratioRepository repository;
    private Registratio registratio;

    private EditText editTitle;
    private EditText editHour;
    private EditText editMinute;
    private Button btnSave;
    private CheckBox cbMon, cbTue, cbWed, cbThu, cbFri, cbSat, cbSun;
    private Spinner spinnerWeeks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_registratio);

        repository = new RegistratioRepository(this);

        initViews();
        loadIfEdit();

        btnSave.setOnClickListener(v -> onSave());

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.repeat_weeks_options,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerWeeks.setAdapter(adapter);
    }

    private void initViews() {
        editTitle = findViewById(R.id.editTitle);
        editHour = findViewById(R.id.editHour);
        editMinute = findViewById(R.id.editMinute);
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

        String days = r.getWeekDays();
        if (days == null) days = "";

        cbMon.setChecked(days.contains("1"));
        cbTue.setChecked(days.contains("2"));
        cbWed.setChecked(days.contains("3"));
        cbThu.setChecked(days.contains("4"));
        cbFri.setChecked(days.contains("5"));
        cbSat.setChecked(days.contains("6"));
        cbSun.setChecked(days.contains("7"));

        int v = r.getRepeatEveryWeeks();   // 0..4
        if (v < 0) v = 0;
        if (v > 4) v = 4;
        spinnerWeeks.setSelection(v);
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

        return true;
    }

    private String buildWeekDays() {
        List<String> days = new ArrayList<>();

        if (cbMon.isChecked()) days.add("1");
        if (cbTue.isChecked()) days.add("2");
        if (cbWed.isChecked()) days.add("3");
        if (cbThu.isChecked()) days.add("4");
        if (cbFri.isChecked()) days.add("5");
        if (cbSat.isChecked()) days.add("6");
        if (cbSun.isChecked()) days.add("7");

        return TextUtils.join(",", days);
    }


    private int parseIntSafe(EditText et) {
        try {
            return Integer.parseInt(et.getText().toString());
        } catch (Exception e) {
            return 0;
        }
    }

    private void showError(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
