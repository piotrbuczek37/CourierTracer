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
    Double courierLatitude;
    Double courierLongitude;
    String courierID;
    LatLng courierLocalization;
    LatLng clientLocalization;
    boolean isClicked = false;
    String courierName;
    String courierEndTime;
    String courierPhoneNumber;
    String courierCar;
    boolean isLooking;
    boolean isTicking;
    CountDownTimer countDownTimer;

    Geocoder geocoder;
    List<Address> addresses;
    String address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

        isLooking = true;
        isTicking = true;
        geocoder = new Geocoder(this, Locale.forLanguageTag("pl"));

        reference = FirebaseDatabase.getInstance().getReference();
        Bundle extras = getIntent().getExtras();
        packageNumber = extras.getString("packageNumber");
        reference.child("packages").child(packageNumber.toUpperCase().trim()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    courierID = dataSnapshot.child("courierID").getValue().toString();
                    reference.child("couriers").child(courierID.toUpperCase().trim()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()){
                                courierLatitude = Double.parseDouble(dataSnapshot.child("latitude").getValue().toString());
                                courierLongitude = Double.parseDouble(dataSnapshot.child("longitude").getValue().toString());
                                courierLocalization = new LatLng(courierLatitude,courierLongitude);
                                courierMarker = mMap.addMarker(moveCourierMarker(courierLocalization,"Lokalizacja kuriera"));
                                mMap.moveCamera(CameraUpdateFactory.newLatLng(courierLocalization));
                                mMap.animateCamera(CameraUpdateFactory.zoomTo(DEFUALT_ZOOM),3000,null);

                                reference.child("couriers").child(courierID.toUpperCase().trim()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.exists()){
                                            courierName = dataSnapshot.child("firstName").getValue().toString();
                                            courierEndTime = dataSnapshot.child("endTime").getValue().toString();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                                try {
                                    addresses = geocoder.getFromLocation(courierLatitude, courierLongitude, 1);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                if(addresses!=null){
                                    address = addresses.get(0).getAddressLine(0);
                                }
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

        Toast.makeText(getApplicationContext(),"Trwa wykrywanie sygnału GPS...",Toast.LENGTH_LONG).show();

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                clientLocalization = null;

                if(location!=null&&isLooking){
                    clientLocalization = new LatLng(location.getLatitude(),location.getLongitude());
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
                        reference.child("packages").child(packageNumber.toUpperCase().trim()).child("clientLatitude").setValue(clientLocalization.latitude);
                        reference.child("packages").child(packageNumber.toUpperCase().trim()).child("clientLongitude").setValue(clientLocalization.longitude);
                    }
                }
                if(courierLocalization!=null&&isLooking){
                    if(mMap!=null){
                        courierMarker = mMap.addMarker(moveCourierMarker(courierLocalization,"Lokalizacja kuriera"));
                        reference.child("couriers").child(courierID.toUpperCase().trim()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(dataSnapshot.exists()&&!isClicked){
                                    courierLatitude = Double.parseDouble(dataSnapshot.child("latitude").getValue().toString());
                                    courierLongitude = Double.parseDouble(dataSnapshot.child("longitude").getValue().toString());
                                    courierName = dataSnapshot.child("firstName").getValue().toString();
                                    courierEndTime = dataSnapshot.child("endTime").getValue().toString();
                                    courierLocalization = new LatLng(courierLatitude,courierLongitude);
                                    courierPhoneNumber = dataSnapshot.child("phoneNumber").getValue().toString();
                                    if(dataSnapshot.child("car").getValue().toString().equals("")){
                                        courierCar = "Brak opisu";
                                    } else {
                                        courierCar = dataSnapshot.child("car").getValue().toString();
                                    }

                                    try {
                                        addresses = geocoder.getFromLocation(courierLatitude, courierLongitude, 1);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    if(addresses!=null){
                                        address = addresses.get(0).getAddressLine(0);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
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
        LatLng client = new LatLng(54.350866, 18.645663);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(client));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(DEFUALT_ZOOM));
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);

        mMap.setOnCameraIdleListener(this);
//        mMap.setOnCameraMoveStartedListener(this);
        mMap.setOnCameraMoveListener(this);
//        mMap.setOnCameraMoveCanceledListener(this);

        mMap.setOnMarkerClickListener(this);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
            if(marker.equals(courierMarker)){
                isClicked = true;
                new MaterialStyledDialog.Builder(ClientMapsActivity.this)
                        .setTitle("Kurier "+courierName)
                        .setDescription("Aktualna pozycja kuriera: "+ address +"\n" + "Kończy pracę o godz. " +courierEndTime + "\n" + "Numer telefonu: "+courierPhoneNumber+"\n"+"Opis samochodu: " + courierCar)
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
                    double latitudeDifference;
                    double longitudeDifference;
                    if(courierLocalization!=null&&clientLocalization!=null&&!isClicked){
                        latitudeDifference = Math.abs(courierLocalization.latitude+clientLocalization.latitude)/2;
                        longitudeDifference = Math.abs(courierLocalization.longitude+clientLocalization.longitude)/2;
                        LatLng zoomPoint = new LatLng(latitudeDifference,longitudeDifference);
                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(zoomPoint, 12);
                        mMap.animateCamera(cameraUpdate,3000,null);
                    } else if (courierLocalization!=null&&!isClicked){
                        LatLng zoomPoint = new LatLng(courierLocalization.latitude,courierLocalization.longitude);
                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(zoomPoint, 12);
                        mMap.animateCamera(cameraUpdate,3000,null);
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
}
