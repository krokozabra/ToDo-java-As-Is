package com.kondaurov.todojavaasis;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import java.util.Calendar;

public class AddTaskActivity extends AppCompatActivity implements View.OnClickListener {

    final static String LOG_TAG = "work width sql";

    Button btnAdd;
    EditText etName, etDecription, etDate;
    CheckBox cbEveryDay;

    DBHelper dbHelper;

    Calendar  dateExec =Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        btnAdd = findViewById(R.id.btn_add);
        btnAdd.setOnClickListener(this);

        etName = findViewById(R.id.et_name);
        etDecription = findViewById(R.id.et_decription);
        etDate = findViewById(R.id.et_date);
        cbEveryDay = findViewById(R.id.cb_every_day);

        cbEveryDay.setOnClickListener(view -> {
            if(cbEveryDay.isChecked())
                etDate.setVisibility(View.INVISIBLE);
            else
                etDate.setVisibility(View.VISIBLE);

        });

        dbHelper = new DBHelper(this);

        etDate.setOnClickListener(view ->
        {
            setDate(etDate);
        });

        checkEverydayInTask();
    }

    @Override
    public void onClick(View v) {
        String name = etName.getText().toString();
        String description = etDecription.getText().toString();

        SQLiteDatabase database = dbHelper.getWritableDatabase();

        ContentValues contentValues = new ContentValues();

        if(cbEveryDay.isChecked()) {
            contentValues.put(DBHelper.TWO_NAME_TODO, name);
            contentValues.put(DBHelper.TWO_DESCRIPTION_TODO, description);
            database.insert(DBHelper.TABLE_EVERY_DAY_LIST, null, contentValues);
        }
        else {
            contentValues.put(DBHelper.ONE_NAME_TODO, name);
            contentValues.put(DBHelper.ONE_DESCRIPTION_TODO, description);
            contentValues.put(DBHelper.ONE_DAY_TODO, dateExec.get(Calendar.DAY_OF_MONTH));
            contentValues.put(DBHelper.ONE_MONTH_TODO, dateExec.get(Calendar.MONTH));
            contentValues.put(DBHelper.ONE_YEAR_TODO, dateExec.get(Calendar.YEAR));
            contentValues.put(DBHelper.ONE_OK_TODO, 0);
            database.insert(DBHelper.TABLE_TO_DO_LIST, null, contentValues);
        }
        dbHelper.close();

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void checkEverydayInTask()
    {
        Calendar today = Calendar.getInstance();
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        Cursor cursor ;

        Log.d(LOG_TAG, "---INNER JOIN with query---");
        String table = DBHelper.TABLE_TO_DO_LIST+" as t, "+DBHelper.TABLE_EVERY_DAY_LIST+" as e ";
        String[] columns = {"*"};
        String selection = "(" +
                "t."+DBHelper.ONE_DAY_TODO+" = "+today.get(Calendar.DAY_OF_MONTH)+" AND " +
                "t."+DBHelper.ONE_MONTH_TODO+" = "+today.get(Calendar.MONTH)+" AND " +
                "t."+DBHelper.ONE_YEAR_TODO+" = "+today.get(Calendar.YEAR)+" AND " +
                "t."+DBHelper.ONE_NAME_TODO+" = e."+DBHelper.TWO_NAME_TODO+" AND " +
                "t."+DBHelper.ONE_DESCRIPTION_TODO+" = e."+DBHelper.TWO_DESCRIPTION_TODO+")";
        cursor = database.query(table, columns, selection, null, null, null, null);
        System.out.println("размер курсора =" +cursor.getCount());
        logCursor(cursor);

        if(cursor.getCount()==0)
        {
            insertInToDo();
        }

        cursor.close();
        Log.d(LOG_TAG, "--- ---");


        System.out.println();
        dbHelper.close();

    }

    public void insertInToDo()
    {
        Calendar today = Calendar.getInstance();
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        Cursor cursor = database.query(DBHelper.TABLE_EVERY_DAY_LIST, null, null, null, null, null, null);
        ContentValues cv = new ContentValues();
        try {
            if (cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(DBHelper.TWO_NAME_TODO);
                int descIndex = cursor.getColumnIndex(DBHelper.TWO_DESCRIPTION_TODO);
                do {
                    cv.put(DBHelper.ONE_NAME_TODO, cursor.getString(nameIndex));
                    cv.put(DBHelper.ONE_DESCRIPTION_TODO, cursor.getString(descIndex));
                    cv.put(DBHelper.ONE_DAY_TODO, today.get(Calendar.DAY_OF_MONTH));
                    cv.put(DBHelper.ONE_MONTH_TODO, today.get(Calendar.MONTH));
                    cv.put(DBHelper.ONE_YEAR_TODO, today.get(Calendar.YEAR));
                    cv.put(DBHelper.ONE_OK_TODO, 0);

                    database.insert(DBHelper.TABLE_TO_DO_LIST, null, cv);

                } while (cursor.moveToNext());
            } else
                Log.d("mainLog", "0 rows");
        } catch (Exception e) {
            Log.d("mainLog", "exept: "+e);
        }
        cursor.close();
        dbHelper.close();
    }

    @SuppressLint("Range")
    void logCursor(Cursor cursor) {
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                String str;
                do {
                    str = "";
                    for (String cn : cursor.getColumnNames()) {
                        str = str.concat(cn + " = " + cursor.getString(cursor.getColumnIndex(cn)) + "; ");
                    }
                    Log.d("addTaskAct", str);
                } while (cursor.moveToNext());
            }
        } else Log.d("addTaskAct", "Cursor is null");
    }


    // отображаем диалоговое окно для выбора даты
    public void setDate(View v) {
        new DatePickerDialog(AddTaskActivity.this, d,
                dateExec.get(Calendar.YEAR),
                dateExec.get(Calendar.MONTH),
                dateExec.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    // вывод на экран даты
    private void showDate() {

        etDate.setText(DateUtils.formatDateTime(this,
                dateExec.getTimeInMillis(),
                DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR));
    }


    // установка обработчика выбора даты
    DatePickerDialog.OnDateSetListener d= (view, year, monthOfYear, dayOfMonth) -> {
        dateExec.set(Calendar.YEAR, year);
        dateExec.set(Calendar.MONTH, monthOfYear);
        dateExec.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        showDate();
    };
}
