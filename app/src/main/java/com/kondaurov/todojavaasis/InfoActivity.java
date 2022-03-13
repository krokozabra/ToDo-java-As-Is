package com.kondaurov.todojavaasis;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import java.util.Calendar;

public class InfoActivity extends AppCompatActivity {

    TextView name, description, date;
    int everyday,delete;
    int idTask;
    DBHelper dbHelper;
    Button dellBtn, completeBtn;

    public static final String EVERYDAY = "everyday";
    public static final String IDTASK = "idtask";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        Bundle arguments = getIntent().getExtras();
        everyday = arguments.getInt(EVERYDAY);
        idTask = arguments.getInt(IDTASK);

        name = findViewById(R.id.ai_name_tack);
        description = findViewById(R.id.ai_description_tack);
        date = findViewById(R.id.ai_date);
        dellBtn = findViewById(R.id.ai_btn_dell);
        completeBtn = findViewById(R.id.ai_btn_complete);

        dbHelper = new DBHelper(this);

        GetCurrentTask gct = new GetCurrentTask();
        gct.execute();

        dellBtn.setOnClickListener(view ->
        {
            delete =1;
            dellCompTask dct = new dellCompTask();
            dct.execute();
        });

        completeBtn.setOnClickListener(view ->
        {
            delete =0;
            dellCompTask dct = new dellCompTask();
            dct.execute();
        });

    }

    private class GetCurrentTask extends AsyncTask<Void, Void, ToDoData> {

        @Override
        protected ToDoData doInBackground(Void... params) {
            SQLiteDatabase database = dbHelper.getWritableDatabase();
            ToDoData currentTask;
            Cursor cursor;
            System.out.println(idTask+" id task");
            if (everyday == 0) {
                cursor = database.query(DBHelper.TABLE_TO_DO_LIST, null, DBHelper.ONE_KEY_ID + " = " + idTask, null, null, null, null);
                cursor.moveToFirst();

                int idIndex = cursor.getColumnIndex(DBHelper.ONE_KEY_ID);
                int nameIndex = cursor.getColumnIndex(DBHelper.ONE_NAME_TODO);
                int descIndex = cursor.getColumnIndex(DBHelper.ONE_DESCRIPTION_TODO);
                int dayIndex = cursor.getColumnIndex(DBHelper.ONE_DAY_TODO);
                int monthIndex = cursor.getColumnIndex(DBHelper.ONE_MONTH_TODO);
                int yearIndex = cursor.getColumnIndex(DBHelper.ONE_YEAR_TODO);
                int OKIndex = cursor.getColumnIndex(DBHelper.ONE_OK_TODO);


                currentTask = new ToDoData(cursor.getInt(idIndex), cursor.getString(nameIndex), cursor.getString(dayIndex),cursor.getString(monthIndex),cursor.getString(yearIndex), cursor.getString(descIndex), cursor.getInt(OKIndex), everyday);
                Log.d("mLog", "0 rows");
            }
            else {
                cursor = database.query(DBHelper.TABLE_EVERY_DAY_LIST, null, DBHelper.TWO_KEY_ID + " = " + idTask, null, null, null, null);

                cursor.moveToFirst();

                int idIndex = cursor.getColumnIndex(DBHelper.ONE_KEY_ID);
                int nameIndex = cursor.getColumnIndex(DBHelper.ONE_NAME_TODO);
                int descIndex = cursor.getColumnIndex(DBHelper.ONE_DESCRIPTION_TODO);
                int dayIndex = cursor.getColumnIndex(DBHelper.ONE_DAY_TODO);
                int monthIndex = cursor.getColumnIndex(DBHelper.ONE_MONTH_TODO);
                int yearIndex = cursor.getColumnIndex(DBHelper.ONE_YEAR_TODO);
                int OKIndex = cursor.getColumnIndex(DBHelper.ONE_OK_TODO);

                currentTask = new ToDoData(cursor.getInt(idIndex), cursor.getString(nameIndex), cursor.getString(dayIndex),cursor.getString(monthIndex),cursor.getString(yearIndex), cursor.getString(descIndex), cursor.getInt(OKIndex), everyday);
                Log.d("mLog", "0 rows");
            }

            dbHelper.close();

            return currentTask;

        }

        @Override
        protected void onPostExecute(ToDoData currentTask) {
            super.onPostExecute(currentTask);

            name.setText(currentTask.name);
            description.setText(currentTask.description);
            Calendar currentDate = Calendar.getInstance();
            currentDate.set(Integer.parseInt(currentTask.year),Integer.parseInt(currentTask.month),Integer.parseInt(currentTask.day));
            date.setText(DateUtils.formatDateTime(InfoActivity.this,
                    currentDate.getTimeInMillis(),
                    DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR));
        }
    }



    private class dellCompTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            SQLiteDatabase database = dbHelper.getWritableDatabase();
            String OK="1";
            ContentValues cv = new ContentValues();
            cv.put(DBHelper.ONE_OK_TODO,OK);
            int cursor;
            System.out.println(idTask+" id task");
            if (delete == 1)
                cursor = database.delete(DBHelper.TABLE_TO_DO_LIST, DBHelper.ONE_KEY_ID + " = " + idTask, null);
            else
                cursor = database.update(DBHelper.TABLE_TO_DO_LIST, cv, DBHelper.ONE_KEY_ID + " = " + idTask, null);

            System.out.println("некий текст"+cursor);
            dbHelper.close();

            return null;
        }

        @Override
        protected void onPostExecute(Void params) {
            super.onPostExecute(params);

            Intent intent = new Intent(InfoActivity.this, MainActivity.class);
            startActivity(intent);
        }
    }
}