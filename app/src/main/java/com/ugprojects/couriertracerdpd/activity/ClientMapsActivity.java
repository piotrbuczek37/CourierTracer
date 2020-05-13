package com.ugprojects.couriertracerdpd.activity;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
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

import java.io.IOException;
import java.util.List;
import java.util.Locale;

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

        Toast.makeText(getApplicationContext(),"Trwa wykrywanie sygnału GPS...",Toast.LENGTH_LONG).show();

        changeCourierLocation();

        askAboutGPSPermission();
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
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                .title(title);

        return markerOptions;
    }

    public MarkerOptions moveCourierMarker(LatLng latLng, String title){
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_car_marker_transparent))
                .title(title);

        return markerOptions;
    }

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

    @Override
    public boolean onMarkerClick(Marker marker) {
            if(marker.equals(courierMarker)){
                isClicked = true;
                new MaterialStyledDialog.Builder(ClientMapsActivity.this)
                        .setTitle("Kurier "+courier.getFirstName())
                        .setDescription("Aktualna pozycja kuriera: "+ address +"\n" + "Kończy pracę o godz. " +courier.getEndTime() + "\n" + "Numer telefonu: "+courier.getPhoneNumber()+"\n"+"Opis samochodu: " + courier.getCarInfo())
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

    @Override
    public void onBackPressed() {
        isLooking = false;
        finish();
    }

    @Override
    public void onCameraIdle() {
        if(isTicking){
            countDownTimer = new CountDownTimer(6000,1000){
                @Override
                public void onTick(long millisUntilFinished) {
                    isTicking = false;
                }
                @Override
                public void onFinish() {
                    if(courierLocalization!=null&&clientLocalization!=null&&!isClicked){
                        centerCameraBetweenCourierAndClient();
                    } else if (courierLocalization!=null&&!isClicked){
                        centerCameraOnCourier();
                    }
                    isTicking = true;
                }
            };
            countDownTimer.start();
        }
    }

    @Override
    public void onCameraMove() {
        isTicking = true;
        if(countDownTimer!=null){
            countDownTimer.cancel();
        }
    }

    private void askAboutGPSPermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.ACCESS_FINE_LOCATION},1);
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
    }

    public void getExtras(){
        Bundle extras = getIntent().getExtras();
        packageNumber = extras.getString("packageNumber");
    }

    private void getCourierInfoFromPackageNumberAndMoveMapToCourierPosition(){
        reference.child("packages").child(packageNumber.toUpperCase().trim()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    courierID = dataSnapshot.child("courierID").getValue().toString();
                    courier = new CourierBuilder().withCourierID(courierID).build();
                    courier.setCourierID(courierID);

                    reference.child("couriers").child(courier.getCourierID()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()){
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

    private void moveMapToCourierLocalization(){
        courierLocalization = mapsService.getCourierLocalization(courier);
        courierMarker = mMap.addMarker(moveCourierMarker(courierLocalization,"Lokalizacja kuriera"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(courierLocalization));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(DEFUALT_ZOOM),3000,null);
    }

    private void changeCourierLocation(){
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                clientLocalization = null;

                if(location!=null&&isLooking){
                    clientLocalization = mapsService.getClientLocalization(location);
                }
                if(clientMarker!=null&&isLooking){
                    clientMarker.remove();
                }
                if(clientLocalization!=null&&courierMarker!=null&&isLooking){
                    courierMarker.remove();
                }
                if(clientLocalization!=null&&isLooking){
                    if(mMap!=null){
                        clientMarker = mMap.addMarker(moveMarker(clientLocalization,"Moja lokalizacja"));
                        firebaseService.saveClientLocation(packageNumber, clientLocalization.latitude, clientLocalization.longitude);
                    }
                }
                if(courierLocalization!=null&&isLooking){
                    if(mMap!=null){
                        courierMarker = mMap.addMarker(moveCourierMarker(courierLocalization,"Lokalizacja kuriera"));

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

    private void centerCameraBetweenCourierAndClient(){
        double latitudeDifference = Math.abs(courierLocalization.latitude+clientLocalization.latitude)/2;
        double longitudeDifference = Math.abs(courierLocalization.longitude+clientLocalization.longitude)/2;
        LatLng zoomPoint = new LatLng(latitudeDifference,longitudeDifference);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(zoomPoint, 12);
        mMap.animateCamera(cameraUpdate,3000,null);
    }

    private void centerCameraOnCourier(){
        LatLng zoomPoint = new LatLng(courierLocalization.latitude,courierLocalization.longitude);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(zoomPoint, 12);
        mMap.animateCamera(cameraUpdate,3000,null);
    }
}
