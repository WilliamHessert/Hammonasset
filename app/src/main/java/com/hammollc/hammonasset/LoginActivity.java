package com.hammollc.hammonasset;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

public class LoginActivity extends AppCompatActivity {

    CheckBox remBox;
    EditText eEmail;
    EditText ePassw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

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
        pText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://www.iubenda.com/privacy-policy/47554841";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                LoginActivity.this.startActivity(i);
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
}
