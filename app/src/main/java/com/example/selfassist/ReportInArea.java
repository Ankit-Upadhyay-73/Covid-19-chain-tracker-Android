package com.example.selfassist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.legacy.content.WakefulBroadcastReceiver;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.core.Repo;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class ReportInArea extends FragmentActivity implements OnMapReadyCallback
{
    HashMap<String,String> values = new HashMap<>();
    TextView usernameWithReportText;
    EditText userNameORPlace,locationPoint;
    FusedLocationProviderClient mFusedLocationClient;
    int PERMISSION_ID = 44;
    GoogleMap googleMap;
    double[] latLng = new double[2];
    SupportMapFragment supportMapFragment;
    String contactNumber;
    LinearLayout fieldContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getLastLocation();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_in_area);
        usernameWithReportText = findViewById(R.id.usernameWithreport);
        userNameORPlace  = findViewById(R.id.personNameOrLocation);
        locationPoint =   findViewById(R.id.locationDetails);
        Intent loginIntent  = getIntent();
        usernameWithReportText.setText(loginIntent.getStringExtra("username")+" Report in your area");
        contactNumber = loginIntent.getStringExtra("mobileNumber");
        supportMapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
        fieldContainer = findViewById(R.id.fieldsContainer);
    }

    //map Work

    @Override
    public void onMapReady(final GoogleMap googleMap)
    {
        this.googleMap = googleMap;
        final MarkerOptions markerOptions  =    new MarkerOptions();
        final LatLng position = new LatLng(latLng[0],latLng[1]);
        Geocoder gcd;
        List<Address> addresses;
        Address address;
        try
        {
            gcd = new Geocoder(ReportInArea.this, Locale.getDefault());
            addresses = gcd.getFromLocation(latLng[0], latLng[1], 1);
            address = addresses.get(0);
            locationPoint.setText(address.getAddressLine(0));
        }

        catch (IOException e) { e.printStackTrace(); }

        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener()
        {
            @Override
            public void onMapClick(LatLng latLng)
            {
                //set location in field
                try
                {
                    Geocoder  gcd = new Geocoder(ReportInArea.this, Locale.getDefault());
                    Address address = gcd.getFromLocation(latLng.latitude, latLng.longitude, 1).get(0);
                    values.put("FeatureName",address.getFeatureName());
                    values.put("Thoroughfare",address.getThoroughfare());
                    values.put("SubThoroughfare",address.getSubThoroughfare());
                    values.put("Phone",address.getPhone());
                    values.put("Locality",address.getLocality());
                    values.put("SubLocality",address.getSubLocality());
                    values.put("AdminArea",address.getAdminArea());
                    values.put("Premises",address.getPremises());
                    values.put("SubAdminArea",address.getSubAdminArea());
                    values.put("CountryName",address.getCountryName());
                    System.out.println(address.getFeatureName()+"   "+address.getThoroughfare()+"   "+address.getSubThoroughfare()+"    "+address.getPhone()+"    "+address.getLocality()+"  "+address.getCountryName()+"    "+address.getPostalCode()+" "+address.getAdminArea()+"  "+address.getPremises()+"   "+address.getSubLocality()+"    "+address.getSubAdminArea()+"   ");
                    locationPoint.setText(address.getAddressLine(0));
                }

                catch (IOException e) { e.printStackTrace(); }
                googleMap.clear();
                googleMap.addCircle(new CircleOptions().center(position).radius(150.0).visible(true).strokeColor(Color.RED));
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,16));
                googleMap.addMarker(markerOptions.position(latLng).title("Meeted with at"));
            }
        });
        googleMap.addCircle(new CircleOptions().center(position).radius(150.0).visible(true).strokeColor(Color.RED));
        Marker marker =    googleMap.addMarker(markerOptions.position(position).title("Meeted with at"));
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position,16));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position,16));
    }

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
        if (requestCode == PERMISSION_ID)
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                // Granted. Start getting the location information
            }
        }
    }

    //location work

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
                                        Geocoder gcd = new Geocoder(ReportInArea.this, Locale.getDefault());
                                        List<Address> addresses = gcd.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                                        System.out.println("--------------In getloc"+location.getLatitude()+"   "+location.getLongitude());
                                        Address address = gcd.getFromLocation(location.getLatitude(), location.getLongitude(), 1).get(0);
                                        values.put("FeatureName",address.getFeatureName());
                                        values.put("Thoroughfare",address.getThoroughfare());
                                        values.put("SubThoroughfare",address.getSubThoroughfare());
                                        values.put("Phone",address.getPhone());
                                        values.put("Locality",address.getLocality());
                                        values.put("SubLocality",address.getSubLocality());
                                        values.put("AdminArea",address.getAdminArea());
                                        values.put("Premises",address.getPremises());
                                        values.put("SubAdminArea",address.getSubAdminArea());
                                        values.put("CountryName",address.getCountryName());
                                        latLng[0] = location.getLatitude();
                                        latLng[1] = location.getLongitude();
                                        updateUserCurrentLocation();
                                        supportMapFragment.getMapAsync(ReportInArea.this);
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

    private void updateUserCurrentLocation()
    {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(contactNumber);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               Iterator<DataSnapshot> iterator = dataSnapshot.getChildren().iterator();
               while (iterator.hasNext())
               {
                   DataSnapshot dataSnapshot1 = iterator.next();
                   String locKey =dataSnapshot1.getKey().toString();
                   if (locKey.equals("locationDetails"))
                   {
                       dataSnapshot1.getRef().setValue(values);
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

    //Controller Work

    public void onReportPositive(View view)
    {
        Intent intent = new Intent(ReportInArea.this,ReportPositive.class);
        intent.putExtra("mobileNumber",contactNumber);
        startActivity(intent);
    }

    public void setOnPlaceReport(View view)
    {
        if (userNameORPlace.getText().toString().isEmpty() || locationPoint.getText().toString().isEmpty())
        {
            AlertDialog.Builder alertWindow = new AlertDialog.Builder(this);
            alertWindow.create();
            alertWindow.setMessage("Fields Cannot be empty");
            alertWindow.show();
        }
        else
        {
            System.out.println("Contact number is "+contactNumber);
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(contactNumber);
            final DatabaseReference workingRef =    databaseReference.child("meetPersons");
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            Date date = new Date();
            String curDate = simpleDateFormat.format(date);
            final DatabaseReference dbWorkingRef = workingRef.child(curDate);
            LocationAndPerson locationAndPerson = new LocationAndPerson(userNameORPlace.getText().toString(),values);
            dbWorkingRef.setValue(locationAndPerson);
            Snackbar.make(fieldContainer,"Location Added With Person", Snackbar.LENGTH_LONG).setBackgroundTint(getResources().getColor(R.color.colorRed)).setAction("undo", new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    dbWorkingRef.removeValue();
                }
            }).setActionTextColor(getResources().getColor(R.color.colorPrimary)).show();
        }
    }
}

class LocationAndPerson
{
    String person;
    HashMap<String,String> locationDetails;

    public LocationAndPerson(String person, HashMap<String,String> locationDetails)
    {
        this.person = person;
        this.locationDetails = locationDetails;
    }

    public HashMap<String, String> getLocationDetails() { return locationDetails; }
    public String getPerson()
    {
        return person;
    }
}

