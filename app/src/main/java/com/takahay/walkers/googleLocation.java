package com.takahay.walkers;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.support.v4.content.ContextCompat;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;

/**
 * Created by takahay on 2018/01/24.
 */

public class googleLocation extends Activity{

    private static final String TAG = "walkers.googleLocation";

    private WalkerPref sValues;
    /**
     * Provides access to the Fused Location Provider API.
     */
    private FusedLocationProviderClient mFusedLocationClient;

    /**
     * Provides access to the Location Settings API.
     */
    private SettingsClient mSettingsClient;

    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    private LocationRequest mLocationRequest;

    /**
     * Stores the types of location services the client is interested in using. Used for checking
     * settings to determine if the device has optimal location settings.
     */
    private LocationSettingsRequest mLocationSettingsRequest;

    /**
     * Callback for Location events.
     */
    private LocationCallback mLocationCallback;

    private LocationCallBack StackLocationCallback;

    private Context mContext;

    /**
     *      Constructor
     */
    public googleLocation(  Context context, WalkerPref sv, LocationCallBack callback)
    {
        Log.i(TAG, "googleLocation");

        mContext = context;
        sValues = sv;
        StackLocationCallback = callback;
    }

    public boolean createRequest()
    {
        Log.i(TAG, "createRequest start");

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext);
        mSettingsClient = LocationServices.getSettingsClient(mContext);

        // Kick off the process of building the LocationCallback, LocationRequest, and
        // LocationSettingsRequest objects.
        createLocationCallback();
        createLocationRequest();
        buildLocationSettingsRequest();

        if ( Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission( mContext, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission( mContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "createRequest permission error.");
            return false;
        }

        FusedLocationProviderClient client =
                LocationServices.getFusedLocationProviderClient(mContext);
//
//        Intent intent = new Intent(mContext, MainActivity.class);
//        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 999, intent, PendingIntent.FLAG_ONE_SHOT);
//
//
//        client.requestLocationUpdates(LocationRequest.create(), pendingIntent)
//                .addOnCompleteListener(new OnCompleteListener() {
//                    @Override
//                    public void onComplete(@NonNull Task task) {
//                        Log.i(TAG, "Result: " + task.getResult());
//                    }
//                });


        /**
         *  Looper : 	The Looper object whose message queue will be used to
         *  implement the callback mechanism, or null to make callbacks on the calling thread.
         */
        client.requestLocationUpdates(mLocationRequest,
                mLocationCallback,
                null /* Looper */);

        new HttpResHelper().postStatusCode( 3, 1 );
        Log.i(TAG, "createRequest finish");
        return true;
    }

    /**
     * Creates a callback for receiving location events.
     */
    private void createLocationCallback() {
        Log.i(TAG, "createLocationCallback start");


        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);


                /**
                 *  call location stack func here.
                 */
                Log.i(TAG, "onLocationResult");

                Location lot = locationResult.getLastLocation();
                StackLocationCallback.stackLocation(lot,
                            sValues.LocationMinimumDistance,
                            sValues.LocationRejectAccuracy,
                            sValues.LocationStackcount);

//                for (Location lot : locationResult.getLocations()) {
//                    StackLocationCallback.stackLocation(lot,
//                            sValues.getLocationMinimumDistance(),
//                            sValues.getLocationRejectAccuracy(),
//                            sValues.getLocationStackcount());
//                }
            }
        };

        Log.i(TAG, "createLocationCallback finish");
    }

    /**
     * Sets up the location request. Android has two location request settings:
     * {@code ACCESS_COARSE_LOCATION} and {@code ACCESS_FINE_LOCATION}. These settings control
     * the accuracy of the current location. This sample uses ACCESS_FINE_LOCATION, as defined in
     * the AndroidManifest.xml.
     * <p/>
     * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
     * interval (5 seconds), the Fused Location Provider API returns location updates that are
     * accurate to within a few feet.
     * <p/>
     * These settings are appropriate for mapping applications that show real-time location
     * updates.
     */
    private void createLocationRequest() {

        Log.i(TAG, "createLocationRequest start");

        Log.i(TAG, String.format("createLocationRequest " +
                        "interval=%d smallestDisplacement=%f",
                sValues.LocationUpdateInterval,
                sValues.LocationSmallestDisplacementForAPI));

        mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        long interval = sValues.LocationUpdateInterval;
        mLocationRequest.setInterval(interval);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(interval/2);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        mLocationRequest.setSmallestDisplacement(sValues.LocationSmallestDisplacementForAPI);

        Log.i(TAG, "createLocationRequest finish");
    }

    /**
     * Uses a {@link com.google.android.gms.location.LocationSettingsRequest.Builder} to build
     * a {@link com.google.android.gms.location.LocationSettingsRequest} that is used for checking
     * if a device has the needed location settings.
     */
    private void buildLocationSettingsRequest() {
        Log.i(TAG, "buildLocationSettingsRequest");

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }
}
