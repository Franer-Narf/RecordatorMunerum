package nc.instrumentum.recordatormunerum.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import nc.instrumentum.recordatormunerum.R;
import nc.instrumentum.recordatormunerum.alarm.RegistratioAlarmScheduler;
import nc.instrumentum.recordatormunerum.data.repository.RegistratioRepository;
import nc.instrumentum.recordatormunerum.model.Registratio;
import nc.instrumentum.recordatormunerum.util.WorkScheduler;

public class MainActivity extends AppCompatActivity {

    private RegistratioRepository repo;
    private RegistratioAdapter adapter;
    private ActivityResultLauncher<String> notifPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WorkScheduler.scheduleDaily(getApplicationContext());

        notifPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> {
                    // No additional action needed.
                }
        );

        requestNotifPermissionIfNeeded();

        repo = new RegistratioRepository(this);

        RecyclerView recycler = findViewById(R.id.recyclerRegistratio);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        adapter = new RegistratioAdapter(new ArrayList<>(), new RegistratioAdapter.Listener() {
            @Override
            public void onOpen(Registratio registratio) {
                openEditor(registratio.getId());
            }

            @Override
            public void onLongPress(Registratio registratio) {
                confirmDeleteOne(registratio);
            }
        });
        recycler.setAdapter(adapter);

        findViewById(R.id.btnQuickAdd).setOnClickListener(v -> openEditor());
        findViewById(R.id.btnDeleteAll).setOnClickListener(v -> confirmDeleteAll());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        new Thread(() -> {
            List<Registratio> data = repo.getActivas();
            runOnUiThread(() -> adapter.setData(data));
        }).start();
    }

    private void openEditor() {
        startActivity(new Intent(this, EditRegistratioActivity.class));
    }

    private void openEditor(int id) {
        Intent intent = new Intent(this, EditRegistratioActivity.class);
        intent.putExtra("id", id);
        startActivity(intent);
    }

    private void confirmDeleteOne(Registratio registratio) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.main_dialog_delete_one_title)
                .setMessage(getString(R.string.main_dialog_delete_one_message, registratio.getTitle()))
                .setPositiveButton(R.string.main_dialog_confirm, (dialog, which) -> deleteOne(registratio))
                .setNegativeButton(R.string.main_dialog_cancel, null)
                .show();
    }

    private void deleteOne(Registratio registratio) {
        new Thread(() -> {
            repo.deleteById(registratio.getId());
            RegistratioAlarmScheduler.cancelOne(MainActivity.this, registratio.getId());
            runOnUiThread(this::loadData);
        }).start();
    }

    private void confirmDeleteAll() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.main_dialog_delete_all_title)
                .setMessage(R.string.main_dialog_delete_all_message)
                .setPositiveButton(R.string.main_dialog_confirm, (dialog, which) -> deleteAllActive())
                .setNegativeButton(R.string.main_dialog_cancel, null)
                .show();
    }

    private void deleteAllActive() {
        new Thread(() -> {
            List<Registratio> activeItems = repo.getActivas();
            for (Registratio registratio : activeItems) {
                RegistratioAlarmScheduler.cancelOne(MainActivity.this, registratio.getId());
            }
            repo.deleteAllActivas();

            runOnUiThread(() -> {
                loadData();
                Toast.makeText(this, R.string.main_toast_deleted_all, Toast.LENGTH_SHORT).show();
            });
        }).start();
    }

    private void requestNotifPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }
}