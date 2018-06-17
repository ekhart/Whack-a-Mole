package pl.ekhart.whack_a_mole;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.sql.SQLException;

/**
 * Created by Ekh on 2015-06-17.
 */
public class DatabaseAdapter {

    public static final String
        KEY_ROWID = "_id",
        KEY_SOUND_SETTING = "sound_setting",
        TAG = "DBAdapter",
        DATABASE_NAME = "settingsdata",
        SETTINGS_TABLE = "settings";
    private final Context context;

    private DatabaseHelper helper;
    private SQLiteDatabase db;

    public DatabaseAdapter(Context context) {
        this.context = context;
        helper = new DatabaseHelper(context);
    }

    private static class DatabaseHelper
        extends SQLiteOpenHelper {

        private static final int DATABASE_VERSION = 1;
        private static final String CREATE_SETTINGS_TABLE =
            "create table " + SETTINGS_TABLE +
            "(_id integer primary key autoincrement," +
            " sound_setting text not null);";

        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_SETTINGS_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion
                + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS todo");
            onCreate(db);
        }
    }

    public DatabaseAdapter open() throws SQLException {
        db = helper.getWritableDatabase();
        return this;
    }

    public void close() {
        helper.close();
    }

    public long insertRecord(String newSoundSetting) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_SOUND_SETTING, newSoundSetting);
        return db.insert(SETTINGS_TABLE, null, initialValues);
    }

    public boolean updateRecord(long rowId, String newSoundSetting) {
        ContentValues values = new ContentValues();
        values.put(KEY_SOUND_SETTING, newSoundSetting);
        return db.update(SETTINGS_TABLE, values,
                equalRowId(rowId), null) > 0;
    }

    public void insertOrUpdateRecord(String newSoundSettings) {
        String INSERT_OR_UPDATE_RECORD =
            "INSERT OR REPLACE INTO " + SETTINGS_TABLE +
            "(" + KEY_ROWID + "," + KEY_SOUND_SETTING + ")" +
            "VALUES (1,'" + newSoundSettings + "');";
        db.execSQL(INSERT_OR_UPDATE_RECORD);
    }

    public boolean deleteRecord(long rowId) {
        return db.delete(SETTINGS_TABLE, equalRowId(rowId), null) > 0;
    }

    private String equalRowId(long rowId) {
        return KEY_ROWID + "=" + rowId;
    }

    String[] columns = new String[] {
            KEY_ROWID, KEY_SOUND_SETTING
    };

    public Cursor getAllRecords() {
        return db.query(SETTINGS_TABLE, columns, null, null, null, null, null);
    }

    public Cursor getRecord(long rowId) throws SQLException {
        Cursor cursor = db.query(true, SETTINGS_TABLE, columns,
                equalRowId(rowId),
                null, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }
}
