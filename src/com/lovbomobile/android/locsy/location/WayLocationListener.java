package com.lovbomobile.android.locsy.location;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

public class WayLocationListener implements LocationListener{

	private static final String TAG = WayLocationListener.class.getSimpleName();
	
	public WayLocationListener() {
		Log.d(TAG,"Listener loaded...");
	}
	
	public void onLocationChanged(Location location) {
		Log.d(TAG, "my new location is " + location.getLatitude() + " " + location.getLongitude());
	}

	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}

}
