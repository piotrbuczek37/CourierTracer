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
import com.ugprojects.couriertracerdpd.service.MapsService;

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
    MapsService mapsService;

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
        mapsService = new MapsService(CourierMapsActivity.this);

        Toast.makeText(getApplicationContext(), "Trwa wykrywanie sygnału GPS...", Toast.LENGTH_LONG).show();

        moveCameraToCourierPositionAndSaveItToFirebase();

        askAboutGPSPermission();
    }

    /**
     * This method checks if the permission for GPS Location is present
     * If so it enables GPS function to listen the location of the user
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startListening();
        }
    }

    /**
     * This method checks if the internet connection is enabled. If so, it enables the location manager
     * which connects with GPS provider
     */
    public void startListening() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }
    }

    /**
     * This method moves marker to the specified localization and sets the title of the marker
     *
     * @param latLng is the location
     * @param title  is the title of marker
     * @return marker with location and title
     */
    public MarkerOptions moveMarker(LatLng latLng, String title) {
        return new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_car_marker_transparent))
                .title(title);
    }

    /**
     * This method moves the marker to the specified address and sets the title of the marker
     * (this is used for packages)
     *
     * @param latLng is the location of package
     * @param title  is the title of marker
     * @return marker with location and title
     */
    public MarkerOptions moveMarkerForAddress(LatLng latLng, String title) {
        return new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                .title(title);
    }

    /**
     * Gets location from the given address
     *
     * @param strAddress is the address in plain text
     * @return location on the map
     */
    public LatLng getLocationFromAddress(String strAddress) {
        return mapsService.getLocationFromAddress(strAddress);
    }

    /**
     * This method creates map with Google Maps, sets the marker location, UISettings and moves camera
     * to the marker
     *
     * @param googleMap is the map from Google API
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng courier = new LatLng(54.350866, 18.645663);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(courier));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(12), 3000, null);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);

        addPackagesMarkerOnMap();
    }

    /**
     * This method is called when back button is pressed and it cleans the information about courier
     * of the package
     */
    @Override
    public void onBackPressed() {
        isWorking = false;
        for (String packageNumber : packageNumbers) {
            firebaseService.removeCourierIDFromPackage(packageNumber);
        }
        finish();
    }

    /**
     * Gets information from the previous activity
     */
    private void getExtras() {
        Bundle extras = getIntent().getExtras();
        courierID = extras.getString("courierID");
        packageAddresses = extras.getStringArrayList("packageAddresses");
        packageNumbers = extras.getStringArrayList("packageNumbers");
    }

    /**
     * Gets address from list of package address and creates markers on the map
     */
    private void addPackagesMarkerOnMap() {
        for (String packageAddress : packageAddresses) {
            mMap.addMarker(moveMarkerForAddress(getLocationFromAddress(packageAddress), packageAddress));
            LatLng point = getLocationFromAddress(packageAddress);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(point));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(12), 3000, null);
        }
    }

    /**
     * This method updates the position of courier and after every change of location it updates
     * the database with new courier location. If it's first launch of the map, it moves the camera
     * to the specified localization on the map
     */
    private void moveCameraToCourierPositionAndSaveItToFirebase() {
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                LatLng courierLocalization = null;
                if (location != null && isWorking) {
                    courierLocalization = new LatLng(location.getLatitude(), location.getLongitude());
                    firebaseService.saveCourierLocation(courierID, location.getLatitude(), location.getLongitude());
                }
                if (courierMarker != null && isWorking) {
                    courierMarker.remove();
                }
                if (courierLocalization != null && isWorking) {
                    if (mMap != null) {
                        courierMarker = mMap.addMarker(moveMarker(courierLocalization, "Moja lokalizacja"));
                        firebaseService.saveCourierLocation(courierID, courierLocalization.latitude, courierLocalization.longitude);
                        if (firstZoom) {
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(courierLocalization));
                            mMap.animateCamera(CameraUpdateFactory.zoomTo(12), 3000, null);
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
    }

    /**
     * Creates notification about agreement to enabling the GPS location
     */
    private void askAboutGPSPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
    }
}
