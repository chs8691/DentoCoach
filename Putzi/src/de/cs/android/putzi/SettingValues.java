package de.cs.android.putzi;

import android.net.Uri;

public interface SettingValues {

	/**
	 * How long to brush
	 * 
	 * @return long with duration in ms
	 */
	public long getDurationMs();

	/**
	 * How long to brush
	 * 
	 * @return int with duration in seconds
	 */
	public int getDurationS();

	/**
	 * Ringtone to be played at end of brushing
	 * 
	 * @return Uri to ringtone or null if silent configured
	 */
	public Uri getRingtone();

	/**
	 * Brush speed
	 * 
	 * @return Speed
	 */
	public Speed getSpeed();
}