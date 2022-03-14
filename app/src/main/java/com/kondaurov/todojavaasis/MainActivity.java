package com.kondaurov.todojavaasis;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.reactivestreams.Subscription;

import java.io.BufferedReader;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Calendar;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    DBHelper dbHelper;

    ListView list;

    ArrayList<ToDoData> toDoList = new ArrayList<>();
    ToDoAdapter toDoAdapter;

    SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    public void onRefresh() {
        toDoList.clear();
        list.setAdapter(null);
        /*AsyncToDo aa = new AsyncToDo();
        aa.execute();*/
        mSwipeRefreshLayout.setRefreshing(false); // останавливает анимацию загрузки

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSwipeRefreshLayout = findViewById(R.id.swipe_to_do);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        toDoAdapter = new ToDoAdapter(this, toDoList);

        list = findViewById(R.id.am_list);

        setTitle("Квесты на сегодня");

        dbHelper = new DBHelper(this);
        checkEverydayInTask();


        Observable.fromArray(getToDoList())
                .subscribeOn(Schedulers.newThread())
                .subscribe(observer);
    }

    Observer<ArrayList<ToDoData>> observer = new Observer<ArrayList<ToDoData>>() {

        @Override
        public void onSubscribe(Disposable d) {
            System.out.println("onSubscribe: ");

        }

        @Override
        public void onNext(ArrayList<ToDoData> toDoData) {
            for (ToDoData x : toDoData) {
                System.out.println(x.id + " " + x.name + " " + x.OK);
            }

            toDoList.clear();

            toDoList.addAll(toDoData);

            list.setAdapter(toDoAdapter);
        }

        @Override
        public void onError(Throwable e) {
            System.out.println("onError: ");
        }

        @Override
        public void onComplete() {
            System.out.println("onComplete: All Done!");
        }
    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.dots_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_add: {
                Intent intent = new Intent(this, AddTaskActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.action_every: {
                Intent intent = new Intent(this, EverydayActivity.class);
                startActivity(intent);
                break;
            }
        }
        return true;
    }

    //источник данных для observable
    private ArrayList<ToDoData> getToDoList() {
        ArrayList<ToDoData> todos = new ArrayList<>();
        Calendar today = Calendar.getInstance();
        SQLiteDatabase database = dbHelper.getWritableDatabase();
//            Cursor cursor = database.query(DBHelper.TABLE_TO_DO_LIST, null, DBHelper.ONE_DAY_TODO +"="+ Calendar.DAY_OF_MONTH +" AND "+DBHelper.ONE_MONTH_TODO +"="+ Calendar.MONTH +" AND "+DBHelper.ONE_YEAR_TODO +"="+ Calendar.YEAR, null, null, null, null);
        Log.d("mainLog", "today = " + today.get(Calendar.DAY_OF_MONTH));

        Cursor cursor = database.query(DBHelper.TABLE_TO_DO_LIST, null, DBHelper.ONE_DAY_TODO + " = " + today.get(Calendar.DAY_OF_MONTH) + " AND " + DBHelper.ONE_MONTH_TODO + " = " + today.get(Calendar.MONTH) + " AND " + DBHelper.ONE_YEAR_TODO + " = " + today.get(Calendar.YEAR), null, null, null, null);
        try {
            if (cursor.moveToFirst()) {
                int idIndex = cursor.getColumnIndex(DBHelper.ONE_KEY_ID);
                int nameIndex = cursor.getColumnIndex(DBHelper.ONE_NAME_TODO);
                int descIndex = cursor.getColumnIndex(DBHelper.ONE_DESCRIPTION_TODO);
                int dayIndex = cursor.getColumnIndex(DBHelper.ONE_DAY_TODO);
                int monthIndex = cursor.getColumnIndex(DBHelper.ONE_MONTH_TODO);
                int yearIndex = cursor.getColumnIndex(DBHelper.ONE_YEAR_TODO);
                int OKIndex = cursor.getColumnIndex(DBHelper.ONE_OK_TODO);
                do {
                    ToDoData rec = new ToDoData(cursor.getInt(idIndex), cursor.getString(nameIndex), cursor.getString(dayIndex), cursor.getString(monthIndex), cursor.getString(yearIndex), cursor.getString(descIndex), cursor.getInt(OKIndex), 0);
                    todos.add(rec);
                } while (cursor.moveToNext());
            } else
                Log.d("mainLog", "0 rows");
        } catch (Exception e) {
            Log.d("mainLog", "exept: " + e);
        }
        cursor.close();
        dbHelper.close();

        return todos;
    }


    public void checkEverydayInTask() {
        Calendar today = Calendar.getInstance();
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        Cursor cursor;

        String table = DBHelper.TABLE_TO_DO_LIST + " as t, " + DBHelper.TABLE_EVERY_DAY_LIST + " as e ";
        String[] columns = {"*"};
        String selection = "(" +
                "t." + DBHelper.ONE_DAY_TODO + " = " + today.get(Calendar.DAY_OF_MONTH) + " AND " +
                "t." + DBHelper.ONE_MONTH_TODO + " = " + today.get(Calendar.MONTH) + " AND " +
                "t." + DBHelper.ONE_YEAR_TODO + " = " + today.get(Calendar.YEAR) + " AND " +
                "t." + DBHelper.ONE_NAME_TODO + " = e." + DBHelper.TWO_NAME_TODO + " AND " +
                "t." + DBHelper.ONE_DESCRIPTION_TODO + " = e." + DBHelper.TWO_DESCRIPTION_TODO + ")";
        cursor = database.query(table, columns, selection, null, null, null, null);
        System.out.println("размер курсора =" + cursor.getCount());

        if (cursor.getCount() == 0) {
            insertInToDo();
        }

        cursor.close();


        System.out.println();
        dbHelper.close();

    }

    public void insertInToDo() {
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
            Log.d("mainLog", "exept: " + e);
        }
        cursor.close();
        dbHelper.close();
    }

}