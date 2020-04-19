package com.hammollc.hammonasset;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

public class PayItemAdapter extends BaseAdapter {

    boolean review;
    Context context;
    ArrayList<PayItem> items;
    private final String[] valueList;

    private static LayoutInflater inflater = null;

    public PayItemAdapter(Context context, ArrayList<PayItem> items, boolean review) {
        this.context = context;
        this.review = review;
        this.items = items;

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        valueList = new String[items.size()];

        for(int i=0; i<items.size(); i++) {
            valueList[i] = "";
        }
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View vi = convertView;
        if (vi == null)
            vi = inflater.inflate(R.layout.pay_item, null);

        final View view = vi;
        final PayItem item = items.get(position);

        TextView code = vi.findViewById(R.id.payItemCodeText);
        code.setText(item.getCode());

        TextView name = vi.findViewById(R.id.payItemNameText);
        name.setText(item.getName());

        final EditText inpt = vi.findViewById(R.id.payItemInput);
        final Context fContext = context;
        items.get(position).setEditText(inpt);

        if(review) {
            String text = item.getSimpleValue();
            inpt.setActivated(false);
            inpt.setText(text);

            if(item.getName().length() > 35) {
                String n = item.getName().substring(0, 32);
                n += "...";
                name.setText(n);
            }
        }
        else {
            inpt.setHint(item.getUnit());

//            inpt.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    AlertDialog.Builder builder = new AlertDialog.Builder(fContext);
//                    builder.setTitle("Unable to Process");
//                    builder.setMessage("Please click anywhere on this item except this box to enter the value");
//
//                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            dialog.dismiss();
//                        }
//                    });
//
//                    builder.create().show();
//                }
//            });

            inpt.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    items.get(position).setValue(s.toString());
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) { }

                @Override
                public void afterTextChanged(Editable s) { }
            });
        }

        if(!item.getValue().equals(""))
            inpt.setText(item.getValue());
        else
            inpt.setText("");

        return vi;
    }

    @Override
    public boolean isEnabled(int position) {
        if(review)
            return false;

        return super.isEnabled(position);
    }
}
