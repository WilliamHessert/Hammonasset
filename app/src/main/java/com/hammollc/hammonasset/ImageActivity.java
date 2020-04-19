package com.hammollc.hammonasset;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class ImageActivity extends AppCompatActivity {

    private String uid, name;

    private ProgressBar pBar;
    private RelativeLayout iView;

    private EditText imageName;
    private EditText bridgeNumber;
    private Button uploadBtn, submitBtn;

    private ImageView iImage;
    private Bitmap iBitmap = null;
    private String encodedString = "";
    private String mCameraFileName = "";

    private static final int CAMERA_REQUEST = 1888;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;

    private static final int STORAGE_REQUEST = 2888;
    private static final int RESULT_LOAD_IMG = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

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

        iView = findViewById(R.id.iView);
        iImage = findViewById(R.id.iImage);
        pBar = findViewById(R.id.imageProgress);

        uploadBtn = findViewById(R.id.iUploadBtn);
        submitBtn = findViewById(R.id.iSubmitBtn);

        imageName = findViewById(R.id.iName);
        bridgeNumber = findViewById(R.id.iBridgeNumber);

        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ImageActivity.this);
                builder.setTitle("Select");
                builder.setMessage("How would you like to add this image?");

                builder.setPositiveButton("Gallery", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        iView.setVisibility(View.GONE);
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

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validate();
            }
        });

        pBar.setVisibility(View.GONE);
        iView.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                ImageActivity.this.finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void validate() {
        String name = imageName.getText().toString();
        String bNum = bridgeNumber.getText().toString().replaceFirst("^0+(?!$)", "");

        if(name.isEmpty() || bNum.isEmpty()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(ImageActivity.this);
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

        uploadImage(name, bNum);
    }

    private void uploadImage(final String name, final String bNum) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        final String date = sdf.format(new Date());

        iView.setVisibility(View.GONE);
        pBar.setVisibility(View.VISIBLE);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        iBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        final String iid = generateId();
        final StorageReference ref = FirebaseStorage
                .getInstance().getReference().child("images/"+date+"_"+bNum+"_"+iid+".jpg");

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
                    uploadValues(iid, downloadUri.toString(), date, name, bNum);
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ImageActivity.this);
                    builder.setTitle("Error");
                    builder.setMessage("Could not upload receipt at this time. Please try again later");

                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ImageActivity.this.finish();
                        }
                    });

                    builder.create().show();
                }
            }
        });
    }

    private void uploadValues(String id, String url, String date, final String imageName, String bNum) {
        EditText eNotes = findViewById(R.id.iNotes);
        final String notes = eNotes.getText().toString();
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("images").child(bNum).child(date).child(id);

        ref.child("url").setValue(url).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                ref.child("name").setValue(imageName).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        ref.child("foremanName").setValue(name).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                ref.child("notes").setValue(notes).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        closeActivity();
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });
    }

    private void closeActivity() {
        Toast.makeText(this, "Image Successfully Uploaded", Toast.LENGTH_LONG).show();
        finish();
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
                iView.setVisibility(View.VISIBLE);
                Toast.makeText(this, "Storage Permission Denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        pBar.setVisibility(View.GONE);
        iView.setVisibility(View.VISIBLE);

        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            try {
                final Uri imageUri = Uri.fromFile(new File(mCameraFileName));
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap bitmap = BitmapFactory.decodeStream(imageStream);

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);

                iBitmap = bitmap;
                iImage.setImageBitmap(bitmap);

                byte[] byteArray = byteArrayOutputStream.toByteArray();
                encodedString = Base64.encodeToString(byteArray, Base64.DEFAULT);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(ImageActivity.this,
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

                iBitmap = bitmap;
                iImage.setImageBitmap(bitmap);

                byte[] byteArray = byteArrayOutputStream.toByteArray();
                encodedString = Base64.encodeToString(byteArray, Base64.DEFAULT);
            } catch (Exception e) {
                Toast.makeText(ImageActivity.this,
                        "Error loading image...", Toast.LENGTH_LONG).show();
            }
        }
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
