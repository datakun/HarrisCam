package com.main.harriscam;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class SettingsPreference extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.settings);
	}
}
