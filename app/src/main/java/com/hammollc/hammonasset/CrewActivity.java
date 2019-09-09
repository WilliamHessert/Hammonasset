package com.hammollc.hammonasset;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CrewActivity extends AppCompatActivity {

    final ArrayList<String> addedEmps = new ArrayList();
    ArrayList<ArrayList<String>> cCrew;
    ArrayList<ArrayList<DataSnapshot>> cData;
    ArrayList<String> cNames;

    int cNum;
    int copyCount;
    private int count = 0;

    ArrayList<Crewman> crew;
    ArrayList<String> crewIds;

    EditText dField;
    String date;
    boolean day;

    ArrayList<DataSnapshot> eData;
    final ArrayList<Integer> eIndeces = new ArrayList();
    ArrayList<String> eNames;

    int eNum;
    int num;

    ProgressBar pBar;
    EditText pField;

    boolean setCnum;
    String time;
    String uid;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView((int) R.layout.activity_crew);
        this.uid = getIntent().getStringExtra("uid");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        this.pBar = (ProgressBar) findViewById(R.id.crewProgress);
        this.pField = (EditText) findViewById(R.id.enterPoNumber);
        this.dField = (EditText) findViewById(R.id.enterCrewDate);
        this.pBar.setVisibility(View.VISIBLE);
        this.pField.setVisibility(View.GONE);
        this.dField.setVisibility(View.GONE);
        downloadPoNums();
    }

    private void downloadPoNums() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("poNums");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<String> poNums = new ArrayList<>();

                for(DataSnapshot poNumData: dataSnapshot.getChildren()) {
                   poNums.add(poNumData.getValue(String.class));
                }

                openInitView(poNums);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 16908332:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void openInitView(final ArrayList<String> poNums) {
        pBar.setVisibility(View.GONE);
        pField.setVisibility(View.VISIBLE);
        dField.setVisibility(View.VISIBLE);

        final EditText dText = dField;
        final EditText pNum = pField;
        Calendar mcurrentDate = Calendar.getInstance();

        final int mYear = mcurrentDate.get(Calendar.YEAR);
        final int mMonth = mcurrentDate.get(Calendar.MONTH);
        final int mDay = mcurrentDate.get(Calendar.DATE);

        pNum.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                final Dialog dialog = new Dialog(CrewActivity.this);
                dialog.requestWindowFeature(1);
                dialog.setCancelable(false);
                dialog.setContentView(R.layout.dialog_select_view);
                ListView empList = (ListView) dialog.findViewById(R.id.selectList);
                empList.setAdapter(new ArrayAdapter(CrewActivity.this, android.R.layout.simple_list_item_1, poNums));
                empList.setOnItemClickListener(new OnItemClickListener() {
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                        pNum.setText(poNums.get(position));
                        dField.setClickable(true);

                        dText.setOnClickListener(new OnClickListener() {
                            /* renamed from: com.hammollc.hammonasset.CrewActivity$3$1 */
                            class C03601 implements OnDateSetListener {
                                C03601() {
                                }

                                public void onDateSet(DatePicker datepicker, int selectedyear, int selectedmonth, int selectedday) {
                                    selectedmonth++;
                                    dText.setText("" + selectedmonth + "/" + selectedday + "/" + selectedyear);
                                    CrewActivity.this.setDate(selectedmonth, selectedday, selectedyear);
                                    CrewActivity.this.addBtnViews();
                                }
                            }

                            public void onClick(View v) {
                                DatePickerDialog mDatePicker = new DatePickerDialog(CrewActivity.this, new C03601(), mYear, mMonth, mDay);
                                mDatePicker.setTitle("Select Date");
                                mDatePicker.show();
                            }
                        });

                        dialog.dismiss();
                    }
                });
                ((Button) dialog.findViewById(R.id.closeDialog)).setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });
    }

    private void addBtnViews() {
        this.time = "";
        ((RelativeLayout) findViewById(R.id.dayNigHolder)).setVisibility(View.VISIBLE);
        final Button d = findViewById(R.id.dBtn);
        final Button n = findViewById(R.id.nBtn);
        d.setEnabled(false);
        n.setEnabled(false);
        d.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                searchForExistingCrew("day");
                d.setBackground(getDrawable(R.drawable.box_filled));
                d.setTextColor(-1);
                n.setBackground(getDrawable(R.drawable.box));
                n.setTextColor(getResources().getColor(R.color.colorPrimary));
                day = true;
            }
        });

        n.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                searchForExistingCrew("night");
                n.setBackground(getDrawable(R.drawable.box_filled));
                n.setTextColor(-1);
                d.setBackground(getDrawable(R.drawable.box));
                d.setTextColor(getResources().getColor(R.color.colorPrimary));
                day = false;
            }
        });

        Builder builder = new Builder(this);
        builder.setTitle("Select");
        builder.setMessage("Please select whether this is a day crew or a night crew");
        builder.setPositiveButton("Night", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                n.performClick();
                dialog.dismiss();
            }
        });

        builder.setNeutralButton("Day", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                d.performClick();
                dialog.dismiss();
            }
        });

        builder.create().show();
    }

    private void setDate(int m, int d, int y) {
        String mString = m + "";
        String dString = d + "";

        if (m < 10) {
            mString = "0" + mString;
        }
        if (d < 10) {
            dString = "0" + dString;
        }

        this.date = y + "" + mString + "" + dString;
    }

    private void searchForExistingCrew(String time) {
        pBar.setVisibility(View.VISIBLE);
        dField.setOnClickListener(null);
        pField.setOnClickListener(null);

        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(this.uid).child("crews").child(this.date).child(time);
        ref.child("num").addListenerForSingleValueEvent(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    getEmpNum(ref);
                } else {
                    loadExistingCrew(ref, (String) dataSnapshot.getValue(String.class));
                }
            }

            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void loadExistingCrew(final DatabaseReference ref, String nString) {
        this.num = Integer.parseInt(nString);
        if (this.num <= 0) {
            getEmpNum(ref);
        }
        ref.addChildEventListener(new ChildEventListener() {
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                addCrewman(dataSnapshot, ref);
            }

            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void addCrewman(DataSnapshot data, DatabaseReference ref) {
        if (this.crew.size() == this.num) {
            existingCrewAlert();
        }
    }

    private void existingCrewAlert() {
        Builder builder = new Builder(this);
        builder.setTitle("Existing Crew");
        builder.setMessage("You already have a crew entered. Do you want to edit this crew?");
        builder.setPositiveButton("Edit Crew", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
    }

    private void getEmpNum(final DatabaseReference ref) {
        Log.i("AHHH", "Get Emp Num");
        FirebaseDatabase.getInstance().getReference("activeNum").addListenerForSingleValueEvent(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                int empNum = Integer.parseInt((String) dataSnapshot.getValue(String.class));
                setEmpNum(empNum);
                getEmployees(ref);
            }

            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void setEmpNum(int eNum) {
        this.eNum = eNum - 1;
    }

    private void getEmployees(final DatabaseReference ref) {
        eData = new ArrayList<>();
        eNames = new ArrayList<>();

        FirebaseDatabase.getInstance().getReference("Users").addChildEventListener(new ChildEventListener() {
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String key = dataSnapshot.getKey();
//                if (key.length() > 20 && !key.equals(uid)) {
//                    addEmp(dataSnapshot, ref);
//                }
                if (!key.equals(uid)) {
                    addEmp(dataSnapshot, ref);
                }
            }

            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            public void onCancelled(DatabaseError databaseError) {
            }
        });

        openEmployeeDialog(ref);
    }

    private void addEmp(DataSnapshot data, DatabaseReference ref) {
        String lName = data.child("info").child("lName").getValue(String.class);
        String name = data.child("info").child("fName").getValue(String.class) + " " + lName;
        Log.i("AHHH", name);
        eNames.add(name);
        Collections.sort(eNames);
        eData.add(eNames.indexOf(name), data);


        if (this.eNames.size() == this.eNum) {
            openEmployeeDialog(ref);
        }
    }

    private void getDefaultCrews(final DatabaseReference ref) {
        DatabaseReference cRef = FirebaseDatabase.getInstance().getReference("Users").child("uid").child("defaultCrews");
        setCnum = false;

        cRef.addChildEventListener(new ChildEventListener() {
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String key = dataSnapshot.getKey();

                if (key.equals("number")) {
                    setCnum(key, ref);
                } else {
                    handleDefaultCrew(dataSnapshot, ref);
                }
            }

            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void setCnum(String cString, DatabaseReference ref) {
        cNum = Integer.parseInt(cString);
        setCnum = true;
        checkCanContinue(ref);
    }

    private void handleDefaultCrew(DataSnapshot data, DatabaseReference ref) {
        ArrayList<String> cIds = new DefaultCrew(data.getKey(), data).getcIds();
        for (int i = 0; i < cIds.size(); i++) {
            Log.i("AHHH", (String) cIds.get(i));
        }
    }

    private void checkCanContinue(DatabaseReference ref) {

    }

    private void openEmployeeDialog(final DatabaseReference ref) {
        pField.setVisibility(View.GONE);
        dField.setVisibility(View.GONE);
        pBar.setVisibility(View.GONE);

        (findViewById(R.id.dayNigHolder)).setVisibility(View.GONE);
        (findViewById(R.id.selectCrewView)).setVisibility(View.VISIBLE);

        ListView lView = findViewById(R.id.addEmpList);
        final ArrayAdapter<String> ad = new ArrayAdapter(this, android.R.layout.simple_list_item_1, addedEmps);
        lView.setAdapter(ad);

        copyCount = 0;
        final ArrayList<String> empCopy = new ArrayList();
        final ArrayList<String> unchangedCopy = new ArrayList();
        ((Button) findViewById(R.id.addEmpBtn)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (copyCount == 0 || copyCount != eNames.size()) {
                    empCopy.clear();
                    unchangedCopy.clear();
                    for (int i = 0; i < eNames.size(); i++) {
                        String eName = (String) eNames.get(i);
                        empCopy.add(eName);
                        unchangedCopy.add(eName);
                    }
                    updateCopyCount(unchangedCopy.size());
                }
                final Dialog dialog = new Dialog(CrewActivity.this);
                dialog.requestWindowFeature(1);
                dialog.setCancelable(false);
                dialog.setContentView(R.layout.dialog_employee_view);

                ListView empList = (ListView) dialog.findViewById(R.id.viewAllEmps);
                empList.setAdapter(new ArrayAdapter(CrewActivity.this, android.R.layout.simple_list_item_1, empCopy));
                empList.setOnItemClickListener(new OnItemClickListener() {
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                        eIndeces.add(Integer.valueOf(unchangedCopy.indexOf(empCopy.get(position))));
                        addedEmps.add(empCopy.get(position));
                        ad.notifyDataSetChanged();
                        dialog.dismiss();
                        empCopy.remove(position);
                    }
                });

                (dialog.findViewById(R.id.empCloseDialog)).setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });
        (findViewById(R.id.addDefCrewBtn)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(CrewActivity.this);
                builder.setTitle("Coming Soon");
                builder.setMessage("This functionality is currently under maintenance. However," +
                        " if you save a default crew, it will be saved to your account and you" +
                        " will be able to view it once this functionality is fixed.");

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                builder.create().show();
            }
        });
        lView.setOnItemClickListener(new OnItemClickListener() {

            /* renamed from: com.hammollc.hammonasset.CrewActivity$18$2 */
            class C03572 implements DialogInterface.OnClickListener {


                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }

            public void onItemClick(AdapterView<?> adapterView, View view, final int position, long id) {
                Builder builder = new Builder(CrewActivity.this);
                builder.setTitle("Confirm");
                builder.setMessage("Are you sure you want to remove this employee from the crew?");
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        empCopy.add(((Integer) eIndeces.get(position)).intValue(), (String) addedEmps.get(position));
                        eIndeces.remove(position);
                        addedEmps.remove(position);
                        ad.notifyDataSetChanged();
                        dialog.dismiss();
                    }
                });
                builder.setNeutralButton("Cancel", new C03572());
                builder.create().show();
            }
        });
        ((Button) findViewById(R.id.createCrewBtn)).setOnClickListener(new OnClickListener() {

            /* renamed from: com.hammollc.hammonasset.CrewActivity$19$1 */
            class C03581 implements DialogInterface.OnClickListener {
                C03581() {
                }

                public void onClick(DialogInterface dialog, int which) {
                    getCrewIds(ref);
                    dialog.dismiss();
                }
            }

            /* renamed from: com.hammollc.hammonasset.CrewActivity$19$2 */
            class C03592 implements DialogInterface.OnClickListener {
                C03592() {
                }

                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }

            public void onClick(View v) {
                Builder builder = new Builder(CrewActivity.this);
                builder.setTitle("Confirm");
                builder.setMessage("Are you sure you want to create this crew?");
                builder.setPositiveButton("Yes", new C03581());
                builder.setNegativeButton("No", new C03592());
                builder.create().show();
            }
        });
    }

    private void updateCopyCount(int copyCount) {
        this.copyCount = copyCount;
    }

    private void getCrewIds(DatabaseReference ref) {
        RelativeLayout mainLayout = findViewById(R.id.selectCrewView);
        mainLayout.setVisibility(View.GONE);
        pBar.setVisibility(View.VISIBLE);

        this.crewIds = new ArrayList();
        ArrayList<String> crewNames = new ArrayList();
        for (int i = 0; i < this.eIndeces.size(); i++) {
            int j = ((Integer) this.eIndeces.get(i)).intValue();
            this.crewIds.add(((DataSnapshot) this.eData.get(j)).getKey());
            crewNames.add((String) this.eNames.get(j));
        }
        this.crewIds.add(this.uid);
        addDataToCrew(crewNames, ref);
    }

    private void addDataToCrew(final ArrayList<String> names, DatabaseReference ref1) {
        String newDate = reformatDate();
        String mondayDate = getMonday(newDate);
        DatabaseReference vRef = FirebaseDatabase.getInstance().getReference("Users");
        String uid = FirebaseAuth.getInstance().getUid();
        String dString = "night";

        if (day) {
            dString = "day";
        }
        final DatabaseReference ref = vRef.child(uid).child("crews").child(mondayDate).child(newDate).child(dString);
        for (int i = 0; i < names.size(); i++) {
            final String id = (String) this.crewIds.get(i);
            ref.child("crew").child(id).child("name").setValue((String) names.get(i)).addOnCompleteListener(new OnCompleteListener<Void>() {

                /* renamed from: com.hammollc.hammonasset.CrewActivity$20$1 */
                class C05141 implements OnCompleteListener<Void> {
                    C05141() {
                    }

                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            checkIfContinue(names.size());
                        } else {
                            displayError("Error creating crew, please contact the system admin");
                        }
                    }
                }

                public void onComplete(@NonNull Task<Void> task) {
                    ref.child("crew").child(id).child("approved").setValue("false").addOnCompleteListener(new C05141());
                }
            });
        }
    }

    private void checkIfContinue(int cap) {
        this.count++;
        if (this.count == cap) {
            this.count = 0;
            addDataToEmps();
        }
    }

    private void addDataToEmps() {
        final String newDate = reformatDate();
        final String mondayDate = getMonday(newDate);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        if (mondayDate.equals("Parse Error")) {
            displayError("Error uploading data");
            return;
        }

        String dString = "night";
        if (this.day) {
            dString = "day";
        }

        for (int i = 0; i < this.crewIds.size(); i++) {
            ref.child((String) this.crewIds.get(i)).child("hours").child(mondayDate).child(newDate).child(dString).child("foreman").setValue(this.uid).addOnCompleteListener(new OnCompleteListener<Void>() {
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        addDataToPoNumber(mondayDate, newDate);
                    } else {
                        displayError("Error uploading data, please contact the system admin");
                    }
                }
            });
        }
    }

    private void addDataToPoNumber(String mDate, String nDate) {
        String dString = "night";

        if(day)
            dString = "day";

        addCrewmanToPoNumber(0, FirebaseDatabase.getInstance().getReference("Contracts").child("16PSX0176").child("poNums").child(this.pField.getText().toString()).child("crews").child(mDate).child(nDate).child(dString).child(uid), mDate, nDate);
    }

    private void addCrewmanToPoNumber(int i, DatabaseReference ref, String mDate, String nDate) {
        if (i == this.crewIds.size()) {
            setReportAndPoNumFields(mDate, nDate);
            return;
        }
        final int i2 = i;
        final DatabaseReference databaseReference = ref;
        final String str = mDate;
        final String str2 = nDate;
        ref.child("" + i).setValue((String) this.crewIds.get(i)).addOnCompleteListener(new OnCompleteListener<Void>() {
            public void onComplete(@NonNull Task<Void> task) {
                addCrewmanToPoNumber(i2 + 1, databaseReference, str, str2);
            }
        });
    }

    private void setReportAndPoNumFields(final String mDate, final String nDate) {
        String dStringInit = "night";
        if (this.day) {
            dStringInit = "day";
        }

        final String dString = dStringInit;
        EditText et = findViewById(R.id.enterPoNumber);
        final String poNumber = et.getText().toString();

        FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getUid()).child("crews").child(mDate).child(nDate).child(dString).child("approveAlert").setValue("false").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getUid()).child("crews").child(mDate).child(nDate).child(dString).child("report").setValue("false").addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getUid()).child("crews").child(mDate).child(nDate).child(dString).child("poNumber").setValue(poNumber).addOnCompleteListener(new OnCompleteListener<Void>() {
                            public void onComplete(@NonNull Task<Void> task) {
                                createDefaultCrewDialog(nDate);
                            }
                        });
                    }
                });
            }
        });

    }

    private void createDefaultCrewDialog(final String nDate) {
        this.count++;
        if (this.count == this.crewIds.size()) {
            Builder builder = new Builder(this);
            builder.setTitle("Save Crew");
            builder.setMessage("Would you like to save this crew as a default crew? This means you will be able to select this crew with a simple button click rather than having to reselect the crew person by person.");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    getDefaultCrewName(nDate);
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    finish();
                }
            });
            builder.create().show();
        }
    }

    private void getDefaultCrewName(final String nDate) {
        Builder builder = new Builder(this);
        builder.setTitle("Name Default Crew");
        builder.setMessage("Please enter a name for this crew (so you can use it later).");
        final EditText input = new EditText(this);
        input.setInputType(1);
        builder.setView(input);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                createDefaultCrew(input.getText().toString(), nDate);
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

    private void createDefaultCrew(final String nam, String nDate) {
        this.count = 0;
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(this.uid).child("defaultCrews").child(nDate);
        for (int i = 0; i < this.crewIds.size(); i++) {
            String id = (String) this.crewIds.get(i);
            if (!id.equals(this.uid)) {
                ref.child(i + "").setValue(id).addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            checkCanClose(ref, nam);
                        } else {
                            displayError("Error Saving Default Crew");
                        }
                    }
                });
            }
        }
    }

    private void checkCanClose(DatabaseReference ref, String nam) {
        this.count++;
        if (this.count == this.crewIds.size() - 1) {
            this.cNum++;
            ref.child("name").setValue(nam).addOnCompleteListener(new OnCompleteListener<Void>() {

                /* renamed from: com.hammollc.hammonasset.CrewActivity$29$1 */
                class C05151 implements OnCompleteListener<Void> {
                    C05151() {
                    }

                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(CrewActivity.this, "Success!", Toast.LENGTH_LONG).show();
                        finish();
                    }
                }

                public void onComplete(@NonNull Task<Void> task) {
                    FirebaseDatabase.getInstance().getReference("Users").child(uid).child("defaultCrews").child("number").setValue(Integer.valueOf(cNum)).addOnCompleteListener(new C05151());
                }
            });
        }
    }

    private String reformatDate() {
        String y = date.substring(0, 4);
        String m = date.substring(4, 6);
        return m+"-"+date.substring(6)+"-"+y;
    }

    private String getMonday(String nDate) {
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.US);
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy", Locale.US);
        try {
            Date cDate = dateFormat.parse(nDate);
            return dateFormat.format(new Date(cDate.getTime() - (((long) daysAfterMonday(dayFormat.format(cDate))) * 86400000)));
        } catch (ParseException e) {
            e.printStackTrace();
            return "Parse Error";
        }
    }

    private int daysAfterMonday(String d) {
        if (d.equals("Monday")) {
            return 0;
        }
        if (d.equals("Tuesday")) {
            return 1;
        }
        if (d.equals("Wednesday")) {
            return 2;
        }
        if (d.equals("Thursday")) {
            return 3;
        }
        if (d.equals("Friday")) {
            return 4;
        }
        if (d.equals("Saturday")) {
            return 5;
        }
        return 6;
    }

    private void displayError(String m) {
        Builder builder = new Builder(this);
        builder.setTitle("Error");
        builder.setMessage(m);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                CrewActivity.this.finish();
            }
        });
        builder.create().show();
    }
}