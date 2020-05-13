package com.ugprojects.couriertracerdpd.activity;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ugprojects.couriertracerdpd.R;
import com.ugprojects.couriertracerdpd.service.FirebaseService;

import java.util.ArrayList;
import java.util.List;

public class CourierMapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;
    Marker courierMarker;

    String courierID;
    boolean isWorking;
    boolean firstZoom;
    ArrayList<String> packageAddresses;
    ArrayList<String> packageNumbers;

    FirebaseService firebaseService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_courier_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

        isWorking = true;
        firstZoom = true;

        getExtras();

        firebaseService = new FirebaseService();

        Toast.makeText(getApplicationContext(),"Trwa wykrywanie sygnału GPS...",Toast.LENGTH_LONG).show();

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener= new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                LatLng courierLocalization = null;
                if(location!=null&&isWorking){
                    courierLocalization = new LatLng(location.getLatitude(),location.getLongitude());
                    firebaseService.saveCourierLocation(courierID,location.getLatitude(),location.getLongitude());
                }
                if(courierMarker!=null&&isWorking){
                    courierMarker.remove();
                }
                if(courierLocalization!=null&&isWorking){
                    if(mMap!=null){
                        courierMarker = mMap.addMarker(moveMarker(courierLocalization,"Moja lokalizacja"));
                        firebaseService.saveCourierLocation(courierID,courierLocalization.latitude,courierLocalization.longitude);
                        if(firstZoom){
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(courierLocalization));
                            mMap.animateCamera(CameraUpdateFactory.zoomTo(12),3000,null);
                            firstZoom = false;
                        }
                    }
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.ACCESS_FINE_LOCATION},1);
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(lastKnownLocation!=null){
//                locationOfCourier=lastKnownLocation;
//                LatLng firstLocation = new LatLng(lastKnownLocation.getLatitude(),lastKnownLocation.getLongitude());
//                mMap.moveCamera(CameraUpdateFactory.newLatLng(firstLocation));
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            startListening();
        }
    }

    public void startListening(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }
    }

    public MarkerOptions moveMarker(LatLng latLng,String title){
        MarkerOptions markerOptions =new MarkerOptions()
                .position(latLng)
                //.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_car_marker_transparent))
                .title(title);

        return markerOptions;
    }

    public MarkerOptions moveMarkerForAddress(LatLng latLng, String title){
        MarkerOptions markerOptions =new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                .title(title);

        return markerOptions;
    }

    public LatLng getLocationFromAddress(Context context, String strAddress)
    {
        Geocoder coder= new Geocoder(context);
        List<Address> address;
        LatLng p1 = null;

        try
        {
            address = coder.getFromLocationName(strAddress, 5);
            if(address==null)
            {
                return null;
            }
            Address location = address.get(0);
            location.getLatitude();
            location.getLongitude();

            p1 = new LatLng(location.getLatitude(), location.getLongitude());
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return p1;

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng courier = new LatLng(54.350866, 18.645663);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(courier));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(12),3000,null);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);

        for (String packageAddress : packageAddresses) {
            mMap.addMarker(moveMarkerForAddress(getLocationFromAddress(getApplicationContext(),packageAddress),packageAddress));
            LatLng point = getLocationFromAddress(getApplicationContext(),packageAddress);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(point));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(12),3000,null);
        }
    }

    @Override
    public void onBackPressed(){
        isWorking = false;
        for (String packageNumber : packageNumbers) {
            firebaseService.removeCourierIDFromPackage(packageNumber);
        }
        finish();
    }

    private void getExtras(){
        Bundle extras = getIntent().getExtras();
        courierID = extras.getString("courierID");
        packageAddresses = extras.getStringArrayList("packageAddresses");
        packageNumbers = extras.getStringArrayList("packageNumbers");
    }
}
