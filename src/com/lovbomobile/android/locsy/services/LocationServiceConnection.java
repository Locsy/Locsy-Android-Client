package com.lovbomobile.android.locsy.services;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Messenger;
import android.util.Log;

public class LocationServiceConnection implements ServiceConnection {

	private static final String TAG = LocationServiceConnection.class.getSimpleName();

	private Messenger serviceMessenger;

	public void onServiceConnected(ComponentName name, IBinder service) {
		Log.d(TAG, "connected to service");
		serviceMessenger = new Messenger(service);
	}

	public void onServiceDisconnected(ComponentName name) {
		Log.d(TAG, "disconnected from service");
	}

}
