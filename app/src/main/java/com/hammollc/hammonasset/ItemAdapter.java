package com.hammollc.hammonasset;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class ItemAdapter extends BaseAdapter {

    Context context;
    ArrayList<Item> items;
    private static LayoutInflater inflater = null;

    public ItemAdapter(Context context, ArrayList<Item> items) {
        this.context = context;
        this.items = items;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
    public View getView(int position, View convertView, ViewGroup parent) {

        View vi = convertView;
        if (vi == null)
            vi = inflater.inflate(R.layout.item, null);

        Item item = items.get(position);

        TextView title = (TextView) vi.findViewById(R.id.iTitle);
        String titleText = item.getTitle();

        if(titleText.length() < 20) {
            title.setText(titleText);
        }
        else {
            titleText = titleText.substring(0, 17)+"...";
            title.setText(titleText);
        }

        TextView subTitle = (TextView) vi.findViewById(R.id.iDesc);
        String sub = item.getSub();
        subTitle.setText(sub);

        ImageView image = (ImageView) vi.findViewById(R.id.iPic);
        int pic = item.getPic();
        image.setImageResource(pic);

        if(pic == context.getResources().getIdentifier(
                "@drawable/done",
                "drawable",
                context.getPackageName())) {
            image.setColorFilter(context.getResources().getColor(R.color.colorPrimary));
        }

        return vi;
    }
}
