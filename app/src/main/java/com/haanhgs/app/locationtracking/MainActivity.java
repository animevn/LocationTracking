package com.haanhgs.app.locationtracking;

import android.Manifest;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.button_location)
    Button bnLocation;
    @BindView(R.id.textview_location)
    TextView tvLocation;
    @BindView(R.id.imageview_android)
    ImageView ivAndroid;

    private static final String TAG = "D.MainActivity";
    private static final String TRACKING_KEY = "is_tracking";

    private static final int PERMISSION = 1;
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final int GRANTED = PackageManager.PERMISSION_GRANTED;

    private AnimatorSet asRotate;

    private FusedLocationProviderClient fusedLocationClient;
    private Location lastLocation;
    private LocationCallback locationCallback;
    private boolean isTracking;

    private void setupAnimatorSet() {
        asRotate = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.rotate);
        asRotate.setTarget(ivAndroid);
    }

    private LocationRequest locationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }

    private void startTrackingLocation() {
        if (ActivityCompat.checkSelfPermission(this, FINE_LOCATION) != GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{FINE_LOCATION}, PERMISSION);
        } else {
            isTracking = true;
            fusedLocationClient.requestLocationUpdates(locationRequest(), locationCallback, null);
            tvLocation.setText(getString(R.string.address_text,
                    getString(R.string.loading),
                    System.currentTimeMillis()));
            bnLocation.setText(R.string.stop_tracking_location);
            asRotate.start();
        }
    }

    private void stopTrackingLocation() {
        if (isTracking) {
            isTracking = false;
            bnLocation.setText(R.string.start_tracking_location);
            tvLocation.setText(R.string.textview_hint);
            asRotate.end();

        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION:
                // If the permission is granted, get the location, otherwise, show a Toast
                if (grantResults.length > 0 && grantResults[0] == GRANTED) {
                    startTrackingLocation();
                } else {
                    Toast.makeText(this, R.string.location_permission_denied, Toast.LENGTH_SHORT)
                            .show();
                }
                break;
        }
    }

    private void initFuseLocation(){
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                tvLocation.setText(getString(R.string.location_text,
                        location.getLatitude(),
                        location.getLongitude(),
                        location.getTime()));
            } else {
                tvLocation.setText(getString(R.string.no_location));
            }
        });
    }

    private void initLocationCallback(){
        locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Repo.getAddress(MainActivity.this, locationResult.getLastLocation())
                        .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null){
                        tvLocation.setText(getString(R.string.address_text,
                                task.getResult(),
                                System.currentTimeMillis()));
                    }

                });
            }
        };
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setupAnimatorSet();
        initFuseLocation();
        initLocationCallback();
    }

    @Override
    protected void onPause() {
        if (isTracking) {
            stopTrackingLocation();
            isTracking = true;
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (isTracking) {
            startTrackingLocation();
        }
        super.onResume();
    }

    @OnClick(R.id.button_location)
    public void onViewClicked() {
        if (!isTracking){
            startTrackingLocation();
        }else {
            stopTrackingLocation();
        }
    }
}
