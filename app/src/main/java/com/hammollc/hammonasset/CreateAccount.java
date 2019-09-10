package com.hammollc.hammonasset;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CreateAccount extends AppCompatActivity {

    ProgressBar pBar;
    String userId;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        pBar = findViewById(R.id.createProgress);
        userId = getIntent().getStringExtra("userId");
        auth = FirebaseAuth.getInstance();

        Button create = findViewById(R.id.acctBtn);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createUser();
            }
        });
    }

    private void createUser() {
        EditText email = findViewById(R.id.eEmail);
        EditText ipass = findViewById(R.id.ePassword);
        EditText cpass = findViewById(R.id.cPassword);

        final String pass1 = ipass.getText().toString();
        String pass2 = cpass.getText().toString();

        if(pass1.equals(pass2)) {
            pBar.setVisibility(View.VISIBLE);

            final String ueml = email.getText().toString();
            auth.createUserWithEmailAndPassword(ueml, pass1)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        handleCreation(ueml, pass1, task);
                    }
                });
        }
        else {
            Toast.makeText(this, "Passwords don't match", Toast.LENGTH_LONG).show();
        }
    }

    private void handleCreation(final String ueml, String pass, final Task<AuthResult> task) {
        if(task.isSuccessful()) {
            auth.signInWithEmailAndPassword(ueml, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> localTask) {
                    if(task.isSuccessful()) {
                        String newId = auth.getUid();
                        addEmployeeRecord(ueml, newId);
                    }
                    else {
                        error();
                    }
                }
            });
        }
        else {
            error();
        }
    }

    private void addEmployeeRecord(final String eml, final String newId) {
        final String fName = getIntent().getStringExtra("fName");
        final String lName = getIntent().getStringExtra("lName");
        String name = fName+" "+lName;

        final FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference ref = db.getReference("employees").child(newId);

        ref.setValue(name).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                transition(eml, newId, db, fName, lName);
            }
        });
    }

    private void transition(String eml, String nid, FirebaseDatabase db, String fName, String lName) {
        String hDate = getIntent().getStringExtra("hDate");
        String type  = getIntent().getStringExtra("type");

        DatabaseReference ref = db.getReference("Users").child(nid);
        ref.child("grade").setValue(0);
        ref.child("type").setValue(type);

        DatabaseReference cRef = ref.child("eContact");
        cRef.child("email").setValue("");
        cRef.child("name").setValue("");
        cRef.child("pPhone").setValue("");
        cRef.child("relationship").setValue("");
        cRef.child("sPhone").setValue("");

        DatabaseReference iRef = ref.child("info");
        iRef.child("address").setValue("");
        iRef.child("city").setValue("");
        iRef.child("email").setValue(eml);
        iRef.child("fName").setValue(fName);
        iRef.child("hireDate").setValue(hDate);
        iRef.child("lName").setValue(lName);
        iRef.child("phone").setValue("");
        iRef.child("state").setValue("");
        iRef.child("zip").setValue("");

        db.getReference("unregistered").child(userId).removeValue();
        db.getReference("employees").child(userId).removeValue();
        db.getReference("Users").child(userId).removeValue();

        db.getReference("OldIds").push().setValue(userId);

        AlertDialog.Builder builder = new AlertDialog.Builder(CreateAccount.this);
        builder.setTitle("Success");
        builder.setMessage("Created Account Successfully");

        builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                pBar.setVisibility(View.GONE);
                Intent i = new Intent(CreateAccount.this, LoginActivity.class);
                CreateAccount.this.startActivity(i);
            }
        });

        builder.create().show();
    }

    private void error() {
        AlertDialog.Builder builder = new AlertDialog.Builder(CreateAccount.this);

        builder.setTitle("Error");
        builder.setMessage("Could not create account, please email" +
                "nucleusdevelopmentinc@gmail.com for IT Support. Sorry for the inconvenience.");
        builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();
    }
}
