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

package com.lovbomobile.android.locsy.activities;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.lovbomobile.android.locsy.R;
import com.lovbomobile.android.locsy.client.LocsyClient;
import com.lovbomobile.android.locsy.entities.ParceableLocation;
import com.lovbomobile.android.locsy.services.GoogleMapManager;
import com.lovbomobile.android.locsy.services.LocationService;
import com.lovbomobile.android.locsy.services.LocationServiceConnection;
import com.lovbomobile.android.locsy.services.MapManager;
import com.lovbomobile.android.locsy.utils.LocationUtils;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MainActivity extends FragmentActivity implements LocationListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int SETTINGS_ACTIVITY_REQUEST_CODE = 0;
    private static final String HANDLER_KEY_ERROR_MESSAGE = "error";
    private static final String HANDLER_KEY_LOCATION_MAP = "locationMap";
    private static final String MY_LOCATION_BUTTON_KEY = "myLocationButton";
    private static Handler handler;
    public LocationServiceConnection serviceConnection = new LocationServiceConnection();
    private TextView infoTextView;
    private ImageButton refreshButton;
    private ImageView locationServiceStatusIcon;
    private Map<String, View> userButtons;
    private ProgressDialog progressDialog;
    private boolean isMyLocationLoadedTheFirstTime;
    private MapManager mapManager;



    private LocsyClient locsyClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setAndBindLayout();

        initializeLocsyApplicationAndClient();

        initializeButtonCaches();
        initializeLocationServiceStatusIcon();
        initializeRefreshButton();

        bindLocationService();
        initializeMapManagerAndLogTextField();
        initializeHandler();

        //updating locations is done in on Resume
    }

    private void initializeLocsyApplicationAndClient() {
        LocsyApplication.onCreate();
        locsyClient = LocsyApplication.getLocsyClient();
    }

    private void initializeButtonCaches() {
        userButtons = new HashMap<String, View>();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isMyLocationLoadedTheFirstTime = true;
        if (isSettingsEntered()) {
            setMyAndMyFriendsLocations();
        } else {
            startSettingsActivity();
        }
    }

    private boolean isSettingsEntered() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String serverAddress = preferences.getString(getString(R.string.pref_key_server_address), "");
        String username = preferences.getString(getString(R.string.pref_key_username), "");
        if (!serverAddress.isEmpty() && !username.isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    private void setAndBindLayout() {

        setContentView(R.layout.activity_main);
    }

    private void initializeLocationServiceStatusIcon() {
        locationServiceStatusIcon = (ImageView) findViewById(R.id.locationServiceStatusIcon);
        if (isLocationServiceRunning()) {
            setLocationServiceStatusIconToEnabled();
        } else {
            setLocationServiceStatusIconToDisabled();
        }
    }

    private void setLocationServiceStatusIconToEnabled() {
        if (locationServiceStatusIcon != null) {
            locationServiceStatusIcon.setImageResource(R.drawable.connected);
        }
    }

    private void setLocationServiceStatusIconToDisabled() {
        if (locationServiceStatusIcon != null) {
            locationServiceStatusIcon.setImageResource(R.drawable.disconnected);
        }
    }

    private void initializeRefreshButton() {
        refreshButton = (ImageButton) findViewById(R.id.refreshButton);
        refreshButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                setMyAndMyFriendsLocations();
            }
        });
    }

    private void showProgressDialog(String title, String message) {
        progressDialog = ProgressDialog.show(this, title, message);
    }

    private void hideProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    private void initializeMapManagerAndLogTextField() {
        //debugging text field
        initializeLogTextField();
        initializeMapManager();
    }

    private void setMyAndMyFriendsLocations() {
        showProgressDialog(getResources().getString(R.string.please_wait), getResources().getString(R.string.loading_locations));
        showLastLocation();
        requestMyCurrentLocation();
        requestMyFriendsLocations();
    }

    private void bindLocationService() {
        bindService(new Intent(getApplicationContext(), LocationService.class), serviceConnection,
                Context.BIND_AUTO_CREATE);
    }

    private void initializeMapManager() {
        GoogleMap map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
        mapManager = new GoogleMapManager(map,getResources());
    }

    private void initializeHandler() {
        handler = new Handler(getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                Log.d(TAG, "message from thread received");

                if (isMessageValidLocationUpdate(msg)) {
                    updateFriendsLocationsOnMap(extractFriendsLocationsFromMessage(msg));
                } else {
                    logFieldLog(msg.getData().getString(HANDLER_KEY_ERROR_MESSAGE));
                    Toast.makeText(getApplicationContext(), R.string.location_update_error, Toast.LENGTH_LONG).show();
                }
                hideProgressDialog();
                logFieldLog("finished receiving locations");
                super.handleMessage(msg);
            }

            private Map<String, ParceableLocation> extractFriendsLocationsFromMessage(Message message) {

                return (HashMap<String, ParceableLocation>) message.getData().getSerializable(
                        HANDLER_KEY_LOCATION_MAP);
            }

            private boolean isMessageValidLocationUpdate(Message message) {
                return (!message.getData().containsKey(HANDLER_KEY_ERROR_MESSAGE)
                        && message.getData().containsKey(HANDLER_KEY_LOCATION_MAP));
            }

        };
    }

    private void requestMyCurrentLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_MEDIUM);
            locationManager.requestSingleUpdate(criteria, this, null);
        }
    }

    private void updateFriendsLocationsOnMap(Map<String, ParceableLocation> locations) {
        if (locations != null) {
            iterateOverFriendsPositionsAndUpdateMarkersAndButtons(locations);
        }

    }

    private void iterateOverFriendsPositionsAndUpdateMarkersAndButtons(Map<String, ParceableLocation> friendsLocations) {
        Iterator<String> iterator = friendsLocations.keySet().iterator();
        while (iterator.hasNext()) {
            String friendsName = iterator.next();
            ParceableLocation location = friendsLocations.get(friendsName);
            mapManager.updateSingleFriendsLocationOnMap(friendsName, location);
            addOrUpdateFriendsLocationButton(friendsName, location);//added after update

        }
    }

    private void logUsersLocationUpdate(String friendsName, ParceableLocation friendsLocation, Calendar calendar) {
        logFieldLog("user: " + friendsName + " " + calendar.get(Calendar.HOUR_OF_DAY) + ":"
                + calendar.get(Calendar.MINUTE) + " " + friendsLocation.latitude + " " + friendsLocation.longitude + " ac "
                + friendsLocation.accuracy);
    }

    private void updateMyLocationButton(Location location) {
        LatLng myPositionLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        updateMyLocationButton(myPositionLatLng);
    }

    private void addOrUpdateMyLocationButton(final ParceableLocation location) {
        View.OnClickListener onClickListener = new View.OnClickListener() {

            public void onClick(View v) {
                mapManager.animateMapToMyLocation();
            }
        };
        addOrUpdateLocationButton(MY_LOCATION_BUTTON_KEY, getResources().getString(R.string.my_location), onClickListener);
    }

    private void addOrUpdateFriendsLocationButton(final String userID, final ParceableLocation location) {
        View.OnClickListener onClickListener = new View.OnClickListener() {
            public void onClick(View v) {
                mapManager.animateMapToFriendsLocation(userID);
            }
        };
        addOrUpdateLocationButton(userID, userID, onClickListener);


    }

    private void addOrUpdateLocationButton(final String keyInUserButtonsMap, String label, View.OnClickListener onClickListener) {
        boolean needToCreateNewUserButton = !isUserButtonAlreadyCreated(keyInUserButtonsMap);
        View userButton = createNewOrGetExistingUserButton(keyInUserButtonsMap, label);

        userButton.setOnClickListener(onClickListener);


        if (needToCreateNewUserButton) {
            addUserButtonToUserArea(keyInUserButtonsMap, userButton);
        }
    }

    private void addUserButtonToUserArea(String userID, View userButton) {
        LinearLayout userArea = (LinearLayout) findViewById(R.id.userArea);
        if (userArea != null) {
            userArea.addView(userButton);
            userButtons.put(userID, userButton);
        }
    }

    private View createNewOrGetExistingUserButton(String keyInUserButtonsMap, String label) {
        boolean needToCreateNewUserButton = !isUserButtonAlreadyCreated(keyInUserButtonsMap);
        View view;
        if (needToCreateNewUserButton) {
            view = inflateNewUserButton(label);

        } else {
            view = userButtons.get(keyInUserButtonsMap);
        }
        return view;
    }

    private View inflateNewUserButton(String label) {
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.user_button, null);
        ((TextView) view.findViewById(R.id.username)).setText(label);
        return view;
    }

    private boolean isUserButtonAlreadyCreated(String userID) {
        View view = userButtons.get(userID);
        return view != null;
    }

    private void initializeLogTextField() {
        infoTextView = (TextView) findViewById(R.id.log);
        if (infoTextView != null) {
            infoTextView.setMovementMethod(new ScrollingMovementMethod());
        }
    }

    private void logFieldLog(String logEntry) {
        if (infoTextView != null) {
            infoTextView.setText(infoTextView.getText() + "* " + logEntry + "\n");
        }
    }

    private void updateMyLocationButton(LatLng myPositionLatLng) {
        ParceableLocation parceableLocation = new ParceableLocation(myPositionLatLng.latitude,
                myPositionLatLng.longitude);
        addOrUpdateMyLocationButton(parceableLocation);
    }

    private void showLastLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location location = LocationUtils.getBestLastLocation(locationManager);

        if (location != null) {
            logFieldLog("your last location was " + location.getLatitude() + " " + location.getLongitude());
            Log.d(TAG, "my last location was " + location.getLatitude() + " " + location.getLongitude());
            updateMyLocationButton(location);
            mapManager.updateMyCurrentPositionOnMap(location);
        } else {
            Log.d(TAG, "could not access any location");
            logFieldLog("could not access your location");

        }

    }

    private void requestMyFriendsLocations() {
        logFieldLog("trying to receive locations");
        FriendLocationReceiver friendLocationReceiver = new FriendLocationReceiver();
        friendLocationReceiver.start();
    }

    private void configureAlarmManagerForLocationService(AlarmManager alarmManager) {
        long updateInterval = LocationService.getUpdateInterval(getApplicationContext());
        Intent intent = new Intent(getApplicationContext(), LocationService.class);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 0, intent, 0);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, 0, updateInterval, pendingIntent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_stop_service:
                stopLocationService();
                return true;
            case R.id.menu_start_service:
                startLocationService();
                return true;
            case R.id.menu_settings:
                startSettingsActivity();
                return true;
            case R.id.menu_show_hide_log:
                toggleLog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void startLocationService() {

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            setLocationServiceRunningPropertyTo(true);
            configureAlarmManagerForLocationService(alarmManager);
        }

        logFieldLog("location service started");
        setLocationServiceStatusIconToEnabled();
    }

    private void stopLocationService() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            setLocationServiceRunningPropertyTo(false);
            cancelLocationServiceInAlarmManager(alarmManager);
        }
        getApplicationContext().stopService(new Intent(getApplicationContext(), LocationService.class));
        logFieldLog("location service stopped");
        setLocationServiceStatusIconToDisabled();
    }

    private void startSettingsActivity() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivityForResult(intent, SETTINGS_ACTIVITY_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SETTINGS_ACTIVITY_REQUEST_CODE: {
                handleSettingsUpdate();
                break;
            }
        }
    }

    private void handleSettingsUpdate() {
        if (isLocationServiceRunning()) {
            restartLocationService();
        }
    }

    private void restartLocationService() {
        stopLocationService();
        startLocationService();
    }

    private void cancelLocationServiceInAlarmManager(AlarmManager alarmManager) {
        Intent intent = new Intent(getApplicationContext(), LocationService.class);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 0, intent, 0);
        alarmManager.cancel(pendingIntent);
    }

    private void setLocationServiceRunningPropertyTo(boolean running) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        preferences.edit().putBoolean(LocationService.PREFERENCE_UPDATING, running).commit();
    }

    private boolean isLocationServiceRunning() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        return preferences.getBoolean(LocationService.PREFERENCE_UPDATING, false);
    }

    private void toggleLog() {
        toggleLogHeader();
        toggleLogMainWindow();
    }

    private void toggleLogMainWindow() {
        TextView log;

        log = (TextView) findViewById(R.id.log);
        if (log != null) {
            if (log.getVisibility() == View.VISIBLE) {
                log.setVisibility(View.GONE);
            } else {
                log.setVisibility(View.VISIBLE);
            }
        }
    }

    private void toggleLogHeader() {
        TextView log = (TextView) findViewById(R.id.log_header);
        if (log != null) {
            if (log.getVisibility() == View.VISIBLE) {
                log.setVisibility(View.GONE);
            } else {
                log.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    protected void onDestroy() {
        unregisterLocationUpdate();
        unbindService(serviceConnection);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    public void onLocationChanged(Location location) {
        Log.d(TAG,"location update received");
        logFieldLog("new current location received");
        mapManager.updateMyCurrentPositionOnMap(location);
        updateMyLocationButton(location);
        if (isMyLocationLoadedTheFirstTime) {
            mapManager.animateMapToMyLocationAndZoomIn();
            isMyLocationLoadedTheFirstTime = false;
        }
    }

    private void unregisterLocationUpdate() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub

    }

    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub

    }

    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub

    }


    private class FriendLocationReceiver extends Thread {
        @Override
        public void run() {

            HashMap<String, ParceableLocation> locations = getLocationsFromServer();

            if (locations != null) {
                sendLocationsToHandler(locations);
            } else {
                sendReceiveLocationErrorToHandler();
            }
        }

        private HashMap<String, ParceableLocation> getLocationsFromServer() {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String userID = preferences.getString(getString(R.string.pref_key_username), "");
            String password = preferences.getString(getString(R.string.pref_key_password), "");
            String serverAddress = preferences.getString(getString(R.string.pref_key_server_address), "");

            return locsyClient
                    .getFriendsLocation(userID, password, serverAddress);
        }

        private void sendLocationsToHandler(HashMap<String, ParceableLocation> locations) {
            Bundle bundle = new Bundle();
            bundle.putSerializable(HANDLER_KEY_LOCATION_MAP, locations);
            Message message = handler.obtainMessage();

            message.setData(bundle);
            handler.sendMessage(message);
        }

        private void sendReceiveLocationErrorToHandler() {
            Bundle bundle = new Bundle();
            Message message = handler.obtainMessage();
            bundle.putString(HANDLER_KEY_ERROR_MESSAGE, getResources().getString(R.string.could_not_receive_locations));
            message.setData(bundle);
            handler.sendMessage(message);
        }

    }

}
