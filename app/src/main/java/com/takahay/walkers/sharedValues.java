package com.takahay.walkers;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.TextView;

import com.google.gson.annotations.SerializedName;

import org.w3c.dom.Text;

/**
 * Created by takahay on 2018/01/29.
 */

    public class sharedValues extends Application {

    public sharedValues(){
        w = new WalkerPref();
    }

    public WalkerPref w;

}