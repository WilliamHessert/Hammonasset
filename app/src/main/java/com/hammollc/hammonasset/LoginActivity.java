package com.hammollc.hammonasset;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    CheckBox remBox;
    EditText eEmail;
    EditText ePassw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        remBox = findViewById(R.id.remBox);
        Button logBtn = findViewById(R.id.loginBtn);
        Button regBtn = findViewById(R.id.regBtn);

        eEmail = findViewById(R.id.enterEmail);
        ePassw = findViewById(R.id.enterPassword);
        pop();

        logBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
                LoginActivity.this.startActivity(i);
            }
        });

        if(ContextCompat.checkSelfPermission(this, android.Manifest
                .permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {

            ActivityCompat.requestPermissions(
                    this, new String[] {
                            android.Manifest.permission.ACCESS_FINE_LOCATION  }, 0);
        }

        TextView pText = findViewById(R.id.privacyText);
        TextView fPass = findViewById(R.id.forgotPassword);

        pText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://www.iubenda.com/privacy-policy/47554841";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                LoginActivity.this.startActivity(i);
            }
        });

        fPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                forgotPassword(FirebaseAuth.getInstance());
            }
        });
    }

    private void attemptLogin(){
        String email = eEmail.getText().toString();
        String passw = ePassw.getText().toString();
        rem(email, passw);

        Intent i = new Intent(LoginActivity.this, ProcessorActivity.class);
        i.putExtra("email", email);
        i.putExtra("passw", passw);
        LoginActivity.this.startActivity(i);
    }

    private void pop() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String email = preferences.getString("email", "");
        String passw = preferences.getString("passw", "");

        eEmail.setText(email);
        ePassw.setText(passw);

        if(!email.equals("") && !passw.equals("")) {
            remBox.performClick();
            attemptLogin();
        }
    }

    private void rem(String em, String ps) {
        if(!remBox.isChecked()) {
            em = "";
            ps = "";
        }

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("email", em);
        editor.putString("passw", ps);
        editor.apply();
    }

    private void forgotPassword(final FirebaseAuth auth) {
        final Context c = LoginActivity.this;

        final AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setTitle("Reset Password");
        builder.setMessage("Are you sure you want to reset your password?");

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                promptForEmail(c, auth);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();
    }

    private void promptForEmail(final Context c, final FirebaseAuth auth) {
        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setTitle("Enter Email");

        View dialogView = LayoutInflater.from(c).inflate(R.layout.dialog_email, null);
        final EditText eText = dialogView.findViewById(R.id.emailDialog);
        builder.setView(dialogView);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String email = eText.getText().toString();

                if(email.contains("@")) {
                    sendForgotPasswordEmail(email, c, auth);
                }
                else {
                    AlertDialog.Builder eBuilder = new AlertDialog.Builder(c);
                    eBuilder.setTitle("Invalid Email");
                    eBuilder.setMessage("Please enter a valid email address");

                    eBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            promptForEmail(c, auth);
                            dialog.dismiss();
                        }
                    });

                    eBuilder.create().show();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();
    }

    private void sendForgotPasswordEmail(String email, final Context c, FirebaseAuth auth) {
        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(c);
                        String title = "Error";
                        String message = "We're sorry, there was an error sending you a reset " +
                                "password email. Please confirm you entered the correct email." +
                                " If you're experiencing technical difficulties, please contact" +
                                " your supervisor";

                        if (task.isSuccessful()) {
                            title = "Email Sent";
                            message = "An email has been sent to you with a link to reset your " +
                                    "password.";
                        }

                        builder.setTitle(title);
                        builder.setMessage(message);
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                        builder.create().show();
                    }
                });
    }
}
