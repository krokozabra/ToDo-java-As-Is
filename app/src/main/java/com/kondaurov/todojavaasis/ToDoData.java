package com.kondaurov.todojavaasis;

import java.sql.Blob;
import java.util.Calendar;

public class ToDoData {
    int id;
    String name;
    String day;
    String month;
    String year;
    String description;
    int OK;
    int everyday;

    public ToDoData(int id, String name, String day, String month, String year, String description, int OK, int everyday) {
        this.id = id;
        this.name = name;
        this.day = day;
        this.month = month;
        this.year = year;
        this.description = description;
        this.OK = OK;
        this.everyday = everyday;
    }
}
