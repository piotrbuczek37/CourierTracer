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

import java.util.ArrayList;
import java.util.List;

public class CourierMapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final float DEFUALT_ZOOM = 15f;

    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;
    //Location locationOfCourier;
    Marker courierMarker;
    DatabaseReference reference;
    String courierID;
    boolean isWorking;
    boolean firstZoom;
    ArrayList<String> packageAddresses;
    ArrayList<String> packageNumbers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_courier_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

        isWorking = true;
        firstZoom = true;

        reference = FirebaseDatabase.getInstance().getReference();
        Bundle extras = getIntent().getExtras();
        courierID = extras.getString("courierID");
        packageAddresses = extras.getStringArrayList("packageAddresses");
        packageNumbers = extras.getStringArrayList("packageNumbers");
        Toast.makeText(getApplicationContext(),"Trwa wykrywanie sygnału GPS...",Toast.LENGTH_LONG).show();

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener= new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                LatLng courierLocalization = null;
                if(location!=null&&isWorking){
                    courierLocalization = new LatLng(location.getLatitude(),location.getLongitude());
                    reference.child("couriers").child(courierID).child("latitude").setValue(location.getLatitude());
                    reference.child("couriers").child(courierID).child("longitude").setValue(location.getLongitude());
                }
                if(courierMarker!=null&&isWorking){
                    courierMarker.remove();
                }
                if(courierLocalization!=null&&isWorking){
                    if(mMap!=null){
                        courierMarker = mMap.addMarker(moveMarker(courierLocalization,"Moja lokalizacja"));
                        reference.child("couriers").child(courierID).child("latitude").setValue(courierLocalization.latitude);
                        reference.child("couriers").child(courierID).child("longitude").setValue(courierLocalization.longitude);
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

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
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
            reference.child("packages").child(packageNumber).child("courierID").setValue("none");
        }
        finish();
    }
}
