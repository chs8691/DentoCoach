package de.cs.android.putzi;

import java.util.HashMap;
import java.util.Map;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class Pref extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {

	// Preference keys are unfortunately simple strings, so we have to declare
	// them here a second time
	public static final String KEY_PREF_DURATION = "pref_duration";
	public static final String KEY_PREF_SPEED = "pref_speed";
	public static final String KEY_PREF_RINGTONE = "pref_ringtone";

	@Override
	protected void onCreate(final Bundle savedState) {
		super.onCreate(savedState);

		// Initialize first calling: Set default values
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

		// We want to see our preference.xml
		addPreferencesFromResource(R.xml.preferences);

		// Update summaries
		onSharedPreferenceChanged(findPreference(KEY_PREF_DURATION)
				.getSharedPreferences(), KEY_PREF_DURATION);
		onSharedPreferenceChanged(findPreference(KEY_PREF_SPEED)
				.getSharedPreferences(), KEY_PREF_SPEED);
		onSharedPreferenceChanged(findPreference(KEY_PREF_RINGTONE)
				.getSharedPreferences(), KEY_PREF_RINGTONE);

		// This class is our listener for value changing
		PreferenceManager.getDefaultSharedPreferences(this)
				.registerOnSharedPreferenceChangeListener(this);
	}

	/**
	 * implements method from OnSharedPreferenceChangeListener
	 */
	public void onSharedPreferenceChanged(final SharedPreferences prefs,
			final String key) {

		// concatenate summaries
		if (key.equals(KEY_PREF_DURATION)) {
			final Preference durationPref = findPreference(key);
			durationPref.setSummary(getText(R.string.pref_duration_summary)
					+ " " + prefs.getString(key, ""));
		}
		if (key.equals(KEY_PREF_SPEED)) {
			final Map<String, String> map = toMap(getResources()
					.getStringArray(R.array.pref_speed_values), getResources()
					.getStringArray(R.array.pref_speed_entries));
			final Preference speedPref = findPreference(key);
			speedPref.setSummary(getText(R.string.pref_speed_summary) + " "
					+ map.get(prefs.getString(key, "-")));

		}
		if (key.equals(KEY_PREF_RINGTONE)) {
			final Preference ringtonePref = findPreference(key);
			String ringtoneName = null;
			try {
				ringtoneName = RingtoneManager.getRingtone(this,
						Uri.parse(prefs.getString(key, ""))).getTitle(this);
			} catch (Exception e) {
				ringtoneName = "-";
			}

			ringtonePref.setSummary(getText(R.string.pref_Ringtone_summary)
					+ " " + ringtoneName);
		}
	}

	/**
	 * Transforms two String-Array into a map
	 * 
	 * @param keys
	 * @param values
	 * @return map
	 */
	private Map<String, String> toMap(final String[] keys, final String[] values) {
		final Map<String, String> map = new HashMap<String, String>();
		for (int i = 0; i < keys.length; i++) {
			map.put(keys[i], values[i]);
		}

		return map;
	}
}
