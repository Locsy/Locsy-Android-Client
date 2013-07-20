package com.lovbomobile.android.locsy.activities;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.lovbomobile.android.locsy.R;

public class SettingsActivity extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}

}
