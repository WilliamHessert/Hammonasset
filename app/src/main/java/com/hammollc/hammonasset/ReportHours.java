package com.hammollc.hammonasset;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReportHours extends AppCompatActivity {

    int hours;
    int bHours;
    double hoursLeft;
    String dayOfMon;

    int sHourHolder;
    int sMinHolder;
    int eHourHolder;
    int eMinHolder;

    int bIndex;
    boolean isDay;
    ArrayList<Block> blocks;
    BlockAdapter ad;

    int nextIndex = 0;
    ArrayList<String> descs = new ArrayList<>();
    ArrayList<Integer> indeces = new ArrayList<>();
    ArrayList<Boolean> isNewOther = new ArrayList<>();
    ArrayList<String[]> savedOthers = new ArrayList<>();

    ArrayList<HourOption> sOptions = new ArrayList<>();
    ArrayList<HourOption> nOptions = new ArrayList<>();

    String uid, week, date, dString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_hours);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        uid = FirebaseAuth.getInstance().getUid();
        getOptions();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                ReportHours.this.finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void getOptions() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("PayClassifications");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot data: dataSnapshot.getChildren()) {
                    String label = data.child("label").getValue(String.class);
                    String classification = data.child("classification").getValue(String.class);
                    String standard = data.child("standard").getValue(String.class);

                    HourOption ho = new HourOption(label, classification, standard);

                    if(ho.isStandard())
                        sOptions.add(ho);
                    else
                        nOptions.add(ho);
                }

                sOptions.add(new HourOption("Other...", "", "true"));
                setViews();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setViews() {
        ProgressBar pBar = findViewById(R.id.hourProgress);
        RelativeLayout layout = findViewById(R.id.dateTimeView);

        pBar.setVisibility(View.GONE);
        layout.setVisibility(View.VISIBLE);

        Bundle extras = getIntent().getExtras();
        String date = extras.getString("date", "");
        String time = extras.getString("time", "");

        isDay = true;
        getSavedOthers();

        setDateTimePickers(date);
        setDayNig(time);
        setConfirm();
    }

    private void getSavedOthers() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ReportHours.this);
        int i = -1;
        String nOther = "placeholder";

        while(!nOther.equals("")) {
            i++;
            nOther = prefs.getString(uid+"savedOthersTypes"+i, "");
        }

        i--;
        for(int j=0; j<=i; j++) {
            String[] other = new String[2];
            other[0] = prefs.getString(uid+"savedOthersTypes"+j, "");
            other[1] = prefs.getString(uid+"savedOthersDescs"+i, "");

            savedOthers.add(other);
        }

        nextIndex = savedOthers.size();
    }

    private void setDateTimePickers(String date) {
        final EditText dText = findViewById(R.id.reportDateField);
        final EditText sTime = findViewById(R.id.reportStartField);
        final EditText eTime = findViewById(R.id.reportEndField);

        dText.setInputType(0);
        sTime.setInputType(0);
        eTime.setInputType(0);

        final Calendar mcurrentDate = Calendar.getInstance();
        final int mYear = mcurrentDate.get(Calendar.YEAR);
        final int mMonth = mcurrentDate.get(Calendar.MONTH);
        final int mDay = mcurrentDate.get(Calendar.DAY_OF_MONTH);

        Date currentDate = mcurrentDate.getTime();
        final SimpleDateFormat sdf = new SimpleDateFormat("EEEE");
        dayOfMon = sdf.format(currentDate);

        setDefaultDate(mYear, mMonth+1, mDay, dText);

        dText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog mDatePicker;
                mDatePicker = new DatePickerDialog(ReportHours.this, new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker datepicker, int selectedyear, int selectedmonth, int selectedday) {
                        selectedmonth = selectedmonth + 1;
                        String dString = ""+selectedmonth+"/"+selectedday+"/"+selectedyear;

                        dText.setText(dString);
                        Calendar c = Calendar.getInstance();

                        c.set(Calendar.MONTH, selectedmonth-1);
                        c.set(Calendar.DATE, selectedday);
                        c.set(Calendar.YEAR, selectedyear);

                        Date thisDate = c.getTime();
                        dayOfMon = sdf.format(thisDate);
                    }
                }, mYear, mMonth, mDay);
                mDatePicker.show();
            }
        });

        sTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickTime(sTime, mcurrentDate);
            }
        });

        eTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickTime(eTime, mcurrentDate);
            }
        });

        if(!date.equals("")) {
            date = date.replaceAll("-", "/");

            if(date.substring(0, 1).equals("0"))
                date = date.substring(1);

            dText.setText(date);
        }
    }

    private void setDefaultDate(int y, int m, int d, EditText dt) {
        String date = m+"/"+d+"/"+y;
        dt.setText(date);
    }

    private void pickTime(final EditText e, Calendar mcurrentTime) {
        int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = mcurrentTime.get(Calendar.MINUTE);
        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(ReportHours.this, new TimePickerDialog.OnTimeSetListener() {

            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                String sHor = ""+selectedHour;
                String sMin = ""+selectedMinute;
                String noon = "AM";

                if(selectedMinute < 10) {
                    sMin = "0"+sMin;
                }

                if(selectedHour > 12) {
                    int newHr = selectedHour - 12;
                    sHor = ""+newHr;
                    noon = "PM";
                }

                String eSetText =  sHor + ":" + sMin +" " + noon;
                e.setText(eSetText);
                calcHours(e, selectedHour, selectedMinute);
            }
        }, hour, minute, false);
        mTimePicker.show();
    }

    private void calcHours(EditText e, int sHour, int sMin) {
        if(e == findViewById(R.id.reportStartField)) {
            sHourHolder = sHour;
            sMinHolder = sMin;
        } else {
            eHourHolder = sHour;
            eMinHolder = sMin;
        }
    }

    private void setDayNig(String time) {
        final Button day = findViewById(R.id.dayBtn);
        final Button nig = findViewById(R.id.nigBtn);

        final EditText str = findViewById(R.id.reportStartField);
        final EditText end = findViewById(R.id.reportEndField);

        day.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeDay();

                day.setBackground(getDrawable(R.drawable.box_filled));
                day.setTextColor(Color.WHITE);

                nig.setBackground(getDrawable(R.drawable.box));
                nig.setTextColor(getResources().getColor(R.color.colorPrimary));

                str.setText("8:00 A.M.");
                end.setText("4:00 P.M.");
                hours = 8*60;

                sHourHolder = 8;
                eHourHolder = 16;
                sMinHolder = 0;
                eMinHolder = 0;
            }
        });

        nig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeDay();

                nig.setBackground(getDrawable(R.drawable.box_filled));
                nig.setTextColor(Color.WHITE);

                day.setBackground(getDrawable(R.drawable.box));
                day.setTextColor(getResources().getColor(R.color.colorPrimary));

                str.setText("8:00 P.M.");
                end.setText("5:00 A.M.");
                hours = 9*60;

                sHourHolder = 20;
                eHourHolder = 5;
                sMinHolder = 0;
                eMinHolder = 0;
            }
        });

        if(time.equals("") || time.equals("Day")) {
            day.performClick();
            isDay = true;
        }
        else {
            nig.performClick();
            isDay = false;
        }
    }

    private void changeDay() {
        isDay = !isDay;
    }

    private void setConfirm() {
        Button confirm = findViewById(R.id.confirmDateTime);
        final RelativeLayout init = findViewById(R.id.dateTimeView);
        final RelativeLayout next = findViewById(R.id.hourBlockView);

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                init.setVisibility(View.GONE);
                next.setVisibility(View.VISIBLE);
                setNextViews();
            }
        });
    }

    private void setNextViews() {
        calculateHours();
        setProgressBar();
        updateProgress();

        setListView();
        setAddBlock();
        setReportBtn();
    }

    private void calculateHours() {
        bHours = 0;
        hours = 0;

        if(sHourHolder > eHourHolder) {
            eHourHolder += 24;
        }

        if(sMinHolder%15 != 0) {
            sMinHolder += 15 - (sMinHolder % 15);
        }

        if(eMinHolder%15 != 0) {
            eMinHolder += 15 - (eMinHolder % 15);
        }

        int sTotal = (sHourHolder*60)+sMinHolder;
        int eTotal = (eHourHolder*60)+eMinHolder;
        hours = eTotal - sTotal;
    }

    private void setListView() {
        ListView listView = findViewById(R.id.blockList);
        blocks = new ArrayList<>();
        ad = new BlockAdapter(ReportHours.this, blocks, false);

        listView.setAdapter(ad);
    }

    private void setAddBlock() {
        Button add = findViewById(R.id.addHourBlock);

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ReportHours.this);
                builder.setTitle("Add Block");

                final View alertLayout = getLayoutInflater().inflate(R.layout.alert_block_type, null);
                builder.setView(alertLayout);

                ListView listView = alertLayout.findViewById(R.id.blockTypeList);
                HourOptionAdapter alAd = new HourOptionAdapter(ReportHours.this, sOptions);
                listView.setAdapter(alAd);

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                final AlertDialog dialog = builder.create();
                dialog.show();

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        dialog.dismiss();
                        if(position != sOptions.size()-1)
                            addBlock(sOptions.get(position));
                        else
                            handleOther();
                    }
                });
            }
        });
    }

    private void handleOther() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ReportHours.this);
        builder.setTitle("Warning");
        builder.setMessage("You are about to view non-standard pay classifications. Confirm with your foreman before selecting any of these");

        builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                displayNonStandard();
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

    private void displayNonStandard() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ReportHours.this);
        builder.setTitle("Add Block");

        final View alertLayout = getLayoutInflater().inflate(R.layout.alert_block_type, null);
        builder.setView(alertLayout);

        ListView listView = alertLayout.findViewById(R.id.blockTypeList);
        HourOptionAdapter alAd = new HourOptionAdapter(ReportHours.this, nOptions);
        listView.setAdapter(alAd);

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        final AlertDialog dialog = builder.create();
        dialog.show();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                dialog.dismiss();
                addBlock(nOptions.get(position));
            }
        });
    }

//    private void enterOther() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(ReportHours.this);
//        builder.setTitle("Enter");
//        builder.setMessage("Please enter what work you did.");
//
//        final EditText tInput = new EditText(ReportHours.this);
//        tInput.setInputType(InputType.TYPE_CLASS_TEXT);
//        builder.setView(tInput);
//
//        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                String type = tInput.getText().toString();
//                enterOtherDesc(type);
//            }
//        });
//
//        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
//            }
//        });
//
//        builder.create().show();
//    }
//
//    private void enterOtherDesc(final String t) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(ReportHours.this);
//        builder.setTitle("Enter");
//        builder.setMessage("Please describe the work you did.");
//
//        final EditText dInput = new EditText(ReportHours.this);
//        dInput.setInputType(InputType.TYPE_CLASS_TEXT);
//        builder.setView(dInput);
//
//        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                String desc = dInput.getText().toString();
//                isNewOther.add(true);
//                descs.add(desc);
//
//                int index = blocks.size();
//                indeces.add(index);
//                addBlock(t);
//            }
//        });
//
//        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
//            }
//        });
//
//        builder.create().show();
//    }
//
//    private void chooseOther() {
//        if(savedOthers.size() == 0) {
//            AlertDialog.Builder builder = new AlertDialog.Builder(ReportHours.this);
//            builder.setTitle("Error");
//            builder.setMessage("Sorry, but you have no saved previous values.");
//
//            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    dialog.dismiss();
//                }
//            });
//
//            builder.create().show();
//        }
//        else {
//            final ArrayList<String> types = new ArrayList<>();
//
//            for (int i=0; i<savedOthers.size(); i++) {
//                types.add(savedOthers.get(i)[0]);
//            }
//
//            final Dialog dialog = new Dialog(ReportHours.this);
//            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//            dialog.setCancelable(false);
//            dialog.setContentView(R.layout.dialog_select_view);
//
//            ListView tList = dialog.findViewById(R.id.selectList);
//            ArrayAdapter<String> tAdapter = new ArrayAdapter<>(
//                    ReportHours.this, android.R.layout.simple_list_item_1, types);
//            tList.setAdapter(tAdapter);
//
//            tList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//                @Override
//                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                    String t = types.get(position);
//                    String d = savedOthers.get(position)[1];
//                    descs.add(d);
//
//                    int index = blocks.size();
//                    indeces.add(index);
//                    addBlock(t);
//
//                    isNewOther.add(false);
//                    dialog.dismiss();
//                }
//            });
//
//            Button closeBtn = dialog.findViewById(R.id.closeDialog);
//            closeBtn.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    dialog.dismiss();
//                }
//            });
//
//            dialog.show();
//        }
//    }

    private void addBlock(HourOption hourOption) {
        String type = hourOption.getLabel();
        String clss = hourOption.getClassification();
        Log.i("AHHH", clss);
        Block block = new Block(type, clss, 0);
        blocks.add(block);

        bHours= 0;
        ad.notifyDataSetChanged();
    }

    public void remBlock(int i) {
        bHours -= blocks.get(i).getHours();
        updateProgress();

        blocks.remove(i);
        ad.notifyDataSetChanged();

        int j = indeces.indexOf(i);
        if(j != -1) {
            indeces.remove(j);
            descs.remove(j);
        }
    }

    public void updateHourAmount(int b, int h) {
        bHours = 0;
        blocks.get(b).setHours(h);

        for(int i=0; i<blocks.size(); i++) {
            bHours += blocks.get(i).getHours();
        }

        updateProgress();
    }

    private void setProgressBar() {
        Resources res = getResources();
        Drawable drawable = res.getDrawable(R.drawable.progress_circle);
        ProgressBar bar = findViewById(R.id.reportProgress);

        bar.setProgress(0);
        bar.setSecondaryProgress(100);
        bar.setMax(100);
        bar.setProgressDrawable(drawable);
    }

    private void updateProgress() {
        ProgressBar bar = findViewById(R.id.reportProgress);
        TextView txt = findViewById(R.id.reportProgressText);
        TextView lft = findViewById(R.id.hoursLeftText);

        int progress = bHours*100;
        progress = progress/hours;

        bar.setProgress(progress);
        String txtString = progress+"%";
        txt.setText(txtString);

        double hoursTotl = hours/60+(((hours%60)/15)*0.25);
        double hoursSelc = bHours/60+(((bHours%60)/15)*0.25);
        hoursLeft = hoursTotl-hoursSelc;

        String hLeft = "Need to assign "+hoursLeft+" hours";
        lft.setText(hLeft);

        if(progress > 100) {
            AlertDialog.Builder builder = new AlertDialog.Builder(ReportHours.this);
            builder.setTitle("Error");
            builder.setMessage("You have assigned too much time. " +
                    "Please reduce or delete time blocks before continuing");

            builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            builder.create().show();
        }
    }

    private void setReportBtn() {
        Button rep = findViewById(R.id.confirmHours);
        rep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyReport();
            }
        });
    }

    private void verifyReport() {
        if(bHours == hours) {
            confirmReport();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(ReportHours.this);
            builder.setTitle("Error");
            builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            if(bHours < hours) {
                builder.setMessage("You have "
                        +hoursLeft+" unaccounted hours. Please assign more time before advancing");
            } else {
                builder.setMessage("You have assigned too much time ("+hoursLeft+"). " +
                        "Please reduce or delete time blocks before continuing");
            }

            builder.create().show();
        }
    }

    private void confirmReport() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ReportHours.this);
        builder.setTitle("Verify");
        builder.setMessage("Do you verify that the hours you have reported are completely true?");

        builder.setPositiveButton("Verify", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                report();
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

    private void report() {
        RelativeLayout mainView = findViewById(R.id.hourBlockView);
        mainView.setVisibility(View.GONE);

        ProgressBar pBar = findViewById(R.id.hourProgress);
        pBar.setVisibility(View.VISIBLE);

        if ( ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {

            ActivityCompat.requestPermissions( this, new String[] {  android.Manifest.permission.ACCESS_FINE_LOCATION  },
                    0);
        }
        else {
            try {
                String uid = FirebaseAuth.getInstance().getUid();
                date = ((EditText) (findViewById(R.id.reportDateField))).getText().toString();
                String sTime = ((EditText) (findViewById(R.id.reportStartField))).getText().toString();
                String eTime = ((EditText) (findViewById(R.id.reportEndField))).getText().toString();

                date = date.replaceAll("/", "-");

                SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yyyy", Locale.US);
                Date dDate = formatter.parse(date);

                Calendar c = Calendar.getInstance();
                c.setTime(dDate);

                while(c.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
                    c.add(Calendar.DATE, -1);
                }

                dDate = c.getTime();
                week = formatter.format(dDate);

                date = formatDate(date);
                dString = getDayString();
                DatabaseReference ref = FirebaseDatabase.getInstance()
                        .getReference().child("Users").child(uid)
                        .child("hours").child(week).child(date).child(dString);

                updateHours(ref, sTime, eTime);

            } catch (Exception e) {
                e.printStackTrace();
                reportError();
            }
        }
    }

    private void updateHours(final DatabaseReference ref, final String sTime, final String eTime) {
        ref.child("tHours").setValue(hours).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    updateStartTime(ref, sTime, eTime);
                }
            }
        });
    }

    private void updateStartTime(final DatabaseReference ref, String sTime, final String eTime) {
        ref.child("sTime").setValue(sTime).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    updateEndTime(ref, eTime);
                }
            }
        });
    }

    private void updateEndTime(final DatabaseReference ref, String eTime) {
        ref.child("eTime").setValue(eTime).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    bIndex = 0;
                    updateBlocks(ref);
                }
            }
        });
    }

    private void updateBlocks(DatabaseReference ref) {
        if(bIndex < blocks.size()) {
            updateBlock(ref, blocks.get(bIndex));
        } else {
            createHourApproveAlert(ref);
        }
    }

    private void updateBlock(final DatabaseReference ref, final Block block) {
        ref.child("blocks").child(""+bIndex).child("type")
                .setValue(block.getType()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    updateBlockHours(ref, block);
                } else {
                    reportError();
                }
            }
        });
    }

    private void updateBlockHours(final DatabaseReference ref, final Block block) {
        ref.child("blocks").child(""+bIndex).child("hours")
                .setValue(block.getHours()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    updateBlockClassification(ref, block);
//                    int ind1 = indeces.indexOf(bIndex);
//
//                    if(ind1 != -1) {
//                        if(isNewOther.get(ind1)) {
//                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(ReportHours.this).edit();
//                            editor.putString(uid + "savedOthersTypes" + nextIndex, blocks.get(bIndex).getType());
//                            editor.putString(uid + "savedOthersDescs" + nextIndex, descs.get(ind1));
//                            editor.apply();
//                        }
//
//                        ref.child("blocks").child(""+bIndex).child("description").setValue(descs.get(ind1)).addOnCompleteListener(new OnCompleteListener<Void>() {
//                            @Override
//                            public void onComplete(@NonNull Task<Void> task) {
//                                bIndex++;
//                                updateBlocks(ref);
//                            }
//                        });
//                    }
//                    else {
//                        bIndex++;
//                        updateBlocks(ref);
//                    }
                } else {
                    reportError();
                }
            }
        });
    }

    private void updateBlockClassification(final DatabaseReference ref, Block block) {
        Log.i("AHHH", "Starting classification: "+block.getClassification());
        ref.child("blocks").child(""+bIndex).child("classification")
                .setValue(block.getClassification()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    Log.i("AHHH", "Completed classification for "+bIndex);
                    bIndex++;
                    updateBlocks(ref);
                }
                else
                    reportError();
            }
        });
    }

    private void createHourApproveAlert(final DatabaseReference ref) {
        ref.child("foreman").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String foreman = dataSnapshot.getValue(String.class);

                try {
                    if (foreman.equals("") || foreman.equals(null) || foreman.equals(uid))
                        getLocationAndFinish(ref);
                    else {
                        DatabaseReference nRef = FirebaseDatabase.getInstance()
                                .getReference("Users").child(foreman).child("crews");
                        nRef = nRef.child(week).child(date).child(dString).child("approveAlert");
                        nRef.setValue("true").addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                getLocationAndFinish(ref);
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    getLocationAndFinish(ref);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }
    private void getLocationAndFinish(DatabaseReference ref) {
        final DatabaseReference r = ref.child("location");

        try {
            LocationManager mLocationManager =
                    (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        0);
            }

            Location location;
            Location locationGPS = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Location locationNet = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            long GPSLocationTime = 0;
            if (null != locationGPS) {
                GPSLocationTime = locationGPS.getTime();
            }

            long NetLocationTime = 0;

            if (null != locationNet) {
                NetLocationTime = locationNet.getTime();
            }

            if (0 < GPSLocationTime - NetLocationTime) {
                location = locationGPS;
            } else {
                location = locationNet;
            }

            final String lat = "" + location.getLatitude();
            final String lon = "" + location.getLongitude();

            r.child("lat").setValue(lat).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        r.child("lon").setValue(lon).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(
                                        ReportHours.this,
                                        "Successfully Reported Hours!",
                                        Toast.LENGTH_LONG).show();
                                ReportHours.this.finish();
                            }
                        });
                    }
                }
            });
        } catch (Exception e) {
            reportWithoutLocation(r);
        }
    }

    private void reportWithoutLocation(final DatabaseReference r) {
        r.child("lat").setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    r.child("lon").setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(
                                    ReportHours.this,
                                    "Successfully Reported Hours!",
                                    Toast.LENGTH_LONG).show();
                            ReportHours.this.finish();
                        }
                    });
                }
            }
        });
    }

    private void reportError() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ReportHours.this);
        builder.setTitle("Error");
        builder.setMessage("Could not report hours. Please try again at another time.");

        builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();
    }

    private String formatDate(String date) {
        String[] comps = date.split("-");
        String m = comps[0];
        String d = comps[1];

        if(m.length() == 1)
            m = "0"+m;

        if(d.length() == 1)
            d = "0"+d;

        return m+"-"+d+"-"+comps[2];
    }

    private String getDayString() {
        if(!isDay)
            return "night";

        return "day";
    }
}
