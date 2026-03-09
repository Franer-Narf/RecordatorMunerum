package nc.instrumentum.recordatormunerum.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import nc.instrumentum.recordatormunerum.R;
import nc.instrumentum.recordatormunerum.data.repository.RegistratioRepository;
import nc.instrumentum.recordatormunerum.model.Registratio;
import nc.instrumentum.recordatormunerum.util.WorkScheduler;

import android.Manifest;
import android.os.Build;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import android.content.pm.PackageManager;


public class MainActivity extends AppCompatActivity {

    private RegistratioRepository repo;
    private RegistratioAdapter adapter;
    private ActivityResultLauncher<String> notifPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WorkScheduler.scheduleDaily(getApplicationContext());

        notifPermissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                    // si quieres, muestra toast si deniega, pero no es obligatorio
                });

        requestNotifPermissionIfNeeded();

        RecyclerView recycler = findViewById(R.id.recyclerRegistratio);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        repo = new RegistratioRepository(this);

        adapter = new RegistratioAdapter(new ArrayList<>());
        recycler.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fabAddRegistratio);

        fab.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditRegistratioActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        new Thread(() -> {
            List<Registratio> data = repo.getActivas();
            runOnUiThread(() -> adapter.setData(data)); // o adapter.updateData(data)
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