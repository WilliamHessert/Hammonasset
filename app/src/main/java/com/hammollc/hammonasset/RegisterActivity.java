package com.hammollc.hammonasset;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class RegisterActivity extends AppCompatActivity {

    ArrayList<DataSnapshot> users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        users = new ArrayList<>();
        populateUsers();

        final RelativeLayout enterLayout = findViewById(R.id.ssnCont);
        final ProgressBar progressBar = findViewById(R.id.ssnProgress);

        Button enterSsn = findViewById(R.id.findEmp);
        enterSsn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enterLayout.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                findEmp();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                RegisterActivity.this.finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void populateUsers() {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        usersRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                users.add(dataSnapshot);
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

    private void findEmp() {
        ArrayList<DataSnapshot> matchedUsers = new ArrayList<>();

        for(int i=0; i<users.size(); i++) {
            DataSnapshot snap = users.get(i);
            String userId = snap.getKey();

            if(compare(userId)) {
                matchedUsers.add(snap);
            }
        }

        Log.i("AHHH", ""+matchedUsers.size());
        if(matchedUsers.size() == 0) {
            notMe();
        }
        else if(matchedUsers.size() > 1) {
            sepMatches(matchedUsers);
        }
        else if(matchedUsers.size() == 1){
            DataSnapshot snap = matchedUsers.get(0);

            EditText eSsn = findViewById(R.id.eSsn);
            String entSsn = eSsn.getText().toString();

            String userId = snap.getKey();
            String fName = snap.child("info").child("fName").getValue().toString();
            String lName = snap.child("info").child("lName").getValue().toString();
            String hDate = snap.child("info").child("hireDate").getValue().toString();
            String type = snap.child("type").getValue().toString();

            confirmEmp(userId, fName, lName, hDate, type, entSsn);
        }
    }

    private boolean compare(String userId) {
        EditText eSsn = findViewById(R.id.eSsn);
        String entSsn = eSsn.getText().toString();
        Log.i("AHHH", userId+" "+entSsn+" "+getSsn(userId));
        return (!userId.equals("userId") && entSsn.equals(getSsn(userId)));
    }

    private String getSsn(String userId) {
        if(userId.length() == 20) {
            String userSsn = "";

            userSsn += userId.substring(19);
            userSsn += userId.substring(18, 19);
            userSsn += userId.substring(12, 13);
            userSsn += userId.substring(10, 11);

            return userSsn;
        }

        return "";
    }

    private void confirmEmp(final String userId, final String fName,
                            final String lName, final String hDate, final String type, String ssn) {
        removeProgress();
        final RelativeLayout conf = findViewById(R.id.confCont);
        conf.setVisibility(View.VISIBLE);

        TextView confText = findViewById(R.id.confText);
        String text = "Is this you?\n\n"+fName+" "+lName+"\nHired on: "+hDate+"\nSocial Security: ***-**-"+ssn;
        confText.setText(text);
        confText.setVisibility(View.VISIBLE);

        Button confBtn = findViewById(R.id.confBtn);
        confBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(RegisterActivity.this, CreateAccount.class);
                i.putExtra("userId", userId);

                i.putExtra("fName", fName);
                i.putExtra("lName", lName);
                i.putExtra("hDate", hDate);
                i.putExtra("type", type);

                RegisterActivity.this.startActivity(i);
            }
        });

        Button cancelBtn = findViewById(R.id.notMe);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                conf.setVisibility(View.GONE);
                notMe();
            }
        });
    }

    private void notMe() {
        removeProgress();

        RelativeLayout notMe = findViewById(R.id.contactAdmin);
        notMe.setVisibility(View.VISIBLE);

        Button reEnter = findViewById(R.id.reenterSsn);
        reEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RegisterActivity.this.recreate();
            }
        });
    }

    private void sepMatches(final ArrayList<DataSnapshot> matchedUsers) {
        AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
        builder.setTitle("Need More Info");
        builder.setMessage("To better help identify you, please enter your last name");

        final EditText input = new EditText(RegisterActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);

        input.setLayoutParams(lp);
        builder.setView(input);

        builder.setPositiveButton("Enter Name", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String lName = input.getText().toString();
                checkLastName(matchedUsers, lName);
            }
        });

        builder.create().show();
    }

    private void checkLastName(ArrayList<DataSnapshot> matchedUsers, String lName) {
        boolean haveMatch = false;

        for(int i = 0; i<matchedUsers.size(); i++) {
            DataSnapshot snap = matchedUsers.get(i);
            String matchedName = snap.child("info").child("lName").getValue().toString();

            if(lName.equals(matchedName)) {
                String usrId = snap.getKey();
                String usSsn = getSsn(usrId);
                String fName = snap.child("info").child("fName").getValue().toString();
                String hDate = snap.child("info").child("hireDate").getValue().toString();
                String type = snap.child("type").getValue().toString();

                haveMatch = true;
                confirmEmp(usrId, fName, lName, hDate, type, usSsn);
                i = users.size();
            }
        }

        if(!haveMatch) {
            notMe();
        }
    }

    private void removeProgress() {
        ProgressBar pBar = findViewById(R.id.ssnProgress);
        pBar.setVisibility(View.GONE);
    }
}
