package com.example.selfassist.dbWork;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DbUses extends SQLiteOpenHelper
{
    public DbUses(@Nullable Context context)
    {
        super(context,"covidAssistant.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL("CREATE TABLE currentUser(name text(200),mobileNumber text(100) primary key)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL("DROP table currentUser");
        db.execSQL("CREATE TABLE currentUser(name text(200),mobileNumber text(100) primary key)");
    }
}
