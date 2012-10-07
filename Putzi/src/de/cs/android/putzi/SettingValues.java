package de.cs.android.putzi;

import android.net.Uri;

public interface SettingValues {

	/**
	 * How long to brush
	 * 
	 * @return long with duration in ms
	 */
	long getDurationMs();

	/**
	 * How long to brush
	 * 
	 * @return int with duration in seconds
	 */
	int getDurationS();

	/**
	 * Ringtone to be played at end of brushing
	 * 
	 * @return Uri to ringtone or null if silent configured
	 */
	Uri getRingtone();

	/**
	 * Brush speed
	 * 
	 * @return Speed
	 */
	Speed getSpeed();

	int getStepDurationMs();

}