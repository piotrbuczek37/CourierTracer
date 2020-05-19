/* Copyright (C) Piotr Buczek - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Piotr Buczek <piotr.buczek37@gmail.com>, May 2020
 */

package com.ugprojects.couriertracerdpd.activity;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.github.javiersantos.materialstyleddialogs.enums.Style;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ugprojects.couriertracerdpd.R;
import com.ugprojects.couriertracerdpd.model.Courier;
import com.ugprojects.couriertracerdpd.model.CourierBuilder;
import com.ugprojects.couriertracerdpd.service.FirebaseService;
import com.ugprojects.couriertracerdpd.service.MapsService;

public class ClientMapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnCameraIdleListener, GoogleMap.OnCameraMoveListener {

    private static final float DEFUALT_ZOOM = 15f;

    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;
    Marker clientMarker;
    Marker courierMarker;

    DatabaseReference reference;

    String packageNumber;
    String courierID;

    LatLng courierLocalization;
    LatLng clientLocalization;

    boolean isClicked;
    boolean isLooking;
    boolean isTicking;
    CountDownTimer countDownTimer;

    String address;

    Courier courier;

    FirebaseService firebaseService;
    MapsService mapsService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

        isLooking = true;
        isTicking = true;
        isClicked = false;

        firebaseService = new FirebaseService();
        mapsService = new MapsService(ClientMapsActivity.this);

        reference = FirebaseDatabase.getInstance().getReference();

        getExtras();

        courier = new CourierBuilder().build();

        getCourierInfoFromPackageNumberAndMoveMapToCourierPosition();

        Toast.makeText(getApplicationContext(), "Trwa wykrywanie sygnału GPS...", Toast.LENGTH_LONG).show();

        changeCourierLocation();

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
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                .title(title);
    }

    /**
     * This method moves the marker to the specified address and sets the title of the marker
     * (this is used for courier)
     *
     * @param latLng is the location of courier
     * @param title  is the title of marker
     * @return marker with location and title
     */
    public MarkerOptions moveCourierMarker(LatLng latLng, String title) {
        return new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_car_marker_transparent))
                .title(title);
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

        LatLng client = new LatLng(54.350866, 18.645663);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(client));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(DEFUALT_ZOOM));
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);

        mMap.setOnCameraIdleListener(this);
        mMap.setOnCameraMoveListener(this);
        mMap.setOnMarkerClickListener(this);
    }

    /**
     * This method creates a window where client can see information about the courier
     *
     * @param marker is the clicked marker
     */
    @Override
    public boolean onMarkerClick(Marker marker) {
        if (marker.equals(courierMarker)) {
            isClicked = true;
            new MaterialStyledDialog.Builder(ClientMapsActivity.this)
                    .setTitle("Kurier " + courier.getFirstName())
                    .setDescription("Aktualna pozycja kuriera: " + address + "\n" +
                            "Kończy pracę o godz. " + courier.getEndTime() + "\n" +
                            "Numer telefonu: " + courier.getPhoneNumber() + "\n" +
                            "Opis samochodu: " + courier.getCarInfo())
                    .setStyle(Style.HEADER_WITH_TITLE)
                    .setPositiveText("Ok")
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            isClicked = false;
                            dialog.dismiss();
                        }
                    })
                    .show();

            return true;
        }
        return false;
    }

    /**
     * This method is called when back button is pressed, sets the isLooking flag to false
     */
    @Override
    public void onBackPressed() {
        isLooking = false;
        finish();
    }

    /**
     * This method is called when user is doing nothing with map. It starts the timer and after 6 seconds
     * it centers the camera between the courier and client or on the courier only
     * After every action timer resets
     */
    @Override
    public void onCameraIdle() {
        if (isTicking) {
            countDownTimer = new CountDownTimer(6000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    isTicking = false;
                }

                @Override
                public void onFinish() {
                    if (courierLocalization != null && clientLocalization != null && !isClicked) {
                        centerCameraBetweenCourierAndClient();
                    } else if (courierLocalization != null && !isClicked) {
                        centerCameraOnCourier();
                    }
                    isTicking = true;
                }
            };
            countDownTimer.start();
        }
    }

    /**
     * Resets timer on every camera move
     */
    @Override
    public void onCameraMove() {
        isTicking = true;
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
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

    /**
     * Gets information from the previous activity
     */
    public void getExtras() {
        Bundle extras = getIntent().getExtras();
        packageNumber = extras.getString("packageNumber");
    }

    /**
     * This method gets some important information about the courier from the database like:
     * courierID, location, first name, end work time. Then moves the camera to the courier location
     * and gets the address of the location
     */
    private void getCourierInfoFromPackageNumberAndMoveMapToCourierPosition() {
        reference.child("packages").child(packageNumber.toUpperCase().trim()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    courierID = dataSnapshot.child("courierID").getValue().toString();
                    courier = new CourierBuilder().withCourierID(courierID).build();
                    courier.setCourierID(courierID);

                    reference.child("couriers").child(courier.getCourierID()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                courier.setLatitude(Double.parseDouble(dataSnapshot.child("latitude").getValue().toString()));
                                courier.setLongitude(Double.parseDouble(dataSnapshot.child("longitude").getValue().toString()));

                                moveMapToCourierLocalization();

                                courier.setFirstName(firebaseService.getCourierFirstName(courier));
                                courier.setEndTime(firebaseService.getCourierEndTime(courier));

                                address = mapsService.getAddressOfCourierLocalization(courier);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /**
     * This method moves the camera to the current courier localization
     */
    private void moveMapToCourierLocalization() {
        courierLocalization = mapsService.getCourierLocalization(courier);
        courierMarker = mMap.addMarker(moveCourierMarker(courierLocalization, "Lokalizacja kuriera"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(courierLocalization));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(DEFUALT_ZOOM), 3000, null);
    }

    /**
     * This method updates both courier and client location after every location change.
     * Also updates the courier information
     */
    private void changeCourierLocation() {
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                clientLocalization = null;

                if (location != null && isLooking) {
                    clientLocalization = mapsService.getClientLocalization(location);
                }
                if (clientMarker != null && isLooking) {
                    clientMarker.remove();
                }
                if (clientLocalization != null && courierMarker != null && isLooking) {
                    courierMarker.remove();
                }
                if (clientLocalization != null && isLooking) {
                    if (mMap != null) {
                        clientMarker = mMap.addMarker(moveMarker(clientLocalization, "Moja lokalizacja"));
                        firebaseService.saveClientLocation(packageNumber, clientLocalization.latitude, clientLocalization.longitude);
                    }
                }
                if (courierLocalization != null && isLooking) {
                    if (mMap != null) {
                        courierMarker = mMap.addMarker(moveCourierMarker(courierLocalization, "Lokalizacja kuriera"));

                        courier.setLatitude(firebaseService.getCourierLatitude(courier));
                        courier.setLongitude(firebaseService.getCourierLongitude(courier));
                        courierLocalization = mapsService.getCourierLocalization(courier);
                        courier.setFirstName(firebaseService.getCourierFirstName(courier));
                        courier.setEndTime(firebaseService.getCourierEndTime(courier));
                        courier.setPhoneNumber(firebaseService.getCourierPhoneNumber(courier));
                        courier.setCarInfo(firebaseService.getCourierCarInfo(courier));

                        address = mapsService.getAddressOfCourierLocalization(courier);
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
     * This method gets the distance between client and courier and moves the camera between them
     */
    private void centerCameraBetweenCourierAndClient() {
        double latitudeDifference = Math.abs(courierLocalization.latitude + clientLocalization.latitude) / 2;
        double longitudeDifference = Math.abs(courierLocalization.longitude + clientLocalization.longitude) / 2;
        LatLng zoomPoint = new LatLng(latitudeDifference, longitudeDifference);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(zoomPoint, 12);
        mMap.animateCamera(cameraUpdate, 3000, null);
    }

    /**
     * This method centers the camera on the courier location
     */
    private void centerCameraOnCourier() {
        LatLng zoomPoint = new LatLng(courierLocalization.latitude, courierLocalization.longitude);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(zoomPoint, 12);
        mMap.animateCamera(cameraUpdate, 3000, null);
    }
}
