package com.hammollc.hammonasset;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
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

public class MaintenanceActivity extends AppCompatActivity {

    private String uid, name;

    private Vehicle vehicle;
    private boolean vSelected = false;
    private ArrayList<Vehicle> vehicles;
    private ArrayList<String> vehicleNames;

    private ProgressBar pBar;
    private RelativeLayout container;
    private EditText dateFieldToPopulate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maintenance);

        getSupportActionBar().setTitle("Log Maintenance");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        getExtraValues();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                MaintenanceActivity.this.finish();
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
        pBar = findViewById(R.id.maintenanceProgress);
        container = findViewById(R.id.maintenanceContainer);

        pBar.setVisibility(View.GONE);
        container.setVisibility(View.VISIBLE);

        EditText nameText = findViewById(R.id.maintenanceNameField);
        final EditText vehcText = findViewById(R.id.maintenanceVehicleField);

        nameText.setText(name);
        vehcText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(MaintenanceActivity.this);
                dialog.requestWindowFeature(1);
                dialog.setCancelable(false);

                dialog.setContentView(R.layout.dialog_select_view);
                ListView vehList = dialog.findViewById(R.id.selectList);
                vehList.setAdapter(new ArrayAdapter(
                        MaintenanceActivity.this, android.R.layout.simple_list_item_1, vehicleNames));

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

        final EditText sDate = findViewById(R.id.maintenanceStartDate);
        final EditText eDate = findViewById(R.id.maintenanceEndDate);
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

        sDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateFieldToPopulate = sDate;
                DatePickerDialog dialog = new DatePickerDialog(
                        MaintenanceActivity.this,
                        dateSetListener, calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

                dialog.show();
            }
        });

        eDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateFieldToPopulate = eDate;
                DatePickerDialog dialog = new DatePickerDialog(
                        MaintenanceActivity.this,
                        dateSetListener, calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

                dialog.show();
            }
        });

        final EditText typeText = findViewById(R.id.maintenanceType);
        final EditText costText = findViewById(R.id.maintenanceCost);
        final EditText descText = findViewById(R.id.maintenanceNotes);

        Button submitBtn = findViewById(R.id.maintenanceSubmitBtn);
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sdte = sDate.getText().toString();
                String type = typeText.getText().toString();
                String cost = costText.getText().toString();

                String edte = eDate.getText().toString();
                String desc = descText.getText().toString();

                validateFields(sdte, type, cost, edte, desc);
            }
        });
    }

    private void updateDateLabel(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        dateFieldToPopulate.setText(sdf.format(date));
    }

    private void validateFields(String s, String t, String c, String e, String d) {
        if(!vSelected || s.equals("") || t.equals("") || c.equals("")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MaintenanceActivity.this);
            builder.setTitle("Error");
            builder.setMessage("Please complete all fields before continuing");

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            builder.create().show();
            return;
        }

        logMaintenance(s, t, c, e, d);
    }

    private void logMaintenance(final String s, final String t, String c, final String e, final String d) {
        container.setVisibility(View.GONE);
        pBar.setVisibility(View.VISIBLE);

        DatabaseReference baseRef = FirebaseDatabase.getInstance().getReference("Vehicles");
        final DatabaseReference ref = baseRef.child(vehicle.getId()).child("maintenance").child(generateId());

        ref.child("cost").setValue(c).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                ref.child("description").setValue(d).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        ref.child("endDate").setValue(e).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                ref.child("loggedBy").setValue(name).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        ref.child("startDate").setValue(s).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                ref.child("type").setValue(t).addOnCompleteListener(new OnCompleteListener<Void>() {
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
        Toast.makeText(MaintenanceActivity.this, "Success!", Toast.LENGTH_LONG).show();
        MaintenanceActivity.this.finish();
    }

    private String generateId() {
        String SALTCHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder idBuilder = new StringBuilder();
        Random rnd = new Random();

        while (idBuilder.length() < 20) {
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            idBuilder.append(SALTCHARS.charAt(index));
        }

        return idBuilder.toString();
    }
}
