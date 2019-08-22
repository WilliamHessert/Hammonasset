package com.hammollc.hammonasset;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProcessorActivity extends AppCompatActivity {

    String uid, fName, lName, email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_processor);
        attemptLogin();
    }

    private void attemptLogin() {
        email = getIntent().getStringExtra("email");
        String passw = getIntent().getStringExtra("passw");

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, passw)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {

            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful())
                    getAccountType();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i("AHHH", e.getLocalizedMessage());
                displayLoginError();
            }
        });
    }

    private void getAccountType() {
        uid = FirebaseAuth.getInstance().getUid();
        DatabaseReference ref = FirebaseDatabase
                .getInstance().getReference().child("Users").child(uid).child("type");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                handleType(dataSnapshot.getValue(String.class));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }

    private void handleType(String t) {
        handleForeman(t);
//        if(t.equals("Laborer"))
//            openLaborer();
//        else
//            handleForeman();
    }

    private void openLaborer() {
        Intent i = new Intent(ProcessorActivity.this, MainActivity.class);
        ProcessorActivity.this.startActivity(i);
        ProcessorActivity.this.finish();
    }

    private void handleForeman(final String type) {
        final DatabaseReference ref =
                FirebaseDatabase.getInstance().getReference("Users").child(uid).child("info");
        ref.child("fName").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                setFirstName(dataSnapshot.getValue(String.class));
                ref.child("lName").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        setLastName(dataSnapshot.getValue(String.class));
                        openForeman(type);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) { }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }

    private void setFirstName(String fName) {
        this.fName = fName;
    }

    private void setLastName(String lName) {
        this.lName = lName;
    }

    private void openForeman(String type) {
        Intent i = new Intent(ProcessorActivity.this, ForemanActivity.class);
        i.putExtra("uid", uid);

        i.putExtra("fName", fName);
        i.putExtra("lName", lName);
        i.putExtra("email", email);
        i.putExtra("type", type);

        ProcessorActivity.this.startActivity(i);
        ProcessorActivity.this.finish();
    }

    private void displayLoginError() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ProcessorActivity.this);
        builder.setTitle("Login Error");
        builder.setMessage("Error logging in. Please make sure your email and password are correct");

        builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                ProcessorActivity.this.finish();
            }
        });

        builder.create().show();
    }

    private void displayTypeError(String t) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ProcessorActivity.this);
        builder.setTitle("Not Mobile Recommended");
        builder.setMessage(t+" accounts have a much better experience on a computer. Please log" +
                " in at hammollc.com on a computer to continue");

        builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                ProcessorActivity.this.finish();
            }
        });

        builder.create().show();
    }
}
