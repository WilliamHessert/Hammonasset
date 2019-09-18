package com.hammollc.hammonasset;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.SubMenu;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

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

public class ForemanActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    String mode;
    ListView list;
    RelativeLayout hView;

    ArrayList<Item> home, info;
    ItemAdapter hAdapter, iAdapter;

    boolean pInfo, eInfo;
    String uid, fName, lName, email;
    boolean noNotices, foreman, supervisor;
    
    String[] perInfo, conInfo, licInfo, medInfo, oshInfo;
    ArrayList<String> hourBlacklist;
    ArrayList<String> reportBlacklist;

    boolean holding;
    NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        foreman = false;
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Bundle args = getIntent().getExtras();
        String type = args.getString("type", "");

        if(type.equals("Foreman") || type.equals("Admin") || type.equals("Supervisor")) {
            foreman = true;
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_foreman);
        uid = args.getString("uid");

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Hammonasset");
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        hourBlacklist = new ArrayList<>();
        reportBlacklist = new ArrayList<>();

        holding = false;
        getUserInfo();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(0).setChecked(true);

        setNavView(navigationView);
        setItemClickListener();
        setHourView();

        final SwipeRefreshLayout pullToRefresh = findViewById(R.id.pullToRefresh);
        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                reloadList();
                pullToRefresh.setRefreshing(false);
            }
        });
    }

    private void getUserInfo() {
        hView = findViewById(R.id.hourView);
        hView.setVisibility(View.GONE);


        noNotices = true;
        supervisor = false;

        pInfo = false;
        eInfo = false;

        home = new ArrayList<>();
        info = new ArrayList<>();

        hAdapter = new ItemAdapter(ForemanActivity.this, home);
        iAdapter = new ItemAdapter(ForemanActivity.this, info);

        list = findViewById(R.id.blockList);
        list.setAdapter(hAdapter);
        mode = "home";
        
        perInfo = new String[8];
        conInfo = new String[5];
        licInfo = new String[4];
        medInfo = new String[2];
        oshInfo = new String[3];

        populateLists();
    }

    private void populateLists() {
        try {
            final int infResource = getResources().getIdentifier(
                    "@drawable/red_info",
                    "drawable",
                    ForemanActivity.this.getPackageName());

            int defResource = getResources().getIdentifier(
                    "@drawable/done",
                    "drawable",
                    ForemanActivity.this.getPackageName());

            home.add(new Item(
                    "No Notices",
                    "No items need your attention",
                    "none",
                    defResource));
            hAdapter.notifyDataSetChanged();

            info.add(new Item(
                    "Emergency Contact",
                    "All information\nis up to date",
                    "con",
                    defResource));
            iAdapter.notifyDataSetChanged();

            info.add(new Item(
                    "Personal Info",
                    "All information\nis up to date",
                    "per",
                    defResource));
            iAdapter.notifyDataSetChanged();

            info.add(new Item(
                    "License",
                    "All information\nis up to date",
                    "lic",
                    defResource));
            iAdapter.notifyDataSetChanged();

            info.add(new Item(
                    "Medical Certificate",
                    "All information\nis up to date",
                    "med",
                    defResource));
            iAdapter.notifyDataSetChanged();

            info.add(new Item(
                    "OSHA Card",
                    "All information\nis up to date",
                    "osh",
                    defResource));
            iAdapter.notifyDataSetChanged();

            String uid = FirebaseAuth.getInstance().getUid();
            DatabaseReference ref =
                    FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

            ref.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    if (dataSnapshot.exists() && dataSnapshot.getKey().equals("eContact")) {
                        String eml = dataSnapshot.child("email").getValue(String.class);;
                        String nme = dataSnapshot.child("name").getValue(String.class);;
                        String pPh = dataSnapshot.child("pPhone").getValue(String.class);;
                        String rel = dataSnapshot.child("relationship").getValue(String.class);;
                        String sPh = dataSnapshot.child("sPhone").getValue(String.class);;

                        conInfo[0] = eml;
                        conInfo[1] = nme;
                        conInfo[2] = pPh;
                        conInfo[3] = rel;
                        conInfo[4] = sPh;

                        if (rel.equals("") || nme.equals("") ||
                                pPh.equals("") || (eml.equals("") && sPh.equals(""))) {

                            emeAlert(infResource);
                        }
                    } else if (dataSnapshot.exists() && dataSnapshot.getKey().equals("info")) {
                        String add = dataSnapshot.child("address").getValue(String.class);
                        String cty = dataSnapshot.child("city").getValue(String.class);
                        String fnm = dataSnapshot.child("fName").getValue(String.class);
                        String hdt = dataSnapshot.child("hireDate").getValue(String.class);
                        String lnm = dataSnapshot.child("lName").getValue(String.class);
                        String pho = dataSnapshot.child("phone").getValue(String.class);
                        String ste = dataSnapshot.child("state").getValue(String.class);
                        String zip = dataSnapshot.child("zip").getValue(String.class);

                        perInfo[0] = add;
                        perInfo[1] = cty;
                        perInfo[2] = fnm;
                        perInfo[3] = hdt;
                        perInfo[4] = lnm;
                        perInfo[5] = pho;
                        perInfo[6] = ste;
                        perInfo[7] = zip;
                        
                        if (add.equals("") ||
                                cty.equals("") || pho.equals("") || ste.equals("") || zip.equals("")) {

                            perAlert(infResource);
                        }

                        try {
                            String num = dataSnapshot.child("license").child("licenseNumber").getValue(String.class);
                            String exp = dataSnapshot.child("license").child("licenseExpiration").getValue(String.class);
                            String lst = dataSnapshot.child("license").child("licenseState").getValue(String.class);
                            String img = dataSnapshot.child("license").child("licenseImage").getValue(String.class);

                            licInfo[0] = num;
                            licInfo[1] = exp;
                            licInfo[2] = lst;
                            licInfo[3] = img;

                            if(num.equals("") || num == null || lst.equals("") || lst == null ||
                                    exp.equals("") || exp == null || img.equals("") || img == null)
                                licAlert(infResource);
                            else
                                licExpAlert(exp, infResource);
                        } catch (Exception e) {
                            licAlert(infResource);
                        }

                        try {
                            String exp = dataSnapshot.child("medicalCertificate").child("medicalCertificateExpiration").getValue(String.class);
                            String img = dataSnapshot.child("medicalCertificate").child("medicalCertificateImage").getValue(String.class);

                            medInfo[0] = exp;
                            medInfo[1] = img;

                            if(exp.equals("") || exp == null || img.equals("") || img == null)
                                medAlert(infResource);
                            else
                                medExpAlert(exp, infResource);
                        } catch (Exception e) {
                            medAlert(infResource);
                        }

                        try {
                            String num = dataSnapshot.child("oshaCard").child("oshaCardNumber").getValue(String.class);
                            String exp = dataSnapshot.child("oshaCard").child("oshaCardExpiration").getValue(String.class);
                            String img = dataSnapshot.child("oshaCard").child("oshaCardImage").getValue(String.class);

                            oshInfo[0] = num;
                            oshInfo[1] = exp;
                            oshInfo[2] = img;

                            if(num.equals("") || num == null ||
                                    exp.equals("") || exp == null || img.equals("") || img == null)
                                oshAlert(infResource);
                            else
                                oshExpAlert(exp, infResource);
                        } catch (Exception e) {
                            oshAlert(infResource);
                        }
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) { }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) { }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) { }

                @Override
                public void onCancelled(DatabaseError databaseError) { }
            });

            ref.child("hours").addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    String bDate = dataSnapshot.getKey();
                    String[] allDatesForWeek = allDates(bDate);
                    ArrayList<String> datesOfAlert = new ArrayList<>();

                    for(int i=0; i < allDatesForWeek.length; i++) {
                        String d = allDatesForWeek[i];
                        String v1 = dataSnapshot
                                .child(d).child("day").child("foreman").getValue(String.class);
                        String v2 = dataSnapshot
                                .child(d).child("night").child("foreman").getValue(String.class);

                        try {
                            if (!v1.equals("")) {
                                v1 = dataSnapshot.child(d)
                                        .child("day").child("sTime").getValue(String.class);

                                try {
                                    if(v1.equals(""))
                                        datesOfAlert.add(d + " Day");
                                } catch (NullPointerException e1) {
                                    datesOfAlert.add(d + " Day");
                                }
                            }
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        }

                        try {
                            if (!v2.equals("")) {
                                v2 = dataSnapshot.child(d)
                                        .child("night").child("sTime").getValue(String.class);

                                try {
                                    if(v2.equals(""))
                                        datesOfAlert.add(d + " Night");
                                } catch (NullPointerException e1) {
                                    datesOfAlert.add(d + " Night");
                                }
                            }
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        }
                    }

                    if (datesOfAlert.size() != 0) {
                        for (int j = 0; j < datesOfAlert.size(); j++) {
                            String d = datesOfAlert.get(j);
                            hourBlacklist.add(d);
                            home.add(new Item(
                                    "Report Hours",
                                    "Need to Report\n" + d,
                                    "hou",
                                    infResource));
                            removeDefaultItem();
                        }
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) { }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) { }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) { }

                @Override
                public void onCancelled(DatabaseError databaseError) { }
            });

            if(foreman) {
                ref.child("crews").addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                        String bDate = dataSnapshot.getKey();
                        String[] allDatesForWeek = allDates(bDate);
                        ArrayList<String> datesOfAlert = new ArrayList<>();
                        ArrayList<String> daysNeedingApproval = new ArrayList<>();

                        for (int i = 0; i < allDatesForWeek.length; i++) {
                            String d = allDatesForWeek[i];
                            String v1 = dataSnapshot
                                    .child(d).child("day").child("report").getValue(String.class);
                            String v2 = dataSnapshot
                                    .child(d).child("night").child("report").getValue(String.class);
                            String p1 = dataSnapshot
                                    .child(d).child("day").child("poNumber").getValue(String.class);
                            String p2 = dataSnapshot
                                    .child(d).child("night").child("poNumber").getValue(String.class);
                            String a1 = dataSnapshot
                                    .child(d).child("day").child("approveAlert").getValue(String.class);
                            String a2 = dataSnapshot
                                    .child(d).child("night").child("approveAlert").getValue(String.class);

                            try {
                                if(v1.equals("false"))
                                    datesOfAlert.add(p1 + " Report\n" + d + " Day");
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            }

                            try {
                                if(v2.equals("false"))
                                    datesOfAlert.add(p2 + " Report\n" + d + " Night");
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            }

                            try {
                                if(a1.equals("true"))
                                    daysNeedingApproval.add(p1 + " Hours need Approval\n"+d+" Day");
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            }

                            try {
                                if(a2.equals("true"))
                                    daysNeedingApproval.add(p2 + " Hours need Approval\n"+d+" Night");
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            }
                        }

                        if(datesOfAlert.size() != 0) {
                            for(int j = 0; j < datesOfAlert.size(); j++) {
                                String d = datesOfAlert.get(j);
                                home.add(new Item("Daily Report", d, "rep", infResource));
                                removeDefaultItem();
                            }
                        }

                        if(daysNeedingApproval.size() != 0) {
                            for(int k = 0; k < daysNeedingApproval.size(); k++) {
                                String d = daysNeedingApproval.get(k);
                                home.add(new Item("Approve Hours", d, "app", infResource));
                                removeDefaultItem();
                            }
                        }
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) { }

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
            e.printStackTrace();
        }
    }

    private void emeAlert(int infResource) {
        home.add(new Item(
                "Missing Info",
                "Need to update\nEmergency Contact",
                "con",
                infResource));

        info.get(0).setSub("Missing Info");
        info.get(0).setPic(infResource);
        iAdapter.notifyDataSetChanged();

        removeDefaultItem();
    }

    private void perAlert(int infResource) {
        home.add(new Item(
                "Missing Info",
                "Need to update\nPersonal Information",
                "per",
                infResource));

        info.get(1).setSub("Missing Info");
        info.get(1).setPic(infResource);
        iAdapter.notifyDataSetChanged();

        removeDefaultItem();
    }

    private void licAlert(int infResource) {
        home.add(new Item(
                "Missing Info",
                "Need to update\nLicense Information",
                "lic",
                infResource));

        info.get(2).setSub("Missing Info");
        info.get(2).setPic(infResource);
        iAdapter.notifyDataSetChanged();

        removeDefaultItem();
    }

    private void licExpAlert(String exp, int infResource) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
        String sub = "";

        try {
            Date d1 = sdf.parse(exp);
            Date d2 = new Date();

            if(d2.after(d1) || d1.equals(d2))
                sub = "License Expired";
            else {
                Calendar c = Calendar.getInstance();
                c.setTime(d1);
                c.add(Calendar.DAY_OF_YEAR, -31);

                d1 = c.getTime();

                if(d2.after(d1))
                    sub = "License Expiring Soon";
            }
        } catch (Exception e) {
            e.printStackTrace();
            sub = "Invalid expiration date";
        }

        if(!sub.equals("")) {
            home.add(new Item(
                    "License",
                    sub,
                    "lic",
                    infResource));

            info.get(2).setSub(sub);
            info.get(2).setPic(infResource);
            iAdapter.notifyDataSetChanged();

            removeDefaultItem();
        }
    }

    private void medAlert(int infResource) {
        home.add(new Item(
                "Missing Info",
                "Need to update\nMed Certificate",
                "med",
                infResource));

        info.get(3).setSub("Missing Info");
        info.get(3).setPic(infResource);
        iAdapter.notifyDataSetChanged();

        removeDefaultItem();
    }

    private void medExpAlert(String exp, int infResource) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
        String sub = "";

        try {
            Date d1 = sdf.parse(exp);
            Date d2 = new Date();

            if(d2.after(d1) || d1.equals(d2))
                sub = "Med Cert Expired";
            else {
                Calendar c = Calendar.getInstance();
                c.setTime(d1);
                c.add(Calendar.DAY_OF_YEAR, -31);

                d1 = c.getTime();

                if(d2.after(d1))
                    sub = "Med Cert Expiring Soon";
            }
        } catch (Exception e) {
            e.printStackTrace();
            sub = "Invalid expiration date";
        }

        if(!sub.equals("")) {
            home.add(new Item(
                    "Med Cert",
                    sub,
                    "med",
                    infResource));

            info.get(3).setSub(sub);
            info.get(3).setPic(infResource);
            iAdapter.notifyDataSetChanged();

            removeDefaultItem();
        }
    }

    private void oshAlert(int infResource) {
        home.add(new Item(
                "Missing Info",
                "Need to update\nOSHA Card",
                "osh",
                infResource));

        info.get(4).setSub("Missing Info");
        info.get(4).setPic(infResource);
        iAdapter.notifyDataSetChanged();

        removeDefaultItem();
    }

    private void oshExpAlert(String exp, int infResource) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
        String sub = "";

        try {
            Date d1 = sdf.parse(exp);
            Date d2 = new Date();

            if(d2.after(d1) || d1.equals(d2))
                sub = "OSHA Card Expired";
            else {
                Calendar c = Calendar.getInstance();
                c.setTime(d1);
                c.add(Calendar.DAY_OF_YEAR, -31);

                d1 = c.getTime();

                if(d2.after(d1))
                    sub = "OSHA Card Expiring Soon";
            }
        } catch (Exception e) {
            e.printStackTrace();
            sub = "Invalid expiration date";
        }

        if(!sub.equals("")) {
            home.add(new Item(
                    "OSHA Card",
                    sub,
                    "osh",
                    infResource));

            info.get(4).setSub(sub);
            info.get(4).setPic(infResource);
            iAdapter.notifyDataSetChanged();

            removeDefaultItem();
        }
    }

    private void removeDefaultItem() {
        if(noNotices) {
            noNotices = false;
            home.remove(0);
            hAdapter.notifyDataSetChanged();

            openInfo();
            openHome();
        }

        hAdapter.notifyDataSetChanged();
    }

    private void reloadList() {
        home.clear();
        info.clear();
        noNotices = true;

        hAdapter.notifyDataSetChanged();
        iAdapter.notifyDataSetChanged();

        perInfo = new String[8];
        conInfo = new String[5];

        reportBlacklist.clear();
        populateLists();
    }

    private void setNavView(NavigationView nView) {
        fName = getIntent().getStringExtra("fName");
        lName = getIntent().getStringExtra("lName");
        email = getIntent().getStringExtra("email");

        View header = nView.getHeaderView(0);
        TextView nm = header.findViewById(R.id.nameText);
        TextView em = header.findViewById(R.id.emailText);

        String n = fName+" "+lName;
        nm.setText(n);
        em.setText(email);

        displaySelectedScreen(R.id.nav_home);

        if(foreman) {
            getMenuInflater().inflate(R.menu.foreman_submenu, nView.getMenu());
        }
    }

    private void setHourView() {
        Button rep = findViewById(R.id.reportHoursBtn);
        Button his = findViewById(R.id.hoursHistoryBtn);

        rep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ForemanActivity.this, ReportHours.class);
                i.putExtra("uid", uid);
                i.putExtra("foreman", true);
                ForemanActivity.this.startActivity(i);
            }
        });

        his.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ForemanActivity.this, HoursHistoryActivity.class);
                i.putExtra("uid", uid);
                ForemanActivity.this.startActivity(i);
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.foreman, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            ForemanActivity.this.finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        displaySelectedScreen(itemId);
        return true;
    }
    
    private void setItemClickListener() {
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String code = "";
                Item item;

                if(list.getAdapter().equals(hAdapter))
                    item = home.get(position);
                else
                    item = info.get(position);
                
                if(mode.equals("home")) 
                    code = item.getCode();
                else if(mode.equals("info"))
                    code = item.getCode();

                if(code.equals("con")) {
                    Intent i = new Intent(ForemanActivity.this, EmergencyContact.class);
                    i.putExtra("uid", uid);
                    i.putExtra("conInfo", conInfo);
                    ForemanActivity.this.startActivity(i);
                }
                else if(code.equals("per")) {
                    Intent i = new Intent(ForemanActivity.this, PersonalInfo.class);
                    i.putExtra("uid", uid);
                    i.putExtra("perInfo", perInfo);
                    ForemanActivity.this.startActivity(i);
                }
                else if(code.equals("rep")) {
                    Intent i = new Intent(ForemanActivity.this, DailyReportActivity.class);
                    i.putExtra("pNum", getPoNumString(item));
                    i.putExtra("date", getReportDateString(item));
                    i.putExtra("time", getReportTimeString(item));

                    i.putExtra("fName", fName);
                    i.putExtra("lName", lName);
                    ForemanActivity.this.startActivity(i);
                }
                else if(code.equals("hou")){
                    String[] hTexts = item.getSub().split("\n");
                    String text = hTexts[1];

                    hTexts = text.split(" ");
                    String date = hTexts[0];
                    String time = hTexts[1];

                    Intent i = new Intent(ForemanActivity.this, ReportHours.class);
                    i.putExtra("date", date);
                    i.putExtra("time", time);
                    i.putExtra("foreman", true);

                    ForemanActivity.this.startActivity(i);
                }
                else if(code.equals("app")) {
                    String[] aTexts = item.getSub().split("\n");
                    String text = aTexts[1];

                    aTexts = text.split(" ");
                    String date = aTexts[0];
                    String time = aTexts[0];

                    openCrewHistory(date, time);
                }
                else if(code.equals("lic")) {
                    Intent i = new Intent(ForemanActivity.this, LicenseActivity.class);
                    i.putExtra("uid", uid);
                    i.putExtra("licInfo", licInfo);

                    try {
                        ForemanActivity.this.startActivity(i);
                    } catch (Exception e) {
                        licInfo[3] = "failedDueToSize";

                        i = new Intent(ForemanActivity.this, LicenseActivity.class);
                        i.putExtra("uid", uid);
                        i.putExtra("licInfo", licInfo);
                        ForemanActivity.this.startActivity(i);
                    }
                }
                else if(code.equals("med")) {
                    Intent i = new Intent(ForemanActivity.this, MedicalCertificateActivity.class);
                    i.putExtra("uid", uid);
                    i.putExtra("medInfo", medInfo);

                    try {
                        ForemanActivity.this.startActivity(i);
                    } catch(Exception e) {
                        medInfo[1] = "failedDueToSize";

                        i = new Intent(ForemanActivity.this, MedicalCertificateActivity.class);
                        i.putExtra("uid", uid);
                        i.putExtra("medInfo", medInfo);
                        ForemanActivity.this.startActivity(i);
                    }
                }
                else if(code.equals("osh")) {
                    Intent i = new Intent(ForemanActivity.this, OshaCardActivity.class);
                    i.putExtra("uid", uid);
                    i.putExtra("oshInfo", oshInfo);

                    try {
                        ForemanActivity.this.startActivity(i);
                    } catch(Exception e) {
                        oshInfo[2] = "failedDueToSize";

                        i = new Intent(ForemanActivity.this, OshaCardActivity.class);
                        i.putExtra("uid", uid);
                        i.putExtra("oshInfo", oshInfo);
                        ForemanActivity.this.startActivity(i);
                    }
                }
                else {
                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(ForemanActivity.this);
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

    private void displaySelectedScreen(int itemId) {
        switch (itemId) {
            case R.id.nav_home:
                openHome();
                break;
            case R.id.nav_info:
                openInfo();
                break;
            case R.id.nav_pHours:
                openHours();
                break;
            case R.id.nav_drive:
                openDriveActivity();
                break;
            case R.id.nav_vehicle_receipt:
                openVehicleReceiptActivity();
                break;
            case R.id.nav_maintenance:
                openMaintenanceActivity();
                break;
            case R.id.nav_accident:
                openAccidentActivity();
                break;
            case R.id.nav_crew:
                openCrewActivity();
                break;
            case R.id.nav_orders:
                openCrewHistory("", "");
                break;
            case R.id.nav_receipts:
                openReceiptActivity();
                break;
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    private void openHome() {
        mode = "home";
        navigationView.getMenu().getItem(0).setChecked(true);

        list.setAdapter(hAdapter);
        hView.setVisibility(View.GONE);
        list.setVisibility(View.VISIBLE);
    }

    private void openInfo() {
        mode = "info";
        navigationView.getMenu().getItem(1).setChecked(true);

        list.setAdapter(iAdapter);
        hView.setVisibility(View.GONE);
        list.setVisibility(View.VISIBLE);
    }

    private void openHours() {
        mode = "hour";
        navigationView.getMenu().getItem(2).setChecked(true);

        list.setVisibility(View.GONE);
        hView.setVisibility(View.VISIBLE);
    }

    private void openDriveActivity() {
        Intent i = new Intent(ForemanActivity.this, DriveActivity.class);
        i.putExtra("uid", uid);
        i.putExtra("fName", fName);
        i.putExtra("lName", lName);

        ForemanActivity.this.startActivity(i);
    }

    private void openVehicleReceiptActivity() {
        Intent i = new Intent(ForemanActivity.this, VehicleReceiptActivity.class);
        i.putExtra("uid", uid);
        i.putExtra("fName", fName);
        i.putExtra("lName", lName);

        ForemanActivity.this.startActivity(i);
    }

    private void openMaintenanceActivity() {
        Intent i = new Intent(ForemanActivity.this, MaintenanceActivity.class);
        i.putExtra("uid", uid);
        i.putExtra("fName", fName);
        i.putExtra("lName", lName);

        ForemanActivity.this.startActivity(i);
    }

    private void openAccidentActivity() {
        Intent i = new Intent(ForemanActivity.this, AccidentActivity.class);
        i.putExtra("uid", uid);
        i.putExtra("fName", fName);
        i.putExtra("lName", lName);

        ForemanActivity.this.startActivity(i);
    }

    private void openCrewActivity() {
        Intent i = new Intent(ForemanActivity.this, CrewActivity.class);
        i.putExtra("uid", uid);
        ForemanActivity.this.startActivity(i);
    }

    private void openCrewHistory(String date, String time) {
        Intent i = new Intent(ForemanActivity.this, CrewHistory.class);
        i.putExtra("uid", uid);
        i.putExtra("date", date);
        i.putExtra("time", time);

        i.putExtra("fName", fName);
        i.putExtra("lName", lName);
        ForemanActivity.this.startActivity(i);
    }

    private void openReceiptActivity() {
        Intent i = new Intent(ForemanActivity.this, ReceiptActivity.class);
        i.putExtra("uid", uid);
        i.putExtra("fName", fName);
        i.putExtra("lName", lName);
        ForemanActivity.this.startActivity(i);
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

        return output;
    }

    private String getPoNumString(Item i) {
        String sub = i.getSub();
        String[] newLineSplit = sub.split("\n");
        sub = newLineSplit[0];

        String[] spaceSplit = sub.split(" ");
        String output = spaceSplit[0];

        for(int j=1; j<spaceSplit.length; j++) {
            String s = spaceSplit[j];

            if(!s.equals("Report")) {
                output += " "+s;
            }
        }

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
