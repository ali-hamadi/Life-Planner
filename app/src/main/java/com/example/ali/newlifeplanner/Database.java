package com.example.ali.newlifeplanner;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class Database extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "entries.db";

    //Constructor of SQLiteOpenHelper subclass.
    public Database(Context context, String name, CursorFactory factory, int version) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //Execute an SQL statement; creating the table with the attributes for a task.
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE TASKS (_id INTEGER PRIMARY KEY AUTOINCREMENT, NAME text, " +
                "DETAILS text, MINUTE integer, HOUR integer,DAY integer, MONTH integer, YEAR integer)");
    }

    //Executes when the table is upgraded
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS TASKS");
        onCreate(db);
    }

}
