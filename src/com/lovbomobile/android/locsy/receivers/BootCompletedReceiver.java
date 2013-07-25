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

package com.lovbomobile.android.locsy.receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.lovbomobile.android.locsy.services.LocationService;

public class BootCompletedReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent receiverIntent) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		if (isLocationServiceActivated(preferences)) {
            sheduleLocationService(context, preferences);
		}
	}

    private void sheduleLocationService(Context context, SharedPreferences preferences) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            setLocationServicePreferenceToRunning(preferences);
            registerServiceInAlarmManager(context, alarmManager);
        }
    }

    private void registerServiceInAlarmManager(Context context, AlarmManager alarmManager) {
        long updateInterval = LocationService.getUpdateInterval(context);
        Intent intent = new Intent(context, LocationService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, 0);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, 0, updateInterval, pendingIntent);
    }

    private void setLocationServicePreferenceToRunning(SharedPreferences preferences) {
        preferences.edit().putBoolean(LocationService.PREFERENCE_UPDATING, true).commit();
    }

    private boolean isLocationServiceActivated(SharedPreferences preferences) {
        return preferences.getBoolean(LocationService.PREFERENCE_UPDATING, false);
    }

}
