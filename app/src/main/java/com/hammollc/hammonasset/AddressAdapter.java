package com.hammollc.hammonasset;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AddressAdapter extends BaseAdapter {

    boolean review;
    Context context;
    ArrayList<AddressBlock> items;

    private static LayoutInflater inflater = null;

    public AddressAdapter(Context context, ArrayList<AddressBlock> items, boolean review) {
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
            vi = inflater.inflate(R.layout.block_address, null);

        final View view = vi;
        final AddressBlock item = items.get(position);

        final EditText ste = vi.findViewById(R.id.blockAddressState);
        final List<String> towns = Arrays.asList(context.getResources().getStringArray(R.array.statesFull));
        ste.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(context);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setCancelable(false);
                dialog.setContentView(R.layout.dialog_select_view);

                ListView tList = dialog.findViewById(R.id.selectList);
                ArrayAdapter<String> tAdapter = new ArrayAdapter<>(
                        context, android.R.layout.simple_list_item_1, towns);
                tList.setAdapter(tAdapter);

                tList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String town = towns.get(position);
                        ste.setText(town);
                        dialog.dismiss();
                    }
                });

                Button closeBtn = dialog.findViewById(R.id.closeDialog);
                closeBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                dialog.show();
            }
        });

        ImageButton closeBtn = vi.findViewById(R.id.blockAddressCloseBtn);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeBlock(position);
            }
        });

        return vi;
    }

    private void removeBlock(int b) {
        DriverApplicationActivity driverApplicationActivity = (DriverApplicationActivity) context;
        driverApplicationActivity.remAddressBlock(b);
    }
}
