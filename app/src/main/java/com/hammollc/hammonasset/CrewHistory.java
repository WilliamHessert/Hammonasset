package com.hammollc.hammonasset;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CrewHistory extends AppCompatActivity {

    boolean detailView;
    boolean singleView;
    ArrayList<Crew> crews;
    String uid, date, time;

    FirebaseDatabase db;
    DatabaseReference ref;

    ProgressBar pBar;
    ListView listView;
    RelativeLayout crewView;
    RelativeLayout membView;

    Crew crew;
    ArrayAdapter<String> ad;
    ArrayList<String> crewNames;

    CrewMemberAdapter cad;
    ArrayList<CrewMember> crewMembers;
    Context context = CrewHistory.this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crew_history);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Your Work Orders");

        detailView = false;
        singleView = false;
        getExtras();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if(detailView)
                    showFullView();
                else if(singleView)
                    returnToDetailView();
                else
                    CrewHistory.this.finish();

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void getExtras() {
        Bundle extras = getIntent().getExtras();
        uid = extras.getString("uid", "");

        if(uid.equals("")) {
            Toast.makeText(context,
                    "Error, please log out and log in again", Toast.LENGTH_LONG).show();
        }
        else {
            date = extras.getString("date", "");
            time = extras.getString("time", "");

            loadViews();
        }
    }

    private void loadViews() {
        pBar = findViewById(R.id.crewHistoryProgress);
        membView = findViewById(R.id.singleView);
        listView = findViewById(R.id.crewList);
        crewView = findViewById(R.id.crewView);

        listView.setVisibility(View.VISIBLE);
        crewView.setVisibility(View.GONE);
        membView.setVisibility(View.GONE);
        pBar.setVisibility(View.GONE);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openCrewView(position);
            }
        });

        crewNames = new ArrayList<>();
        ad = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, crewNames);
        listView.setAdapter(ad);

        loadData();
    }

    private void loadData() {
        crews = new ArrayList<>();
        db = FirebaseDatabase.getInstance();
        ref = db.getReference("Users").child(uid).child("crews");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
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

                            String poNumber = "";
                            boolean report = false;
                            boolean approve = false;

                            for(Map.Entry<String, String> nnnEntry: nnMap.entrySet()) {
                                String key = nnnEntry.getKey();

                                if(key.equals("poNumber"))
                                    poNumber = nnnEntry.getValue();

                                if(key.equals("report")) {
                                    if(nnnEntry.getValue().equals("true"))
                                        report = true;
                                }

                                if(key.equals("approveAlert")) {
                                    if(nnnEntry.getValue().equals("true"))
                                        approve = true;
                                }
                            }

                            getCrewData(week, date, time, poNumber, report, approve);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }

    private void getCrewData(final String w, final String d,
                             final String t, final String p, final boolean r, final boolean a) {
        ref.child(w).child(d).child(t).child("crew").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot data) {
                ArrayList<String[]> cData = new ArrayList<>();
                HashMap<String, HashMap<String, String>> entry = (HashMap<String, HashMap<String, String>>)(data.getValue());

                for(Map.Entry<String, HashMap<String, String>> nEntry: entry.entrySet()) {
                    String name = "";
                    String isComplete = "false";
                    String id = nEntry.getKey();
                    HashMap<String, String> nMap = nEntry.getValue();

                    for(Map.Entry<String, String> nnEntry: nMap.entrySet()) {
                        String key = nnEntry.getKey();

                        if(key.equals("name"))
                            name = nnEntry.getValue();
                        else
                            isComplete = nnEntry.getValue();
                    }

                    String[] crewman = { id, name, isComplete };
                    cData.add(crewman);
                }

                Crew crew = new Crew(w, d, t, p);
                crew.setReport(r);
                crew.setApprove(a);
                crew.setCrewData(cData);
                crews.add(crew);

                crewNames.add(d+" "+t.substring(0,1).toUpperCase()+t.substring(1)+": "+p);
                ad.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }

    private void openCrewView(int i) {
        crew = crews.get(i);
        detailView = true;

        TextView dText = findViewById(R.id.dateText);
        String d = crew.getDate()
                +" " +crew.getTime().substring(0, 1).toUpperCase()+crew.getTime().substring(1);
        dText.setText(d);

        pBar.setVisibility(View.VISIBLE);
        listView.setVisibility(View.GONE);

        Button dBtn = findViewById(R.id.dailyReportBtn);
        Button pBtn = findViewById(R.id.payItemsBtn);

        dBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkForDailyReport(crew, true);
            }
        });

        pBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkForDailyReport(crew, false);
            }
        });

        crewMembers = new ArrayList<>();
        ListView crewList = findViewById(R.id.crewmanList);
        final ArrayList<String[]> cData = crew.getCrewData();

        crewList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openSingleView(position);
            }
        });

        cad = new CrewMemberAdapter(context, crewMembers);
        crewList.setAdapter(cad);
        getCrewMember(cData, 0, crew);
    }

    private void getCrewMember(final ArrayList<String[]> cData, final int i, final Crew crew) {
        if(i == cData.size()) {
            showCrewView(crew);
        }
        else {
            String[] datum = cData.get(i);

            final String id = datum[0];
            final String name = datum[1];
            final String isCompleted = datum[2];

            final String week = crew.getWeek();
            final String date = crew.getDate();
            final String time = crew.getTime();

            final DatabaseReference cRef = db.getReference("Users").child(id);
            cRef.child("info").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    final String phone = dataSnapshot.child("phone").getValue(String.class);
                    final String email = dataSnapshot.child("email").getValue(String.class);
                    final DatabaseReference nRef = cRef.child("hours").child(week).child(date).child(time);

                    nRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String sTime = dataSnapshot.child("sTime").getValue(String.class);
                            String eTime = dataSnapshot.child("eTime").getValue(String.class);
                            String tHours = dataSnapshot.child("tHours").getValue(Integer.class) + "";
                            String app = dataSnapshot.child("approved").getValue(String.class);

                            ArrayList<Block> blocks = new ArrayList<>();
                            int bCount = (int) (dataSnapshot.child("blocks").getChildrenCount());

                            for(int j=0; j<bCount; j++) {
                                String type = dataSnapshot.child("blocks")
                                        .child(j+"").child("type").getValue(String.class);
                                int hours = dataSnapshot.child("blocks")
                                        .child(j+"").child("hours").getValue(Integer.class);
                                blocks.add(new Block(type, hours));
                            }

                            boolean reported;
                            boolean approved;

                            try {
                                if(sTime.equals(null) || sTime.equals("")) {
                                    reported = false;
                                    approved = false;
                                }
                                else {
                                    reported = true;
                                    approved = Boolean.parseBoolean(isCompleted);
                                }
                            } catch (Exception e) {
                                reported = false;
                                approved = false;
                            }

                            addCrewMember(reported, approved, id,
                                    name, phone, email, sTime, eTime, tHours, blocks, cData, i, crew);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) { }
                    });
                }

                @Override
                public void onCancelled(DatabaseError databaseError) { }
            });
        }
    }

    private void addCrewMember(boolean r, boolean a, String id, String n, String p, String e, String st,
                               String et, String t, ArrayList<Block> b, ArrayList<String[]> d, int i, Crew cr) {

        CrewMember c = new CrewMember(n, b, r, a);
        c.setId(id);
        c.setPhone(p);
        c.setEmail(e);
        c.setsTime(st);
        c.seteTime(et);
        c.settHours(t);

        crewMembers.add(c);
        cad.notifyDataSetChanged();
        getCrewMember(d, i+1, cr);
    }

    private void showCrewView(Crew crew) {
        pBar.setVisibility(View.GONE);
        crewView.setVisibility(View.VISIBLE);
        getSupportActionBar().setTitle(crew.getPoNumber());
    }

    private void showFullView() {
        detailView = false;
        crewView.setVisibility(View.GONE);
        listView.setVisibility(View.VISIBLE);
        getSupportActionBar().setTitle("Your Work Orders");
    }

    private void checkForDailyReport(final Crew crew, final boolean openReport) {
        DatabaseReference rRef = db.getReference("Users").child(uid).child("crews")
                .child(crew.getWeek()).child(crew.getDate()).child(crew.getTime()).child("report");
        rRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean report;

                try {
                    report = Boolean.parseBoolean(dataSnapshot.getValue(String.class));
                } catch (Exception e) {
                    report = false;
                }

                if(openReport) {
                    if (report) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle("Report Already Submitted");
                        builder.setMessage("You already submitted a daily report for this Work Order.");

                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                        builder.create().show();
                    } else
                        openDailyReport(crew);
                }
                else {
                    if(report)
                        downloadPayItems(crew);
                    else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle("No Report Submitted");
                        builder.setMessage("You have not yet " +
                                "submitted a daily report. Please submit a report.");

                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                        builder.create().show();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }

    private void openDailyReport(Crew crew) {
        Intent i = new Intent(CrewHistory.this, DailyReportActivity.class);
        i.putExtra("pNum", crew.getPoNumber());
        i.putExtra("date", crew.getDate());
        i.putExtra("time", crew.getTime());

        String fName = getIntent().getStringExtra("fName");
        String lName = getIntent().getStringExtra("lName");
        i.putExtra("fName", fName);
        i.putExtra("lName", lName);

        CrewHistory.this.startActivity(i);
    }

    private void downloadPayItems(Crew crew) {
        final ArrayList<PayItem> payItems = new ArrayList<>();
        DatabaseReference pRef = db.getReference("Contracts").child("16PSX0176").child("poNums");
        pRef.child(crew.getPoNumber()).child("reports").child(crew.getDate()).child(crew.getTime())
                .child(uid).child("payItems").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashMap<String, HashMap<String, String>> pItems =
                        (HashMap<String, HashMap<String, String>>)(dataSnapshot.getValue());

                for(Map.Entry<String, HashMap<String, String>> data: pItems.entrySet()) {
                    String num = data.getKey();
                    String nam = dataSnapshot.child(num).child("name").getValue(String.class);
                    String unt = dataSnapshot.child(num).child("unit").getValue(String.class);
                    String val = dataSnapshot.child(num).child("value").getValue(String.class);

                    PayItem payItem = new PayItem(num, nam, unt);
                    payItem.setValue(val);
                    payItems.add(payItem);
                }

                openPayItemDialog(payItems);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }

    private void openPayItemDialog(ArrayList<PayItem> pItems) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_select_view);

        for(int i=0; i<pItems.size(); i++) {
            pItems.get(i).setValue(pItems.get(i).getSimpleValue());
        }

        ListView pList = dialog.findViewById(R.id.selectList);
        PayItemAdapter pAdapter = new PayItemAdapter(context, pItems, true);
        pList.setAdapter(pAdapter);

        Button closeBtn = dialog.findViewById(R.id.closeDialog);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void openSingleView(int i) {
        detailView = false;
        singleView = true;

        pBar.setVisibility(View.VISIBLE);
        crewView.setVisibility(View.GONE);
        final CrewMember crewMember = crewMembers.get(i);

        TextView uText = findViewById(R.id.unreportedText);
        TextView pText = findViewById(R.id.crewMemberPhone);
        TextView eText = findViewById(R.id.crewMemberEmail);

        String phone = crewMember.getPhone();
        String email = crewMember.getEmail();

        if(phone.equals(""))
            pText.setVisibility(View.GONE);
        else {
            pText.setVisibility(View.VISIBLE);
            phone = "Phone: " + crewMember.getPhone();
            pText.setText(phone);

            pText.setPaintFlags(pText.getPaintFlags()| Paint.UNDERLINE_TEXT_FLAG);
            pText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        }

        if(email.equals(""))
            eText.setVisibility(View.GONE);
        else {
            eText.setVisibility(View.VISIBLE);
            email = "Email: "+crewMember.getEmail();
            eText.setText(email);

            eText.setPaintFlags(eText.getPaintFlags()| Paint.UNDERLINE_TEXT_FLAG);
            eText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        }

        TextView stText = findViewById(R.id.crewMemberStart);
        TextView etText = findViewById(R.id.crewMemberEnd);
        TextView ttText = findViewById(R.id.crewMemberTotal);

        Button rBtn = findViewById(R.id.rejectButton);
        Button aBtn = findViewById(R.id.approveButton);
        ListView bList = findViewById(R.id.crewMemberBlocks);

        if(crewMember.isReported()) {
            stText.setVisibility(View.VISIBLE);
            etText.setVisibility(View.VISIBLE);
            ttText.setVisibility(View.VISIBLE);

            rBtn.setVisibility(View.VISIBLE);
            aBtn.setVisibility(View.VISIBLE);
            bList.setVisibility(View.VISIBLE);

            uText.setVisibility(View.GONE);

            String st = "Start Time: " + crewMember.getsTime();
            String et = "End Time: " + crewMember.geteTime();
            String tt = "Total Hours: " + getConvertedTime(Integer.parseInt(crewMember.gettHours()));

            stText.setText(st);
            etText.setText(et);
            ttText.setText(tt);

            if(crewMember.isApproved()) {
                View.OnClickListener ocl = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle("Cannot Perform Action");
                        builder.setMessage("You cannot approve or reject this crewmember's hours" +
                                " because you already have.");

                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                        builder.create().show();
                    }
                };

                rBtn.setOnClickListener(ocl);
                aBtn.setOnClickListener(ocl);
            }
            else {
                rBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        rejectHours(crewMember);
                    }
                });

                aBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        approveHours(crewMember);
                    }
                });
            }

            ArrayList<Block> blocks = crewMember.getBlocks();
            BlockAdapter bad = new BlockAdapter(context, blocks, true);
            bList.setAdapter(bad);
        }
        else {
            stText.setVisibility(View.GONE);
            etText.setVisibility(View.GONE);
            ttText.setVisibility(View.GONE);

            rBtn.setVisibility(View.GONE);
            aBtn.setVisibility(View.GONE);
            bList.setVisibility(View.GONE);

            uText.setVisibility(View.VISIBLE);
        }

        pBar.setVisibility(View.GONE);
        membView.setVisibility(View.VISIBLE);
    }

    private void rejectHours(final CrewMember crewMember) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Reject Hours");
        builder.setMessage("Are you sure you want to reject this crew-member's hours?");

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                getRejectionReason(crewMember);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();
    }

    private void getRejectionReason(final CrewMember crewMember) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Reason");
        builder.setMessage("Please enter why you are rejecting this crew-member's hours:");

        final EditText input = new EditText(context);
        input.setInputType(1);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                rejectUpdates(crew, crewMember, input.getText().toString());
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                input.setInputType(0);
                dialog.dismiss();
            }
        });

        builder.create().show();
    }

    private void rejectUpdates(final Crew cr, final CrewMember cm, final String r) {
        pBar.setVisibility(View.VISIBLE);
        membView.setVisibility(View.GONE);

        String id = cm.getId();
        final DatabaseReference ref = db.getReference("Users").child(id).child("rejections");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int i = (int)(dataSnapshot.getChildrenCount());
                final DatabaseReference nRef = ref.child(i+"");
                nRef.child("week").setValue(cr.getWeek()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        nRef.child("date").setValue(cr.getDate()).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                nRef.child("time").setValue(cr.getTime()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        nRef.child("reason").setValue(r).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                completeUpdate(cr, cm);
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }

    private void approveHours(final CrewMember crewMember) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Approve Hours");
        builder.setMessage("Are you sure you want to approve this crew-member's hours?");

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                completeUpdate(crew, crewMember);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();
    }

    private void completeUpdate(final Crew crew, final CrewMember crewMember) {
        pBar.setVisibility(View.VISIBLE);
        membView.setVisibility(View.GONE);

        DatabaseReference ref = db.getReference("Users").child(uid).child("crews");
        final DatabaseReference nRef = ref.child(crew.getWeek()).child(crew.getDate()).child(crew.getTime());
        nRef.child("crew").child(crewMember.getId()).child("approved").setValue("true").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                checkApproveAlert(nRef, crewMember);
            }
        });
    }

    private void checkApproveAlert(DatabaseReference ref, CrewMember crewMember1) {
        boolean approveAlert = false;

        for(int i=0; i<crewMembers.size(); i++) {
            CrewMember crewMember2 = crewMembers.get(i);

            if(!crewMember1.equals(crewMember2)) {
                if(crewMember2.isReported() && !crewMember2.isApproved())
                    approveAlert = true;
            }
        }

        if(!approveAlert) {
            ref.child("approveAlert").setValue("false").addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Toast.makeText(context, "Success!", Toast.LENGTH_LONG).show();
                    CrewHistory.this.finish();
                }
            });
        }
        else {
            Toast.makeText(context, "Success!", Toast.LENGTH_LONG).show();
            CrewHistory.this.finish();
        }
    }

    private void returnToDetailView() {
        singleView = false;
        detailView = true;

        pBar.setVisibility(View.GONE);
        membView.setVisibility(View.GONE);
        crewView.setVisibility(View.VISIBLE);
    }

    private String getConvertedTime(int minutes) {
        int hours = minutes/60;
        minutes = minutes%60;

        String h = hours+".";

        if(minutes == 15)
            return h+"25";
        if(minutes == 30)
            return h+"50";
        if(minutes == 45)
            return  h+"75";

        return h+"00";
    }
}
