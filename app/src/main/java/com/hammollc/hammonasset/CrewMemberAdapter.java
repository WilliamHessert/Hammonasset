package com.hammollc.hammonasset;


import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;

public class CrewMemberAdapter extends BaseAdapter {

    Context context;
    ArrayList<CrewMember> crewMembers;
    private static LayoutInflater inflater = null;

    public CrewMemberAdapter(Context context, ArrayList<CrewMember> crewMembers) {
        this.context = context;
        this.crewMembers = crewMembers;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return crewMembers.size();
    }

    @Override
    public Object getItem(int position) {
        return crewMembers.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View vi = convertView;

        if (vi == null)
            vi = inflater.inflate(R.layout.crewmember, null);

        final CrewMember crewMember = crewMembers.get(position);

        TextView name = vi.findViewById(R.id.crewMemberName);
        name.setText(crewMember.getName());

        TextView rText = vi.findViewById(R.id.reportedText);
        String r = "Reported";

        if(crewMember.isReported())
            rText.setTextColor(Color.GREEN);
        else {
            rText.setTextColor(Color.RED);
            r = "Not Reported";
        }

        rText.setText(r);

        TextView aText = vi.findViewById(R.id.approvedText);
        String a = "Approved";

        if(crewMember.isApproved())
            aText.setTextColor(Color.GREEN);
        else {
            aText.setTextColor(Color.RED);
            a = "Not Approved";
        }

        aText.setText(a);
        return vi;
    }
}
