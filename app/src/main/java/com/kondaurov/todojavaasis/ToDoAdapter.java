package com.kondaurov.todojavaasis;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Observable;

public class ToDoAdapter extends BaseAdapter {

    Context ctx;
    LayoutInflater lInflater;
    ArrayList<ToDoData> objects;
    TextView name;
    CheckBox flagTack;
    RelativeLayout spase;
    DBHelper dbHelper;

    public ToDoAdapter(Context context, ArrayList<ToDoData> todos) {
        ctx = context;
        objects = todos;
        lInflater = (LayoutInflater) ctx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        dbHelper = new DBHelper(context);

    }

    // кол-во элементов
    @Override
    public int getCount() {
        return objects.size();
    }

    // элемент по позиции
    @Override
    public Object getItem(int position) {
        return objects.get(position);
    }

    // id по позиции
    @Override
    public long getItemId(int position) {
        return position;
    }

    // пункт списка
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {


        // используем созданные, но не используемые view
        View view = convertView;
        if (view == null) {
            view = lInflater.inflate(R.layout.item_task, parent, false);
        }

        ToDoData p = getToDoData(position);
        // заполняем View в пункте списка данными из истории
        name = view.findViewById(R.id.it_name);
        name.setText(p.name);
        spase = view.findViewById(R.id.it_space);
        spase.setOnClickListener(v -> {
            System.out.println("название " + name.getText().toString());
            //переход на экран описание таска
            Intent intent = new Intent(ctx, InfoActivity.class);
            intent.putExtra(InfoActivity.EVERYDAY, p.everyday);
            intent.putExtra(InfoActivity.IDTASK, p.id);
            ctx.startActivity(intent);
        });

        flagTack = view.findViewById(R.id.it_check);
        flagTack.setOnClickListener(v->
        {
            System.out.println("значение текущего таска: "+p.OK);
            compTask ct = new compTask();
            if (p.OK==0) {
                ct.execute(1, p.id);
                p.OK = 1;
                spase.setBackgroundColor(ContextCompat.getColor(ctx, R.color.green));

            }
            else {
                ct.execute(0, p.id);
                p.OK = 0;
                spase.setBackgroundColor(ContextCompat.getColor(ctx, R.color.white));

            }
        });


        if (p.OK == 1) {
            ((CheckBox) view.findViewById(R.id.it_check)).setChecked(true);
            spase.setBackgroundColor(ContextCompat.getColor(ctx, R.color.green));
        }
        else {
            ((CheckBox) view.findViewById(R.id.it_check)).setChecked(false);
            spase.setBackgroundColor(ContextCompat.getColor(ctx, R.color.white));

        }

        return view;
    }

    ToDoData getToDoData(int position) {
        return ((ToDoData) getItem(position));
    }

    private class compTask extends AsyncTask<Integer, Void, Void> {

        @Override
        protected Void doInBackground(Integer... params) {
            SQLiteDatabase database = dbHelper.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put(DBHelper.ONE_OK_TODO, params[0]);
            int cursor;
            cursor = database.update(DBHelper.TABLE_TO_DO_LIST, cv, DBHelper.ONE_KEY_ID + " = " + params[1], null);

            System.out.println("изменено строк: " + cursor);
            System.out.println("что меняем: " + params[1]);
            System.out.println("на что меняем: " + params[0]);

            dbHelper.close();

            return null;
        }

        @Override
        protected void onPostExecute(Void params) {
            super.onPostExecute(params);

        }
    }
}
