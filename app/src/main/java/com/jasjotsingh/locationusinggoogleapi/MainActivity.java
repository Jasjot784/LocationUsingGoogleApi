package com.jasjotsingh.locationusinggoogleapi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class MainActivity extends AppCompatActivity implements FetchAddressTask.OnTaskCompleted {
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private static final String TRACKING_LOCATION_KEY = "tracking_location";
    Button button_location;
    public static final String TAG = MainActivity.class.getSimpleName();
    Location mLastLocation;
    ImageView mAndroidImageView;
    AnimatorSet mRotateAnim;
    TextView textView_location;
    Boolean mTrackingLocation = false;
    FusedLocationProviderClient mFusedLocationProviderClient;
    LocationCallback mLocationCallback;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            mTrackingLocation = savedInstanceState.getBoolean(
                    TRACKING_LOCATION_KEY);
        }

        mAndroidImageView = (ImageView) findViewById(R.id.imageview_android);

        mRotateAnim = (AnimatorSet) AnimatorInflater.loadAnimator
                (this, R.animator.rotate);

        mRotateAnim.setTarget(mAndroidImageView);

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        textView_location = findViewById(R.id.textview_location);
        button_location = findViewById(R.id.button_location);
        button_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //getLocation();
                if (!mTrackingLocation) {
                    startTrackingLocation();
                } else {
                    stopTrackingLocation();
                }
            }
        });

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                // If tracking is turned on, reverse geocode into an address
                if (mTrackingLocation) {
                    new FetchAddressTask(MainActivity.this, MainActivity.this)
                            .execute(locationResult.getLastLocation());
                }
            }
        };
    }
    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]
                            {Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        } else {
//            mFusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
//                @Override
//                public void onSuccess(Location location) {
//                    if (location != null) {
////                        mLastLocation = location;
////                        textView_location.setText(
////                                getString(R.string.location_text,
////                                        mLastLocation.getLatitude(),
////                                        mLastLocation.getLongitude(),
////                                        mLastLocation.getTime()));
//                        new FetchAddressTask(MainActivity.this,
//                                MainActivity.this).execute(location);
//                        textView_location.setText(getString(R.string.address_text,
//                                getString(R.string.loading),
//                                System.currentTimeMillis()));
//                    } else {
//                        textView_location.setText(R.string.no_location);
//                    }
//                }
//            });
            mFusedLocationProviderClient.requestLocationUpdates
                    (getLocationRequest(), mLocationCallback,
                            null /* Looper */);

        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSION:
                // If the permission is granted, get the location,
                // otherwise, show a Toast
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //getLocation();
                    startTrackingLocation();
                } else {
                    Toast.makeText(this,
                            R.string.location_permission_denied,
                            Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public void onTaskCompleted(String result) {
        if (mTrackingLocation){
            // Update the UI
            textView_location.setText(getString(R.string.address_text,
                    result, System.currentTimeMillis()));
        }

    }
    public void startTrackingLocation(){
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]
                            {Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        } else {
            mTrackingLocation = true;
            mFusedLocationProviderClient.requestLocationUpdates
                    (getLocationRequest(),
                            mLocationCallback,
                            null /* Looper */);

            // Set a loading text while you wait for the address to be
            // returned
            textView_location.setText(getString(R.string.address_text,
                    getString(R.string.loading),
                    System.currentTimeMillis()));
            button_location.setText(R.string.stop_tracking_location);
            mRotateAnim.start();
        }
    }
    public void stopTrackingLocation(){
        if (mTrackingLocation) {
            mTrackingLocation = false;
            button_location.setText(R.string.start_tracking_location);
            textView_location.setText(R.string.textview_hint);
            mRotateAnim.end();
            mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
        }
    }
    private LocationRequest getLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }

    @Override
    protected void onResume() {
        if (mTrackingLocation)
            startTrackingLocation();
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (mTrackingLocation){
            stopTrackingLocation();
            mTrackingLocation = true;
        }
        super.onPause();
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(TRACKING_LOCATION_KEY, mTrackingLocation);
        super.onSaveInstanceState(outState);
    }
}