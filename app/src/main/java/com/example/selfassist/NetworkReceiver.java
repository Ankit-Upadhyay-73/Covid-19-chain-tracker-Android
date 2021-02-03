package com.example.selfassist;

import android.app.Application;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.legacy.content.WakefulBroadcastReceiver;

import com.example.selfassist.dbWork.DbUses;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public  class NetworkReceiver extends BroadcastReceiver
{

//    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onReceive(Context context, Intent intent)
    {
        backgroundService(context);
    }

//    @RequiresApi(api = Build.VERSION_CODES.M)
    public void backgroundService(Context context)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        boolean isConnected = networkInfo != null;
        if (isConnected)
        {
            DbUses dbUses = new DbUses(context);
            SQLiteDatabase database = dbUses.getReadableDatabase();
            String contactNumber = null;
            Cursor cursor = database.rawQuery("SELECT * FROM currentUser",null);
            if (cursor.getCount()!=0)
            {
                cursor.moveToFirst();
                contactNumber = cursor.getString(1);
                System.out.println("Contact Number is on db "+contactNumber);
            }
            Intent serviceIntent = new Intent(context,ServiceForNotification.class);
            serviceIntent.putExtra("mobileNumber",contactNumber);
            context.startService(serviceIntent);
        }

/*
        if (!ConnectionHelper.isConnectedOrConnecting(context))
        {
            if (context != null)
            {
                boolean show = false;
                if (ConnectionHelper.lastNoConnectionTs == -1)
                {//first time
                    show = true;
                    ConnectionHelper.lastNoConnectionTs = System.currentTimeMillis();
                }
                else
                {
                    if (System.currentTimeMillis() - ConnectionHelper.lastNoConnectionTs > 1000)
                    {
                        show = true;
                        ConnectionHelper.lastNoConnectionTs = System.currentTimeMillis();
                    }
                }

                if (show && ConnectionHelper.isOnline)
                {
                    ConnectionHelper.isOnline = false;
                    Log.i("NETWORK123","Connection lost");
                    //manager.cancelAll();
                }
            }
        } else
            {
            Log.i("NETWORK123","Connected");
            // Perform your actions here
            ConnectionHelper.isOnline = true;
            Intent serviceIntent = new Intent(context,ServiceForNotification.class);
            context.startService(serviceIntent);
        }
*/
    }

    //for testing
    public void createNotification(Context context)
    {
        String CHANNEL_ID = "1001";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.icons_use)
                .setContentTitle("SafetyGuard")
                .setContentText("A person covid-19 positive in this area")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                // Set the intent that will fire when the user taps the notification
                .setAutoCancel(true);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(1, builder.build());
    }
}