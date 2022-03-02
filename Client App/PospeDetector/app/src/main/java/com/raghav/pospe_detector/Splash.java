package com.raghav.pospe_detector;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class Splash extends AppCompatActivity {
    String[] PERMISSIONS_REQUIRED = {Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_BACKGROUND_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        requestPermissions();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean permissionGranted = true;
        if (grantResults.length > 0) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PERMISSION_GRANTED) {
                    permissionGranted = false;
                    break;
                }
            }
        } else {
            permissionGranted = false;
        }

        if (!permissionGranted) {
            new AlertDialog.Builder(this).setTitle("Please grant all the permissions to continue. \nYou can go to phone's settings >> Applications >> Orrder Driver and manually grant the permissions.")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            requestPermissions();
                        }
                    }).show();
        } else {
            redirect();
        }
    }

    void requestPermissions() {
        if (ContextCompat.checkSelfPermission(this, PERMISSIONS_REQUIRED[0]) == PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, PERMISSIONS_REQUIRED[1]) == PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, PERMISSIONS_REQUIRED[2]) == PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, PERMISSIONS_REQUIRED[3]) == PERMISSION_GRANTED||ContextCompat.checkSelfPermission(this, PERMISSIONS_REQUIRED[4]) == PERMISSION_GRANTED) {
            redirect();
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, PERMISSIONS_REQUIRED[0]) || ActivityCompat.shouldShowRequestPermissionRationale(this, PERMISSIONS_REQUIRED[1]) || ActivityCompat.shouldShowRequestPermissionRationale(this, PERMISSIONS_REQUIRED[2])||ActivityCompat.shouldShowRequestPermissionRationale(this, PERMISSIONS_REQUIRED[3])||ActivityCompat.shouldShowRequestPermissionRationale(this, PERMISSIONS_REQUIRED[4])) {
                new AlertDialog.Builder(this).setTitle("Please Accept all the permissions.").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(Splash.this, PERMISSIONS_REQUIRED, 100);
                    }
                }).show();
            } else {
                ActivityCompat.requestPermissions(this, PERMISSIONS_REQUIRED, 100);
            }
        }

    }

    private void redirect() {
        Thread t=new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    sleep(1000);
                    Intent i = new Intent(Splash.this, HomeActivity.class);
                    startActivity(i);
                    finish();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        t.start();
    }
}


