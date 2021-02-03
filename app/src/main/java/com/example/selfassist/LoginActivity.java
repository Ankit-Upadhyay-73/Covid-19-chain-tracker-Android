package com.example.selfassist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.selfassist.dbWork.DbUses;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

public class LoginActivity extends AppCompatActivity
{
    CountDownLatch countDownLatch = new CountDownLatch(1);
    ArrayList<String> mobileNumbers = new ArrayList<>();
    EditText username,mobileNumber;
    FusedLocationProviderClient mFusedLocationClient;
    int PERMISSION_ID = 44;
    boolean isAvlbl = false;
    boolean isCovidAffected  = false;
    HashMap<String,String> locationDetails = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        backendLoginWork();
        getLastLocation();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        username =   findViewById(R.id.username);
        mobileNumber = findViewById(R.id.mobileNumber);
        mobileNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                backendLoginWork();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    // location work

    private boolean checkPermissions()
    {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    private void requestPermissions()
    {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSION_ID
        );
    }

    private boolean isLocationEnabled()
    {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Granted. Start getting the location information
            }
        }
    }

    private double[] getLastLocation()
    {
        if (checkPermissions())
        {
            if (isLocationEnabled())
            {
                mFusedLocationClient.getLastLocation().addOnCompleteListener(
                        new OnCompleteListener<Location>() {
                            @Override
                            public void onComplete(@NonNull Task<Location> task) {
                                Location location = task.getResult();
                                if (location == null)
                                {
                                    requestNewLocationData();
                                }
                                else
                                {
                                    try
                                    {
                                        Geocoder gcd = new Geocoder(LoginActivity.this, Locale.getDefault());
                                        System.out.println("--------------In getloc"+location.getLatitude()+"   "+location.getLongitude());
                                        updateUserCurrentLocation(location.getLatitude(),location.getLongitude());
                                    }
                                    catch (IOException e)
                                    {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                );
            }
            else
            {
                Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        } else {
            requestPermissions();
        }
        return null;
    }

    //backend Functions

    private void updateUserCurrentLocation(double Latitude,double Longitude) throws IOException
    {
        Geocoder  gcd = new Geocoder(LoginActivity.this, Locale.getDefault());
        Address address = gcd.getFromLocation(Latitude, Longitude, 1).get(0);
        locationDetails.put("FeatureName",address.getFeatureName());
        locationDetails.put("Thoroughfare",address.getThoroughfare());
        locationDetails.put("SubThoroughfare",address.getSubThoroughfare());
        locationDetails.put("Phone",address.getPhone());
        locationDetails.put("Locality",address.getLocality());
        locationDetails.put("SubLocality",address.getSubLocality());
        locationDetails.put("AdminArea",address.getAdminArea());
        locationDetails.put("Premises",address.getPremises());
        locationDetails.put("SubAdminArea",address.getSubAdminArea());
        locationDetails.put("CountryName",address.getCountryName());
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(mobileNumber.getText().toString());
        databaseReference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                Iterator<DataSnapshot> iterator = dataSnapshot.getChildren().iterator();
                while (iterator.hasNext())
                {
                    DataSnapshot dataSnapshot1 = iterator.next();
                    String locKey =dataSnapshot1.getKey().toString();
                    if (locKey.equals("locationDetails"))
                    {
                        dataSnapshot1.getRef().setValue(locationDetails);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void requestNewLocationData()
    {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationCallback mlocationCallback=new LocationCallback();
        mLocationRequest.setInterval(0);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.requestLocationUpdates(
                mLocationRequest,mlocationCallback,
                Looper.myLooper()
        );
    }


    // controller work

    public void setOnSubmit(View view)
    {
        if (username.getText().toString().isEmpty() || mobileNumber.getText().toString().isEmpty())
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.create();
            builder.setMessage("Fields are required");
            builder.show();
        }
        else
        {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            final DatabaseReference rootRef = database.getReference().child("users");
            if (!isAvlbl && !isCovidAffected)
            {
                User userDetails = new User(username.getText().toString(),mobileNumber.getText().toString(),locationDetails);
                rootRef.child(mobileNumber.getText().toString()).setValue(userDetails);
            }
            else
            {
                if (isCovidAffected)
                {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(LoginActivity.this);
                    alertDialog.setMessage("You Are Found to be 2019-nCoV positive.. needs to be self Quarantine");
                    alertDialog.create();
                    alertDialog.show();
                }
            }
            if (!isCovidAffected)
            {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(LoginActivity.this);
                alertDialog.setMessage("Login Successfully");
                alertDialog.create();
                alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        Intent intent = new Intent(LoginActivity.this,ReportInArea.class);
                        intent.putExtra("username",username.getText().toString());
                        intent.putExtra("mobileNumber",mobileNumber.getText().toString());
                        startActivity(intent);
                    }
                });
                DbUses dbUses = new DbUses(this);
                SQLiteDatabase sqLiteDatabase = dbUses.getWritableDatabase();
                sqLiteDatabase.execSQL("INSERT into currentUser values('"+username.getText().toString()+"','"+mobileNumber.getText().toString()+"')");
                alertDialog.show();
            }
        }
    }

    public void backendLoginWork()
    {
        isAvlbl = false;
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference rootRef = database.getReference().child("users");
        rootRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                Iterator<DataSnapshot> usersAvailble = dataSnapshot.getChildren().iterator();
                while (usersAvailble.hasNext())
                {
                    DataSnapshot data =    usersAvailble.next();
                    if (data.getKey().equals(mobileNumber.getText().toString()))
                    {
                        isAvlbl = true;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });

        DatabaseReference covidAffectedRef =  FirebaseDatabase.getInstance().getReference().child("covidAffectedUsers");
        covidAffectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                 Iterator<DataSnapshot> dataSnapshotIterator = dataSnapshot.getChildren().iterator();
                 if (dataSnapshotIterator.hasNext())
                 {
                     DataSnapshot values = dataSnapshotIterator.next();
                     if (values.getKey().equals(mobileNumber.getText().toString()))
                     {
                         isCovidAffected = true;
                     }
                 }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}

class User
{
    String username;
    String mobileNumber;
    HashMap<String ,String> locationDetails;
    public User(String username, String mobileNumber, HashMap<String,String> locationDetails)
    {
        this.username = username;
        this.mobileNumber = mobileNumber;
        this.locationDetails = locationDetails;
    }

    public HashMap<String, String> getLocationDetails() {
        return locationDetails;
    }

    public String getUsername() {
        return username;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }
}
