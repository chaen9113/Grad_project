package com.termproject.metaverse;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "database";
    public static final int DATABASE_VERSION = 1;
    public DBHelper (Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sqlUser = "CREATE TABLE IF NOT EXISTS USER ("
                + "uid text primary key);";
        db.execSQL(sqlUser);

        String sqlGuest = "CREATE TABLE IF NOT EXISTS GUEST ("
                + "gid text primary key);";
        db.execSQL(sqlGuest);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVer, int newVer) {
        String sqlUser = "DROP TABLE IF EXISTS USER";
        db.execSQL(sqlUser);

        String sqlGuest = "DROP TABLE IF EXISTS GUEST";
        db.execSQL(sqlGuest);

        onCreate(db);
    }
}
