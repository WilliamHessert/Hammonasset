package com.hammollc.hammonasset;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class ReceiptActivity extends AppCompatActivity {

    private String uid, name;

    private ProgressBar pBar;
    private EditText dateText;
    private RelativeLayout rView;
    private Button uploadBtn, submitBtn;

    private ImageView rImage;
    private String encodedString = "";

    private FirebaseDatabase db;
    private ArrayList<String> poNumbers;

    private static final int CAMERA_REQUEST = 1888;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;

    private static final int STORAGE_REQUEST = 2888;
    private static final int RESULT_LOAD_IMG = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt);
        setValues();
    }

    private void setValues() {
        String fName = getIntent().getStringExtra("fName");
        String lName = getIntent().getStringExtra("lName");
        name = fName+" "+lName;

        uid = getIntent().getStringExtra("uid");
        initiateViews();
    }

    private void initiateViews() {
        getSupportActionBar().setTitle("Upload Receipt");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        rView = findViewById(R.id.rView);
        rImage = findViewById(R.id.rImage);
        pBar = findViewById(R.id.receiptProgress);

        dateText = findViewById(R.id.rDate);
        uploadBtn = findViewById(R.id.rUploadBtn);
        submitBtn = findViewById(R.id.rSubmitBtn);

        setDateText();
        db = FirebaseDatabase.getInstance();

        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ReceiptActivity.this);
                builder.setTitle("Select");
                builder.setMessage("How would you like to add this image?");

                builder.setPositiveButton("Gallery", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        rView.setVisibility(View.GONE);
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

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        downloadPoNumsNum();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                ReceiptActivity.this.finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setDateText() {
        final Calendar myCalendar = Calendar.getInstance();
        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                updateDateText(myCalendar);
            }
        };

        dateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(ReceiptActivity.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }

    private void updateDateText(Calendar c) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy", Locale.US);
        String dateString = sdf.format(c.getTime());
        dateText.setText(dateString);
    }

    private void downloadPoNumsNum() {
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Contracts").child("16PSX0176").child("poNums");
        ref.child("number").addListenerForSingleValueEvent(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                downloadPoNums(Integer.parseInt(dataSnapshot.getValue(String.class)), ref);
            }

            public void onCancelled(DatabaseError databaseError) { }
        });
    }

    private void downloadPoNums(final int num, DatabaseReference ref) {
        final ArrayList<String> poNums = new ArrayList<>();
        ref.addChildEventListener(new ChildEventListener() {
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (!dataSnapshot.getKey().equals("number")) {
                    poNums.add(dataSnapshot.getKey());

                    if (num == poNums.size()) {
                        poNumbers = poNums;
                        showView();
                    }
                }
            }

            public void onChildChanged(DataSnapshot dataSnapshot, String s) { }

            public void onChildRemoved(DataSnapshot dataSnapshot) { }

            public void onChildMoved(DataSnapshot dataSnapshot, String s) { }

            public void onCancelled(DatabaseError databaseError) { }
        });
    }

    private void showView() {
        pBar.setVisibility(View.GONE);
        rView.setVisibility(View.VISIBLE);
        allowPoNumberDialog();
    }

    private void allowPoNumberDialog() {
        final EditText pText = findViewById(R.id.rPoNum);
        pText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectPoNum(pText);
            }
        });
    }

    private void selectPoNum(final EditText pText) {
        final Dialog dialog = new Dialog(ReceiptActivity.this);
        dialog.setTitle("Select PO Number");
        dialog.setCancelable(false);

        dialog.setContentView(R.layout.dialog_select_view);
        ListView poNumList = dialog.findViewById(R.id.selectList);

        poNumList.setAdapter(new ArrayAdapter(ReceiptActivity.this, android.R.layout.simple_list_item_1, poNumbers));
        poNumList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                pText.setText(poNumbers.get(position));
                dialog.dismiss();
            }
        });

        ((Button) dialog.findViewById(R.id.closeDialog)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
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
                rView.setVisibility(View.VISIBLE);
                pBar.setVisibility(View.GONE);
                Toast.makeText(this, "Storage Permission Denied", Toast.LENGTH_LONG).show();
            }
        }
    }


    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        rView.setVisibility(View.VISIBLE);
        pBar.setVisibility(View.GONE);

        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);

            rImage.setImageBitmap(bitmap);
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

                rImage.setImageBitmap(bitmap);
                byte[] byteArray = byteArrayOutputStream.toByteArray();
                encodedString = Base64.encodeToString(byteArray, Base64.DEFAULT);
            } catch (Exception e) {
                Toast.makeText(ReceiptActivity.this,
                        "Error loading image...", Toast.LENGTH_LONG).show();
            }
        }
    }
}
