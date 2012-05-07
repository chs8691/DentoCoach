package de.cs.android.dentocoach.settings;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Simple notes database access helper class. Defines the basic CRUD operations
 * for the notepad example, and gives the ability to list all notes as well as
 * retrieve or modify a specific note.
 * 
 * This has been improved from the first version of this tutorial through the
 * addition of better error handling and also using returning a Cursor instead
 * of using a collection of inner classes (which is less scalable and not
 * recommended).
 */
class DentoDbAdapter {

	enum Column {
		ID("_id", 0), KEY("key", 1), VALUE("value", 2);
		private final String dbName;
		private final int dbIndex;

		private Column(String dbName, int dbIndex) {
			this.dbName = dbName;
			this.dbIndex = dbIndex;
		}

		public int dbIndex() {
			return dbIndex;
		}

		public String dbName() {
			return this.dbName;
		}
	}

	private static class DatabaseHelper extends SQLiteOpenHelper {
		private static final String DATABASE_NAME = "dentocoach.db";
		private static final int DATABASE_VERSION = 2;

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
			Log.v(TAG, "---> Creating DatabaseHelper");
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			Log.v(TAG, "----> DatabaseAdapter.onCreate():" + DATABASE_CREATE);
			db.execSQL(DATABASE_CREATE);

		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
			onCreate(db);
		}
	}

	private static final String TAG = "DentoDbAdapter";
	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;
	private final Context mCtx;

	/**
	 * Database creation sql statement
	 */
	private static final String DATABASE_TABLE = "dento_setting";

	private static final String DATABASE_CREATE = "create table "
			+ DATABASE_TABLE + " (_id integer primary key autoincrement, "
			+ "key text not null, value text not null);";

	/**
	 * Constructor - takes the context to allow the database to be
	 * opened/created
	 * 
	 * @param ctx
	 *            the Context within which to work
	 */
	public DentoDbAdapter(Context ctx) {
		Log.v(TAG, "----> Creating DentoDbAdapter");
		this.mCtx = ctx;
	}

	public void close() {
		mDbHelper.close();

	}

	/**
	 * Create a new note using the title and body provided. If the note is
	 * successfully created return the new rowId for that note, otherwise return
	 * a -1 to indicate failure.
	 * 
	 * @param key
	 *            the title of the note
	 * @param value
	 *            the body of the note
	 * @return rowId or -1 if failed
	 */
	public long createEntry(String key, String value) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(Column.KEY.dbName(), key);
		initialValues.put(Column.VALUE.dbName(), value);

		long row = mDb.insert(DATABASE_TABLE, null, initialValues);

		Log.v(TAG, "Create Entry in " + DATABASE_TABLE
				+ " with initial values: " + initialValues + " ...");
		if (row > 0)
			Log.v(TAG, "Successfull! Returned RowId=" + row);
		else
			Log.e(TAG,
					"Failed! Could create entry in database! Returned RowId="
							+ row);

		return row;
	}

	/**
	 * Delete the note with the given rowId
	 * 
	 * @param rowId
	 *            id of note to delete
	 * @return true if deleted, false otherwise
	 */
	public boolean deleteEntry(long rowId) {

		return mDb.delete(DATABASE_TABLE, Column.ID.dbName() + "=" + rowId,
				null) > 0;
	}

	/**
	 * Return a Cursor over the list of all notes in the database
	 * 
	 * @return Cursor over all notes
	 */
	public Cursor fetchAllEntries() {

		return mDb.query(DATABASE_TABLE, new String[] { Column.ID.dbName(),
				Column.KEY.dbName(), Column.VALUE.dbName() }, null, null, null,
				null, null);
	}

	/**
	 * Return a Cursor positioned at the note that matches the given rowId
	 * 
	 * @param rowId
	 *            id of note to retrieve
	 * @return Cursor positioned to matching note, if found
	 * @throws SQLException
	 *             if note could not be found/retrieved
	 */
	private Cursor fetchEntry(long rowId) throws SQLException {

		Cursor mCursor =

		mDb.query(true, DATABASE_TABLE, new String[] { Column.ID.dbName(),
				Column.KEY.dbName(), Column.VALUE.dbName() },
				Column.ID.dbName() + "=" + rowId, null, null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;

	}

	public Cursor fetchEntry(String key) {
		Log.v(TAG, "---> Starting fetchEntry:key=" + key);
		Cursor cursor = mDb.query(true, // distinct
				DATABASE_TABLE, // table
				new String[] { Column.ID.dbName(), Column.KEY.dbName(),
						Column.VALUE.dbName() }, // columns
				// Column.KEY.dbColumnName() + "=?",// selection,
				// new String[] { key },// selectionArgs,
				Column.KEY.dbName() + "='" + key + "'",// selection,
				null,// selectionArgs,
				null,// groupBy,
				null,// having,
				null,// orderBy,
				null// limit
				);

		Log.v(TAG, "Cursor-Info:");
		Log.v(TAG, "->Open=" + !cursor.isClosed());
		Log.v(TAG, "->Number of Columns=" + cursor.getColumnCount()
				+ ". Column names:");
		for (int i = 0; i < cursor.getColumnCount(); i++) {
			Log.v(TAG, "Column " + i + "=" + cursor.getColumnName(i));
		}
		Log.v(TAG, "---------------------------------------");
		Log.v(TAG, "Number of rows=" + cursor.getCount() + ". All rows:");
		if (cursor.moveToFirst()) {
			do {
				String row = "|";
				for (int j = 0; j < cursor.getColumnCount(); j++) {
					if (cursor.getColumnCount() == 0)
						row = row + cursor.getInt(j) + "|";
					else
						row = row + cursor.getString(j) + "|";
				}
				Log.v(TAG, row);
			} while (cursor.moveToNext());
		} else
			Log.v(TAG, "No rows");
		Log.v(TAG, "---------------------------------------");
		return cursor;
	}

	/**
	 * Open the notes database. If it cannot be opened, try to create a new
	 * instance of the database. If it cannot be created, throw an exception to
	 * signal the failure
	 * 
	 * @return this (self reference, allowing this to be chained in an
	 *         initialization call)
	 * @throws SQLException
	 *             if the database could be neither opened or created
	 */
	public DentoDbAdapter open() throws SQLException {
		Log.v(TAG, "----> DentoDbAdapter.Open()");
		mDbHelper = new DatabaseHelper(mCtx);
		Log.v(TAG, "----> Calling getWritableDatabase()");
		mDb = mDbHelper.getWritableDatabase();
		Log.v(TAG, "----> getWritableDatabase() called");
		return this;
	}

	/**
	 * Update the note using the details provided. The note to be updated is
	 * specified using the rowId, and it is altered to use the title and body
	 * values passed in
	 * 
	 * @param rowId
	 *            id of note to update
	 * @param key
	 *            value to set note title to
	 * @param value
	 *            value to set note body to
	 * @return true if the note was successfully updated, false otherwise
	 */
	public boolean updateEntry(long rowId, String key, String value) {
		ContentValues args = new ContentValues();
		args.put(Column.KEY.dbName(), key);
		args.put(Column.VALUE.dbName(), value);

		Log.v(TAG, "Try to update existing entry in database " + DATABASE_TABLE
				+ ": " + args);

		long rows = mDb.update(DATABASE_TABLE, args, Column.ID.dbName() + "="
				+ rowId, null);
		if (rows > 0)
			Log.v(TAG, "...Succesfull! Number of rows=" + rows);
		else
			Log.e(TAG, "Failed! Couldn't update database entry.");

		return rows > 0;

	}

}
