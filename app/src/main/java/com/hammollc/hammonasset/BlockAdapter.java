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

public class BlockAdapter extends BaseAdapter {

    boolean review;
    Context context;
    ArrayList<Block> items;
    private static LayoutInflater inflater = null;

    public BlockAdapter(Context context, ArrayList<Block> items, boolean review) {
        this.review = review;
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
            vi = inflater.inflate(R.layout.block_item, null);

        final View view = vi;
        final Block item = items.get(position);

        String type = item.getType();
        if(type.length() > 30) {
            type = type.substring(0, 27)+"...";
        }

        TextView title = vi.findViewById(R.id.blockType);
        title.setText(type);

        ImageButton closeBtn = vi.findViewById(R.id.closeBtn);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeBlock(position);
            }
        });

        Spinner selectHours = vi.findViewById(R.id.blockHours);
        int selection = item.getHours()/15;
        final String[] hItems = new String[]{"0", ".25", ".5", ".75", "1", "1.25", "1.5", "1.75",
                "2", "2.25", "2.5", "2.75","3", "3.25", "3.5", "3.75","4", "4.25", "4.5", "4.75",
                "5", "5.25", "5.5", "5.75","6", "6.25", "6.5", "6.75","7", "7.25", "7.5", "7.75",
                "8", "8.25", "8.5", "8.75", "9", "9.25", "9.5", "9.75", "10", "10.25", "10.5",
                "10.75", "11", "11.25", "11.5", "11.75", "12" };

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                context, android.R.layout.simple_spinner_dropdown_item, hItems);
        selectHours.setAdapter(adapter);
        selectHours.setSelection(selection, false);

        if(review) {
            closeBtn.setVisibility(View.INVISIBLE);
            closeBtn.setClickable(false);
            selectHours.setEnabled(false);
            selectHours.setActivated(false);
            selectHours.setClickable(false);
        }
        else {
            closeBtn.setVisibility(View.VISIBLE);
            closeBtn.setClickable(true);
            selectHours.setActivated(true);
            selectHours.setEnabled(true);
            selectHours.setClickable(true);

            selectHours.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                    updateHours(position, pos);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) { }
            });
        }

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
