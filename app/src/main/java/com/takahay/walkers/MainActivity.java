package com.takahay.walkers;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.google.gson.Gson;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "walker.MainActivity";
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    private sharedValues sValues = null;

    // UI Widgets.
    TextView ui_updateInterval;
    TextView ui_smallestDsplacement;
    TextView ui_minimumDistance;
    TextView ui_minimumAccuracy;
    TextView ui_StackNumber;
    TextView ui_LastStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //initialize preferences
        PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);
        sValues = (sharedValues)this.getApplication();
        SharedValueFromPreference(sValues);

                FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        //populate resources to ui.
        ui_updateInterval = findViewById(R.id.text_updateInterval);
        ui_smallestDsplacement = findViewById(R.id.text_smallestDisplacement);
        ui_minimumDistance = findViewById(R.id.text_minimumDistance);
        ui_minimumAccuracy = findViewById(R.id.text_minimumAccuracy);
        ui_StackNumber = findViewById(R.id.text_StackNumber);
        ui_LastStatus = findViewById(R.id.text_LastStatus);
        ui_LastStatus.setText("");

        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //Update Your UI here..
                ui_LastStatus.setText(intent.getStringExtra("LastStatus"));
            }
        };
        registerReceiver(broadcastReceiver, new IntentFilter(WalkersService.BROADCAST_ACTION));

        Switch switch_button = (Switch) findViewById(R.id.start_service_switch);
        switch_button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){

                    Log.i(TAG, "onClick Start view");

                    if (!checkPermissions()) {
                        requestPermissions();
                    } else {
                        Log.i(TAG, "onCreate.Permission Granted");
                        Intent intent = new Intent(MainActivity.this, WalkersService.class);
                        Gson gson = new Gson();
                        String json = gson.toJson(sValues.w, WalkerPref.class);
                        Log.i(TAG, "json=" + json);

                        Bundle bundle = new Bundle();
                        bundle.putString("WalkerPref", json );
                        intent.putExtras(bundle);
                        Log.i(TAG, "onClick before calling service");
                        startService(intent);
                    }

                }
                else{

                    Log.i(TAG, "onClick Stop view");
                    stopService(new Intent(MainActivity.this, WalkersService.class));

                }
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();

        ui_updateInterval.setText(  Long.toString(sValues.w.LocationUpdateInterval) );
        ui_smallestDsplacement.setText( Float.toString(sValues.w.LocationSmallestDisplacementForAPI) );
        ui_minimumDistance.setText(  Float.toString(sValues.w.LocationMinimumDistance) );
        ui_minimumAccuracy.setText(  Double.toString(sValues.w.LocationRejectAccuracy) );
        ui_StackNumber.setText( Integer.toString(sValues.w.LocationStackcount));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent1 = new android.content.Intent(this, SettingsActivity.class);
            startActivity(intent1);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Shows a {@link Snackbar} using {@code text}.
     *
     * @param text The Snackbar text.
     */
    private void showSnackbar(final String text) {
        View container = findViewById(android.R.id.content);
        if (container != null) {
            Snackbar.make(container, text, Snackbar.LENGTH_LONG).show();
        }
    }

    /**
     * Shows a {@link Snackbar}.
     *
     * @param mainTextStringId The id for the string resource for the Snackbar text.
     * @param actionStringId   The text of the action item.
     * @param listener         The listener associated with the Snackbar action.
     */
    private void showSnackbar(final int mainTextStringId, final int actionStringId,
                              View.OnClickListener listener) {
        Snackbar.make(findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show();
    }

    /**
     * Return the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void startLocationPermissionRequest() {
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_PERMISSIONS_REQUEST_CODE);
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");

            showSnackbar(R.string.permission_rationale, android.R.string.ok,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            startLocationPermissionRequest();
                        }
                    });

        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            startLocationPermissionRequest();
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted.
                Log.i(TAG, "onRequestPermissionsResult.Permission Granted");
                startService(new Intent(MainActivity.this, WalkersService.class));
            } else {
                // Permission denied.
                Log.i(TAG, "onRequestPermissionsResult.Permission Denied");

                // Notify the user via a SnackBar that they have rejected a core permission for the
                // app, which makes the Activity useless. In a real app, core permissions would
                // typically be best requested during a welcome-screen flow.

                // Additionally, it is important to remember that a permission might have been
                // rejected without asking the user for permission (device policy or "Never ask
                // again" prompts). Therefore, a user interface affordance is typically implemented
                // when permissions are denied. Otherwise, your app could appear unresponsive to
                // touches or interactions which have required permissions.
                showSnackbar(R.string.permission_denied_explanation, R.string.title_activity_settings,
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        });
            }
        }
    }

    void SharedValueFromPreference(sharedValues sValues)
    {
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(this);
        sValues.w.LocationUpdateInterval = Long.parseLong(SP.getString("Location_Update_Interval", "90000")) ;
        sValues.w.LocationMinimumDistance = Float.parseFloat(SP.getString("Location_Minimum_Distance", "15")) ;
        sValues.w.LocationSmallestDisplacementForAPI = Float.parseFloat(SP.getString("Location_Smallest_Displacement", "10")) ;
        sValues.w.LocationRejectAccuracy = Float.parseFloat( SP.getString("Location_Reject_Accuracy", "20") );
        sValues.w.Locationlowaccuracytimes = Integer.parseInt(SP.getString("Location_low_accuracy_times", "5"));
        sValues.w.LocationUpdateIntervalLowAccuracy = Long.parseLong(SP.getString("Location_Update_Interval_Low_Accuracy", "600000"));
        sValues.w.LocationStackcount = Integer.parseInt(SP.getString("Location_Stack_count", "5"));
        sValues.w.PostStatusLog = SP.getBoolean("Post_Status_Log", true);
    }

}
