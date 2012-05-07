package de.cs.android.dentocoach.settings;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import de.cs.android.dentocoach.Speed;

/**
 * Entity that holds ALL settings data. Guarantees valid entries.
 */
class Settings implements SettingValues {
	public enum Key {
		SPEED("speed"), DURATION("duration"), RINGTONESWITCH("ringtoneswitch"), RINGTONE(
				"ringtone");
		private final String dbFieldName;

		private Key(String key) {
			this.dbFieldName = key;
		}

		@Override
		public String toString() {
			return dbFieldName;
		}

	}

	private static final String TAG = "SettingValues";

	private final DentoDbAdapter dbAdapter;

	private int duration;
	private boolean ringtoneSwitch;
	private String ringtone;

	private Speed speed;

	/** Standard constructor with default values */
	Settings(Context ctx) {
		this.dbAdapter = new DentoDbAdapter(ctx);
		readValues();
	}

	private void _readValues() {
		Cursor cursor = dbAdapter.fetchEntry(Key.DURATION.toString());
		if (cursor.moveToFirst()) {
			duration = Integer.valueOf(cursor
					.getString(DentoDbAdapter.Column.VALUE.dbIndex()));
			Log.v(TAG, "Duration found with duration=" + duration);
		} else {
			Log.v(TAG, "Duration not found in database!");
			_setDuration(1);
		}

		cursor.close();

		cursor = dbAdapter.fetchEntry(Key.SPEED.toString());
		if (cursor.moveToFirst()) {
			speed = Speed.create(Integer.valueOf(cursor
					.getString(DentoDbAdapter.Column.VALUE.dbIndex())));
			Log.v(TAG, "Speed found with speed=" + speed.value());
		} else {
			Log.v(TAG, "Speed not found in database!");
			_setSpeed(Speed.SLOW);
		}

		cursor.close();

		cursor = dbAdapter.fetchEntry(Key.RINGTONESWITCH.toString());
		if (cursor.moveToFirst()) {
			ringtoneSwitch = String.valueOf(cursor
					.getString(DentoDbAdapter.Column.VALUE.dbIndex())) == "1" ? false
					: true;
			Log.v(TAG, "RingtoneSwitch found with value=" + ringtoneSwitch);
		} else {
			Log.v(TAG, "RingtoneSwitch not found in database!");
			ringtoneSwitch = true; // Set default value
			_setRingtoneSwitch(ringtoneSwitch);
		}

		cursor.close();

		cursor = dbAdapter.fetchEntry(Key.RINGTONE.toString());
		if (cursor.moveToFirst()) {
			ringtone = String.valueOf(cursor
					.getString(DentoDbAdapter.Column.VALUE.dbIndex()));
			Log.v(TAG, "Ringtone found with value=" + ringtone);
		} else {
			Log.v(TAG, "Ringtone not found in database!");
			ringtone = ""; // Set default value
			_setRingtone(ringtone);
		}

		cursor.close();

	}

	/** Sets the duration to a valid value in [1..5] */
	private void _setDuration(int duration) {
		if (duration > 0) {
			if (duration < 5)
				this.duration = duration;
			else
				this.duration = 5;

		} else
			this.duration = 1;
		Log.v(TAG, "--->Entering _SetDuration(). Trying to save duration="
				+ this.duration);

		modifyDbEntry(Key.DURATION.toString(), String.valueOf(this.duration));
	}

	private void _setRingtone(String ringtone) {
		Log.v(TAG, "--->Entering _SetRingtone(). Trying to save ringtone="
				+ ringtone);

		this.ringtone = ringtone;
		modifyDbEntry(Key.RINGTONE.toString(), this.ringtone);
	}

	/**
	 * Saves result as 0 (false) or 1 (true) to database
	 * 
	 * @param rs
	 */
	private void _setRingtoneSwitch(boolean rs) {
		Log.v(TAG,
				"--->Entering _SetRingtoneSwitch(). Trying to save ringtone="
						+ ringtoneSwitch);

		this.ringtoneSwitch = rs;
		String rsDbValue = rs ? "1" : "0";
		modifyDbEntry(Key.RINGTONESWITCH.toString(), rsDbValue);
	}

	/** Sets the speed to a valid value in [1..3] */
	private void _setSpeed(Speed speed) {
		Log.v(TAG,
				"--->Entering _SetSpeed(). Trying to save speed="
						+ speed.value());

		this.speed = speed;
		modifyDbEntry(Key.SPEED.toString(), String.valueOf(this.speed.value()));

	}

	public SettingValues createSettingValues(Context ctx) {
		return new Settings(ctx);
	}

	@Override
	public int getDuration() {
		return duration;
	}

	@Override
	public String getRingtone() {
		return ringtone;
	}

	@Override
	public boolean getRingtoneSwitch() {

		return ringtoneSwitch;
	}

	@Override
	public Speed getSpeed() {
		return speed;
	}

	private void logError(String msg, int rowID, String keyName, String valueStr) {
		Log.e(TAG, "Error: " + msg + " rowID=" + rowID + " key=" + keyName
				+ ", value=" + valueStr);
	}

	private void logSuccess(String msg, int rowID, String keyName,
			String durationStr) {
		Log.v(TAG, "Sucess: " + msg + " rowID=" + rowID + " key=" + keyName
				+ ", value=" + durationStr);
	}

	private void modifyDbEntry(String keyName, String speedStr) {
		Cursor cursor = dbAdapter.fetchEntry(keyName);
		if (cursor.moveToFirst()) {

			int rowId = cursor.getInt(DentoDbAdapter.Column.ID.dbIndex());
			Log.v(TAG, "Dataset exists: Will update it! rowId=" + rowId);
			Boolean updateOk = dbAdapter.updateEntry(rowId, keyName, speedStr);
			if (updateOk) {
				logSuccess("Update", rowId, keyName, speedStr);
			} else
				logError("Update", rowId, keyName, speedStr);

		} else {
			Log.v(TAG, "Couldn't find entry on database, create new dataset");

			long createRet = dbAdapter.createEntry(keyName, speedStr);
			if (createRet == -1)
				logError("Create", 0, keyName, speedStr);
			else
				logSuccess("Create", 0, keyName, speedStr);
		}
		cursor.close();
	}

	/**
	 * Load values from database or, for first time, initialize with default
	 * values
	 */
	private void readValues() {
		dbAdapter.open();
		_readValues();
		dbAdapter.close();
	}

	public void setDuration(int duration) {
		dbAdapter.open();
		_setDuration(duration);
		dbAdapter.close();
	}

	public void setRingtoneSwitch(boolean rs) {
		dbAdapter.open();
		_setRingtoneSwitch(rs);
		dbAdapter.close();
	}

	public void setSpeed(Speed speed) {
		dbAdapter.open();
		_setSpeed(speed);
		dbAdapter.close();

	}

}
