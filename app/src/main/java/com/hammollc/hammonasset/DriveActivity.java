package com.hammollc.hammonasset;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class DriveActivity extends AppCompatActivity {

    private String uid, name;

    private Vehicle vehicle;
    private boolean vSelected = false;
    private ArrayList<Vehicle> vehicles;
    private ArrayList<String> vehicleNames;

    private ProgressBar pBar;
    private RelativeLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drive);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        getSupportActionBar().setTitle("Log a Drive");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        getExtraValues();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                DriveActivity.this.finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void getExtraValues() {
        Bundle extras = getIntent().getExtras();
        uid = extras.getString("uid", "");
        name = extras.getString("fName", "")+
                " "+extras.getString("lName", "");

        loadVehicles();
    }

    private void loadVehicles() {
        vehicles = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Vehicles");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot childSnapshot: dataSnapshot.getChildren()) {
                    String id = childSnapshot.getKey();
                    String name = childSnapshot.child("name").getValue(String.class);
                    String plates = childSnapshot.child("plates").getValue(String.class);

                    if(childSnapshot.child("drivers").getValue() == null ||
                            childSnapshot.child("drivers").child(uid).getValue() != null) {
                        vehicles.add(new Vehicle(id, name, plates));
                    }
                }

                loadVehicleNames();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }

    private void loadVehicleNames() {
        vehicleNames = new ArrayList<>();

        for(Vehicle vehicle: vehicles) {
            vehicleNames.add(vehicle.getName()+" - "+vehicle.getPlates());
        }

        loadViews();
    }

    private void loadViews() {
        pBar = findViewById(R.id.driveProgress);
        container = findViewById(R.id.driveContainer);

        pBar.setVisibility(View.GONE);
        container.setVisibility(View.VISIBLE);

        final EditText nameText = findViewById(R.id.driveNameField);
        final EditText vehcText = findViewById(R.id.driveVehicleField);

        nameText.setText(name);
        vehcText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(DriveActivity.this);
                dialog.requestWindowFeature(1);
                dialog.setCancelable(false);

                dialog.setContentView(R.layout.dialog_select_view);
                ListView vehList = dialog.findViewById(R.id.selectList);
                vehList.setAdapter(new ArrayAdapter(
                        DriveActivity.this, android.R.layout.simple_list_item_1, vehicleNames));

                vehList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                        vSelected = true;
                        vehicle = vehicles.get(position);
                        vehcText.setText(vehicle.getName());

                        dialog.dismiss();
                    }
                });

                dialog.findViewById(R.id.closeDialog).setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                dialog.create();
                dialog.show();
            }
        });

        final EditText dateText = findViewById(R.id.driveDate);
        final Calendar calendar = Calendar.getInstance();

        final DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                updateDateLabel(calendar.getTime());
            }
        };

        dateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog dialog = new DatePickerDialog(
                        DriveActivity.this,
                        dateSetListener, calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

                dialog.show();
            }
        });

        final EditText loctText = findViewById(R.id.driveLocation);
        final EditText reasText = findViewById(R.id.driveReason);
        final EditText mileText = findViewById(R.id.driveMileage);
        final EditText noteText = findViewById(R.id.driveNotes);

        Button submitBtn = findViewById(R.id.driveSubmitBtn);
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String date = dateText.getText().toString();
                String loct = loctText.getText().toString();
                String reas = reasText.getText().toString();
                String mile = mileText.getText().toString();
                String note = noteText.getText().toString();

                validateInputs(date, loct, reas, mile, note);
            }
        });
    }

    private void updateDateLabel(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        EditText dateText = findViewById(R.id.driveDate);
        dateText.setText(sdf.format(date));
    }

    private void validateInputs(String d, String l, String r, String m, String n) {
        if(!vSelected || d.equals("") || l.equals("") || r.equals("") || m.equals("")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(DriveActivity.this);
            builder.setTitle("Error");
            builder.setMessage("Please enter all values before submitting");

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            builder.create().show();
            return;
        }

        logDrive(d, l, r, m, n);
    }

    private void logDrive(String d, final String l, final String r, final String m, final String n) {
        container.setVisibility(View.GONE);
        pBar.setVisibility(View.VISIBLE);

        DatabaseReference baseRef = FirebaseDatabase.getInstance().getReference("Vehicles");
        final DatabaseReference ref = baseRef.child(vehicle.getId()).child("drives").child(generateId());

        ref.child("date").setValue(d).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                ref.child("driverName").setValue(name).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        ref.child("location").setValue(l).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                ref.child("mileage").setValue(m).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        ref.child("notes").setValue(n).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                ref.child("reason").setValue(r).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        finishAndExit();
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
        });
    }

    private void finishAndExit() {
        Toast.makeText(DriveActivity.this, "Success!", Toast.LENGTH_LONG).show();
        DriveActivity.this.finish();
    }

    private String generateId() {
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder idBuilder = new StringBuilder();
        Random rnd = new Random();

        while (idBuilder.length() < 20) {
            int index = (int) (rnd.nextFloat() * chars.length());
            idBuilder.append(chars.charAt(index));
        }

        return idBuilder.toString();
    }
}
