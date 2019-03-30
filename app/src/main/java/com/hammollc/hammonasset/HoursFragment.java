package com.hammollc.hammonasset;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;

/**
 * Created by williamhessert on 9/5/18.
 */

public class HoursFragment extends Fragment {

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.hours, container, false);

        setButtons(v);

        return v;
    }

    private void setButtons(View v) {
        final String uid = FirebaseAuth.getInstance().getUid();

        Button reportBtn = v.findViewById(R.id.reportHoursBtn);
        reportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(), ReportHours.class);
                i.putExtra("uid", uid);
                getContext().startActivity(i);
            }
        });

        Button historyBtn = v.findViewById(R.id.hoursHistoryBtn);
        historyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
}
