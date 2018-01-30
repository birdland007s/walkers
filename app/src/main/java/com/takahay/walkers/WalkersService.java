package com.takahay.walkers;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import android.location.Location;

import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.ConnectionResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by takahay on 2018/01/22.
 */



public class WalkersService extends Service {
    private static final String TAG = "walkers.WalkerService";

    /**
     * Constant used in the location settings dialog.
     */
    private static final int REQUEST_CHECK_SETTINGS = 0x1;

    /**
     *  set ACCURACY_DECIMAL_POINT = 2, then 123.43564 -> 123.44
     */
    private static final int ACCURACY_DECIMAL_POINT = 2;
    private static final int DURATION_DECIMAL_POINT = 2;

    /**
     * Stores the types of location services the client is interested in using. Used for checking
     * settings to determine if the device has optimal location settings.
     */
    private LocationSettingsRequest mLocationSettingsRequest;

    /*
    *   Structure for Stock Data
    * */
    private class LocationData {
        public double longitude;
        public double latitude;
        public double accuracy;
        public Date time;
        // Time duration in minute.
        public double duration;
        public double distance;
    }

    private ArrayList<LocationData> LocationDataArray = new ArrayList<>();
    private LocationData mLastLocationData = null;

    /**
     *
     * @param v         It is a value which is be rounded off
     * @param point     It is a decimal point, that is if this number is 2, then 1234.34543 becomes 1234.35.
     * @return
     */
    private double RoundOffDouble( double v, int point )
    {
        return Math.round( v * Math.pow(10, (double)point ))
                / Math.pow(10, (double)point );
    }

    /**
     * Calculate distance between two points in latitude and longitude taking
     * into account height difference. If you are not interested in height
     * difference pass 0.0. Uses Haversine method as its base.
     *
     * lat1, lon1 Start point lat2, lon2 End point el1 Start altitude in meters
     * el2 End altitude in meters
     * @returns Distance in Meters
     */
    public static double getDistance(double lat1, double lon1, double el1,
                                  double lat2, double lon2, double el2 ){

        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        double height = el1 - el2;

        distance = Math.pow(distance, 2) + Math.pow(height, 2);

        return Math.sqrt(distance);
    }

    private LocationCallBack callback = new LocationCallBack() {

        @Override
        public void stackLocation(Location location, double MinimumDistance,
                                  double RejectAccuracy, int Stackcount) {
            if (location != null) {

                double lotAccuracy = RoundOffDouble( location.getAccuracy(), ACCURACY_DECIMAL_POINT );

                Log.i(TAG, String.format("Location = [%.2f, %.2f], accuracy = %f",
                        location.getLatitude(),
                        location.getLongitude(),
                        lotAccuracy));

                if( lotAccuracy > RejectAccuracy )
                {
                    Log.i(TAG, String.format("Stack rejection, Accuracy is over limitation. = %f",
                            RejectAccuracy));
                    return;
                }

                Date current = new Date();
                if( mLastLocationData != null ) {

                    double dist = getDistance(
                            mLastLocationData.latitude,
                            mLastLocationData.longitude,
                            0.0,
                            location.getLatitude(),
                            location.getLongitude(),
                            0.0);

                    Log.i(TAG, String.format("Distance = %f", dist));

                    // check distances between prev and current locations.
                    if( dist < MinimumDistance ) {

                        Log.i(TAG, String.format("Location is constant. Distance is shorter than %f",
                                MinimumDistance));
                        return;
                    }

                    // update the previous location's time duration.
                    double d = (double)( current.getTime() - mLastLocationData.time.getTime())
                            / (double)java.util.concurrent.TimeUnit.MINUTES.toMillis(1);
                    mLastLocationData.duration = RoundOffDouble( d, DURATION_DECIMAL_POINT );

                    Log.i(TAG, String.format("Duration = %f", d));

                    // calculate a distance between prev and current locations.
                    mLastLocationData.distance = dist;

                    // stack last location data.
                    LocationDataArray.add(mLastLocationData);
                }
            }

            int stackSize = LocationDataArray.size();
            //Post locations to the web server, if the stack number is even to the limit.
            Log.i(TAG, String.format("LocationDataArrayCount=%d", stackSize ));

            if( stackSize > Stackcount - 1 ) {

                try {
                    //set Location data to Json and clear.
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    JSONArray jary = new JSONArray();
                    for (LocationData d : LocationDataArray) {
                        JSONObject jobj = new JSONObject();
                        jobj.put("longitude", d.longitude);
                        jobj.put("latitude", d.latitude);
                        jobj.put("accuracy", d.accuracy);
                        jobj.put("time", sdf.format(d.time));
                        jobj.put("duration", d.duration);
                        jobj.put("distance", d.distance);
                        jobj.put("host", 1);
                        jary.put(jobj);
                    }
                    //Log.i(TAG, String.format("%s", jary.toString()));
                    new HttpResponsAsync().execute("api/entries/", jary.toString());
                    LocationDataArray.clear();
                }
                catch (JSONException error) {
                    Log.i(TAG, error.toString());
                }
            }

            //Stack the current location. The duration should be updated when the next location is given.
            if (location != null) {
                mLastLocationData = new LocationData();
                mLastLocationData.latitude = location.getLatitude();
                mLastLocationData.longitude = location.getLongitude();
                mLastLocationData.accuracy = RoundOffDouble( location.getAccuracy(), ACCURACY_DECIMAL_POINT );
//                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
//                lct.time = sdf.format(new Date());
                mLastLocationData.time = new Date();
                mLastLocationData.duration = 0.0;
            }
        }
    };

    /**
     *   Service
     */

    @Override
    public IBinder onBind(Intent arg0)
    {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.i(TAG, String.format("onStartCommand flags[%d] startId[%d]", flags, startId));
        super.onStartCommand(intent, flags, startId);
        boolean status;

        //initialize valiables
        mLastLocationData = null;
        sharedValues sValues = (sharedValues)this.getApplication();
        sValues.SharedValueFromPreference();

        Log.i(TAG, String.format("interval=%d distance=%f acc_limit=%f stackNum=%d",
                sValues.getLocationUpdateInterval(),
                sValues.getLocationMinimumDistance(),
                sValues.getLocationRejectAccuracy(),
                sValues.getLocationStackcount()));

        int resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getApplicationContext());
        if (resultCode != ConnectionResult.SUCCESS) {
            Log.i(TAG, "Google Play Services are not available.code=" + Integer.toString(resultCode));

            com.takahay.walkers.Location walkerLocation =
                    new com.takahay.walkers.Location( getApplicationContext(), sValues, callback );
            status = walkerLocation.startLocationUpdates();
        }
        else
        {
            Log.i(TAG, "Google Play Services are available.code=" + Integer.toString(resultCode));

            com.takahay.walkers.googleLocation googleLocation =
                    new com.takahay.walkers.googleLocation( getApplicationContext(), sValues, callback );

            status = googleLocation.createRequest();

        }

        new HttpResHelper().postStatusCode( 1, (status) ? 1 : 0 );

        return START_STICKY;
    }

    @Override
    public void onCreate()
    {
        Log.i(TAG, "onCreate");

    }

    @Override
    public void onDestroy()
    {
        Log.i(TAG, "onDestroy");
        new HttpResHelper().postStatusCode( 2, 1 );
    }

}
