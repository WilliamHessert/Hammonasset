package com.hammollc.hammonasset;

import android.content.DialogInterface;
import android.content.Intent;
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

public class PersonalInfo extends AppCompatActivity {

    private String uid;
    EditText fnm, lnm, hdt, pho, add, cty, ste, zip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_info);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        uid = getIntent().getStringExtra("uid");
        String[] perInfo = getIntent().getStringArrayExtra("perInfo");
        assignValues(perInfo);

        if(uid == null)
            setUid();

        Button sub = findViewById(R.id.iBtn);
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
                PersonalInfo.this.finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void assignValues(String[] info) {
        fnm = findViewById(R.id.fName);
        lnm = findViewById(R.id.lName);
        hdt = findViewById(R.id.hDte);
        pho = findViewById(R.id.iPhone);
        add = findViewById(R.id.address);
        cty = findViewById(R.id.city);
        ste = findViewById(R.id.state);
        zip = findViewById(R.id.zip);

        pop(info);
    }

    private void pop(String[] info) {
        add.setText(info[0]);
        cty.setText(info[1]);
        fnm.setText(info[2]);
        hdt.setText(info[3]);
        lnm.setText(info[4]);
        pho.setText(info[5]);
        zip.setText(info[7]);

        if(info[6].equals("")) {
            ste.setText("Connecticut");
        }
        else {
            ste.setText(info[6]);
        }
    }

    private void validateData() {
        String a = add.getText().toString();
        String c = cty.getText().toString();
        String s = ste.getText().toString();

        if(a.equals("") || c.equals("") || s.equals("")) {
            giveAlert("Please enter all fields");
        }
        else {
            String p = pho.getText().toString();

            if(p.length() != 10) {
                giveAlert("Please enter a valid 10-digit phone number");
            }
            else {
                String z = zip.getText().toString();

                if(z.length() != 5) {
                    giveAlert("Please enter a valid 5-digit zip code");
                }
                else {
                    updateValues(a, c, p, s, z);
                }
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

    private void updateValues(String a, final String c, final String p, final String s, final String z) {
        final DatabaseReference ref = FirebaseDatabase
                .getInstance().getReference().child("Users").child(uid).child("info");

        ref.child("address").setValue(a).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                ref.child("city").setValue(c).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        ref.child("phone").setValue(p).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                ref.child("state").setValue(s).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        ref.child("zip").setValue(z).addOnCompleteListener(new OnCompleteListener<Void>() {
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
                .child("personalInformation").setValue(false).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(PersonalInfo.this, "Success!", Toast.LENGTH_LONG).show();
                PersonalInfo.this.finish();
            }
        });
    }

    private void setUid() {
        try {
            uid = FirebaseAuth.getInstance().getUid();
        } catch (Exception e) {
            Toast.makeText(PersonalInfo.this,
                    "You must login again...", Toast.LENGTH_LONG).show();

            Intent i = new Intent(
                    PersonalInfo.this, LoginActivity.class);
            PersonalInfo.this.startActivity(i);
        }
    }
}
