package nc.instrumentum.recordatormunerum.ui;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import nc.instrumentum.recordatormunerum.R;
import nc.instrumentum.recordatormunerum.model.Registratio;

public class RegistratioAdapter
        extends RecyclerView.Adapter<RegistratioAdapter.ViewHolder> {

    private List<Registratio> data;

    public RegistratioAdapter(List<Registratio> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_registratio, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder, int position) {

        Registratio r = data.get(position);
        holder.title.setText(r.getTitle());
        holder.title.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), EditRegistratioActivity.class);
            intent.putExtra("id", r.getId());
            v.getContext().startActivity(intent);
        });
        holder.check.setChecked(!r.getActive());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        CheckBox check;
        TextView title;
        ImageView open;

        ViewHolder(View itemView) {
            super(itemView);
            check = itemView.findViewById(R.id.checkCompleted);
            title = itemView.findViewById(R.id.textTitle);
            open = itemView.findViewById(R.id.iconOpen);
        }
    }

    public void setData(List<Registratio> newData) {
        this.data = newData;
        notifyDataSetChanged();
    }

}
