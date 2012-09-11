package de.cs.android.putzi;

public interface SettingValues {

	/**
	 * How long to brush
	 * 
	 * @return int with duration in seconds
	 */
	public abstract int getDurationS();

	/**
	 * How long to brush
	 * 
	 * @return long with duration in ms
	 */
	public long getDurationMs();

	/**
	 * 
	 * @return
	 */
	// TODO implement and use ringtone
	public abstract String getRingtone();

	// TODO Remove ringtoneSwitch - nobody needs this
	public abstract boolean getRingtoneSwitch();

	/**
	 * Brush speed
	 * 
	 * @return Speed
	 */
	public abstract Speed getSpeed();
}