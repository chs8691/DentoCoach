package de.cs.android.dentocoach.settings;

import android.content.Context;

/**
 * Public access to Settings
 * 
 * @author ChristianSchulzendor
 * 
 */
public final class SettingsFactory {
	/**
	 * Read-only access to the settings
	 * 
	 * @param ctx
	 *            Context
	 * @return SettingValues
	 */
	public static SettingValues createSettingValues(Context ctx) {
		return new Settings(ctx);
	}
}
