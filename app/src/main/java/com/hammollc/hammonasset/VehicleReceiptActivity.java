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
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class VehicleReceiptActivity extends AppCompatActivity {

    private String uid, name;

    private Vehicle vehicle;
    private boolean vSelected = false;
    private ArrayList<Vehicle> vehicles;
    private ArrayList<String> vehicleNames;

    private ProgressBar pBar;
    private RelativeLayout container;

    private static final int CAMERA_REQUEST = 1888;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;

    private static final int STORAGE_REQUEST = 2888;
    private static final int RESULT_LOAD_IMG = 200;

    private ImageView receiptImage;
    private String encodedString = "";
    private Bitmap receiptBitmap = null;
    private String mCameraFileName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_receipt);

        getSupportActionBar().setTitle("Upload a Receipt");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        getExtraValues();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                VehicleReceiptActivity.this.finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void getExtraValues() {
        Bundle extras = getIntent().getExtras();
        uid = extras.getString("uid", "");
        name = extras.getString("fName", "")+
                " "+extras.getString("lName", "");

        loadVehicles();
    }

    private void loadVehicles() {
        vehicles = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Vehicles");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot childSnapshot: dataSnapshot.getChildren()) {
                    String id = childSnapshot.getKey();
                    String name = childSnapshot.child("name").getValue(String.class);
                    String plates = childSnapshot.child("plates").getValue(String.class);

                    if(childSnapshot.child("drivers").getValue() == null ||
                            childSnapshot.child("drivers").child(uid).getValue() != null) {
                        vehicles.add(new Vehicle(id, name, plates));
                    }
                }

                loadVehicleNames();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }

    private void loadVehicleNames() {
        vehicleNames = new ArrayList<>();

        for(Vehicle vehicle: vehicles) {
            vehicleNames.add(vehicle.getName()+" - "+vehicle.getPlates());
        }

        loadViews();
    }

    private void loadViews() {
        pBar = findViewById(R.id.vehicleProgress);
        container = findViewById(R.id.vehicleContainer);

        pBar.setVisibility(View.GONE);
        container.setVisibility(View.VISIBLE);

        EditText nameText = findViewById(R.id.vehicleNameField);
        final EditText vehcText = findViewById(R.id.vehicleVehicleField);

        nameText.setText(name);
        vehcText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(VehicleReceiptActivity.this);
                dialog.requestWindowFeature(1);
                dialog.setCancelable(false);

                dialog.setContentView(R.layout.dialog_select_view);
                ListView vehList = dialog.findViewById(R.id.selectList);
                vehList.setAdapter(new ArrayAdapter(
                        VehicleReceiptActivity.this, android.R.layout.simple_list_item_1, vehicleNames));

                vehList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                        vSelected = true;
                        vehicle = vehicles.get(position);
                        vehcText.setText(vehicle.getName());

                        dialog.dismiss();
                    }
                });

                dialog.findViewById(R.id.closeDialog).setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                dialog.create();
                dialog.show();
            }
        });

        final EditText dateText = findViewById(R.id.vehicleReceiptDate);
        final Calendar calendar = Calendar.getInstance();

        final DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                updateDateLabel(calendar.getTime());
            }
        };

        dateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog dialog = new DatePickerDialog(
                        VehicleReceiptActivity.this,
                        dateSetListener, calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

                dialog.show();
            }
        });

        final CheckBox pFuelBox = findViewById(R.id.vehicleFuelCheckbox);
        final TextView pFuelText = findViewById(R.id.vehicleFuelTextView);
        final RelativeLayout nonFuelHolder = findViewById(R.id.nonFuelPurchaseFieldsHolder);

        pFuelBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                    nonFuelHolder.setVisibility(View.INVISIBLE);
                else
                    nonFuelHolder.setVisibility(View.VISIBLE);
            }
        });

        pFuelText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pFuelBox.performClick();
            }
        });

        receiptImage = findViewById(R.id.vehicleReceiptImage);
        Button uploadBtn = findViewById(R.id.vehicleReceiptUploadBtn);

        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(VehicleReceiptActivity.this);
                builder.setTitle("Select");
                builder.setMessage("How would you like to add this image?");

                builder.setPositiveButton("Gallery", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        container.setVisibility(View.GONE);
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

        final EditText costText = findViewById(R.id.vehicleReceiptCost);
        final EditText itemText = findViewById(R.id.vehicleReceiptItem);
        final EditText reasText = findViewById(R.id.vehicleReceiptReason);
        final EditText apprText = findViewById(R.id.vehicleReceiptApprover);

        Button submitBtn = findViewById(R.id.vehicleReceiptSubmitBtn);
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String date = dateText.getText().toString();
                String cost = costText.getText().toString();
                String item = itemText.getText().toString();
                String reas = reasText.getText().toString();
                String appr = apprText.getText().toString();

                boolean fuel = pFuelBox.isChecked();
                validateFields(date, cost, item, reas, appr, fuel);
            }
        });
    }

    private void updateDateLabel(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        EditText dateText = findViewById(R.id.vehicleReceiptDate);
        dateText.setText(sdf.format(date));
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
                pBar.setVisibility(View.GONE);
                container.setVisibility(View.VISIBLE);
                Toast.makeText(this, "Storage Permission Denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        pBar.setVisibility(View.GONE);
        container.setVisibility(View.VISIBLE);

        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            try {
                final Uri imageUri = Uri.fromFile(new File(mCameraFileName));
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap bitmap = BitmapFactory.decodeStream(imageStream);

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);

                receiptBitmap = bitmap;
                receiptImage.setImageBitmap(bitmap);

                byte[] byteArray = byteArrayOutputStream.toByteArray();
                encodedString = Base64.encodeToString(byteArray, Base64.DEFAULT);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(VehicleReceiptActivity.this,
                        "Error loading image...", Toast.LENGTH_LONG).show();
            }
        }
        else if (requestCode == RESULT_LOAD_IMG && resultCode == Activity.RESULT_OK) {
            try {
                final Uri imageUri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap bitmap = BitmapFactory.decodeStream(imageStream);

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);

                receiptBitmap = bitmap;
                receiptImage.setImageBitmap(bitmap);

                byte[] byteArray = byteArrayOutputStream.toByteArray();
                encodedString = Base64.encodeToString(byteArray, Base64.DEFAULT);
            } catch (Exception e) {
                Toast.makeText(VehicleReceiptActivity.this,
                        "Error loading image...", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void validateFields(String d, String c, String i, String r, String a, boolean f) {
        if(!vSelected || d.equals("") || c.equals("") || (!f && (i.equals("") || r.equals("") || a.equals(""))) || encodedString.equals("")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(VehicleReceiptActivity.this);
            builder.setTitle("Error");
            builder.setMessage("Please enter all values before submitting");

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            builder.create().show();
            return;
        }

        uploadReceiptImage(d, c, i, r, a, f);
    }

    private void uploadReceiptImage(final String d, final String c,
                                    final String i, final String r, final String a, final boolean f) {

        container.setVisibility(View.GONE);
        pBar.setVisibility(View.VISIBLE);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        receiptBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        final String rid = generateId();
        final String vid = vehicle.getId();
        final StorageReference ref = FirebaseStorage
                .getInstance().getReference().child("vehicleReceipts/"+d+"_"+vid+"_"+rid+".jpg");

        UploadTask uploadTask = ref.putBytes(data);
        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                // Continue with the task to get the download URL
                return ref.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    uploadValues(vid, rid, downloadUri.toString(), d, c, i, r, a, f);
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(VehicleReceiptActivity.this);
                    builder.setTitle("Error");
                    builder.setMessage("Could not upload receipt at this time. Please try again later");

                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            VehicleReceiptActivity.this.finish();
                        }
                    });

                    builder.create().show();
                }
            }
        });
    }

    private void uploadValues(String vid, String rid, final String u, final String d,
                              final String c, final String i, final String r, String a, final boolean f) {

        final DatabaseReference ref = FirebaseDatabase
                .getInstance().getReference("Vehicles").child(vid).child("receipts").child(rid);

        ref.child("approver").setValue(a).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                ref.child("cost").setValue(c).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        ref.child("date").setValue(d).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                ref.child("downloadUrl").setValue(u).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        ref.child("driverName").setValue(name).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                ref.child("fuel").setValue(f).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        ref.child("item").setValue(i).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                ref.child("reason").setValue(r).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        finishAndExit();
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

    private void finishAndExit() {
        Toast.makeText(VehicleReceiptActivity.this, "Success!", Toast.LENGTH_LONG).show();
        VehicleReceiptActivity.this.finish();
    }

    private String generateId() {
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder idBuilder = new StringBuilder();
        Random rnd = new Random();

        while (idBuilder.length() < 20) {
            int index = (int) (rnd.nextFloat() * chars.length());
            idBuilder.append(chars.charAt(index));
        }

        return idBuilder.toString();
    }
}
