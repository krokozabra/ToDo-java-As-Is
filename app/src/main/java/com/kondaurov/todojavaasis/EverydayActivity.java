package com.kondaurov.todojavaasis;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Calendar;

public class EverydayActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener{

    DBHelper dbHelper;

    ListView list;

    ArrayList<ToDoData> toDoList = new ArrayList<>();
    ToDoAdapter toDoAdapter;

    SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    public void onRefresh() {
        toDoList.clear();
        list.setAdapter(null);
        AsyncEveryday ae = new AsyncEveryday();
        ae.execute();
        mSwipeRefreshLayout.setRefreshing(false); // останавливает анимацию загрузки

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.dots_every_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_every_add: {
                Intent intent = new Intent(this, AddTaskActivity.class);
                startActivity(intent);
                break;
            }
        }
        return true;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_everyday);

        mSwipeRefreshLayout = findViewById(R.id.swipe_everyday);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        toDoAdapter = new ToDoAdapter(this, toDoList);

        list = findViewById(R.id.ae_list);

        setTitle("Ежедневные квесты");

        dbHelper = new DBHelper(this);

        AsyncEveryday ae = new AsyncEveryday();
        ae.execute();



    }

    private class AsyncEveryday extends AsyncTask<Void, Void, ArrayList<ToDoData>> {

        @Override
        protected ArrayList<ToDoData> doInBackground(Void... params) {
            ArrayList<ToDoData> todos = new ArrayList<>();
            SQLiteDatabase database = dbHelper.getWritableDatabase();
            Cursor cursor = database.query(DBHelper.TABLE_EVERY_DAY_LIST, null, null, null, null, null, null);
            try {
                if (cursor.moveToFirst()) {
                    int idIndex = cursor.getColumnIndex(DBHelper.TWO_KEY_ID);
                    int nameIndex = cursor.getColumnIndex(DBHelper.TWO_NAME_TODO);
                    int descIndex = cursor.getColumnIndex(DBHelper.TWO_DESCRIPTION_TODO);
                    do {

                        ToDoData rec = new ToDoData(cursor.getInt(idIndex), cursor.getString(nameIndex), "0","0","0", cursor.getString(descIndex), 0, 1);
                        todos.add(rec);
                    } while (cursor.moveToNext());
                } else
                    Log.d("mLog", "0 rows");
            } catch (Exception e) {
                System.out.println("ошибка: " + e);
            }
            dbHelper.close();

            return todos;

        }

        @Override
        protected void onPostExecute(ArrayList<ToDoData> arrayToDo) {
            super.onPostExecute(arrayToDo);
            System.out.println("заполнение листа");

            for (ToDoData x : arrayToDo) {
                System.out.println(x.name);
                System.out.println(x.id);
            }

            toDoList.clear();

            toDoList.addAll(arrayToDo);

            list.setAdapter(toDoAdapter);

        }
    }
}