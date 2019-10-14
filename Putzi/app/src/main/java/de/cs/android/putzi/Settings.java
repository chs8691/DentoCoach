package de.cs.android.putzi;

import android.content.Context;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

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
	private static final String TAG = "SettingValues";

	Settings(Context context) {
		super();
		this.context = context;
		logValues();
	}

	public final long getDurationMs() {
		return getDurationS() * 1000;
	}

	public final int getDurationS() {
		return Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(
				context).getString(Pref.KEY_PREF_DURATION, ""));
	}

	public final Uri getRingtone() {
		return Uri.parse(PreferenceManager.getDefaultSharedPreferences(context)
				.getString(Pref.KEY_PREF_RINGTONE, ""));
	}

	public final Speed getSpeed() {

		return Speed.create(Integer.valueOf(PreferenceManager
				.getDefaultSharedPreferences(context).getString(
						Pref.KEY_PREF_SPEED, "")));
	}

	public int getStepDurationMs() {
		return getSpeed().value() * 1000;
	}

	private final void logValues() {
		Log.v(TAG, "DurationMS=" + getDurationMs());
		Log.v(TAG, "Ringtone=" + getRingtone());
		Log.v(TAG, "Speed=" + getSpeed() + " / value=" + getSpeed().value());

	}

}
