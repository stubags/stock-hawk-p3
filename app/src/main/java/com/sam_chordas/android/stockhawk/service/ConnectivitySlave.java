package com.sam_chordas.android.stockhawk.service;

/**
 * Created by stuartwhitcombe on 16/11/2016.
 */
public interface ConnectivitySlave {
    boolean isConnected();
    void wakeup();
}
