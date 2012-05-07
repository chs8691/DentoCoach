package de.cs.android.dentocoach.settings;

import de.cs.android.dentocoach.Speed;

public interface SettingValues {

	/**
	 * Saved duration
	 * 
	 * @return duration in minutes
	 */
	public abstract int getDuration();

	public abstract String getRingtone();

	public abstract boolean getRingtoneSwitch();

	/**
	 * Saved speed
	 * 
	 * @return Speed
	 */
	public abstract Speed getSpeed();
}