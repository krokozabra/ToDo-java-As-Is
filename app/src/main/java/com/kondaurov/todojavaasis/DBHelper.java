package com.kondaurov.todojavaasis;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION =4;
    public static final String DATABASE_NAME ="todoDB";
    public static final String TABLE_TO_DO_LIST ="todoList";
    public static final String TABLE_EVERY_DAY_LIST ="everyDayList";

    public static final String ONE_KEY_ID = "_id";
    public static final String ONE_NAME_TODO = "name";
    public static final String ONE_DESCRIPTION_TODO = "description";
    public static final String ONE_DAY_TODO = "day";
    public static final String ONE_MONTH_TODO = "month";
    public static final String ONE_YEAR_TODO = "year";
    public static final String ONE_OK_TODO = "OK";


    public static final String TWO_KEY_ID = "_id";
    public static final String TWO_NAME_TODO = "name";
    public static final String TWO_DESCRIPTION_TODO = "description";

    public DBHelper(Context context)
    {
        super(context, DATABASE_NAME,null,DATABASE_VERSION);
    }

    @Override
    public  void onCreate(SQLiteDatabase db)
    {
        db.execSQL("CREATE TABLE "+ TABLE_TO_DO_LIST + "("+
                ONE_KEY_ID + " INTEGER PRIMARY KEY, " +
                ONE_NAME_TODO + " TEXT," +
                ONE_DESCRIPTION_TODO + " TEXT," +
                ONE_DAY_TODO + " NUMERIC," +
                ONE_MONTH_TODO + " NUMERIC," +
                ONE_YEAR_TODO + " NUMERIC," +
                ONE_OK_TODO + " NUMERIC" +
                ")");

        db.execSQL("CREATE TABLE "+ TABLE_EVERY_DAY_LIST + "("+
                TWO_KEY_ID + " INTEGER PRIMARY KEY, " +
                TWO_NAME_TODO + " TEXT," +
                TWO_DESCRIPTION_TODO + " TEXT" +
                ")");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db,int oldVersion, int newVersion)
    {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TO_DO_LIST);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVERY_DAY_LIST);
        onCreate(db);
    }


}
