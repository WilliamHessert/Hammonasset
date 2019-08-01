package com.hammollc.hammonasset;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Parcel;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LicenseActivity extends AppCompatActivity {

    private String uid;
    private EditText n, e;
    private Spinner s;
    private ImageView i;

    private ImageView lImage;
    private String encodedString = "";
    private String mCameraFileName = "";

    private ProgressBar pBar;
    private RelativeLayout view;

    private static final int CAMERA_REQUEST = 1888;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;

    private static final int STORAGE_REQUEST = 2888;
    private static final int RESULT_LOAD_IMG = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_license);
        setViews();
    }

    private void setViews() {
        pBar = findViewById(R.id.lProgress);
        lImage = findViewById(R.id.lImage);
        view = findViewById(R.id.lView);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("License Info");

        uid = getIntent().getStringExtra("uid");
        String[] info = getIntent().getStringArrayExtra("licInfo");

        final Calendar myCalendar = Calendar.getInstance();
        e = findViewById(R.id.lExpiration);
        assignValues(info);

        if(uid == null)
            setUid();

        Button sub = findViewById(R.id.lSubmitBtn);
        sub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });

        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, month);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel(myCalendar);
            }
        };

        e.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(LicenseActivity.this, date,
                        myCalendar.get(Calendar.YEAR),
                        myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        Button imgBtn = findViewById(R.id.lUploadBtn);
        imgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(LicenseActivity.this);
                builder.setTitle("Select");
                builder.setMessage("How would you like to add this image?");

                builder.setPositiveButton("Gallery", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        view.setVisibility(View.GONE);
                        pBar.setVisibility(View.VISIBLE);

                        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED) {
                            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                    STORAGE_REQUEST);
                        }
                        else {
                            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                            photoPickerIntent.setType("image/*");
                            startActivityForResult(photoPickerIntent, RESULT_LOAD_IMG);
                        }

                    }
                });

                builder.setNeutralButton("Camera", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (checkSelfPermission(Manifest.permission.CAMERA)
                                != PackageManager.PERMISSION_GRANTED) {
                            requestPermissions(new String[]{Manifest.permission.CAMERA},
                                    CAMERA_REQUEST);
                        }
                        else {
                            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                            StrictMode.setVmPolicy(builder.build());

                            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                            Date date = new Date();
                            DateFormat df = new SimpleDateFormat("MM/dd/yyyy-mm-ss", Locale.US);

                            String newPicFile = df.format(date) + ".jpg";
                            String outPath = "/sdcard/" + newPicFile;
                            File outFile = new File(outPath);

                            mCameraFileName = outFile.toString();
                            Uri outuri = Uri.fromFile(outFile);

                            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outuri);
                            startActivityForResult(cameraIntent, CAMERA_REQUEST);
                        }
                    }
                });

                builder.create().show();
            }
        });
    }

    private void updateLabel(Calendar myCalendar) {
        String myFormat = "MM/dd/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        e.setText(sdf.format(myCalendar.getTime()));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            } else {
                Toast.makeText(this, "Camera Permission Denied", Toast.LENGTH_LONG).show();
            }
        }
        else {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, RESULT_LOAD_IMG);
            } else {
                view.setVisibility(View.VISIBLE);
                pBar.setVisibility(View.GONE);
                Toast.makeText(this, "Storage Permission Denied", Toast.LENGTH_LONG).show();
            }
        }
    }


    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        view.setVisibility(View.VISIBLE);
        pBar.setVisibility(View.GONE);

        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            try {
                final Uri imageUri = Uri.fromFile(new File(mCameraFileName));
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap bitmap = BitmapFactory.decodeStream(imageStream);

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);

                lImage.setImageBitmap(bitmap);
                byte[] byteArray = byteArrayOutputStream.toByteArray();
                encodedString = Base64.encodeToString(byteArray, Base64.DEFAULT);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(LicenseActivity.this,
                        "Error loading image...", Toast.LENGTH_LONG).show();
            }
//            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
//            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
//
//            lImage.setImageBitmap(bitmap);
//            byte[] byteArray = byteArrayOutputStream .toByteArray();
//            encodedString = Base64.encodeToString(byteArray, Base64.DEFAULT);
        }
        else if (requestCode == RESULT_LOAD_IMG && resultCode == Activity.RESULT_OK) {
            try {
                final Uri imageUri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap bitmap = BitmapFactory.decodeStream(imageStream);

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);

                lImage.setImageBitmap(bitmap);
                byte[] byteArray = byteArrayOutputStream.toByteArray();
                encodedString = Base64.encodeToString(byteArray, Base64.DEFAULT);
            } catch (Exception e) {
                Toast.makeText(LicenseActivity.this,
                        "Error loading image...", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                LicenseActivity.this.finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void assignValues(String[] info) {
        n = findViewById(R.id.lNumber);
        s = findViewById(R.id.lState);
        i = findViewById(R.id.lImage);

        for(int k=0; k<info.length; k++) {
            if(info[k] == null)
                info[k] = "";
        }

        n.setText(info[0]);
        e.setText(info[1]);
        encodedString = info[3];

        String[] states = getResources().getStringArray(R.array.states);

        for(int j=0; j<states.length; j++) {
            if(states[j].equals(info[2])) {
                s.setSelection(j);
            }
        }

        if(encodedString.equals("failedDueToSize"))
            setEncodedString();
        else
            setImageView();

    }

    private void setEncodedString() {
        view.setVisibility(View.GONE);
        pBar.setVisibility(View.VISIBLE);

        DatabaseReference ref = FirebaseDatabase
                .getInstance().getReference("Users").child(uid).child("info").child("license");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                encodedString = dataSnapshot.child("licenseImage").getValue(String.class);

                if(encodedString == null)
                    encodedString = "";

                pBar.setVisibility(View.GONE);
                view.setVisibility(View.VISIBLE);
                setImageView();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }

    private void setImageView() {
        try {
            byte[] decodedString = Base64.decode(encodedString, Base64.DEFAULT);
            lImage.setImageBitmap(
                    BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length));
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void validateData() {
        String nu = n.getText().toString();
        String ex = e.getText().toString();
        String st = s.getSelectedItem().toString();

        String m = "";

        if(nu.equals("") || ex.equals(""))
            m += "Please complete all fields\n";
        if(st.equals("State"))
            m += "Please select in which state your license was issued\n";
        if(!isValidDate(ex))
            m += "Please enter a valid date in mm/dd/yyyy format (01/09/2021)\n";
        if(encodedString.equals(""))
            m += "Please upload an image of your license\n";

        if(m.equals("")) {
            uploadData(nu, ex, st);
        }
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(LicenseActivity.this);
            builder.setTitle("Error");
            builder.setMessage("Please correct these errors before submitting:\n"+m);

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            builder.create().show();
        }
    }

    private void uploadData(String nu, final String ex, final String st) {
        view.setVisibility(View.GONE);
        pBar.setVisibility(View.VISIBLE);

        final DatabaseReference ref = FirebaseDatabase
                .getInstance().getReference("Users").child(uid).child("info").child("license");

        ref.child("licenseNumber").setValue(nu).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                ref.child("licenseExpiration").setValue(ex).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        ref.child("licenseState").setValue(st).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                ref.child("licenseImage").setValue(encodedString).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        LicenseActivity.this.finish();
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });
    }

    private boolean isValidDate(String d) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
            sdf.parse(d);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    private void setUid() {
        try {
            uid = FirebaseAuth.getInstance().getUid();
        } catch (Exception e) {
            Toast.makeText(LicenseActivity.this,
                    "You must login again...", Toast.LENGTH_LONG).show();

            Intent i = new Intent(
                    LicenseActivity.this, LoginActivity.class);
            LicenseActivity.this.startActivity(i);
        }
    }
}