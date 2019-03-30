package com.hammollc.hammonasset;

import android.app.Dialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HoursHistoryActivity extends AppCompatActivity {

    private String uid;
    boolean detailView;

    ArrayList<String> times;
    HashMap<String, ArrayList<String>> weekToTimes;

    ArrayList<String> weeks;
    ArrayList<String[]> dateTimes;
    ArrayList<HourDataBlock> blocks;

    ListView list;
    ProgressBar pBar;
    RelativeLayout detailHolder;

    ArrayAdapter<String> wAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uid = getIntent().getStringExtra("uid");
        setContentView(R.layout.activity_hours_history);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Your Hours History");

        detailView = false;
        setViews();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if(detailView)
                    showFullView();
                else
                    HoursHistoryActivity.this.finish();

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setViews() {
        times = new ArrayList<>();
        weekToTimes = new HashMap<>();

        weeks = new ArrayList<>();
        blocks = new ArrayList<>();
        dateTimes = new ArrayList<>();

        list = findViewById(R.id.historyList);
        pBar = findViewById(R.id.historyProgress);
        detailHolder = findViewById(R.id.historyDetailHolder);

        wAdapter = new ArrayAdapter<String>(
                HoursHistoryActivity.this, android.R.layout.simple_list_item_1, weeks);
        list.setAdapter(wAdapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String week = weeks.get(position);
                times = weekToTimes.get(week);

                final Dialog dialog = new Dialog(HoursHistoryActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setCancelable(false);
                dialog.setContentView(R.layout.dialog_select_view);

                ListView tList = dialog.findViewById(R.id.selectList);
                ArrayAdapter<String> tAdapter = new ArrayAdapter<String>(
                        HoursHistoryActivity.this, android.R.layout.simple_list_item_1, times);
                tList.setAdapter(tAdapter);

                tList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String time = times.get(position);
                        showDetailView(week, time);
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

        getData();
    }

    private void getData() {
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(uid).child("hours").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                HashMap<String, HashMap<String, HashMap<String, HashMap<String, String>>>> values = (HashMap<String, HashMap<String, HashMap<String, HashMap<String, String>>>>)(dataSnapshot.getValue());

                for (Map.Entry<String, HashMap<String, HashMap<String, HashMap<String, String>>>> entry : values.entrySet()) {
                    String week = entry.getKey();
                    HashMap<String, HashMap<String, HashMap<String, String>>> map = entry.getValue();

                    for (Map.Entry<String, HashMap<String, HashMap<String, String>>> nEntry: map.entrySet()) {
                        String date = nEntry.getKey();
                        HashMap<String, HashMap<String, String>> nMap = nEntry.getValue();

                        for(Map.Entry<String, HashMap<String, String>> nnEntry: nMap.entrySet()) {
                            String time = nnEntry.getKey();
                            HashMap<String, String> nnMap = nnEntry.getValue();

                            boolean isApproved = false;
                            boolean isReported = false;
                            boolean hasForeman = false;

                            for(Map.Entry<String, String> nnnEntry: nnMap.entrySet()) {
                                String key = nnnEntry.getKey();

                                if(key.equals("foreman"))
                                    hasForeman = true;

                                if(key.equals("sTime"))
                                    isReported = true;

                                if(key.equals("approved")) {
                                    if(nnnEntry.getValue().equals("true"))
                                        isApproved = true;
                                }
                            }

                            String[] dateTime = new String[6];
                            dateTime[0] = week;
                            dateTime[1] = date;
                            dateTime[2] = time;
                            dateTime[3] = hasForeman+"";
                            dateTime[4] = isReported+"";
                            dateTime[5] = isApproved+"";

                            dateTimes.add(dateTime);
                            handleDateTime(dateTime);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void handleDateTime(String[] dateTime) {
        String week = dateTime[0];
        String time = dateTime[1]+" "+dateTime[2];

        boolean newWeek = true;
        for(int i=0; i<weeks.size(); i++) {
            if(weeks.get(i).equals(week))
                newWeek = false;
        }

        if(newWeek) {
            weeks.add(dateTime[0]);
            ArrayList<String> times = new ArrayList<>();
            times.add(time);

            weekToTimes.put(week, times);
        }
        else
            weekToTimes.get(week).add(time);

        wAdapter.notifyDataSetChanged();
    }

    private void showFullView() {
        detailView = false;

        pBar.setVisibility(View.GONE);
        list.setVisibility(View.VISIBLE);
        detailHolder.setVisibility(View.GONE);
    }

    private void showDetailView(String week, String time) {
        detailView = true;
        list.setVisibility(View.GONE);
        pBar.setVisibility(View.VISIBLE);

        String[] split = time.split(" ");

        for(int i=0; i<dateTimes.size(); i++) {
            String[] dateTime = dateTimes.get(i);
            if(dateTime[0].equals(week) && dateTime[1].equals(split[0]) && dateTime[2].equals(split[1])) {
                i = dateTimes.size();
                showDetailedDateTime(dateTime);
            }
        }
    }

    private void showDetailedDateTime(String[] dateTime) {
        String week = dateTime[0];
        String date = dateTime[1];
        String time = dateTime[2];

        TextView title = findViewById(R.id.historyTitle);
        String titleText = date+" "+getCapital(time);
        title.setText(titleText);

        final boolean hasForeman = Boolean.parseBoolean(dateTime[3]);
        final boolean isReported = Boolean.parseBoolean(dateTime[4]);
        boolean isApproved = Boolean.parseBoolean(dateTime[5]);

        CheckBox rBox = findViewById(R.id.isReported);
        CheckBox aBox = findViewById(R.id.isApproved);

        rBox.setChecked(isReported);
        aBox.setChecked(isApproved);

        DatabaseReference pRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);
        final DatabaseReference ref = pRef.child("hours").child(week).child(date).child(time);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot data) {
                if(hasForeman) {
                    String fid = data.child("foreman").getValue(String.class);
                    DatabaseReference iRef = FirebaseDatabase.getInstance().getReference("Users").child(fid);
                    iRef.child("info").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot iData) {
                            String fName = iData.child("fName").getValue(String.class);
                            String lName = iData.child("lName").getValue(String.class);
                            final String f = fName+" "+lName;

                            if(isReported) {
                                String s = data.child("sTime").getValue(String.class);
                                String e = data.child("eTime").getValue(String.class);
                                int t = (int)(data.child("tHours").getValue());

                                ArrayList<String[]> b = new ArrayList<>();
                                long c = data.child("blocks").getChildrenCount();

                                for(int i=0; i<c; i++) {
                                    int h = (int)(data.child("blocks").child(i+"").child("hours").getValue());
                                    String hString = h+"";

                                    String[] vals = new String[2];
                                    vals[0] = data.child("blocks").child(i+"").child("type").getValue(String.class);
                                    vals[1] = hString;

                                    b.add(vals);
                                }

                                setValues(f, s, e, t, b);
                            }
                            else {
                                String s = "";
                                String e = "";
                                int t = 0;
                                ArrayList<String[]> b = new ArrayList<>();

                                setValues(f, s, e, t, b);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
                else {
                    String f = "No Foreman";

                    if(isReported) {
                        String s = data.child("sTime").getValue(String.class);
                        String e = data.child("eTime").getValue(String.class);
                        int t = data.child("tHours").getValue(Integer.class);

                        ArrayList<String[]> b = new ArrayList<>();
                        long c = data.child("blocks").getChildrenCount();

                        for(int i=0; i<c; i++) {
                            int h = data.child("blocks").child(i+"").child("hours").getValue(Integer.class);
                            String hString = h+"";

                            String[] vals = new String[2];
                            vals[0] = data.child("blocks").child(i+"").child("type").getValue(String.class);
                            vals[1] = hString;

                            b.add(vals);
                        }

                        setValues(f, s, e, t, b);
                    }
                    else {
                        String s = "";
                        String e = "";
                        int t = 0;
                        ArrayList<String[]> b = new ArrayList<>();

                        setValues(f, s, e, t, b);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setValues(String f, String s, String e, int t, ArrayList<String[]> b) {
        TextView fText = findViewById(R.id.historyForeman);
        TextView sText = findViewById(R.id.historyStartTime);
        TextView eText = findViewById(R.id.historyEndTime);
        TextView hText = findViewById(R.id.historyHours);

        f = "Foreman: "+f;
        s = "Start Time: "+s;
        e = "End Time: "+e;

        fText.setText(f);
        sText.setText(s);
        eText.setText(e);

        String tString = convertTime(t);
        hText.setText(tString);

        ListView list = findViewById(R.id.historyBlocks);
        ArrayList<String> blocks = new ArrayList<>();

        for(int i=0; i<b.size(); i++) {
            String[] bl = b.get(i);
            String ty = bl[0];
            String ho = convertTime(Integer.parseInt(bl[1]));

            blocks.add(ty+": "+ho);
        }

        ArrayAdapter<String> ad = new ArrayAdapter<String>(
            HoursHistoryActivity.this, android.R.layout.simple_list_item_1, blocks);
        list.setAdapter(ad);

        pBar.setVisibility(View.GONE);
        detailHolder.setVisibility(View.VISIBLE);
    }

    private String getCapital(String s) {
        return s.substring(0, 1).toUpperCase()+s.substring(1);
    }

    private String convertTime(int t) {
        int h = t/60;
        int m = t%60;
        String mString = "";

        if(m == 15)
            mString = ".25";
        else if(m == 30)
            mString = ".50";
        else if(m == 45)
            mString = ".75";
        else
            mString = ".00";

        return h+mString+" Hours";
    }
}
