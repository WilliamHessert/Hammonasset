package com.hammollc.hammonasset;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EmergencyContact extends AppCompatActivity {

    private String uid;
    EditText nme, rel, pPh, sPh, eml;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_contact);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        uid = getIntent().getStringExtra("uid");
        String[] conInfo = getIntent().getStringArrayExtra("conInfo");
        assignValues(conInfo);

        if(uid == null)
            setUid();

        Button sub = findViewById(R.id.ecBtn);
        sub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                EmergencyContact.this.finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void assignValues(String[] info) {
        nme = findViewById(R.id.eName);
        rel = findViewById(R.id.eRel);
        pPh = findViewById(R.id.pPho);
        sPh = findViewById(R.id.sPho);
        eml = findViewById(R.id.ecEmail);

        pop(info);
    }

    private void pop(String[] info) {
        eml.setText(info[0]);
        nme.setText(info[1]);
        pPh.setText(info[2]);
        rel.setText(info[3]);
        sPh.setText(info[4]);
    }

    private void validateData() {
        String n = nme.getText().toString();
        String r = rel.getText().toString();
        String p = pPh.getText().toString();

        if(n.equals("") || r.equals("") || p.equals("")) {
            giveAlert("Please enter all required fields");
        }
        else {
            String s = sPh.getText().toString();
            String e = eml.getText().toString();

            if(s.equals("") && e.equals("")) {
                giveAlert("Either enter 2 phone " +
                        "numbers or enter the primary phone number and an email addresses");
            }
            else {
                updateValues(n, r, p, s, e);
            }
        }
    }

    private void giveAlert(String mess) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Error");
        builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.setMessage(mess);
        builder.create().show();
    }

    private void updateValues(final String n, final String r, final String p, final String s, String e) {
        final DatabaseReference ref = FirebaseDatabase
                .getInstance().getReference().child("Users").child(uid).child("eContact");

        ref.child("email").setValue(e).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                ref.child("name").setValue(n).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        ref.child("pPhone").setValue(p).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                ref.child("relationship").setValue(r).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        ref.child("sPhone").setValue(s).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                updateAlerts();
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

    private void updateAlerts() {
        FirebaseDatabase.getInstance().getReference("alerts").child(uid)
                .child("emergencyContact").setValue(false).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(EmergencyContact.this, "Success!", Toast.LENGTH_LONG).show();
                EmergencyContact.this.finish();
            }
        });
    }

    private void setUid() {
        try {
            uid = FirebaseAuth.getInstance().getUid();
        } catch (Exception e) {
            Toast.makeText(EmergencyContact.this,
                    "You must login again...", Toast.LENGTH_LONG).show();

            Intent i = new Intent(
                    EmergencyContact.this, LoginActivity.class);
            EmergencyContact.this.startActivity(i);
        }
    }
}
