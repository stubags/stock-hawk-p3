package com.sam_chordas.android.stockhawk.service;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * Created by stuartwhitcombe on 16/11/2016.
 */
public class ConnectivityService extends BroadcastReceiver {
    private static LinkedList<ConnectivitySlave> slaves = new LinkedList<>();
    private static final String LOG_TAG = "ConnectivityService";

    public static void startListening(Context context) {
        Log.w(LOG_TAG, "Starting");
        ComponentName receiver = new ComponentName(context, ConnectivityService.class);

        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    public void stopListening(Context context) {
        Log.w(LOG_TAG, "Stopping");
        ComponentName receiver = new ComponentName(context, ConnectivityService.class);

        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }

    public static void addSlave(ConnectivitySlave slave) {
        slaves.add(slave);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.w(LOG_TAG, "onReceive");
        if(isConnected(context)) {
            Log.w(LOG_TAG, "ooh connected");
            Iterator<ConnectivitySlave> iter = slaves.iterator();
            while(iter.hasNext()) {
                try {
                    ConnectivitySlave slave = iter.next();
                    if (!slave.isConnected()) {
                        Log.w(LOG_TAG, "sleeping slave being woken");
                        slave.wakeup();
                    }
                }
                catch(Exception e) {
                    iter.remove();
                }
            }
            stopListening(context);
        }
    }

    public static boolean isConnected(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnected();
        return isConnected;
    }

}
