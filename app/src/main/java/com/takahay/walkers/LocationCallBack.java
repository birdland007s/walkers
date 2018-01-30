package com.takahay.walkers;

import android.location.*;

/**
 * Created by takahay on 2018/01/24.
 */

public interface LocationCallBack {
    void stackLocation(android.location.Location location, double MinimumDistance, double RejectAccuracy, int Stackcount);
}