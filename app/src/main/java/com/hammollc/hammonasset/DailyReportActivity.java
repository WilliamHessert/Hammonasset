package com.hammollc.hammonasset;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.pdf.PdfDocument;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DailyReportActivity extends AppCompatActivity {

    ProgressBar pBar;
    Button cont, save;
    RelativeLayout cLayout;

    String pol;
    boolean sComp, sig;
    int pNum, aNum, tNum;

    String[] pTemp;
    ArrayList<String> finalNames;
    ArrayList<String> finalTowns;

    ArrayList<String> savedNames;
    ArrayList<String> savedTowns;

    ArrayList<ReportImage> savedImages;
    ArrayList<ReportImage> finalImages;

    ArrayList<PayItem> initPayItems;
    ArrayList<PayItem> finalPayItems;
    ArrayList<String> finalAccomplishments;

    FirebaseDatabase db;
    boolean cSafety, cSig;
    ArrayList<String> values;

    Bitmap sigImage;
    signature mSignature;
    LinearLayout mContent;
    String finalSignature;

    String date, time;
    String layoutValue = "init";
    ArrayList<PayItem> payItemHolder;

    boolean policeTowns = false;
    PayItemAdapter payItemAdapter;
    boolean firstTimeLoadingPayItems = true;

    private static final int CAMERA_REQUEST = 1888;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;

    private static final int WRITE_REQUEST_CODE = 3888;
    private static final int STORAGE_REQUEST = 2888;
    private static final int RESULT_LOAD_IMG = 200;

    ArrayList<String> fileNames;
    ArrayList<ReportImage> cImages;
    ArrayAdapter<String> imageAdapter;

    String preKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_report);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        finalImages = new ArrayList<>();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getDateAndTime();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                switch (layoutValue) {
                    case "init":
                        confirmPageLeave();
                        break;
                    case "item":
                        loadInitInfo();
                        break;
                    case "acmp":
                        showPayItemView();
                        break;
                    case "safe":
                        loadAccomplishmentsView();
                        break;
                    case "irev":
                        loadSafetyView();
                        break;
                    case "psig":
                        openReview(false);
                        break;
                    case "pol1":
                        openSignature();
                        break;
                    case "pol2":
                        openPolice();
                        break;
                    case "flag":
                        if(policeTowns)
                            openPoliceTownView();
                        else
                            openPolice();
                        break;
                    case "misc":
                        openFlaggerView();
                        break;
                    case "imag":
                        loadMiscView();
                        break;
                    case "erev":
                        addImages();
                        break;
                }

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void confirmPageLeave() {
        AlertDialog.Builder builder = new AlertDialog.Builder(DailyReportActivity.this);
        builder.setTitle("Confirm");
        builder.setMessage("You're about to leave this report without saving. Are you sure about this?");

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DailyReportActivity.this.finish();
                dialog.dismiss();
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

    private void getDateAndTime() {
        Bundle args = getIntent().getExtras();
        date = args.getString("date", "");
        time = args.getString("time", "");

        EditText dField = findViewById(R.id.repDate);
        EditText tField = findViewById(R.id.repTime);

        dField.setText(date);
        tField.setText(time);
        preKey = date+time;

        if(date.equals("") || time.equals("")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(DailyReportActivity.this);
            builder.setTitle("Error");
            builder.setMessage("Sorry, there was an error loading this daily report. Please contact your admin");

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    DailyReportActivity.this.finish();
                    dialog.dismiss();
                }
            });

            builder.create().show();
        }
        else
            downloadPoNumsNum();
    }

    private void downloadPoNumsNum() {
        String poNumber = getIntent().getExtras().getString("pNum", "");
        db = FirebaseDatabase.getInstance();
        final DatabaseReference ref = db.getReference(
                "Contracts").child("16PSX0176").child("poNums").child(poNumber);
        Log.i("AHHH", poNumber);
        ref.child("number").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String nString = dataSnapshot.getValue(String.class);
                int num = Integer.parseInt(nString);
                downloadPoNums(num, ref);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }

    private void downloadPoNums(final int num, DatabaseReference ref) {
        initPayItems = new ArrayList<>();
        final ArrayList<PayItem> items = new ArrayList<>();

        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String c = dataSnapshot.child("itemKey").getValue(String.class);

                if(c != null) {
                    DatabaseReference iRef = FirebaseDatabase.getInstance()
                            .getReference("Contracts").child("16PSX0176").child("pItems").child(c);

                    iRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot childSnapshot) {
                            String m = childSnapshot.getKey();
                            String n = childSnapshot.child("name").getValue(String.class);
                            String u = childSnapshot.child("unit").getValue(String.class);

                            items.add(new PayItem(m, n, u));

                            if (num == items.size())
                                initPayItems = items;
                                loadSavedData();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
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
    }

    private void loadSavedData() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(DailyReportActivity.this);
        String pNum = prefs.getString(preKey+"pNum", "");
        String insp = prefs.getString(preKey+"insp", "");
        String bNum = prefs.getString(preKey+"bNum", "");
        String loca = prefs.getString(preKey+"loca", "");
        String town = prefs.getString(preKey+"town", "");

        String safe = prefs.getString(preKey+"safe", "");
        String fNot = prefs.getString(preKey+"fNot", "");
        String sNot = prefs.getString(preKey+"sNot", "");

        String pTyp = prefs.getString(preKey+"pTyp", "");
        String pArr = prefs.getString(preKey+"pArr", "");
        String pEnd = prefs.getString(preKey+"pEnd", "");

        String fArr = prefs.getString(preKey+"fArr", "");
        String fEnd = prefs.getString(preKey+"fEnd", "");
        String fNum = prefs.getString(preKey+"fNum", "");

        String tArr = prefs.getString(preKey+"tArr", "");
        String tEnd = prefs.getString(preKey+"tEnd", "");
        String rArr = prefs.getString(preKey+"rArr", "");
        String rEnd = prefs.getString(preKey+"rEnd", "");
        String sVeh = prefs.getString(preKey+"sVeh", "");

        EditText et0 = findViewById(R.id.poNum);
        EditText et1 = findViewById(R.id.eInspector);
        EditText et2 = findViewById(R.id.eBridge);
        EditText et3 = findViewById(R.id.eLocation);
        EditText et4 = findViewById(R.id.eCity);

        et0.setText(pNum);
        et1.setText(insp);
        et2.setText(bNum);
        et3.setText(loca);
        et4.setText(town);

        EditText et6 = findViewById(R.id.eSafetyNotes);
        EditText et7 = findViewById(R.id.eMiscNotes);

        et6.setText(fNot);
        et7.setText(sNot);

        EditText et9 = findViewById(R.id.policeStartTime);
        EditText et10 = findViewById(R.id.policeEndTime);

        et9.setText(pArr);
        et10.setText(pEnd);

        EditText et11 = findViewById(R.id.flaggerStartTime);
        EditText et12 = findViewById(R.id.flaggerEndTime);
        EditText et13 = findViewById(R.id.flaggerNumber);

        et11.setText(fArr);
        et12.setText(fEnd);
        et13.setText(fNum);

        EditText et14 = findViewById(R.id.trafficStartTime);
        EditText et15 = findViewById(R.id.trafficEndTime);
        EditText et16 = findViewById(R.id.prodStartTime);
        EditText et17 = findViewById(R.id.prodEndTime);
        EditText et18 = findViewById(R.id.vehicleStorage);

        et14.setText(tArr);
        et15.setText(tEnd);
        et16.setText(rArr);
        et17.setText(rEnd);
        et18.setText(sVeh);

        pol = pTyp;
        cSafety = !safe.equals("");

        if(cSafety)
            sComp = safe.equals("Yes");

        savedNames = new ArrayList<>();
        savedTowns = new ArrayList<>();

        loadSavedName(prefs, 0);
    }

    private void loadSavedName(SharedPreferences prefs, int i) {
        String key = "nam"+i;
        String nam = prefs.getString(preKey+key, "");

        if(nam.equals(""))
            loadSavedTown(prefs, 0);
        else {
            savedNames.add(nam);
            loadSavedName(prefs, i+1);
        }
    }

    private void loadSavedTown(SharedPreferences prefs, int i) {
        String key = "tow"+i;
        String tow = prefs.getString(preKey+key, "");

        if(tow.equals("")) {
            finalImages = new ArrayList<>();
            loadSavedImage(prefs, 0);
        }
        else {
            savedTowns.add(tow);
            loadSavedTown(prefs, i+1);
        }
    }

    private void loadSavedImage(SharedPreferences prefs, int i) {
        String key = "img"+i;
        String img = prefs.getString(preKey+key, "");

        if(img.equals(""))
            loadInitInfo();
        else {
            String[] split = img.split("_");

            try {
                ReportImage im = new ReportImage(split[0]);
                im.setName(split[1]);
                im.setDesc(split[2]);

                finalImages.add(im);
                loadSavedImage(prefs, i+1);
                loadInitInfo();
            } catch (Exception e) {
                loadInitInfo();
            }
        }
    }

    private void loadInitInfo() {
        layoutValue = "init";

        cont = findViewById(R.id.contBtn);
        save = findViewById(R.id.saveBtn);
        pBar = findViewById(R.id.reportProgress);
        cLayout = findViewById(R.id.initialInfo);

        pBar.setVisibility(View.GONE);
        cont.setVisibility(View.VISIBLE);
        save.setVisibility(View.VISIBLE);
        cLayout.setVisibility(View.VISIBLE);

        RelativeLayout oLayout = findViewById(R.id.payItemHolder);
        oLayout.setVisibility(View.GONE);

        values = new ArrayList<>();
        final EditText pNum = findViewById(R.id.poNum);
        final EditText city = findViewById(R.id.eCity);
        final EditText insp = findViewById(R.id.eInspector);
        final EditText brid = findViewById(R.id.eBridge);
        final EditText loca = findViewById(R.id.eLocation);

        String poNumber = getIntent().getExtras().getString("pNum", "");
        pNum.setText(poNumber);

        if(poNumber.equals("")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(DailyReportActivity.this);
            builder.setTitle("Error");
            builder.setMessage("Sorry, we couldn't " +
                    "load the P.O. Number for this report. Please contact your system admin");

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    DailyReportActivity.this.finish();
                    dialog.dismiss();
                }
            });
        }

        final List<String> towns = Arrays.asList(getResources().getStringArray(R.array.towns));
        city.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(DailyReportActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setCancelable(false);
                dialog.setContentView(R.layout.dialog_select_view);

                ListView tList = dialog.findViewById(R.id.selectList);
                ArrayAdapter<String> tAdapter = new ArrayAdapter<>(
                        DailyReportActivity.this, android.R.layout.simple_list_item_1, towns);
                tList.setAdapter(tAdapter);

                tList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String town = towns.get(position);
                        city.setText(town);
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

        cont.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addInitValues(pNum, city, insp, brid, loca);
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmExit();
            }
        });
    }

    private void addInitValues(
            EditText pNum, EditText city, EditText insp, EditText brid, EditText loca) {

        String p = pNum.getText().toString();
        String c = city.getText().toString();
        String i = insp.getText().toString();
        String b = brid.getText().toString();
        String l = loca.getText().toString();

        if(p.equals("") || c.equals("") || i.equals("") || b.equals("") || l.equals(""))
            enterAllValuesError();
        else {
            values.add(p);
            values.add(i);
            values.add(b);
            values.add(l);
            values.add(c);

            loadPayItemView();
        }
    }

    private void loadPayItemView() {
        cLayout.setVisibility(View.GONE);
        final ArrayList<PayItem> items = initPayItems;

        layoutValue = "item";
        payItemHolder = items;

        cLayout = findViewById(R.id.payItemHolder);
        cLayout.setVisibility(View.VISIBLE);
        pTemp = new String[items.size()];

        for(int i=0; i<pTemp.length; i++) {
            pTemp[i] = "";
        }

        final ListView pList = findViewById(R.id.payItemList);
        payItemAdapter = new PayItemAdapter(
                DailyReportActivity.this, items, false);
        pList.setAdapter(payItemAdapter);

        if(firstTimeLoadingPayItems)
            loadSavedValues(items, payItemAdapter);

        firstTimeLoadingPayItems = false;

        pList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startGathering(position, items, payItemAdapter);
            }
        });

        cont.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPayItemValues(items);
            }
        });
    }

    private void loadSavedValues(ArrayList<PayItem> items, PayItemAdapter ad) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(DailyReportActivity.this);
        String poNum1 = prefs.getString(preKey+"pNum", "");
        String poNum2 = values.get(0);

        if(poNum1.equals(poNum2)) {
            boolean moreItems = true;
            int i = -1;

            while(moreItems) {
                i++;
                String key = "pItem"+i;
                String payItemValue = prefs.getString(preKey+key, "");

                if(payItemValue.equals("")) {
                    moreItems = false;
                }
                else {
                    String[] split = payItemValue.split("~");

                    if(split.length == 2) {
                        payItemValue = split[1];
                        items.get(i).setValue(payItemValue);
                        ad.notifyDataSetChanged();
                    }
                }
            }
        }
    }

    private void showPayItemView() {
        layoutValue = "item";

        cLayout = findViewById(R.id.payItemHolder);
        cLayout.setVisibility(View.VISIBLE);

        RelativeLayout oLayout = findViewById(R.id.accHolder);
        oLayout.setVisibility(View.GONE);

        for(int i = 0; i < initPayItems.size(); i++) {
            PayItem item = initPayItems.get(i);
            String val = item.getValue();
            String[] vSplit = val.split("~");

            if(vSplit.length == 2) {
                item.setValue(vSplit[1]);
                payItemAdapter.notifyDataSetChanged();
            }
        }

        cont.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPayItemValues(initPayItems);
            }
        });
    }

    private void startGathering(int i, ArrayList<PayItem> items, PayItemAdapter ad) {
        if(i < items.size())
            gatherValue(i, items, ad);
    }

    private void gatherValue(final int i, final ArrayList<PayItem> items, final PayItemAdapter ad) {
        PayItem item = items.get(i);
        final TextView tv = findViewById(R.id.payItemText);

        AlertDialog.Builder builder = new AlertDialog.Builder(DailyReportActivity.this);
        builder.setTitle(item.getCode()+" "+item.getName());
        builder.setMessage("Please enter the "+item.getUnit()+"s for this pay item");

        final EditText input = new EditText(DailyReportActivity.this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String val = input.getText().toString();
                pTemp[i] = val;

                for(int j=0; j<pTemp.length; j++) {
                   if(pTemp[j].equals("")) {
                       items.get(j).setValue("");
                   }
                   else {
                       items.get(j).setValue(pTemp[j]);
                   }
                }

                ad.notifyDataSetChanged();

                int ii = i+1;
                startGathering(ii, items, ad);
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                input.setInputType(0);
                dialog.dismiss();
            }
        });

        builder.create().show();

        input.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    private void addPayItemValues(ArrayList<PayItem> items) {
        boolean invalid = false;
        String[] vals = new String[items.size()];
        ArrayList<String> valuesToAdd = new ArrayList<>();

        for(int k=0; k<vals.length; k++) {
            vals[k] = items.get(k).getValue();
        }

        for(int i=0; i<vals.length; i++) {
            String s = vals[i];

            if(s.equals("")) {
                invalid = true;
                i = vals.length;
            }
            else {
                String p = items.get(i).getCode();
                String v = p+"~"+s;
                valuesToAdd.add(v);
            }
        }

        if(invalid)
            enterAllValuesError();
        else {
            pNum = valuesToAdd.size();
            finalPayItems = new ArrayList<>();

            for(int j=0; j<valuesToAdd.size(); j++) {
                PayItem item = items.get(j);
                String val = valuesToAdd.get(j);
                item.setValue(val);

                finalPayItems.add(item);
            }

            loadAccomplishmentsView();
        }
    }

    private void loadAccomplishmentsView() {
        layoutValue = "acmp";
        cLayout.setVisibility(View.GONE);
        cLayout = findViewById(R.id.accHolder);
        cLayout.setVisibility(View.VISIBLE);

        RelativeLayout oLayout = findViewById(R.id.safetyHolder);
        oLayout.setVisibility(View.GONE);

        ListView aList = findViewById(R.id.accList);
        final ArrayList<String> accs = new ArrayList<>();
        final ArrayAdapter<String> ad = new ArrayAdapter<>(
                DailyReportActivity.this, android.R.layout.simple_list_item_1, accs);

        loadSavedAccomplishments(accs, ad);

        aList.setAdapter(ad);
        aList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder =
                        new AlertDialog.Builder(DailyReportActivity.this);
                builder.setTitle("Confirm");
                builder.setMessage("Are you sure you want to delete this accomplishment?");

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        accs.remove(position);
                        ad.notifyDataSetChanged();
                        dialog.dismiss();
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
        });

        Button accBtn = findViewById(R.id.accBtn);
        accBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(DailyReportActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setCancelable(false);
                dialog.setContentView(R.layout.dialog_add_accomplishment);

                Button add = dialog.findViewById(R.id.addAccDialogBtn);
                Button can = dialog.findViewById(R.id.cancelAccDialogBtn);
                final EditText acc = dialog.findViewById(R.id.eAccomplishment);

                can.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                add.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String a = acc.getText().toString();

                        if(!a.equals("")) {
                            accs.add(a);
                            ad.notifyDataSetChanged();
                        }

                        dialog.dismiss();
                    }
                });

                dialog.show();
            }
        });

        cont.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmAddAccs(accs);
            }
        });
    }

    private void loadSavedAccomplishments(ArrayList<String> accs, ArrayAdapter<String> ad) {
        int i = -1;
        boolean moreItems = true;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(DailyReportActivity.this);

        while(moreItems) {
            i++;
            String key = "acc"+i;
            String acc = prefs.getString(preKey+key, "");

            if(acc.equals(""))
                moreItems = false;
            else {
                accs.add(acc);
                ad.notifyDataSetChanged();
            }
        }
    }

    private void confirmAddAccs(final ArrayList<String> accs) {
        aNum = accs.size();

        if(aNum == 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(DailyReportActivity.this);
            builder.setTitle("No Accomplishments");
            builder.setMessage(
                    "Are you sure you want to proceed without adding any accomplishments?");

            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    addAccs(accs);
                    dialog.dismiss();
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
        else
            addAccs(accs);
    }

    private void addAccs(ArrayList<String> accs) {
        finalAccomplishments = new ArrayList<>(accs);
        loadSafetyView();
    }

    private void loadSafetyView() {
        layoutValue = "safe";
        cLayout.setVisibility(View.GONE);
        cLayout = findViewById(R.id.safetyHolder);
        cLayout.setVisibility(View.VISIBLE);

        RelativeLayout oLayout = findViewById(R.id.sigReviewHolder);
        oLayout.setVisibility(View.GONE);
        String nText = "Continue >";
        cont.setText(nText);

        final Button nBtn = findViewById(R.id.safetyNoBtn);
        final Button yBtn = findViewById(R.id.safetyYesBtn);
        final EditText sNotes = findViewById(R.id.eSafetyNotes);

        if(cSafety) {
            if(sComp) {
                yBtn.setTextColor(Color.WHITE);
                yBtn.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

                nBtn.setBackground(getResources().getDrawable(R.drawable.box));
                nBtn.setTextColor(getResources().getColor(R.color.colorPrimary));

                sNotes.setVisibility(View.VISIBLE);
            }
            else {
                nBtn.setTextColor(Color.WHITE);
                nBtn.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

                yBtn.setBackground(getResources().getDrawable(R.drawable.box));
                yBtn.setTextColor(getResources().getColor(R.color.colorPrimary));

                sNotes.setVisibility(View.GONE);
            }
        }

        nBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nBtn.setTextColor(Color.WHITE);
                nBtn.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

                yBtn.setBackground(getResources().getDrawable(R.drawable.box));
                yBtn.setTextColor(getResources().getColor(R.color.colorPrimary));

                sComp = false;
                cSafety = true;
                sNotes.setVisibility(View.GONE);
            }
        });

        yBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                yBtn.setTextColor(Color.WHITE);
                yBtn.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

                nBtn.setBackground(getResources().getDrawable(R.drawable.box));
                nBtn.setTextColor(getResources().getColor(R.color.colorPrimary));

                sComp = true;
                cSafety = true;
                sNotes.setVisibility(View.VISIBLE);
            }
        });

        final EditText mNotes = findViewById(R.id.eMiscNotes);
        cont.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmAddSafetyValues(sNotes, mNotes);
            }
        });
    }

    private void confirmAddSafetyValues(EditText sNotes, EditText mNotes) {
        if(cSafety) {
            final String s = sNotes.getText().toString();
            final String m = mNotes.getText().toString();

            if (s.equals("") && m.equals("")) {
                AlertDialog.Builder builder =
                        new AlertDialog.Builder(DailyReportActivity.this);
                builder.setTitle("No Notes");
                builder.setMessage("Are you sure you want to proceed without adding any notes?");

                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        addSafetyValues(s, m);
                        dialog.dismiss();
                    }
                });

                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                builder.create().show();
            }
            else
                addSafetyValues(s, m);
        }
        else
            selectOneError("YES");
    }

    private void addSafetyValues(String s, String m) {
        values.add(convertedBool(sComp));
        values.add(s);
        values.add(m);

        openReview(false);
    }

    private void loadSigView() {
        openReview(false);
//        layoutValue = "csig";
//        cLayout.setVisibility(View.GONE);
//        cLayout = findViewById(R.id.sigHolder);
//        cLayout.setVisibility(View.VISIBLE);
//
//        final Button nBtn = findViewById(R.id.sigNoBtn);
//        final Button yBtn = findViewById(R.id.sigYesBtn);
//
//        nBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                nBtn.setTextColor(Color.WHITE);
//                nBtn.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
//
//                yBtn.setBackground(getResources().getDrawable(R.drawable.box));
//                yBtn.setTextColor(getResources().getColor(R.color.colorPrimary));
//
//                sig = false;
//                cSig = true;
//            }
//        });
//
//        yBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                yBtn.setTextColor(Color.WHITE);
//                yBtn.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
//
//                nBtn.setBackground(getResources().getDrawable(R.drawable.box));
//                nBtn.setTextColor(getResources().getColor(R.color.colorPrimary));
//
//                sig = true;
//                cSig = true;
//            }
//        });
//
//        cont.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                confirmSignature();
//            }
//        });
    }

    private void confirmSignature() {
        if(cSig) {
           if(sig)
               openReview(false);
           else
               openPolice();
        }
        else
            selectOneError("yes or no");
    }

    private void openReview(boolean upload) {
        if(upload)
            layoutValue = "erev";
        else {
            layoutValue = "irev";
            mContent = findViewById(R.id.canvasLayout);
            mContent.removeAllViews();

            RelativeLayout oLayout = findViewById(R.id.sigView);
            oLayout.setVisibility(View.GONE);
        }

        cLayout.setVisibility(View.GONE);
        cLayout = findViewById(R.id.sigReviewHolder);
        cLayout.setVisibility(View.VISIBLE);

        TextView pNum = findViewById(R.id.reviewPoNum);
        TextView insp = findViewById(R.id.reviewInsp);
        final TextView brid = findViewById(R.id.reviewBrid);
        TextView loca = findViewById(R.id.reviewLoca);
        TextView town = findViewById(R.id.reviewTown);

        EditText pText = findViewById(R.id.poNum);
        EditText iText = findViewById(R.id.eInspector);
        EditText bText = findViewById(R.id.eBridge);
        EditText lText = findViewById(R.id.eLocation);
        EditText tText = findViewById(R.id.eCity);

        String pNumText = "P.O. Number: "+pText.getText().toString();
        String inspText = "Inspector: "+iText.getText().toString();
        String bridText = "Bridge Number: "+bText.getText().toString();
        String locaText = "Location: "+lText.getText().toString();
        String townText = "Town: "+tText.getText().toString();

        pNum.setText(pNumText);
        insp.setText(inspText);
        brid.setText(bridText);
        loca.setText(locaText);
        town.setText(townText);

        Button pItemsBtn = findViewById(R.id.reviewPayItemsBtn);
        pItemsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(DailyReportActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setCancelable(false);
                dialog.setContentView(R.layout.dialog_select_view);

                ArrayList<PayItem> pItems = new ArrayList<>(finalPayItems);

                for(int i=0; i<pItems.size(); i++) {
                    pItems.get(i).setValue(pItems.get(i).getSimpleValue());
                }

                ListView pList = dialog.findViewById(R.id.selectList);
                PayItemAdapter pAdapter = new PayItemAdapter(
                        DailyReportActivity.this, finalPayItems, true);
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
        });

        Button accBtn = findViewById(R.id.reviewAccBtn);
        accBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(DailyReportActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setCancelable(false);
                dialog.setContentView(R.layout.dialog_select_view);

                ListView aList = dialog.findViewById(R.id.selectList);
                ArrayAdapter<String> aAdapter = new ArrayAdapter<>(
                        DailyReportActivity.this,
                        android.R.layout.simple_list_item_1,
                        finalAccomplishments);

                aList.setAdapter(aAdapter);
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

        TextView cBol = findViewById(R.id.reviewCompBool);
        TextView cNot = findViewById(R.id.reviewCompNotes);
        TextView mNot = findViewById(R.id.reviewMiscNotes);

        EditText fText = findViewById(R.id.eSafetyNotes);
        EditText sText = findViewById(R.id.eMiscNotes);

        String cBolText = "Safety Compliance Check: Yes";
        String cNotText = "Notes: "+fText.getText().toString();
        String mNotText = "Visitors/Misc Notes:"+sText.getText().toString();

        cBol.setText(cBolText);
        cNot.setText(cNotText);
        mNot.setText(mNotText);

        if(upload) {
            RelativeLayout nLayout = findViewById(R.id.moreReviewHolder);
            nLayout.setVisibility(View.VISIBLE);

            TextView pType = findViewById(R.id.reviewPolType);
            TextView pArrTime = findViewById(R.id.reviewPolArrTime);
            TextView pEndTime = findViewById(R.id.reviewPolEndTime);

            EditText aText = findViewById(R.id.policeStartTime);
            EditText eText = findViewById(R.id.policeEndTime);

            String ptVal = pol.substring(0,1).toUpperCase()+pol.substring(1);
            String paVal = aText.getText().toString();
            String peVal = eText.getText().toString();

            String ptText = "Police Type: "+ptVal;
            String paText = "Arrival Time: "+paVal;
            String peText = "End Time: "+peVal;

            pType.setText(ptText);
            pArrTime.setText(paText);
            pEndTime.setText(peText);

            Button pNamesBtn = findViewById(R.id.reviewPolNamesBtn);
            if(!pol.equals("none")) {
                pNamesBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final Dialog dialog = new Dialog(DailyReportActivity.this);
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.setCancelable(false);
                        dialog.setContentView(R.layout.dialog_select_view);

                        ListView tList = dialog.findViewById(R.id.selectList);
                        ArrayAdapter<String> tAdapter = new ArrayAdapter<>(
                                DailyReportActivity.this,
                                android.R.layout.simple_list_item_1, finalNames);

                        tList.setAdapter(tAdapter);
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
            }
            else {
                pNamesBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String m = "No police officers on this job site";
                        Toast.makeText(
                                DailyReportActivity.this, m, Toast.LENGTH_LONG).show();
                    }
                });
            }

            Button pTownsBtn = findViewById(R.id.reviewPolTownsBtn);
            if(pol.equals("local")) {
                pTownsBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            final Dialog dialog = new Dialog(DailyReportActivity.this);
                            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            dialog.setCancelable(false);
                            dialog.setContentView(R.layout.dialog_select_view);

                            ListView tList = dialog.findViewById(R.id.selectList);
                            ArrayAdapter<String> tAdapter = new ArrayAdapter<>(
                                    DailyReportActivity.this,
                                    android.R.layout.simple_list_item_1, finalTowns);

                            tList.setAdapter(tAdapter);
                            Button closeBtn = dialog.findViewById(R.id.closeDialog);
                            closeBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    dialog.dismiss();
                                }
                            });

                            dialog.show();
                        } catch (Exception e) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(DailyReportActivity.this);
                            builder.setTitle("No Towns");
                            builder.setMessage("You have no selected any towns");

                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
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
            else {
                pTownsBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String m = "This is only for local police.";
                        Toast.makeText(
                                DailyReportActivity.this, m, Toast.LENGTH_LONG).show();
                    }
                });
            }

            TextView fNum = findViewById(R.id.reviewFlagNum);
            TextView faTime = findViewById(R.id.reviewFlagArrTime);
            TextView feTime = findViewById(R.id.reviewFlagEndTime);

            EditText fatText = findViewById(R.id.flaggerStartTime);
            EditText fetText = findViewById(R.id.flaggerEndTime);
            EditText fanText = findViewById(R.id.flaggerNumber);

            String faText = "Arrival Time: "+fatText.getText().toString();
            String feText = "End Time: "+fetText.getText().toString();
            String fnText = "Number: "+fanText.getText().toString();

            fNum.setText(fnText);
            faTime.setText(faText);
            feTime.setText(feText);

            EditText tst = findViewById(R.id.trafficStartTime);
            EditText tet = findViewById(R.id.trafficEndTime);
            EditText pst = findViewById(R.id.prodStartTime);
            EditText ped = findViewById(R.id.prodEndTime);
            EditText ves = findViewById(R.id.vehicleStorage);

            TextView tsText = findViewById(R.id.reviewTrafArrTime);
            TextView teText = findViewById(R.id.reviewTrafEndTime);
            TextView psText = findViewById(R.id.reviewProdStaTime);
            TextView prText = findViewById(R.id.reviewProdEndTime);
            TextView veText = findViewById(R.id.reviewVehStorage);

            String ts = "Traffic Arrival Time: "+tst.getText().toString();
            String te = "Traffic End Time: "+tet.getText().toString();
            String ps = "Production Start Time: "+pst.getText().toString();
            String pr = "Production End Time: "+ped.getText().toString();
            String ve = "Vehicle Storage: "+ves.getText().toString();

            tsText.setText(ts);
            teText.setText(te);
            psText.setText(ps);
            prText.setText(pr);
            veText.setText(ve);

            String contText = "Upload & Finish";
            cont.setText(contText);
            cont.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    uploadData();
                }
            });
        }
        else {
            String contText = "Sign >";
            cont.setText(contText);
            cont.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openSignature();
                }
            });
        }
    }

    private void openSignature() {
        layoutValue = "psig";
        cLayout.setVisibility(View.GONE);
        cLayout = findViewById(R.id.sigView);
        cLayout.setVisibility(View.VISIBLE);

        RelativeLayout oLayout = findViewById(R.id.policeHolder);
        oLayout.setVisibility(View.GONE);

        mContent = findViewById(R.id.canvasLayout);
        mSignature = new signature(getApplicationContext(), null);
        mSignature.setBackgroundColor(Color.WHITE);
        mContent.addView(mSignature, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        cont.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cont.setText("Continue >");
                finalSignature = mSignature.save();
                createPDF();
            }
        });
    }

    public class signature extends View {

        private static final float STROKE_WIDTH = 5f;
        private static final float HALF_STROKE_WIDTH = STROKE_WIDTH / 2;
        private Paint paint = new Paint();
        private Path path = new Path();

        private float lastTouchX;
        private float lastTouchY;
        private final RectF dirtyRect = new RectF();

        public signature(Context context, AttributeSet attrs) {
            super(context, attrs);
            paint.setAntiAlias(true);
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeWidth(STROKE_WIDTH);
        }

        public String save() {
            Log.i("AHHH", "HERE");
            if (sigImage == null) {
                //sigImage = Bitmap.createBitmap(mContent.getWidth(), mContent.getHeight(), Bitmap.Config.RGB_565);
                sigImage = mSignature.getBitmap();
            }
            if(mContent == null) {
                Log.i("AHHH", "mContent was null");
            }
            else {
                Log.i("AHHH", mContent.getWidth()+"");
                Log.i("AHHH", mContent.getHeight()+"");
            }
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            sigImage.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream .toByteArray();
            String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
//            Log.i("AHHH",encoded);
            return encoded;
//            Canvas canvas = new Canvas(sigImage);
//            try {
//                // Output the file
////                FileOutputStream mFileOutStream = new FileOutputStream(StoredPath);
////                v.draw(canvas);
//
//
//
////                bitmap.compress(Bitmap.CompressFormat.PNG, 90, mFileOutStream);
//
////                mFileOutStream.flush();
////                mFileOutStream.close();
//
//            } catch (Exception e) {
//                Log.v("log_tag", e.toString());
//            }

        }

        public void clear() {
            path.reset();
            invalidate();
            cont.setEnabled(false);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawPath(path, paint);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float eventX = event.getX();
            float eventY = event.getY();
            cont.setEnabled(true);

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    path.moveTo(eventX, eventY);
                    lastTouchX = eventX;
                    lastTouchY = eventY;
                    return true;

                case MotionEvent.ACTION_MOVE:

                case MotionEvent.ACTION_UP:

                    resetDirtyRect(eventX, eventY);
                    int historySize = event.getHistorySize();
                    for (int i = 0; i < historySize; i++) {
                        float historicalX = event.getHistoricalX(i);
                        float historicalY = event.getHistoricalY(i);
                        expandDirtyRect(historicalX, historicalY);
                        path.lineTo(historicalX, historicalY);
                    }
                    path.lineTo(eventX, eventY);
                    break;

                default:
                    debug("Ignored touch event: " + event.toString());
                    return false;
            }

            invalidate((int) (dirtyRect.left - HALF_STROKE_WIDTH),
                    (int) (dirtyRect.top - HALF_STROKE_WIDTH),
                    (int) (dirtyRect.right + HALF_STROKE_WIDTH),
                    (int) (dirtyRect.bottom + HALF_STROKE_WIDTH));

            lastTouchX = eventX;
            lastTouchY = eventY;

            return true;
        }

        private void debug(String string) {

            Log.v("log_tag", string);

        }

        private void expandDirtyRect(float historicalX, float historicalY) {
            if (historicalX < dirtyRect.left) {
                dirtyRect.left = historicalX;
            } else if (historicalX > dirtyRect.right) {
                dirtyRect.right = historicalX;
            }

            if (historicalY < dirtyRect.top) {
                dirtyRect.top = historicalY;
            } else if (historicalY > dirtyRect.bottom) {
                dirtyRect.bottom = historicalY;
            }
        }

        private void resetDirtyRect(float eventX, float eventY) {
            dirtyRect.left = Math.min(lastTouchX, eventX);
            dirtyRect.right = Math.max(lastTouchX, eventX);
            dirtyRect.top = Math.min(lastTouchY, eventY);
            dirtyRect.bottom = Math.max(lastTouchY, eventY);
        }

        public Bitmap getBitmap() {
            View v = (View) this.getParent();
            Bitmap b = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(b);
            v.layout(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
            v.draw(c);

            return b;
        }
    }

    private void openPolice() {
        layoutValue = "pol1";
        cLayout.setVisibility(View.GONE);
        cLayout = findViewById(R.id.policeHolder);
        cLayout.setVisibility(View.VISIBLE);

        RelativeLayout oLayout1 = findViewById(R.id.townHolder);
        oLayout1.setVisibility(View.GONE);
        RelativeLayout oLayout2 = findViewById(R.id.flaggerHolder);
        oLayout2.setVisibility(View.GONE);

        final Button no = findViewById(R.id.policeNoneBtn);
        final Button st = findViewById(R.id.policeStateBtn);
        final Button lo = findViewById(R.id.policeLocalBtn);

        final EditText s = findViewById(R.id.policeStartTime);
        final EditText e = findViewById(R.id.policeEndTime);

        final ListView nameList = findViewById(R.id.policeNameList);
        final Button addName = findViewById(R.id.policeNameBtn);

        final ArrayList<String> sNames = new ArrayList<>();
        final ArrayAdapter<String> ad = new ArrayAdapter<>(
                DailyReportActivity.this, android.R.layout.simple_list_item_1, sNames);
        nameList.setAdapter(ad);

        for(int i=0; i<savedNames.size(); i++) {
            sNames.add(savedNames.get(i));
            savedNames.remove(i);
        }

        try {
            for (int j = 0; j < finalNames.size(); j++) {
                sNames.add(finalNames.get(j));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        finalNames = new ArrayList<>();

        if(pol.equals("state")) {
            st.setTextColor(Color.WHITE);
            st.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

            no.setBackground(getResources().getDrawable(R.drawable.box));
            no.setTextColor(getResources().getColor(R.color.colorPrimary));

            lo.setBackground(getResources().getDrawable(R.drawable.box));
            lo.setTextColor(getResources().getColor(R.color.colorPrimary));
        }
        else if(pol.equals("local")) {
            lo.setTextColor(Color.WHITE);
            lo.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

            st.setBackground(getResources().getDrawable(R.drawable.box));
            st.setTextColor(getResources().getColor(R.color.colorPrimary));

            no.setBackground(getResources().getDrawable(R.drawable.box));
            no.setTextColor(getResources().getColor(R.color.colorPrimary));
        }
        else if(pol.equals("none")) {
            no.setTextColor(Color.WHITE);
            no.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

            st.setBackground(getResources().getDrawable(R.drawable.box));
            st.setTextColor(getResources().getColor(R.color.colorPrimary));

            lo.setBackground(getResources().getDrawable(R.drawable.box));
            lo.setTextColor(getResources().getColor(R.color.colorPrimary));
        }

        addName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(DailyReportActivity.this);
                builder.setTitle("Add Officer Last Name");
                builder.setMessage("Please enter the last name of a police officer that was on site");

                final EditText input = new EditText(DailyReportActivity.this);
                input.setInputType(1);
                builder.setView(input);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        sNames.add(input.getText().toString());
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
        });

        nameList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(DailyReportActivity.this);
                builder.setTitle("Confirm");
                builder.setMessage("Are you sure you want to delete this name?");

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sNames.remove(position);
                        ad.notifyDataSetChanged();
                        dialog.dismiss();
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
        });

        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                no.setTextColor(Color.WHITE);
                no.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

                st.setBackground(getResources().getDrawable(R.drawable.box));
                st.setTextColor(getResources().getColor(R.color.colorPrimary));

                lo.setBackground(getResources().getDrawable(R.drawable.box));
                lo.setTextColor(getResources().getColor(R.color.colorPrimary));
                pol = "none";

                setEditText(s, true);
                setEditText(e, true);
            }
        });

        st.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                st.setTextColor(Color.WHITE);
                st.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

                no.setBackground(getResources().getDrawable(R.drawable.box));
                no.setTextColor(getResources().getColor(R.color.colorPrimary));

                lo.setBackground(getResources().getDrawable(R.drawable.box));
                lo.setTextColor(getResources().getColor(R.color.colorPrimary));
                pol = "state";

                setEditText(s, false);
                setEditText(e, false);
            }
        });

        lo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lo.setTextColor(Color.WHITE);
                lo.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

                st.setBackground(getResources().getDrawable(R.drawable.box));
                st.setTextColor(getResources().getColor(R.color.colorPrimary));

                no.setBackground(getResources().getDrawable(R.drawable.box));
                no.setTextColor(getResources().getColor(R.color.colorPrimary));
                pol = "local";

                setEditText(s, false);
                setEditText(e, false);
            }
        });

        cont.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPoliceValues(sNames, s, e);
            }
        });
    }

    private void setEditText(final EditText e, boolean clear) {
        if(clear) {
            e.setText("N/A");
            e.setOnClickListener(null);
        }
        else
            e.setText("");

    }

    private void addPoliceValues(ArrayList<String> sNames, EditText s, EditText e) {
        if(pol.equals(""))
            selectOneError("state, local, or none");
        else {
            String sTime = s.getText().toString();
            String eTime = e.getText().toString();

            if(sTime.equals("") || eTime.equals(""))
                enterAllValuesError();
            else if(!pol.equals("none") && sNames.size() == 0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(DailyReportActivity.this);
                builder.setTitle("Must Add Names");
                builder.setMessage("If you had police on the job site, " +
                        "you must specify the last names of the officers. If you did not" +
                        "have police officers on site, please select \"None\"");

                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                builder.create().show();
            }
            else {
                values.add(pol);
                values.add(sTime);
                values.add(eTime);

                finalNames = new ArrayList<>(sNames);

                if(pol.equals("local"))
                    openPoliceTownView();
                else {
                    policeTowns = false;
                    openFlaggerView();
                }
            }
        }
    }

    private void openPoliceTownView() {
        layoutValue = "pol2";
        policeTowns = true;

        cLayout.setVisibility(View.GONE);
        cLayout = findViewById(R.id.townHolder);
        cLayout.setVisibility(View.VISIBLE);

        RelativeLayout oLayout = findViewById(R.id.flaggerHolder);
        oLayout.setVisibility(View.GONE);

        final List<String> towns = Arrays.asList(getResources().getStringArray(R.array.towns));
        final ArrayList<String> sTowns = new ArrayList<>();

        ListView townList = findViewById(R.id.policeTownList);
        final ArrayAdapter<String> ad = new ArrayAdapter<>(
                DailyReportActivity.this, android.R.layout.simple_list_item_1, sTowns);
        townList.setAdapter(ad);

        for(int i=0; i<savedTowns.size(); i++) {
            sTowns.add(savedTowns.get(i));
            savedTowns.remove(i);
        }

        try {
            for (int j = 0; j < finalTowns.size(); j++) {
                sTowns.add(finalTowns.get(j));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        finalTowns = new ArrayList<>();

        Button addTown = findViewById(R.id.policeTownBtn);
        addTown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(DailyReportActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setCancelable(false);
                dialog.setContentView(R.layout.dialog_select_view);

                ListView tList = dialog.findViewById(R.id.selectList);
                ArrayAdapter<String> tAdapter = new ArrayAdapter<>(
                        DailyReportActivity.this, android.R.layout.simple_list_item_1, towns);
                tList.setAdapter(tAdapter);

                tList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String town = towns.get(position);
                        sTowns.add(town);
                        ad.notifyDataSetChanged();

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

        townList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(DailyReportActivity.this);
                builder.setTitle("Confirm");
                builder.setMessage("Are you sure you want to remove this town?");

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sTowns.remove(position);
                        ad.notifyDataSetChanged();
                        dialog.dismiss();
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
        });

        cont.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sTowns.size() == 0) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(DailyReportActivity.this);
                    builder.setTitle("Error");
                    builder.setMessage("Please select at least one town before advancing");

                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

                    builder.create().show();
                }
                else {
                    finalTowns = sTowns;
                    openFlaggerView();
                }
            }
        });
    }

    private void openFlaggerView() {
        layoutValue = "flag";
        cLayout.setVisibility(View.GONE);
        cLayout = findViewById(R.id.flaggerHolder);
        cLayout.setVisibility(View.VISIBLE);

        RelativeLayout oLayout = findViewById(R.id.miscHolder);
        oLayout.setVisibility(View.GONE);

        final EditText s = findViewById(R.id.flaggerStartTime);
        final EditText e = findViewById(R.id.flaggerEndTime);
        final EditText n = findViewById(R.id.flaggerNumber);

        final CheckBox noFlaggers = findViewById(R.id.noFlaggerCheck);
        noFlaggers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(noFlaggers.isChecked()) {
                    s.setText("N/A");
                    e.setText("N/A");
                    n.setText("N/A");
                }
                else {
                    s.setText("");
                    e.setText("");
                    n.setText("");
                }
            }
        });

        cont.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addFlaggerValues(s, e, n);
            }
        });
    }

    private void addFlaggerValues(EditText s, EditText e, EditText n) {
        String sFlag = s.getText().toString();
        String eFlag = e.getText().toString();
        String nFlag = n.getText().toString();

        if(sFlag.equals("") || eFlag.equals("") || nFlag.equals(""))
            enterAllValuesError();
        else {
            values.add(sFlag);
            values.add(eFlag);
            values.add(nFlag);

            loadMiscView();
        }
    }

    private void loadMiscView() {
        layoutValue = "misc";
        cLayout.setVisibility(View.GONE);
        cLayout = findViewById(R.id.miscHolder);
        cLayout.setVisibility(View.VISIBLE);

        RelativeLayout oLayout = findViewById(R.id.imageHolder);
        oLayout.setVisibility(View.GONE);

        String nText = "Continue >";
        cont.setText(nText);

        final EditText tStart = findViewById(R.id.trafficStartTime);
        final EditText tEnd = findViewById(R.id.trafficEndTime);
        final EditText pStart = findViewById(R.id.prodStartTime);
        final EditText pEnd = findViewById(R.id.prodEndTime);
        final EditText vStorage = findViewById(R.id.vehicleStorage);

        cont.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addMiscViews(tStart, tEnd, pStart, pEnd, vStorage);
            }
        });
    }

    private void addMiscViews(EditText ts, EditText te, EditText ps, EditText pe, EditText vs) {
        String tsTime = ts.getText().toString();
        String teTime = te.getText().toString();
        String psTime = ps.getText().toString();
        String peTime = pe.getText().toString();
        String vsText = vs.getText().toString();

        if(tsTime.equals("") || teTime.equals("") ||
                psTime.equals("") || peTime.equals("") || vsText.equals(""))
            enterAllValuesError();
        else {
            values.add(tsTime);
            values.add(teTime);
            values.add(psTime);
            values.add(peTime);
            values.add(vsText);

            addImages();
        }
    }

    private void addImages() {
        layoutValue = "imag";
        cLayout.setVisibility(View.GONE);
        cLayout = findViewById(R.id.imageHolder);
        cLayout.setVisibility(View.VISIBLE);

        RelativeLayout oLayout = findViewById(R.id.sigReviewHolder);
        oLayout.setVisibility(View.GONE);
        cont.setText("Continue >");

        cImages = finalImages;
        fileNames = new ArrayList<>();

        for(int i=0; i<cImages.size(); i++) {
            fileNames.add(cImages.get(i).getName());
        }

        ListView imageList = findViewById(R.id.imageList);
        imageAdapter = new ArrayAdapter<>(
                DailyReportActivity.this, android.R.layout.simple_list_item_1, fileNames);
        imageList.setAdapter(imageAdapter);

        Button add = findViewById(R.id.imageBtn);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(DailyReportActivity.this);
                builder.setTitle("Select");
                builder.setMessage("How would you like to add this image?");

                builder.setPositiveButton("Gallery", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        cont.setVisibility(View.GONE);
                        findViewById(R.id.saveBtn).setVisibility(View.GONE);
                        findViewById(R.id.imageHolder).setVisibility(View.GONE);
                        pBar.setVisibility(View.VISIBLE);

                        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED) {
                            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                    STORAGE_REQUEST);
                        }
                        else {
                            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                            photoPickerIntent.setType("image/*");
                            startActivityForResult(photoPickerIntent, RESULT_LOAD_IMG);
                        }

                    }
                });

                builder.setNeutralButton("Camera", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (checkSelfPermission(Manifest.permission.CAMERA)
                                != PackageManager.PERMISSION_GRANTED) {
                            requestPermissions(new String[]{Manifest.permission.CAMERA},
                                    CAMERA_REQUEST);
                        }
                        else {
                            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(cameraIntent, CAMERA_REQUEST);
                        }
                    }
                });

                builder.create().show();
            }
        });

        imageList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                ReportImage sImage = cImages.get(position);
                String name = sImage.getName();
                String desc = sImage.getDesc();

                String code = sImage.getFile();
                byte[] decodedString = Base64.decode(code, Base64.DEFAULT);
                final Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                final Dialog dialog = new Dialog(DailyReportActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setCancelable(false);
                dialog.setContentView(R.layout.dialog_image);

                EditText eName = dialog.findViewById(R.id.imageName);
                EditText eDesc = dialog.findViewById(R.id.imageDesc);

                eName.setText(name);
                eDesc.setText(desc);

                eName.setFocusable(false);
                eDesc.setFocusable(false);

                ImageView imageView = dialog.findViewById(R.id.viewCapturedImage);
                imageView.setImageBitmap(bitmap);

                Button add = dialog.findViewById(R.id.addImageBtn);
                Button cancel = dialog.findViewById(R.id.cancelImageBtn);

                add.setText("Okay");
                cancel.setText("Delete");
                cancel.setBackgroundColor(getResources().getColor(R.color.colorAccent));

                add.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(DailyReportActivity.this);
                        builder.setTitle("Confirm");
                        builder.setMessage("Are you sure you want to delete this image?");

                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface iDialog, int which) {
                                fileNames.remove(position);
                                cImages.remove(position);
                                imageAdapter.notifyDataSetChanged();

                                iDialog.dismiss();
                                dialog.dismiss();
                            }
                        });

                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface iDialog, int which) {
                                iDialog.dismiss();
                                dialog.dismiss();
                            }
                        });

                        builder.create().show();
                    }
                });

                dialog.show();
            }
        });

        cont.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finalImages = cImages;
                cImages = new ArrayList<>();
                openReview(true);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            } else {
                Toast.makeText(this, "Camera Permission Denied", Toast.LENGTH_LONG).show();
            }
        }
        else if(requestCode == WRITE_REQUEST_CODE) {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                createPDF();
            }
            else{
                Toast.makeText(DailyReportActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                finishAndClose();
            }
        }
        else {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, RESULT_LOAD_IMG);
            } else {
                cont.setVisibility(View.VISIBLE);
                findViewById(R.id.saveBtn).setVisibility(View.VISIBLE);
                findViewById(R.id.imageHolder).setVisibility(View.VISIBLE);
                pBar.setVisibility(View.GONE);
                Toast.makeText(this, "Storage Permission Denied", Toast.LENGTH_LONG).show();
            }
        }
    }


    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        cont.setVisibility(View.VISIBLE);
        findViewById(R.id.saveBtn).setVisibility(View.VISIBLE);
        findViewById(R.id.imageHolder).setVisibility(View.VISIBLE);
        pBar.setVisibility(View.GONE);

        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);

            byte[] byteArray = byteArrayOutputStream .toByteArray();
            final String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);

            final Dialog dialog = new Dialog(DailyReportActivity.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(false);
            dialog.setContentView(R.layout.dialog_image);

            ImageView imageView = dialog.findViewById(R.id.viewCapturedImage);
            imageView.setImageBitmap(bitmap);

            Button add = dialog.findViewById(R.id.addImageBtn);
            Button cancel = dialog.findViewById(R.id.cancelImageBtn);

            add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EditText eName = dialog.findViewById(R.id.imageName);
                    EditText eDesc = dialog.findViewById(R.id.imageDesc);

                    String name = eName.getText().toString();
                    String desc = eDesc.getText().toString();

                    if(name.equals("")) {
                        AlertDialog.Builder builder =
                                new AlertDialog.Builder(DailyReportActivity.this);
                        builder.setTitle("Error");
                        builder.setMessage("Please enter image name before proceeding.");

                        builder.setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface iDialog, int which) {
                                iDialog.dismiss();
                            }
                        });

                        builder.create().show();
                    }

                    ReportImage image = new ReportImage(encoded);
                    image.setName(name);
                    image.setDesc(desc);

                    cImages.add(image);
                    fileNames.add(name);
                    imageAdapter.notifyDataSetChanged();

                    dialog.dismiss();
                }
            });

            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            dialog.show();
        }
        else if (requestCode == RESULT_LOAD_IMG && resultCode == Activity.RESULT_OK) {
            try {
                final Uri imageUri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap bitmap = BitmapFactory.decodeStream(imageStream);

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);

                byte[] byteArray = byteArrayOutputStream.toByteArray();
                final String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);

                final Dialog dialog = new Dialog(DailyReportActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setCancelable(false);
                dialog.setContentView(R.layout.dialog_image);

                ImageView imageView = dialog.findViewById(R.id.viewCapturedImage);
                imageView.setImageBitmap(bitmap);

                Button add = dialog.findViewById(R.id.addImageBtn);
                Button cancel = dialog.findViewById(R.id.cancelImageBtn);

                add.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EditText eName = dialog.findViewById(R.id.imageName);
                        EditText eDesc = dialog.findViewById(R.id.imageDesc);

                        String name = eName.getText().toString();
                        String desc = eDesc.getText().toString();

                        if (name.equals("")) {
                            AlertDialog.Builder builder =
                                    new AlertDialog.Builder(DailyReportActivity.this);
                            builder.setTitle("Error");
                            builder.setMessage("Please enter image name before proceeding.");

                            builder.setPositiveButton("OK",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface iDialog, int which) {
                                            iDialog.dismiss();
                                        }
                                    });

                            builder.create().show();
                        }

                        ReportImage image = new ReportImage(encoded);
                        image.setName(name);
                        image.setDesc(desc);

                        cImages.add(image);
                        fileNames.add(name);
                        imageAdapter.notifyDataSetChanged();

                        dialog.dismiss();
                    }
                });

                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                dialog.show();
            } catch (Exception e) {
                Toast.makeText(DailyReportActivity.this,
                        "Error loading iamge...", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void uploadData() {
        cLayout.setVisibility(View.GONE);
        pBar.setVisibility(View.VISIBLE);
        String pNum = values.get(0);

//        String vCheck = pNum+"\n"+insp+"\n"+bNum+"\n"+loca+"\n"+town+"\n"+safe+"\n"+fNot+"\n"+sNot+"\n"+pTyp+"\n"+pArr+"\n"+pEnd+"\n"+fArr+"\n"+fEnd+"\n"+fNum+"\n"+tArr+"\n"+tEnd+"\n"+rArr+"\n"+rEnd+"\n"+sVeh;
//        Log.i("AHHH", vCheck);
        Bundle extras = getIntent().getExtras();
        String uid = FirebaseAuth.getInstance().getUid();
        String name = extras.getString(
                "fName", "") +" " +extras.getString("lName", "");

        DatabaseReference fRef = FirebaseDatabase.getInstance().getReference("Contracts");
        final DatabaseReference ref = fRef.child("16PSX0176")
                .child("poNums").child(pNum).child("reports").child(date).child(time).child(uid);
        ref.child("foremanName").setValue(name).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                uploadFirstPage(ref);
            }
        });
    }

    private void uploadFirstPage(final DatabaseReference ref) {
        EditText iText = findViewById(R.id.eInspector);
        EditText bText = findViewById(R.id.eBridge);
        EditText lText = findViewById(R.id.eLocation);
        EditText tText = findViewById(R.id.eCity);

        String insp = iText.getText().toString();
        final String bNum = bText.getText().toString();
        final String loca = lText.getText().toString();
        final String town = tText.getText().toString();

        ref.child("inspector").setValue(insp).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                ref.child("bridgeNumber").setValue(bNum).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        ref.child("location").setValue(loca).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                ref.child("town").setValue(town).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        uploadSafetyValues(ref);
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });
    }

    private void uploadSafetyValues(final DatabaseReference ref) {
        EditText fText = findViewById(R.id.eSafetyNotes);
        EditText sText = findViewById(R.id.eMiscNotes);

        String safe = "Yes";
        final String fNot = fText.getText().toString();
        final String sNot = sText.getText().toString();

        ref.child("safetyCheck").setValue(safe).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                ref.child("safetyNotes").setValue(fNot).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        ref.child("visitorMiscNotes").setValue(sNot).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                uploadPoliceValues(ref);
                            }
                        });
                    }
                });
            }
        });
    }

    private void uploadPoliceValues(final DatabaseReference ref) {
        EditText aText = findViewById(R.id.policeStartTime);
        EditText eText = findViewById(R.id.policeEndTime);

        String pTyp = pol;
        final String pArr = aText.getText().toString();
        final String pEnd = eText.getText().toString();

        pTyp = pTyp.substring(0, 1).toUpperCase() + pTyp.substring(1);
        ref.child("policeType").setValue(pTyp).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                ref.child("policeStart").setValue(pArr).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        ref.child("policeEnd").setValue(pEnd).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                uploadFlaggerValues(ref);
                            }
                        });
                    }
                });
            }
        });
    }

    private void uploadFlaggerValues(final DatabaseReference ref) {
        EditText aText = findViewById(R.id.flaggerStartTime);
        EditText eText = findViewById(R.id.flaggerEndTime);
        EditText nText = findViewById(R.id.flaggerNumber);

        String fArr = aText.getText().toString();
        final String fEnd = eText.getText().toString();
        final String fNum = nText.getText().toString();

        ref.child("flaggerStart").setValue(fArr).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                ref.child("flaggerEnd").setValue(fEnd).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        ref.child("flaggerNumber").setValue(fNum).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                uploadMiscValues(ref);
                            }
                        });
                    }
                });
            }
        });
    }

    private void uploadMiscValues(final DatabaseReference ref) {
        EditText taText = findViewById(R.id.trafficStartTime);
        EditText teText = findViewById(R.id.trafficEndTime);
        EditText paText = findViewById(R.id.prodStartTime);
        EditText peText = findViewById(R.id.prodEndTime);
        EditText vsText = findViewById(R.id.vehicleStorage);

        String tArr = taText.getText().toString();
        final String tEnd = teText.getText().toString();
        final String rArr = paText.getText().toString();
        final String rEnd = peText.getText().toString();
        final String sVeh = vsText.getText().toString();

        ref.child("trafficStart").setValue(tArr).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                ref.child("trafficEnd").setValue(tEnd).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        ref.child("productionStart").setValue(rArr).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                ref.child("productionEnd").setValue(rEnd).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        ref.child("vehicleStorage").setValue(sVeh).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                uploadNextAccomplishment(0, ref);
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });
    }

    private void uploadNextAccomplishment(final int i, final DatabaseReference ref) {
        if(i == finalAccomplishments.size())
            uploadPayItem(0 , ref);
        else {
            String si = i+"";
            String acc = finalAccomplishments.get(i);
            DatabaseReference nRef = ref.child("accomplishments").child(si).child("accomplishment");

            nRef.setValue(acc).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    int newI = i+1;
                    uploadNextAccomplishment(newI, ref);
                }
            });
        }
    }

    private void uploadPayItem(final int i, final DatabaseReference ref) {
        final PayItem item = finalPayItems.get(i);
        final DatabaseReference nRef = ref.child("payItems").child(item.getCode());

        nRef.child("name").setValue(item.getName()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                nRef.child("unit").setValue(item.getUnit()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String val = item.getValue();
                        String[] vSplit = val.split("~");

                        if(vSplit.length == 2)
                            val = vSplit[1];

                        nRef.child("value").setValue(val).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                int j = i+1;

                                if(j == finalPayItems.size())
                                    uploadPoliceName(ref, 0);
                                else
                                    uploadPayItem(j, ref);
                            }
                        });
                    }
                });
            }
        });
    }

    private void uploadPoliceName(final DatabaseReference ref, final int i) {
        if(i == finalNames.size())
            uploadPoliceTown(ref, 0);
        else {
            ref.child("policeNames").child(i+"").setValue(finalNames.get(i)).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    uploadPoliceName(ref, i+1);
                }
            });
        }
    }

    private void uploadPoliceTown(final DatabaseReference ref, final int i) {
        try {
            if (i == finalTowns.size())
                uploadImage(ref, 0);
            else {
                ref.child("policeTowns").child(i + "").setValue(finalTowns.get(i)).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        uploadPoliceTown(ref, i + 1);
                    }
                });
            }
        } catch (Exception e) {
            uploadImage(ref, 0);
        }
    }

    private void uploadImage(final DatabaseReference ref, final int i) {
        if(i == finalImages.size())
            uploadSignature(ref);
        else {
            final ReportImage im = finalImages.get(i);
            ref.child("images").child(i+"").child("name").setValue(im.getName()).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    ref.child("images").child(i+"").child("desc").setValue(im.getDesc()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            ref.child("images").child(i+"").child("file").setValue(im.getFile()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    uploadImage(ref, i+1);
                                }
                            });
                        }
                    });
                }
            });
        }
    }

    private void uploadSignature(final DatabaseReference ref) {
        ref.child("signature").setValue(finalSignature).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                updateReportField();
            }
        });
    }

    private void updateReportField() {
        String uid = FirebaseAuth.getInstance().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref = ref.child(uid).child("crews").child(getMondayDate()).child(date).child(time);

        ref.child("report").setValue("true").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                finishAndClose();
            }
        });
    }

    private void createPDF() {
//        String extstoragedir = Environment.getExternalStorageDirectory().toString();
//        File fol = new File(extstoragedir, "pdf");
//        File folder=new File(fol,"pdf");
//        if(!folder.exists()) {
//            boolean bool = folder.mkdir();
//        }
        String path = Environment.getExternalStorageDirectory() + "/";
// Create the parent path
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String fullName = path + "sample.pdf";
        File file = new File (fullName);

        try {
//            final File file = new File(folder, "sample.pdf");
//            file.createNewFile();
            FileOutputStream fOut = new FileOutputStream(file);


            PdfDocument document = new PdfDocument();
            Bitmap background = BitmapFactory.decodeResource(
                    DailyReportActivity.this.getResources(), R.drawable.daily_report);

            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(
                    background.getWidth(), background.getHeight(), 1).create();
            PdfDocument.Page page = document.startPage(pageInfo);
            Canvas canvas = page.getCanvas();

            Paint paint = new Paint();
            paint.setColor(Color.parseColor("#ffffff"));
            canvas.drawPaint(paint);

            background = Bitmap.createScaledBitmap(
                    background, background.getWidth(), background.getHeight(), true);
            paint.setColor(Color.BLUE);
            canvas.drawBitmap(background, 0, 0 , null);

            EditText pText = findViewById(R.id.poNum);
            EditText dText = findViewById(R.id.repDate);
            EditText tText = findViewById(R.id.repTime);
            EditText iText = findViewById(R.id.eInspector);
            EditText bText = findViewById(R.id.eBridge);
            EditText lText = findViewById(R.id.eLocation);
            EditText cText = findViewById(R.id.eCity);
            EditText sText = findViewById(R.id.eSafetyNotes);
            EditText vText = findViewById(R.id.eMiscNotes);

            String p = pText.getText().toString();
            String t = tText.getText().toString();
            String i = iText.getText().toString();
            String b = bText.getText().toString();
            String l = lText.getText().toString();
            String c = cText.getText().toString();
            String d1 = dText.getText().toString();
            String d2 = getWeekEndingDate(d1);
            String w = getTodaysDay(d1);
            String n = getIntent()
                    .getStringExtra("fName")+" "+getIntent().getStringExtra("lName");
            String s = sText.getText().toString();
            String v = vText.getText().toString();

            paint.setColor(Color.BLACK);
            TextPaint tp = new TextPaint();
            tp.set(paint);
            tp.setTextSize(32);

            canvas.drawText(p, convertWidth(background,63), convertHeight(background, 69), tp);
            canvas.drawText(i, convertWidth(background,265), convertHeight(background, 69), tp);
            canvas.drawText(d2, convertWidth(background,500), convertHeight(background, 69), tp);
            canvas.drawText(b, convertWidth(background,82), convertHeight(background, 100), tp);
            canvas.drawText(l, convertWidth(background,200), convertHeight(background, 100), tp);
            canvas.drawText(c, convertWidth(background,435), convertHeight(background, 100), tp);
            canvas.drawText(n, convertWidth(background,225), convertHeight(background, 125), tp);
            canvas.drawText(d1, convertWidth(background,390), convertHeight(background, 153), tp);

            tp.setTextSize(24);
            int newLineIndex = 60;
            ArrayList<String> sNotesLines = new ArrayList<>();

            while(newLineIndex < s.length()) {
                int in;

                if(newLineIndex > s.length())
                    sNotesLines.add(s);
                else {
                    in = s.substring(0, newLineIndex).lastIndexOf(" ");
                    sNotesLines.add(s.substring(0, in));
                    s = s.substring(in+1);
                }
            }

            newLineIndex = 60;
            ArrayList<String> vNotesLines = new ArrayList<>();

            while(newLineIndex < v.length()) {
                int in;

                if(newLineIndex > v.length())
                    vNotesLines.add(v);
                else {
                    in = v.substring(0, newLineIndex).lastIndexOf(" ");
                    vNotesLines.add(v.substring(0, in));
                    v = v.substring(in+1);
                }
            }

            int nWidth = convertWidth(background, 335);
            int nHeight = convertHeight(background, 295);

            for(int jj=0; jj<sNotesLines.size(); jj++) {
                canvas.drawText(sNotesLines.get(jj), nWidth, nHeight, tp);
                nHeight += convertHeight(background, 8);
            }

            nHeight = convertHeight(background, 640);

            for(int kk=0; kk<vNotesLines.size(); kk++) {
                canvas.drawText(vNotesLines.get(kk), nWidth, nHeight, tp);
                nHeight += convertHeight(background, 8);
            }
//
//            canvas.drawText(s, convertWidth(background, 335), convertHeight(background, 295), tp);
//            canvas.drawText(v, convertWidth(background, 335), convertHeight(background, 640), tp);

//            tp.setTextSize(24);
            int width1 = convertWidth(background, 35);
            int width2 = convertWidth(background, 250);
            int width3 = convertWidth(background, 280);
            int height = convertHeight(background, 172);

            for(int j=0; j<finalPayItems.size(); j++) {
                PayItem pItem = finalPayItems.get(j);
                String mainText = pItem.getCode()+" - "+pItem.getName();

                canvas.drawText(mainText, width1, height, tp);
                canvas.drawText(pItem.getUnit(), width2, height, tp);
                canvas.drawText(pItem.getSimpleValue(), width3, height, tp);

                height += convertHeight(background, 15);
            }

            width1 = convertWidth(background, 330);
            height = convertHeight(background, 380);

            for(int k=0; k<finalAccomplishments.size(); k++) {
                String acc = finalAccomplishments.get(k);
                canvas.drawText(acc, width1, height, tp);
                height += convertHeight(background, 13);
            }

            w += " "+t.substring(0, 1).toUpperCase()+t.substring(1);
            canvas.drawText(w, convertWidth(background,420), convertHeight(background, 182), tp);

            paint.setStyle(Paint.Style.STROKE);
            canvas.drawCircle(convertWidth(background,339),
                    convertHeight(background,252),convertWidth(background,11), paint);

            paint.setStyle(Paint.Style.FILL);
            Bitmap sig = Bitmap.createScaledBitmap(sigImage,
                    convertWidth(background, 145), convertHeight(background, 35), true);
            Rect r = new Rect(convertWidth(background, 425),
                    convertHeight(background, 705),
                    convertWidth(background, 570),
                    convertHeight(background, 740));
            canvas.drawBitmap(sig, new Rect(0, 0, sig.getWidth(), sig.getHeight()), r, paint);

            document.finishPage(page);
            document.writeTo(fOut);
            document.close();

            Intent pdfViewIntent = new Intent(Intent.ACTION_VIEW);
            pdfViewIntent.setDataAndType(Uri.fromFile(file),"application/pdf");
            pdfViewIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

            Intent intent = Intent.createChooser(pdfViewIntent, "Open File");

            try {
                if(Build.VERSION.SDK_INT>=24){
                    try{
                        Method method = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                        method.invoke(null);
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }

                startActivity(Intent.createChooser(intent, "Choose PDF Viewer"));
            } catch (ActivityNotFoundException e) {
                Log.i("AHHH", "PDF not viewable");
            }

        } catch (IOException e){
            if(e.getLocalizedMessage().contains("Permission")) {
                String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                requestPermissions(permissions, WRITE_REQUEST_CODE);
            }
            else {
                Toast.makeText(DailyReportActivity.this,
                        "Could not produce PDF", Toast.LENGTH_LONG).show();
                Log.i("error", e.getLocalizedMessage());
            }
        }

        openPolice();
    }

    private int convertWidth(Bitmap b, int x) {
        return ((x*b.getWidth())/612);
    }

    private int convertHeight(Bitmap b, int y) {
        return ((y*b.getHeight())/792);
    }

    private void finishAndClose() {
        Toast.makeText(DailyReportActivity.this, "Successfully Uploaded!", Toast.LENGTH_LONG).show();
        clearAllValues();
        DailyReportActivity.this.finish();
    }

    private void confirmExit() {
        AlertDialog.Builder builder = new AlertDialog.Builder(DailyReportActivity.this);
        builder.setTitle("Confirm");
        builder.setMessage("Are you sure you wish to save your progress and exit? Note this " +
                "will not save your current page, it will save all previous pages. If you" +
                " wish to save this current page, please continue to the next page, then" +
                " save and exit");

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                saveValuesAndExit();
                dialog.dismiss();
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
    private void saveValuesAndExit() {
        SharedPreferences.Editor prefEditor = PreferenceManager.getDefaultSharedPreferences(DailyReportActivity.this).edit();
        clearSavedPayItems();
        clearSavedAccomplishments();

        EditText et0 = findViewById(R.id.poNum);
        EditText et1 = findViewById(R.id.eInspector);
        EditText et2 = findViewById(R.id.eBridge);
        EditText et3 = findViewById(R.id.eLocation);
        EditText et4 = findViewById(R.id.eCity);

        String pNum = et0.getText().toString();
        String insp = et1.getText().toString();
        String bNum = et2.getText().toString();
        String loca = et3.getText().toString();
        String town = et4.getText().toString();

        prefEditor.putString(preKey+"pNum", pNum);
        prefEditor.putString(preKey+"insp", insp);
        prefEditor.putString(preKey+"bNum", bNum);
        prefEditor.putString(preKey+"loca", loca);
        prefEditor.putString(preKey+"town", town);

        EditText et5 = findViewById(R.id.eSafetyNotes);
        EditText et6 = findViewById(R.id.eMiscNotes);

        String safe = convertedBool(cSafety);
        String fNot = et5.getText().toString();
        String sNot = et6.getText().toString();

        prefEditor.putString(preKey+"safe", safe);
        prefEditor.putString(preKey+"fNot", fNot);
        prefEditor.putString(preKey+"sNot", sNot);

        EditText et7 = findViewById(R.id.policeStartTime);
        EditText et8 = findViewById(R.id.policeEndTime);

        String pTyp = pol;
        String pArr = et7.getText().toString();
        String pEnd = et8.getText().toString();

        prefEditor.putString(preKey+"pTyp", pTyp);
        prefEditor.putString(preKey+"pArr", pArr);
        prefEditor.putString(preKey+"pEnd", pEnd);

        EditText et9 = findViewById(R.id.flaggerStartTime);
        EditText et10 = findViewById(R.id.flaggerEndTime);
        EditText et11 = findViewById(R.id.flaggerNumber);

        String fArr = et9.getText().toString();
        String fEnd = et10.getText().toString();
        String fNum = et11.getText().toString();

        prefEditor.putString(preKey+"fArr", fArr);
        prefEditor.putString(preKey+"fEnd", fEnd);
        prefEditor.putString(preKey+"fNum", fNum);

        EditText et12 = findViewById(R.id.trafficStartTime);
        EditText et13 = findViewById(R.id.trafficEndTime);
        EditText et14 = findViewById(R.id.prodStartTime);
        EditText et15 = findViewById(R.id.prodEndTime);
        EditText et16 = findViewById(R.id.vehicleStorage);

        String tArr = et12.getText().toString();
        String tEnd = et13.getText().toString();
        String rArr = et14.getText().toString();
        String rEnd = et15.getText().toString();
        String sVeh = et16.getText().toString();

        prefEditor.putString(preKey+"tArr", tArr);
        prefEditor.putString(preKey+"tEnd", tEnd);
        prefEditor.putString(preKey+"rArr", rArr);
        prefEditor.putString(preKey+"rEnd", rEnd);
        prefEditor.putString(preKey+"sVeh", sVeh);

        try {
            for (int i = 0; i < finalPayItems.size(); i++) {
                String key = "pItem" + i;
                String val = finalPayItems.get(i).getValue();
                prefEditor.putString(preKey+key, val);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            for (int j = 0; j < finalAccomplishments.size(); j++) {
                String key = "acc" + j;
                String acc = finalAccomplishments.get(j);
                prefEditor.putString(preKey+key, acc);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            for(int k = 0; k < finalNames.size(); k++) {
                String key = "nam"+k;
                String nam = finalNames.get(k);
                prefEditor.putString(preKey+key, nam);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            for(int l = 0; l < finalTowns.size(); l++) {
                String key = "tow"+l;
                String tow = finalTowns.get(l);
                prefEditor.putString(preKey+key, tow);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        try {
            if(finalImages.size() != 0) {
                for(int ii=0; ii<finalImages.size(); ii++) {
                    ReportImage im = finalImages.get(ii);
                    String s = im.getFile()+"_"+im.getName()+"_"+im.getDesc();
                    prefEditor.putString(preKey+"img"+ii, s);
                }
            }
            else if(cImages.size() != 0){
                for(int ii=0; ii<cImages.size(); ii++) {
                    ReportImage im = cImages.get(ii);
                    String s = im.getFile()+"_"+im.getName()+"_"+im.getDesc();
                    prefEditor.putString(preKey+"img"+ii, s);
                }
            }
        } catch (Exception e) {
            try {
                if(cImages.size() != 0) {
                    for(int ii=0; ii<cImages.size(); ii++) {
                        ReportImage im = cImages.get(ii);
                        String s = im.getFile()+"_"+im.getName()+"_"+im.getDesc();
                        prefEditor.putString(preKey+"img"+ii, s);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        prefEditor.apply();
        DailyReportActivity.this.finish();
    }

    private void clearAllValues() {
        clearSavedPayItems();
        clearSavedAccomplishments();
        clearPoliceNames();
        clearPoliceTowns();
        clearImages();

        SharedPreferences.Editor prefEditor = PreferenceManager.getDefaultSharedPreferences(DailyReportActivity.this).edit();

        prefEditor.putString(preKey+"pNum", "");
        prefEditor.putString(preKey+"insp", "");
        prefEditor.putString(preKey+"bNum", "");
        prefEditor.putString(preKey+"loca", "");
        prefEditor.putString(preKey+"town", "");

        prefEditor.putString(preKey+"safe", "");
        prefEditor.putString(preKey+"fNot", "");
        prefEditor.putString(preKey+"sNot", "");

        prefEditor.putString(preKey+"pTyp", "");
        prefEditor.putString(preKey+"pArr", "");
        prefEditor.putString(preKey+"pEnd", "");

        prefEditor.putString(preKey+"fArr", "");
        prefEditor.putString(preKey+"fEnd", "");
        prefEditor.putString(preKey+"fNum", "");

        prefEditor.putString(preKey+"tArr", "");
        prefEditor.putString(preKey+"tEnd", "");
        prefEditor.putString(preKey+"rArr", "");
        prefEditor.putString(preKey+"rEnd", "");
        prefEditor.putString(preKey+"sVeh", "");

        prefEditor.apply();
    }

    private void clearSavedPayItems() {
        int i = -1;
        boolean moreItems = true;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(DailyReportActivity.this);

        while(moreItems) {
            i++;
            String key = "pItem"+i;
            String acc = prefs.getString(preKey+key, "");

            if(acc.equals(""))
                i--;
                moreItems = false;
        }

        SharedPreferences.Editor editor = prefs.edit();

        for(int j=0; j<i; j++) {
            String key = "pItem"+j;
            editor.putString(preKey+key, "");
        }

        editor.apply();
    }

    private void clearSavedAccomplishments() {
        int i = -1;
        boolean moreItems = true;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(DailyReportActivity.this);

        while(moreItems) {
            i++;
            String key = "acc"+i;
            String acc = prefs.getString(preKey+key, "");

            if(acc.equals(""))
                i--;
            moreItems = false;
        }

        SharedPreferences.Editor editor = prefs.edit();

        for(int j=0; j<i; j++) {
            String key = "acc"+j;
            editor.putString(preKey+key, "");
        }

        editor.apply();
    }

    private void clearPoliceNames() {
        int i = -1;
        boolean moreItems = true;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(DailyReportActivity.this);

        while(moreItems) {
            i++;
            String key = "nam"+i;
            String nam = prefs.getString(preKey+key, "");

            if(nam.equals(""))
                i--;
            moreItems = false;
        }

        SharedPreferences.Editor editor = prefs.edit();

        for(int j=0; j<i; j++) {
            String key = "nam"+j;
            editor.putString(preKey+key, "");
        }

        editor.apply();
    }

    private void clearPoliceTowns() {
        int i = -1;
        boolean moreItems = true;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(DailyReportActivity.this);

        while(moreItems) {
            i++;
            String key = "tow"+i;
            String tow = prefs.getString(preKey+key, "");

            if(tow.equals(""))
                i--;
            moreItems = false;
        }

        SharedPreferences.Editor editor = prefs.edit();

        for(int j=0; j<i; j++) {
            String key = "tow"+j;
            editor.putString(preKey+key, "");
        }

        editor.apply();
    }

    private void clearImages() {
        int i = -1;
        boolean moreItems = true;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(DailyReportActivity.this);

        while(moreItems) {
            i++;
            String key = "img"+i;
            String img = prefs.getString(preKey+key, "");

            if(img.equals(""))
                i--;
            moreItems = false;
        }

        SharedPreferences.Editor editor = prefs.edit();

        for(int j=0; j<i; j++) {
            String key = "img"+j;
            editor.putString(preKey+key, "");
        }

        editor.apply();
    }

    private String convertedBool(boolean b) {
        if(b)
            return "Yes";

        return "No";
    }

    private void enterAllValuesError() {
        AlertDialog.Builder builder = new AlertDialog.Builder(DailyReportActivity.this);
        builder.setTitle("Error");
        builder.setMessage("Please enter all values before continuing.");

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();
    }

    private void selectOneError(String m) {
        AlertDialog.Builder builder = new AlertDialog.Builder(DailyReportActivity.this);
        builder.setTitle("Error");
        builder.setMessage("Please select "+m+"  before continuing.");

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();
    }

    private String getMondayDate() {
        try {
            SimpleDateFormat format = new SimpleDateFormat("MM-dd-yyyy", Locale.ENGLISH);
            Date cDate = format.parse(date);

            Calendar c = Calendar.getInstance();
            c.setTime(cDate);
            int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
            Log.i("AHHH", dayOfWeek+"");
            int dif = 2-dayOfWeek;
            if(dif == 1)
                dif = -6;
            Log.i("AHHH", dif+"");
            c.add(Calendar.DAY_OF_YEAR, dif);
            return format.format(c.getTime());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return date+" error";
    }

    private String getTodaysDay(String date) {
        Date d;
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy", Locale.US);

        try {
            d = sdf.parse(date);
        } catch (ParseException ex) {
            Log.v("Exception", ex.getLocalizedMessage());
            return "Monday";
        }

        Calendar c = Calendar.getInstance();
        c.setTime(d);
        return c.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());
    }

    private String getWeekEndingDate(String date) {
        Date d;
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy", Locale.US);

        try {
            d = sdf.parse(date);
        } catch (ParseException ex) {
            Log.v("Exception", ex.getLocalizedMessage());
            return date;
        }

        Calendar c = Calendar.getInstance();
        c.setTime(d);
        int day = c.get(Calendar.DAY_OF_WEEK);

        switch(day) {
            case Calendar.SUNDAY:
                return date;
            case Calendar.SATURDAY:
                c.add(Calendar.DATE, 1);
                break;
            case Calendar.FRIDAY:
                c.add(Calendar.DATE, 2);
                break;
            case Calendar.THURSDAY:
                c.add(Calendar.DATE, 3);
                break;
            case Calendar.WEDNESDAY:
                c.add(Calendar.DATE, 4);
                break;
            case Calendar.TUESDAY:
                c.add(Calendar.DATE, 5);
                break;
            case Calendar.MONDAY:
                c.add(Calendar.DATE, 6);
                break;
        }

        d = c.getTime();
        return sdf.format(d);
    }
}
