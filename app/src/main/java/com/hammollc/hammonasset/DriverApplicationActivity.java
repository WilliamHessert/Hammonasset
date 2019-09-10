package com.hammollc.hammonasset;

import android.app.Dialog;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DriverApplicationActivity extends AppCompatActivity {

    private String mode;
    private Button cont;
    private ArrayList<String> values = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_application);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        getSupportActionBar().setTitle("Driver Application");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        setInitView();
    }

    private void setInitView() {
        mode = "init";

        final EditText fnm = findViewById(R.id.daFirstName);
        final EditText lnm = findViewById(R.id.daLastName);
        final EditText add = findViewById(R.id.daAddress);
        final EditText cty = findViewById(R.id.daCity);
        final EditText ste = findViewById(R.id.daState);
        final EditText zip = findViewById(R.id.daZip);
        final EditText dob = findViewById(R.id.daDob);
        final EditText ssn = findViewById(R.id.daSocial);

        final List<String> towns = Arrays.asList(getResources().getStringArray(R.array.statesFull));
        ste.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(DriverApplicationActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setCancelable(false);
                dialog.setContentView(R.layout.dialog_select_view);

                ListView tList = dialog.findViewById(R.id.selectList);
                ArrayAdapter<String> tAdapter = new ArrayAdapter<>(
                        DriverApplicationActivity.this, android.R.layout.simple_list_item_1, towns);
                tList.setAdapter(tAdapter);

                tList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String town = towns.get(position);
                        ste.setText(town);
                        dialog.dismiss();
                    }
                });

                Button closeBtn = dialog.findViewById(R.id.closeDialog);
                closeBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                dialog.show();
            }
        });

        cont = findViewById(R.id.daContBtn);
        cont.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String f = fnm.getText().toString();
                String l = lnm.getText().toString();
                String a = add.getText().toString();
                String c = cty.getText().toString();
                String s = ste.getText().toString();
                String z = zip.getText().toString();
                String d = dob.getText().toString();
                String h = ssn.getText().toString();

                validateInitValues(f, l, a, c, s, z, d, h);
            }
        });
    }

    private void validateInitValues(String fnm, String lnm, String add,
                                    String cty, String ste, String zip, String dob, String ssn) {

        String mess = "";

        if(fnm.equals("") || lnm.equals("") || add.equals("")
                || cty.equals("") || ste.equals("") || dob.equals(""))
            mess = "Please enter all values before continuing.\n\n";

        if(zip.length() != 5)
            mess += "Please enter a 5-digit zip code";


        if(ssn.length() != 9)
            mess += "Please enter a 9-digit social security number";

        if(mess.equals("")) {
            values.add(fnm);
            values.add(lnm);
            values.add(add);
            values.add(cty);
            values.add(ste);
            values.add(zip);
            values.add(dob);
            values.add(ssn);

            setAddressView();
        }
    }

    private void setAddressView() {
        mode = "addr";
    }

    public void remAddressBlock(int i) {

    }

}
