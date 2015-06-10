package com.roysolberg.android.quickapplauncher;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    protected static final int DATABASE_VERSION = 3;
    public static final String DATABASE = "quickapplauncher.db";
    public static final String TABLE_APPS = "apps";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_PACKAGE = "package";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_INSTALL_TIMESTAMP = "install_timestamp";
    public static final String COLUMN_UPDATE_TIMESTAMP = "update_timestamp";
    public static final String COLUMN_NUM_OF_RUNS = "num_of_runs";
    public static final String[] COLUMNS_APPS = new String[]{COLUMN_ID, COLUMN_PACKAGE, COLUMN_NAME, COLUMN_DESCRIPTION, COLUMN_INSTALL_TIMESTAMP, COLUMN_UPDATE_TIMESTAMP, COLUMN_NUM_OF_RUNS};
    
    public DatabaseHelper(Context context) {
        super(context, DATABASE, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_APPS + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_PACKAGE + " STRING,"
                + COLUMN_NAME + " STRING,"
                + COLUMN_DESCRIPTION + " STRING,"
                + COLUMN_INSTALL_TIMESTAMP + " LONG,"
                + COLUMN_UPDATE_TIMESTAMP + " LONG,"
                + COLUMN_NUM_OF_RUNS + " LONG DEFAULT 0,"
                + "UNIQUE(" + COLUMN_PACKAGE + ")"
                + ");");
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_APPS);
        onCreate(db);
    }

}
