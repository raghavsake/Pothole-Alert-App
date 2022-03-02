package com.raghav.pospe_detector;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.raghav.pospe_detector.Utils.Common;
import com.raghav.pospe_detector.Utils.IUploadCallbacks;
import com.raghav.pospe_detector.Utils.ProgressRequestBody;
import com.raghav.pospe_detector.retrofit.IUploadAPI;
import com.raghav.pospe_detector.retrofit.RetrofitClient;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportActivity extends AppCompatActivity implements IUploadCallbacks {

    Uri selectedFileUri;
    ProgressDialog dialog;
    private static final int PICK_FILE_REQUEST = 1000;
    public static final int CAMERA_PERM_CODE = 101;
    public static final int CAMERA_REQUEST_CODE = 102;
    private static final int PERMISSION_REQ_ID = 22;
    public static final int GALLERY_REQUEST_CODE = 105;
    TextView head;
    TextView desc;
    ImageView im;
    Button back;
    Button upload;
    String currentPhotoPath;
    Button select;
    IUploadAPI mService;
    String service;
    private static final String[] REQUESTED_PERMISSIONS = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.INTERNET};


    private IUploadAPI getAPIUpload() {
        return RetrofitClient.getClient().create(IUploadAPI.class);
    }

    private void showAlertDialogReportDetail() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(ReportActivity.this);
        String[] items = {"Report with Image", "Report without Image"};
        final int[] checkedItem = {1};
        alertDialog.setTitle("Select the Option!").setSingleChoiceItems(items, checkedItem[0], new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                checkedItem[0] = which;
            }
        }).setPositiveButton("Proceed", (dialog, which) -> {

            if (service.equals("Pothole")) {
                if (items[checkedItem[0]].equals("Report with Image")) {
                    head.setVisibility(View.VISIBLE);
                    head.setText("Report Pothole");

                    desc.setVisibility(View.VISIBLE);
                    desc.setText("Upload an image to report a pothole");

                    back.setVisibility(View.VISIBLE);
                    select.setVisibility(View.VISIBLE);
                } else {
                    final String[] lat = {""};
                    final String[] lon = {""};
                    FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(ReportActivity.this);
                    if (ActivityCompat.checkSelfPermission(ReportActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(ReportActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    fusedLocationClient.getLastLocation()
                            .addOnSuccessListener(ReportActivity.this, location -> {
                                if (location != null) {
                                    lat[0] = "" + location.getLatitude();
                                    lon[0] = "" + location.getLongitude();
                                    RequestBody latitude = RequestBody.create(MediaType.parse("multipart/form-data"), lat[0]);
                                    RequestBody longitude = RequestBody.create(MediaType.parse("multipart/form-data"), lon[0]);
                                    mService = getAPIUpload();
                                    mService.reportPothole(latitude, longitude).enqueue(new Callback<String>() {
                                        @Override
                                        public void onResponse(Call<String> call, Response<String> response) {
                                            if (response.body().equals("success"))
                                                Toast.makeText(ReportActivity.this, "Pothole Reported!!", Toast.LENGTH_SHORT).show();
                                            else
                                                Toast.makeText(ReportActivity.this, "Pothole Could Not Be Reported!!", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                                            finish();
                                        }

                                        @Override
                                        public void onFailure(Call<String> call, Throwable t) {
                                            Toast.makeText(ReportActivity.this, "Pothole Could Not Be Reported!!", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                                            finish();
                                        }
                                    });
                                }
                            });

                }
            } else {
                if (items[checkedItem[0]].equals("Report with Image")) {
                    head.setVisibility(View.VISIBLE);
                    head.setText("Report SpeedBreaker");

                    desc.setVisibility(View.VISIBLE);
                    desc.setText("Upload an image to report a speedbreaker");

                    back.setVisibility(View.VISIBLE);
                    select.setVisibility(View.VISIBLE);
                } else {
                    final String[] lat = {""};
                    final String[] lon = {""};
                    FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(ReportActivity.this);
                    if (ActivityCompat.checkSelfPermission(ReportActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(ReportActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    fusedLocationClient.getLastLocation()
                            .addOnSuccessListener(ReportActivity.this, location -> {
                                if (location != null) {
                                    lat[0] = "" + location.getLatitude();
                                    lon[0] = "" + location.getLongitude();
                                    RequestBody latitude = RequestBody.create(MediaType.parse("multipart/form-data"), lat[0]);
                                    RequestBody longitude = RequestBody.create(MediaType.parse("multipart/form-data"), lon[0]);
                                    mService = getAPIUpload();
                                    mService.reportPothole(latitude, longitude).enqueue(new Callback<String>() {
                                        @Override
                                        public void onResponse(Call<String> call, Response<String> response) {
                                            if (response.body().equals("success"))
                                                Toast.makeText(ReportActivity.this, "Speedbreaker Reported!!", Toast.LENGTH_SHORT).show();
                                            else
                                                Toast.makeText(ReportActivity.this, "Speedbreaker Could Not Be Reported!!", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                                            finish();
                                        }

                                        @Override
                                        public void onFailure(Call<String> call, Throwable t) {
                                            Toast.makeText(ReportActivity.this, "Speedbreaker Could Not Be Reported!!", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                                            finish();
                                        }
                                    });
                                }
                            });

                }
            }
            dialog.dismiss();
        }).setNegativeButton("Back", (dialog, which) -> {
            startActivity(new Intent(getApplicationContext(), HomeActivity.class));
            finish();
            dialog.dismiss();
        });
        AlertDialog alert = alertDialog.create();
        alert.setCanceledOnTouchOutside(false);
        alert.show();
    }

    private void showAlertDialogReport() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(ReportActivity.this);
        String[] items = {"Report Pothole", "Report Speedbreaker"};
        final int[] checkedItem = {1};
        alertDialog.setTitle("Select the option!").setSingleChoiceItems(items, checkedItem[0], new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                checkedItem[0] = which;
            }
        }).setPositiveButton("Proceed", (dialog, which) -> {
            if (items[checkedItem[0]].equals("Report Pothole")) {
                service = "Pothole";
            } else {
                service = "Speed Breaker";
            }
            showAlertDialogReportDetail();
            dialog.dismiss();
        }).setNegativeButton("Back", (dialog, which) -> {
            startActivity(new Intent(getApplicationContext(), HomeActivity.class));
            finish();
            dialog.dismiss();
        });
        AlertDialog alert = alertDialog.create();
        alert.setCanceledOnTouchOutside(false);
        alert.show();

    }


    private void chooseFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_FILE_REQUEST);
    }


    @Override
    public void onProgressUpdate(int percent) {

    }

    public boolean checkSelfPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(this,
                permission)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    REQUESTED_PERMISSIONS,
                    requestCode);
            return false;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {


        upload.setVisibility(View.VISIBLE);
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == CAMERA_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                File f = new File(currentPhotoPath);
                im.setImageURI(Uri.fromFile(f));
                im.setVisibility(View.VISIBLE);

                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri contentUri = Uri.fromFile(f);
                mediaScanIntent.setData(contentUri);
                this.sendBroadcast(mediaScanIntent);
                selectedFileUri = Uri.fromFile(f);

            }

        }

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PICK_FILE_REQUEST) {
                if (data != null) {
                    selectedFileUri = data.getData();
                    Log.d("tag", "ABsolute Url of Image is " + selectedFileUri);
                    if (selectedFileUri != null && !selectedFileUri.getPath().isEmpty()) {
                        im.setVisibility(View.VISIBLE);
                        im.setImageURI(selectedFileUri);
                    } else
                        Toast.makeText(this, "File not found", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.raghav.pospe_detector",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
//        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        head = findViewById(R.id.report_report);
        head.setVisibility(View.INVISIBLE);

        desc = findViewById(R.id.report_desc);
        desc.setVisibility(View.INVISIBLE);

        im = findViewById(R.id.report_image);
        im.setVisibility(View.INVISIBLE);

        showAlertDialogReport();

        back = findViewById(R.id.report_back);
        back.setVisibility(View.INVISIBLE);
        back.setOnClickListener(v -> {
            Intent i = new Intent(ReportActivity.this, HomeActivity.class);
            startActivity(i);

        });


        select = findViewById(R.id.report_select);
        select.setOnClickListener(v -> {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(ReportActivity.this);
            String[] items = {"From Gallery", "Click an Image"};
            final int[] checkedItem = {1};
            alertDialog.setTitle("Select the option!").setSingleChoiceItems(items, checkedItem[0], new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    checkedItem[0] = which;
                }
            }).setPositiveButton("Proceed", (dialog, which) -> {
                if (items[checkedItem[0]].equals("From Gallery")) {
                    chooseFile();
                } else {
                    dispatchTakePictureIntent();
                }
                dialog.dismiss();
            }).setNegativeButton("Back", (dialog, which) -> {
                startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                finish();
                dialog.dismiss();
            });
            AlertDialog alert = alertDialog.create();
            alert.setCanceledOnTouchOutside(false);
            alert.show();
        });
        select.setVisibility(View.INVISIBLE);

        upload = findViewById(R.id.report_upload);
        upload.setVisibility(View.INVISIBLE);
        upload.setOnClickListener(v -> {
            try {
                uploadFile();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    private void uploadFile() throws InterruptedException {
        upload.setVisibility(View.INVISIBLE);
        select.setVisibility(View.INVISIBLE);
        desc.setText("Here is the detected image!");
        dialog = new ProgressDialog(ReportActivity.this);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setMessage("Uploading...");
        dialog.setIndeterminate(false);
        dialog.setMax(100);
        dialog.setCancelable(false);
        dialog.show();
        File file = null;
        try {

            file = new File(Common.getFilePath(this, selectedFileUri));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        if (file != null) {
            final ProgressRequestBody requestBody = new ProgressRequestBody(file, this);

            MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestBody.create(MediaType.parse("image/*"), file));
            final double[] lat = {0.00};
            final double[] lon = {0.0};
            FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(ReportActivity.this);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                return;
            }
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(ReportActivity.this, location -> {
                        if (location != null) {
                            lat[0] = location.getLatitude();
                            lon[0] = location.getLongitude();
                            RequestBody latitude = RequestBody.create(MediaType.parse("multipart/form-data"), ""+lat[0]);
                            RequestBody longitude = RequestBody.create(MediaType.parse("multipart/form-data"), ""+lon[0]);
                            mService = getAPIUpload();
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    if(service.equals("Pothole")) {
                                        mService.reportPothole(body,latitude,longitude).enqueue(new Callback<String>() {
                                            @Override
                                            public void onResponse(Call<String> call, Response<String> response) {
                                                if(response.body().equals("success")) {
                                                    Toast.makeText(ReportActivity.this, "Pothole Reported!!", Toast.LENGTH_SHORT).show();
                                                String image_processed_link = new StringBuilder("http://192.168.0.178:5000/" +
                                                        response.body().replace("\"", "")).toString();
                                                Picasso.get().load(image_processed_link)
                                                        .into(im);
                                                im.setVisibility(View.VISIBLE);
                                                im.setVisibility(View.VISIBLE);


                                                }
                                                else {
                                                    Toast.makeText(ReportActivity.this, "Pothole Could not be Reported!!", Toast.LENGTH_SHORT).show();
                                                }

                                            }

                                            @Override
                                            public void onFailure(Call<String> call, Throwable t) {
                                                Toast.makeText(ReportActivity.this, "Speedbreaker Could not Reported!!", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                    else {
                                        mService.reportSpeedBreaker(body,latitude,longitude).enqueue(new Callback<String>() {
                                            @Override
                                            public void onResponse(Call<String> call, Response<String> response) {
                                                dialog.dismiss();
                                                if (response.body().equals("success"))
                                                    Toast.makeText(ReportActivity.this, "Speedbreaker Reported!!", Toast.LENGTH_SHORT).show();
                                                else
                                                    Toast.makeText(ReportActivity.this, "Speedbreaker Could not Reported!!", Toast.LENGTH_SHORT).show();

                                                startActivity(new Intent(getApplicationContext(),HomeActivity.class));
                                                finish();
                                            }

                                            @Override
                                            public void onFailure(Call<String> call, Throwable t) {
                                                Toast.makeText(ReportActivity.this, "Speedbreaker Could not Reported!!", Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(getApplicationContext(),HomeActivity.class));
                                                finish();
                                            }
                                        });
                                    }


                                }
                            }).start();
                        }
                    });



        }

    }
}
