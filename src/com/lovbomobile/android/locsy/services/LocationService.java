/**
 Copyright (c) 2013 Sven Schindler

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.
 */

package com.lovbomobile.android.locsy.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.*;
import android.preference.PreferenceManager;
import android.util.Log;
import com.lovbomobile.android.locsy.R;
import com.lovbomobile.android.locsy.client.LocsyClient;
import com.lovbomobile.android.locsy.client.impl.RestLocsyClient;

public class LocationService extends Service implements LocationListener {

    public static final int START_SERVICE = 0;
    public static final int STOP_SERVICE = 1;
    public static final String PREFERENCE_UPDATING = "updating";
    public static final long DEFAULT_UPDATE_INTERVAL = 1000 * 60 * 60;
    public static final long MIN_UPDATE_INTERVAL = 5000;
    public static final long LOCATION_TIMEOUT = 20000;
    private static final String TAG = LocationService.class.getSimpleName();
    private final Messenger messenger = new Messenger(new MessageHandler());
    private Location currentLocation;

    public static long getUpdateInterval(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        long updateInterval = DEFAULT_UPDATE_INTERVAL;
        if (preferences.contains(context.getString(R.string.pref_key_update_interval))) {
            updateInterval = Long.valueOf(preferences.getString(context.getString(R.string.pref_key_update_interval),
                    "0")) * 1000;
        }

        if (updateInterval < MIN_UPDATE_INTERVAL) {
            return MIN_UPDATE_INTERVAL;
        } else {
            return updateInterval;
        }
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "Way location service created");
        super.onCreate();
        ackquireWakeLock();
        requestAndSendCurrentPosition();
    }

    private void ackquireWakeLock() {
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(powerManager.PARTIAL_WAKE_LOCK, "locsy wake lock");
        wakeLock.acquire(LOCATION_TIMEOUT);
    }

    private void requestAndSendCurrentPosition() {
        final LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            Log.d(TAG, "setting current location");
            //TODO: check whether we want to send the last known location
            //currentLocation = LocationUtils.getBestLastLocation(locationManager);
            requestSingleLocationUpdate(locationManager);
            cancelRequestOnTimeoutAndSendLocation(locationManager);
        }
    }

    private void cancelRequestOnTimeoutAndSendLocation(final LocationManager locationManager) {
        final Handler myHandler = new Handler();
        final LocationListener requestingService = this;

        myHandler.postDelayed(new Runnable() {
            public void run() {
                locationManager.removeUpdates(requestingService);
                Log.d(TAG, "location update requests removed");
            }
        }, LOCATION_TIMEOUT);
    }

    private void requestSingleLocationUpdate(LocationManager locationManager) {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_MEDIUM);
        criteria.setPowerRequirement(Criteria.POWER_MEDIUM);
        locationManager.requestSingleUpdate(criteria, this, null);
    }

    public void onLocationChanged(Location location) {
        Log.d(TAG, "received new location in service");
        currentLocation = location;
        LocationUpdater locationUpdater = new LocationUpdater();
        locationUpdater.start();
    }

    private void updateLocation() {
        Log.d(TAG, "start location update");
        requestAndSendCurrentPosition();

    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Way location service destroyed");
        super.onDestroy();
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return messenger.getBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand called");
        updateLocation();
        return START_STICKY;
    }



    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    public void onProviderEnabled(String provider) {
    }

    public void onProviderDisabled(String provider) {
    }

    private class MessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "message received");
            switch (msg.what) {
                case START_SERVICE:

                    break;
                case STOP_SERVICE:

                    break;
                default:
                    super.handleMessage(msg);

            }
        }
    }

    private class LocationUpdater extends Thread {

        @Override
        public void run() {
            Log.d(TAG, "location update startet " + System.currentTimeMillis());
            if (currentLocation != null) {
                Log.d(TAG, "current location: " + currentLocation.getLatitude() + ", " + currentLocation.getLongitude());
                sendLocationToServer();
            }
            Log.d(TAG, "location update finished");
        }

        private void sendLocationToServer() {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String userID = preferences.getString(getString(R.string.pref_key_username), "");
            String password = preferences.getString(getString(R.string.pref_key_password), "");
            String serverAddress = preferences.getString(getString(R.string.pref_key_server_address), "");

            com.lovbomobile.android.locsy.entities.Location location = new com.lovbomobile.android.locsy.entities.Location(currentLocation.getLatitude(), currentLocation.getLongitude());
            location.setAccuracy(currentLocation.getAccuracy());

            LocsyClient locsyClient = new RestLocsyClient();
            locsyClient.sendLocationToServer(location, userID, password, serverAddress);

        }

    }

}
