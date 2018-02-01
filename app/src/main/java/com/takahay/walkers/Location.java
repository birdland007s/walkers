package com.takahay.walkers;

import android.content.Context;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.location.LocationRequest;

/**
 * Created by takahay on 2018/01/24.
 */

public class Location extends AppCompatActivity {

    private static final String TAG = "walkers.Location";
    private WalkerPref sValues;
    private LocationManager mLocationManager = null;

    private LocationCallBack locationCallback;

    private Context mContext;
    /**
     *      Constructor
     */
    public Location(  Context context, WalkerPref sv, LocationCallBack callback)
    {
        mContext = context;
        locationCallback = callback;
        sValues = sv;
    }

    /**
     * Requests location updates from the FusedLocationApi. Note: we don't call this unless location
     * runtime permission has been granted.
     */
    public boolean startLocationUpdates() {
        boolean status = false;
        Log.e(TAG, "startLocationUpdates start");


        long interval = sValues.LocationUpdateInterval;
        float distance = sValues.LocationSmallestDisplacementForAPI;

        initializeLocationManager();
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, interval, distance,
                    mLocationListeners[1]);
            status = true;
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }

        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, interval, distance,
                    mLocationListeners[0]);
            status = true;
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }

        new HttpResHelper().postStatusCode( 3, 1 );
        return status;
    }

    private void initializeLocationManager() {
        Log.i(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        }
    }

    private class LocationListener implements android.location.LocationListener{
        android.location.Location mLastLocation;
        public LocationListener(String provider)
        {
            Log.i(TAG, "LocationListener " + provider);
            mLastLocation = new android.location.Location(provider);
        }
        @Override
        public void onLocationChanged(android.location.Location location)
        {
            Log.i(TAG, "onLocationChanged: " + location);
            mLastLocation.set(location);
            locationCallback.stackLocation(location,
                    sValues.LocationMinimumDistance,
                    sValues.LocationRejectAccuracy,
                    sValues.LocationStackcount);
        }
        @Override
        public void onProviderDisabled(String provider)
        {
            Log.i(TAG, "onProviderDisabled: " + provider);
        }
        @Override
        public void onProviderEnabled(String provider)
        {
            Log.i(TAG, "onProviderEnabled: " + provider);
        }
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
            Log.i(TAG, "onStatusChanged: " + provider);
        }
    }
    LocationListener[] mLocationListeners = new LocationListener[] {
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };


}

