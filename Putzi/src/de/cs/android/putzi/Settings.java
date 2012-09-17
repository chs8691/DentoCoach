package de.cs.android.putzi;

import android.content.Context;
import android.net.Uri;
import android.preference.PreferenceManager;

/**
 * Settings are stored as Preferences. There is no default value handling in
 * this implementation. Existing preferences are mandatory assumed by calling
 * PreferenceManager.setDefaultValues() in the main activity.
 * 
 * @author ChristianSchulzendor
 * 
 */
public class Settings implements SettingValues {

	private final Context context;

	public Settings(Context context) {
		this.context = context;
	}

	public long getDurationMs() {
		return getDurationS() * 1000;
	}

	public int getDurationS() {
		return Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(
				context).getString(Pref.KEY_PREF_DURATION, ""));
	}

	public Uri getRingtone() {
		return Uri.parse(PreferenceManager.getDefaultSharedPreferences(context)
				.getString(Pref.KEY_PREF_RINGTONE, ""));
	}

	public Speed getSpeed() {

		return Speed.create(Integer.valueOf(PreferenceManager
				.getDefaultSharedPreferences(context).getString(
						Pref.KEY_PREF_SPEED, "")));
	}
}
