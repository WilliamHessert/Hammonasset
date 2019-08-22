package com.hammollc.hammonasset;

import android.app.DatePickerDialog;
import android.app.Dialog;
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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class VehicleReceiptActivity extends AppCompatActivity {

    private String uid, name;

    private Vehicle vehicle;
    private ArrayList<Vehicle> vehicles;
    private ArrayList<String> vehicleNames;

    private ProgressBar pBar;
    private RelativeLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_receipt);

        getSupportActionBar().setTitle("Upload a Receipt");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        getExtraValues();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                VehicleReceiptActivity.this.finish();
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
        pBar = findViewById(R.id.vehicleProgress);
        container = findViewById(R.id.vehicleContainer);

        pBar.setVisibility(View.GONE);
        container.setVisibility(View.VISIBLE);

        EditText nameText = findViewById(R.id.vehicleNameField);
        final EditText vehcText = findViewById(R.id.vehicleVehicleField);

        nameText.setText(name);
        vehcText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(VehicleReceiptActivity.this);
                dialog.requestWindowFeature(1);
                dialog.setCancelable(false);

                dialog.setContentView(R.layout.dialog_select_view);
                ListView vehList = dialog.findViewById(R.id.selectList);
                vehList.setAdapter(new ArrayAdapter(
                        VehicleReceiptActivity.this, android.R.layout.simple_list_item_1, vehicleNames));

                vehList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
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
    }
}
