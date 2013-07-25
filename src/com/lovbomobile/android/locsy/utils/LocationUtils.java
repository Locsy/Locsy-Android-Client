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
