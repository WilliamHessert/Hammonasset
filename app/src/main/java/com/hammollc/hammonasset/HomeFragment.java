package com.hammollc.hammonasset;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private ArrayList<Item> list;
    private ItemAdapter ad;
    private boolean noNotices, foreman, supervisor;

    public static HomeFragment newInstance(boolean foreman) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putBoolean("foreman", foreman);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.home, container, false);
        Log.i("AHHH", "Creating View");
        list = new ArrayList<>();
        ListView listView = v.findViewById(R.id.listView);
        ad = new ItemAdapter(getContext(), list);

        try {
            Bundle args = getArguments();
            foreman = args.getBoolean("foreman", false);
        } catch (Exception e) {
            foreman = false;
            e.printStackTrace();
        }

        noNotices = true;
        listView.setAdapter(ad);
        populateList();

        setItemListener(listView);
        return v;
    }

    private void populateList() {
        try {
            final int infResource = getResources().getIdentifier(
                    "@drawable/red_info",
                    "drawable",
                    getActivity().getPackageName());

            int defResource = getResources().getIdentifier(
                    "@drawable/done",
                    "drawable",
                    getActivity().getPackageName());

            HomeFragment.this.list.add(new Item(
                    "No Notices",
                    "No items need your attention",
                    "none",
                    defResource));
            HomeFragment.this.ad.notifyDataSetChanged();

            String uid = FirebaseAuth.getInstance().getUid();
            DatabaseReference ref =
                    FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

            ref.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    if (dataSnapshot.exists() && dataSnapshot.getKey().equals("eContact")) {
                        String eml = dataSnapshot.child("email").getValue().toString();
                        String nme = dataSnapshot.child("name").getValue().toString();
                        String pPh = dataSnapshot.child("pPhone").getValue().toString();
                        String rel = dataSnapshot.child("relationship").getValue().toString();
                        String sPh = dataSnapshot.child("sPhone").getValue().toString();

                        if (rel.equals("") || nme.equals("") ||
                                pPh.equals("") || (eml.equals("") && sPh.equals(""))) {

                            HomeFragment.this.list.add(new Item(
                                    "Missing Info",
                                    "Need to update\nEmergency Contact",
                                    "info",
                                    infResource));
                            removeDefaultItem();
                        }
                    } else if (dataSnapshot.exists() && dataSnapshot.getKey().equals("info")) {
                        String add = dataSnapshot.child("address").getValue().toString();
                        String cty = dataSnapshot.child("city").getValue().toString();
                        String pho = dataSnapshot.child("phone").getValue().toString();
                        String ste = dataSnapshot.child("state").getValue().toString();
                        String zip = dataSnapshot.child("zip").getValue().toString();

                        if (add.equals("") ||
                                cty.equals("") || pho.equals("") || ste.equals("") || zip.equals("")) {

                            HomeFragment.this.list.add(new Item(
                                    "Missing Info",
                                    "Need to update\nPersonal Information",
                                    "info",
                                    infResource));

                            removeDefaultItem();
                        }
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    reloadList();
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });

            ref.child("hours").addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    reloadList();
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            if (foreman) {
                ref.child("crews").addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        String bDate = dataSnapshot.getKey();
                        String[] allDatesForWeek = allDates(bDate);
                        ArrayList<String> datesOfAlert = new ArrayList<>();

                        for (int i = 0; i < allDatesForWeek.length; i++) {
                            String d = allDatesForWeek[i];
                            String v1 = dataSnapshot
                                    .child(d).child("day").child("report").getValue(String.class);
                            String v2 = dataSnapshot
                                    .child(d).child("night").child("report").getValue(String.class);
                            Log.i("AHHH", bDate + " " + d + " " + v1 + " " + v2);
                            try {
                                if (v1.equals("false"))
                                    datesOfAlert.add(d + " Day");
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            }

                            try {
                                if (v2.equals("false"))
                                    datesOfAlert.add(d + " Night");
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            }
                        }
                        Log.i("AHHH", "" + datesOfAlert.size());
                        if (datesOfAlert.size() != 0) {
                            for (int j = 0; j < datesOfAlert.size(); j++) {
                                String d = datesOfAlert.get(j);
                                HomeFragment.this.list.add(new Item(
                                        "Daily Report",
                                        "Need Daily Report\n" + d,
                                        "rep",
                                        infResource));
                                removeDefaultItem();
                            }
                        }
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                        reloadList();
                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        } catch (Exception e) {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .detach(this)
                    .attach(this)
                    .commit();
        }
    }

    private void removeDefaultItem() {
        if(noNotices) {
            HomeFragment.this.noNotices = false;
            HomeFragment.this.list.remove(0);
        }

        HomeFragment.this.ad.notifyDataSetChanged();
    }

    private void reloadList() {
        list.clear();
        noNotices = true;
        populateList();
    }

    private void setItemListener(ListView listView) {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String code = list.get(position).code;

                BottomNavigationView bottomNavigationView;
                bottomNavigationView = getActivity().findViewById(R.id.navigation);

                if(code.equals("info")) {
                    bottomNavigationView.setSelectedItemId(R.id.navigation_info);
                }
                else if(code.equals("con")) {
                    Intent i = new Intent(getContext(), EmergencyContact.class);
                    getContext().startActivity(i);
                }
                else if(code.equals("rep")) {
                    Intent i = new Intent(getContext(), DailyReportActivity.class);
                    i.putExtra("date", getReportDateString(list.get(position)));
                    i.putExtra("time", getReportTimeString(list.get(position)));
                    getContext().startActivity(i);
                }
                else if(code.equals("hou")){
                    Intent i = new Intent(getContext(), ReportHours.class);
                    getContext().startActivity(i);
                }
                else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("All Set");
                    builder.setMessage("There are no items for you to complete. Either explore " +
                            "the app or go and enjoy your day!");

                    builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

                    builder.create().show();
                }
            }
        });
    }

    private String[] allDates(String mDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy", Locale.US);
        String[] output = new String[7];
        Date convertedDate;

        try {
            convertedDate = dateFormat.parse(mDate);
        } catch (ParseException e) {
            e.printStackTrace();
            output = new String[1];
            output[0] = mDate;

            return output;
        }

        output[0] = mDate;
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(convertedDate);

        for(int i=1; i<output.length; i++) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            Date date = calendar.getTime();
            output[i] = dateFormat.format(date);
        }
        Log.i("AHHH", output[0]+" "+output[1]+" "+output[2]+" "+output[3]+" "+output[4]+" "+output[5]+" "+output[6]);
        return output;
    }

    private String getReportDateString(Item i) {
        String sub = i.getSub();
        String[] newLineSplit = sub.split("\n");
        sub = newLineSplit[newLineSplit.length-1];

        String[] spaceSplit = sub.split(" ");
        return spaceSplit[0];
    }

    private String getReportTimeString(Item i) {
        String sub = i.getSub();
        String[] newLineSplit = sub.split("\n");
        sub = newLineSplit[newLineSplit.length-1];

        String[] spaceSplit = sub.split(" ");
        return spaceSplit[1].toLowerCase();
    }
}
