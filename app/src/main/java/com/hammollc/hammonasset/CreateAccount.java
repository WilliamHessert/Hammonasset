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

    private void transition(final String eml, final String nid, final FirebaseDatabase db, final String fName, final String lName) {
        final String type  = getIntent().getStringExtra("type");

        final DatabaseReference ref = db.getReference("Users").child(nid);
        ref.child("grade").setValue(0).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                ref.child("type").setValue(type).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        uploadEmergencyContactInfo(ref, eml, nid, db, fName, lName);
                    }
                });
            }
        });
    }

    private void uploadEmergencyContactInfo(final DatabaseReference ref, final String eml,
                                            final String nid, final FirebaseDatabase db, final String fName, final String lName) {
        final DatabaseReference cRef = ref.child("eContact");
        cRef.child("email").setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                cRef.child("name").setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        cRef.child("pPhone").setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                cRef.child("relationship").setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        cRef.child("sPhone").setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                uploadPersonalInfo(ref, eml, nid, db, fName, lName);
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

    private void uploadPersonalInfo(final DatabaseReference ref, final String eml,
                                            final String nid, final FirebaseDatabase db, final String fName, final String lName) {
        final DatabaseReference iRef = ref.child("info");
        final String hDate = getIntent().getStringExtra("hDate");

        iRef.child("address").setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                iRef.child("city").setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        iRef.child("email").setValue(eml).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                iRef.child("fName").setValue(fName).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        iRef.child("hireDate").setValue(hDate).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                iRef.child("lName").setValue(lName).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        iRef.child("phone").setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                iRef.child("state").setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        iRef.child("zip").setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                uploadAlerts(nid, db);
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
                        });
                    }
                });
            }
        });
    }

    private void uploadAlerts(String nid, final FirebaseDatabase db) {
        final DatabaseReference aRef = db.getReference("alerts").child(nid);

        aRef.child("emergencyContact").setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                aRef.child("license").setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        aRef.child("medCard").setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                aRef.child("oshaCard").setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        aRef.child("personalInformation").setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                aRef.child("unregistered").setValue(false).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        removeOldInformation(db);
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

    private void removeOldInformation(final FirebaseDatabase db) {
        db.getReference("unregistered").child(userId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                db.getReference("employees").child(userId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        db.getReference("Users").child(userId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                db.getReference("OldIds").push().setValue(userId).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        alertAccountCreation();
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });
    }

    private void alertAccountCreation() {
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
