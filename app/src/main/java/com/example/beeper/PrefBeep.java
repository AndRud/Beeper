package com.example.beeper;

import java.lang.reflect.Field;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;

public class PrefBeep extends PreferenceActivity {
	
	ListPreference listSounds;
	//AudioListPreference listSounds;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.pref);
		
		listSounds = (ListPreference) findPreference("listSounds");
		listSounds.setEntries(getEntriesValues(false));
		listSounds.setEntryValues(getEntriesValues(true));
		
	}
	
	private CharSequence[] getEntriesValues(boolean isValues){
		Field[] fields = R.raw.class.getFields();
		CharSequence[] entries = new String[fields.length];
		for (int i = 0; i < fields.length; i++)
			entries[i] = fields[i].getName();
		return entries;
	}
}
