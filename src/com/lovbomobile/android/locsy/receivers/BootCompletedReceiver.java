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
