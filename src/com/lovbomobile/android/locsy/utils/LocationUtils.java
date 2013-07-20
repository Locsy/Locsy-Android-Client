package com.lovbomobile.android.locsy.utils;

import android.location.Location;
import android.location.LocationManager;

public class LocationUtils {

	public static Location getBestLastLocation(LocationManager locationManager) {

		if (locationManager != null) {
			Location gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			Location networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            return getNewerLocation(gpsLocation, networkLocation);
        }
		return null;
	}

    private static Location getNewerLocation( Location gpsLocation, Location networkLocation) {
        Location location = null;
        if (gpsLocation == null && networkLocation != null) {
            location = networkLocation;
        } else if (gpsLocation != null && networkLocation == null) {
            location = gpsLocation;
        } else if (gpsLocation != null && networkLocation != null) {
            if (gpsLocation.getTime() > networkLocation.getTime()) {
                location = gpsLocation;
            } else {
                location = networkLocation;
            }
        }
        return location;
    }

    public static String hexBinaryToString(byte[] data) {
		char[] hexCode = "0123456789ABCDEF".toCharArray();
		StringBuilder r = new StringBuilder(data.length * 2);
		for (byte b : data) {
			r.append(hexCode[(b >> 4) & 0xF]);
			r.append(hexCode[(b & 0xF)]);
		}
		return r.toString();
	}
}
