package com.example.selfassist;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.dynamic.IFragmentWrapper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class ServiceForNotification extends Service
{
    boolean isConnected = false;
    String mobileNumber;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     *
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        mobileNumber = intent.getStringExtra("mobileNumber");
        notificationBackendWork();

//        createNotification();
        return START_NOT_STICKY;
    }

    static final String CONNECTIVITY_CHANGE_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
    NotificationManager manager ;


    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }

    ArrayList<HashMap<String,String>> userVisitedLocation = new ArrayList<>();
    ArrayList<HashMap<String,String>> usersLocations = new ArrayList<>();

    public void notificationBackendWork()
    {
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("users");
        databaseReference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                Iterator<DataSnapshot> dataSnapshotIterator = dataSnapshot.getChildren().iterator();
                System.out.println("notification values are conc value "+mobileNumber);
                if (dataSnapshotIterator.hasNext())
                {
                    DataSnapshot value = dataSnapshotIterator.next();
                    if (value.getKey().toString().equals(mobileNumber))
                    {
                        Iterator<DataSnapshot> allDetails =  value.getChildren().iterator();
                        while (allDetails.hasNext())
                        {
                            DataSnapshot  locationValue = allDetails.next();
                            if (locationValue.getKey().equals("locationDetails"))
                            {
                                System.out.println(locationValue.getValue());
                                userVisitedLocation.add((HashMap)locationValue.getValue());
                            }
                            if (locationValue.getKey().equals("meetPersons"))
                            {
                                locationValue.getRef().addValueEventListener(new ValueEventListener()
                                {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                    {
                                        System.out.println("All childrens in meetpersoniterator "+dataSnapshot.getChildrenCount());
                                        Iterator<DataSnapshot> meetPersonsIterator = dataSnapshot.getChildren().iterator();
                                        while (meetPersonsIterator.hasNext())
                                        {
                                            DataSnapshot db = meetPersonsIterator.next();
                                            Iterator<DataSnapshot> personsLoc = db.getChildren().iterator();
                                            System.out.println("childrens of personloc  "+db.getChildrenCount());
                                            while (personsLoc.hasNext())
                                            {
                                                DataSnapshot p = personsLoc.next();
                                                System.out.println("Location Values");
                                                System.out.println(p.getValue());
                                                if (p.getKey().equals("locationDetails"))
                                                {
                                                    userVisitedLocation.add((HashMap)p.getValue());
                                                }
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError)
                                    {

                                    }
                                });
                            }
                        }
                    }
                }

                final DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("covidAffectedUsers");
                usersRef.addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                         Iterator<DataSnapshot> userValues =  dataSnapshot.getChildren().iterator();
                         while (userValues.hasNext())
                         {
                            Iterator<DataSnapshot> locationDetails =  userValues.next().getChildren().iterator();
                            while (locationDetails.hasNext())
                            {
                                DataSnapshot values = locationDetails.next();
                                if (values.getKey().equals("locationDetails"))
                                {
                                    HashMap hashMap = (HashMap) values.getValue();
                                    usersLocations.add(hashMap);
                                }
                                if (values.getKey().equals("meetPersons"))
                                {
                                    values.getRef().addValueEventListener(new ValueEventListener()
                                    {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                        {
                                            Iterator<DataSnapshot> meetPersonsIterator = dataSnapshot.getChildren().iterator();
                                            while (meetPersonsIterator.hasNext())
                                            {
                                                Iterator<DataSnapshot> personsLoc =  meetPersonsIterator.next().getChildren().iterator();
                                                while (personsLoc.hasNext())
                                                {
                                                    DataSnapshot p = personsLoc.next();
                                                    if (p.getKey().equals("locationDetails"))
                                                    {
                                                        usersLocations.add((HashMap)p.getValue());
                                                    }
                                                }
                                            }

                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError)
                                        {

                                        }
                                    });
                                }
                            }
                         }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                boolean isFoundInLocation = conclusion();
                System.out.println(isFoundInLocation);
                if (isFoundInLocation)
                {
                    createNotification();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });

    }

    public boolean conclusion()
    {
        System.out.println("length of Arrays are: "+userVisitedLocation.size()+"    "+usersLocations.size());
        for (int i=0;i<usersLocations.size();i++)
        {
            HashMap<String,String>  value = usersLocations.get(i);
            if (value!=null)
            {
                for (int j= 0;j<userVisitedLocation.size();j++)
                {
                    HashMap<String,String> userValue  = userVisitedLocation.get(j);
                    System.out.println(value.get("Locality"));
                    if (userValue!=null)
                    {
                        if ((value.get("Thoroughfare").equals(userValue.get("Thoroughfare"))) && (value.get("SubThoroughfare").equals(userValue.get("SubThoroughfare"))) && ((value.get("CountryName").equals(userValue.get("CountryName")))))
                        {
                            System.out.println("in any covid+ in your location :yes");
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public void createNotification()
    {
        Intent intent = new Intent(this, RegisterWindow.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        String CHANNEL_ID = "1001";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.icons_use)
                .setContentTitle("SafetyGuard")
                .setContentText("A person covid-19 positive in this area")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(1, builder.build());
    }

}
