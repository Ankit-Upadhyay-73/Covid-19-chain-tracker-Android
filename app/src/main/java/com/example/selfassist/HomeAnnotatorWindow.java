package com.example.selfassist;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;

import com.example.selfassist.dbWork.DbUses;

public class HomeAnnotatorWindow extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_annotator_window);
        DbUses dbUses = new DbUses(this);
        SQLiteDatabase sqLiteDatabase = dbUses.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM currentUser",null);
        cursor.moveToFirst();
        if (cursor.getCount()!=0)
        {
            Intent intent = new Intent(this,ReportInArea.class);
            intent.putExtra("mobileNumber",cursor.getString(1));
            intent.putExtra("username",cursor.getString(0));
            startActivity(intent);
        }
    }

    public void onSetupClick(View view)
    {
        Intent intent = new Intent(this,LoginActivity.class);
        startActivity(intent);
    }

    public void onReportClick(View view)
    {
        Intent intent = new Intent(this,LoginActivity.class);
        startActivity(intent);
    }
}
