package com.takahay.walkers;

/**
 * Created by takahay on 2018/02/01.
 */

public class WalkerPref {
    public WalkerPref(){
    }
    public long     LocationUpdateInterval;
    public float    LocationMinimumDistance;
    public float    LocationSmallestDisplacementForAPI;
    public double   LocationRejectAccuracy;
    public int      Locationlowaccuracytimes;
    public long     LocationUpdateIntervalLowAccuracy;
    public int      LocationStackcount;
    public boolean  PostStatusLog;
}
