package nc.instrumentum.recordatormunerum.widget;

import android.content.Context;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;


import java.util.ArrayList;
import java.util.List;

import nc.instrumentum.recordatormunerum.R;
import nc.instrumentum.recordatormunerum.data.repository.RegistratioRepository;
import nc.instrumentum.recordatormunerum.model.Registratio;
import nc.instrumentum.recordatormunerum.ui.EditRegistratioActivity;
import nc.instrumentum.recordatormunerum.ui.MainActivity;
import nc.instrumentum.recordatormunerum.util.Horologium;

public class RegistratioWidgetFactory
        implements RemoteViewsService.RemoteViewsFactory {

    private final Context context;
    private final RegistratioRepository repository;
    private List<Registratio> hoy = new ArrayList<>();

    public RegistratioWidgetFactory(Context context) {
        this.context = context;
        repository = new RegistratioRepository(context);
    }

    @Override
    public void onDataSetChanged() {
        hoy.clear();

        for (Registratio r : repository.getActivas()) {
            if (Horologium.tocaHoy(r)) {
                hoy.add(r);
            }
        }
    }

    @Override
    public RemoteViews getViewAt(int position) {

        Registratio r = hoy.get(position);

        RemoteViews rv = new RemoteViews(
                context.getPackageName(),
                R.layout.widget_item
        );

        rv.setTextViewText(
                R.id.widget_item_title,
                r.getTitle()
        );

        Intent fillInIntent = new Intent();
        fillInIntent.putExtra("id", r.getId());

        rv.setOnClickFillInIntent(
                R.id.widget_item_root,
                fillInIntent
        );

        return rv;
    }


    @Override public int getCount() { return hoy.size(); }
    @Override public void onCreate() {}
    @Override public void onDestroy() {}
    @Override public RemoteViews getLoadingView() { return null; }
    @Override public int getViewTypeCount() { return 1; }
    @Override public long getItemId(int i) { return i; }
    @Override public boolean hasStableIds() { return true; }
}
