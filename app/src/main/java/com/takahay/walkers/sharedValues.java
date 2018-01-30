package com.takahay.walkers;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by takahay on 2018/01/29.
 */

public class sharedValues extends Application {

    private long LocationUpdateInterval;
    private float LocationMinimumDistance;
    private double LocationRejectAccuracy;
    private int Locationlowaccuracytimes;
    private long LocationUpdateIntervalLowAccuracy;
    private int LocationStackcount;
    private boolean PostStatusLog;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    //LocationUpdateInterval
    public long getLocationUpdateInterval()
    {
        return LocationUpdateInterval;
    }

    public void setLocationUpdateInterval(long value)
    {
        LocationUpdateInterval = value;
    }

    //LocationMinimumDistance
    public float getLocationMinimumDistance()
    {
        return LocationMinimumDistance;
    }

    public void setLocationMinimumDistance(float value)
    {
        LocationMinimumDistance = value;
    }

    //LocationRejectAccuracy
    public double getLocationRejectAccuracy()
    {
        return LocationRejectAccuracy;
    }

    public void setLocationRejectAccuracy(double value)
    {
        LocationRejectAccuracy = value;
    }

    //Locationlowaccuracytimes
    public int getLocationlowaccuracytimes()
    {
        return Locationlowaccuracytimes;
    }

    public void setLocationlowaccuracytimes(int value)
    {
        Locationlowaccuracytimes = value;
    }

    //LocationUpdateIntervalLowAccuracy
    public long getLocationUpdateIntervalLowAccuracy()
    {
        return LocationUpdateIntervalLowAccuracy;
    }

    public void setLocationUpdateIntervalLowAccuracy(long value)
    {
        LocationUpdateIntervalLowAccuracy = value;
    }

    //LocationStackcount
    public int getLocationStackcount()
    {
        return LocationStackcount;
    }

    public void setLocationStackcount(int value)
    {
        LocationStackcount = value;
    }

    //PostStatusLog
    public boolean getPostStatusLog()
    {
        return PostStatusLog;
    }

    public void setPostStatusLog(boolean value)
    {
        PostStatusLog = value;
    }

    public void SharedValueFromPreference()
    {
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(this);
        sharedValues sValues = this;
        sValues.setLocationUpdateInterval( Long.parseLong(SP.getString("Location_Update_Interval", "90000")) );
        sValues.setLocationMinimumDistance( Float.parseFloat(SP.getString("Location_Minimum_Distance", "15")) );
        sValues.setLocationRejectAccuracy( Float.parseFloat( SP.getString("Location_Reject_Accuracy", "20") ));
        sValues.setLocationlowaccuracytimes(Integer.parseInt(SP.getString("Location_low_accuracy_times", "5")));
        sValues.setLocationUpdateIntervalLowAccuracy(Long.parseLong(SP.getString("Location_Update_Interval_Low_Accuracy", "600000")));
        sValues.setLocationStackcount(Integer.parseInt(SP.getString("Location_Stack_count", "5")));
        sValues.setPostStatusLog(SP.getBoolean("Post_Status_Log", true));
    }
}