package com.hammollc.hammonasset;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
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
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class MedicalCertificateActivity extends AppCompatActivity {

    private String uid;
    private EditText e;
    private ImageView i;

    private ImageView mImage;
    private String encodedString = "";

    private ProgressBar pBar;
    private RelativeLayout view;

    private static final int CAMERA_REQUEST = 1888;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;

    private static final int STORAGE_REQUEST = 2888;
    private static final int RESULT_LOAD_IMG = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medical_certificate);
        setViews();
    }

    private void setViews() {
        pBar = findViewById(R.id.mProgress);
        mImage = findViewById(R.id.mImage);
        view = findViewById(R.id.mView);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Medical Certificate");

        uid = getIntent().getStringExtra("uid");
        String[] info = getIntent().getStringArrayExtra("medInfo");
        assignValues(info);

        if(uid == null)
            setUid();

        Button sub = findViewById(R.id.mSubmitBtn);
        sub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });

        Button imgBtn = findViewById(R.id.mUploadBtn);
        imgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MedicalCertificateActivity.this);
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
                            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(cameraIntent, CAMERA_REQUEST);
                        }
                    }
                });

                builder.create().show();
            }
        });
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
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);

            mImage.setImageBitmap(bitmap);
            byte[] byteArray = byteArrayOutputStream .toByteArray();
            encodedString = Base64.encodeToString(byteArray, Base64.DEFAULT);
        }
        else if (requestCode == RESULT_LOAD_IMG && resultCode == Activity.RESULT_OK) {
            try {
                final Uri imageUri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap bitmap = BitmapFactory.decodeStream(imageStream);

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);

                mImage.setImageBitmap(bitmap);
                byte[] byteArray = byteArrayOutputStream.toByteArray();
                encodedString = Base64.encodeToString(byteArray, Base64.DEFAULT);
            } catch (Exception e) {
                Toast.makeText(MedicalCertificateActivity.this,
                        "Error loading image...", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                MedicalCertificateActivity.this.finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void assignValues(String[] info) {
        e = findViewById(R.id.mExpiration);
        i = findViewById(R.id.mImage);

        for(int k=0; k<info.length; k++) {
            if(info[k] == null)
                info[k] = "";
        }

        e.setText(info[0]);
        encodedString = info[1];

        if(encodedString.equals("failedDueToSize"))
            setEncodedString();
        else
            setImageView();

    }

    private void setEncodedString() {
        view.setVisibility(View.GONE);
        pBar.setVisibility(View.VISIBLE);

        DatabaseReference ref = FirebaseDatabase
                .getInstance().getReference("Users").child(uid).child("info").child("medicalCertificate");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                encodedString = dataSnapshot.child("medicalCertificateImage").getValue(String.class);

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
            mImage.setImageBitmap(
                    BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length));
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void validateData() {
        String ex = e.getText().toString();
        String m = "";

        if(ex.equals(""))
            m += "Please enter expiration date.\n";
        if(!isValidDate(ex))
            m += "Please enter a valid date in mm/dd/yyyy format (01/09/2021)\n";
        if(encodedString.equals(""))
            m += "Please upload an image of your medical certificate\n";

        if(m.equals("")) {
            uploadData(ex);
        }
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(MedicalCertificateActivity.this);
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

    private void uploadData(String ex) {
        view.setVisibility(View.GONE);
        pBar.setVisibility(View.VISIBLE);

        final DatabaseReference ref = FirebaseDatabase
                .getInstance().getReference("Users").child(uid).child("info").child("medicalCertificate");

        ref.child("medicalCertificateExpiration").setValue(ex).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                ref.child("medicalCertificateImage").setValue(encodedString).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        MedicalCertificateActivity.this.finish();
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
            Toast.makeText(MedicalCertificateActivity.this,
                    "You must login again...", Toast.LENGTH_LONG).show();

            Intent i = new Intent(
                    MedicalCertificateActivity.this, LoginActivity.class);
            MedicalCertificateActivity.this.startActivity(i);
        }
    }
}