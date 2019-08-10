package com.hammollc.hammonasset;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

public class HourOptionAdapter extends BaseAdapter {

    Context context;
    ArrayList<HourOption> items;
    private static LayoutInflater inflater = null;

    public HourOptionAdapter(Context context, ArrayList<HourOption> items) {
        this.context = context;
        this.items = items;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        if (vi == null)
            vi = inflater.inflate(R.layout.hour_option_item, null);

        final View view = vi;
        final HourOption item = items.get(position);

        TextView tv = view.findViewById(R.id.hourOptionLabel);
        tv.setText(item.getLabel());
        return vi;
    }

    private void removeBlock(int b) {
        ReportHours reportHours = (ReportHours) context;
        reportHours.remBlock(b);
    }

    private void updateHours(int b, int i) {
        ReportHours reportHours = (ReportHours) context;
        int min = i*15;
        reportHours.updateHourAmount(b, min);
    }
}
