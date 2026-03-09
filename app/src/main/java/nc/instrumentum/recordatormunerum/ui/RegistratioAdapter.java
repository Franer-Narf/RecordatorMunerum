package nc.instrumentum.recordatormunerum.ui;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

import nc.instrumentum.recordatormunerum.R;
import nc.instrumentum.recordatormunerum.model.Registratio;

public class RegistratioAdapter extends RecyclerView.Adapter<RegistratioAdapter.ViewHolder> {

    public interface Listener {
        void onOpen(Registratio registratio);
        void onLongPress(Registratio registratio);
    }

    private List<Registratio> data;
    private final Listener listener;

    public RegistratioAdapter(List<Registratio> data, Listener listener) {
        this.data = data;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_registratio, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Registratio registratio = data.get(position);
        Context context = holder.itemView.getContext();

        holder.title.setText(registratio.getTitle());

        String recurrenceSummary = buildRecurrenceSummary(context, registratio);
        holder.subtitle.setText(
                context.getString(
                        R.string.main_item_subtitle,
                        registratio.getHour(),
                        registratio.getMinute(),
                        recurrenceSummary
                )
        );

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onOpen(registratio);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onLongPress(registratio);
                return true;
            }
            return false;
        });
    }

    private String buildRecurrenceSummary(Context context, Registratio registratio) {
        String monthlyPattern = registratio.getMonthlyPattern();
        if (!TextUtils.isEmpty(monthlyPattern)) {
            String formatted = formatPattern(monthlyPattern);
            if (formatted.isEmpty()) {
                return context.getString(R.string.main_pattern_unknown);
            }
            return context.getString(R.string.main_pattern_label, formatted);
        }

        String[] options = context.getResources().getStringArray(R.array.repeat_weeks_options);
        int repeatIndex = registratio.getRepeatEveryWeeks();
        if (repeatIndex >= 0 && repeatIndex < options.length) {
            return options[repeatIndex];
        }

        return context.getString(R.string.main_repeat_none);
    }

    private String formatPattern(String rawPattern) {
        if (TextUtils.isEmpty(rawPattern)) {
            return "";
        }

        String normalized = rawPattern.trim().replace('_', ' ').toLowerCase(Locale.ROOT);
        if (normalized.isEmpty()) {
            return "";
        }

        return Character.toUpperCase(normalized.charAt(0)) + normalized.substring(1);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        TextView subtitle;

        ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.textTitle);
            subtitle = itemView.findViewById(R.id.textSubtitle);
        }
    }

    public void setData(List<Registratio> newData) {
        this.data = newData;
        notifyDataSetChanged();
    }
}