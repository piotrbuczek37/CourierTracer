/* Copyright (C) Piotr Buczek - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Piotr Buczek <piotr.buczek37@gmail.com>, May 2020
 */

package com.ugprojects.couriertracerdpd.service;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ugprojects.couriertracerdpd.activity.ClientMapsActivity;
import com.ugprojects.couriertracerdpd.model.Courier;
import com.ugprojects.couriertracerdpd.model.Package;

import java.util.List;

public class FirebaseService {
    private DatabaseReference reference;
    private Context context;
    private Fragment fragment;
    private DialogService dialogService;

    public FirebaseService() {
        reference = FirebaseDatabase.getInstance().getReference();
    }

    public FirebaseService(Context context) {
        reference = FirebaseDatabase.getInstance().getReference();
        this.context = context;
    }

    public FirebaseService(Context context, Fragment fragment) {
        reference = FirebaseDatabase.getInstance().getReference();
        this.context = context;
        this.fragment = fragment;
    }

    public FirebaseService(Context context, Fragment fragment, DialogService dialogService) {
        reference = FirebaseDatabase.getInstance().getReference();
        this.context = context;
        this.fragment = fragment;
        this.dialogService = dialogService;
    }

    /**
     * This method saves to database couriers info (phone number, car info)
     *
     * @param courierID   is the courier ID
     * @param phoneNumber is the phone number of courier
     * @param carInfo     is the car info of courier
     */
    public void saveCourierSettings(String courierID, String phoneNumber, String carInfo) {
        reference.child("couriers").child(courierID).child("phoneNumber").setValue(phoneNumber);
        reference.child("couriers").child(courierID).child("car").setValue(carInfo);
    }

    /**
     * This method gets the phone number of courier from database
     *
     * @param courier is the courier object with courier ID
     * @return phone number from database
     */
    public String getCourierPhoneNumber(final Courier courier) {
        reference.child("couriers").child(courier.getCourierID()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    courier.setPhoneNumber(dataSnapshot.child("phoneNumber").getValue().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return courier.getPhoneNumber();
    }

    /**
     * This method gets the car info of courier from database
     *
     * @param courier is the courier object with courier ID
     * @return text "Brak opisu" if there is no info or car info from database
     */
    public String getCourierCarInfo(final Courier courier) {
        reference.child("couriers").child(courier.getCourierID()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.child("car").getValue().toString().equals("")) {
                        courier.setCarInfo("Brak opisu");
                    } else {
                        courier.setCarInfo(dataSnapshot.child("car").getValue().toString());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return courier.getCarInfo();
    }

    /**
     * This method gets the first name of courier from database
     *
     * @param courier is the courier object with courier ID
     * @return first name from database
     */
    public String getCourierFirstName(final Courier courier) {
        reference.child("couriers").child(courier.getCourierID()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    courier.setFirstName(dataSnapshot.child("firstName").getValue().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return courier.getFirstName();
    }

    /**
     * This method gets the end work time of courier from database
     *
     * @param courier is the courier object with courier ID
     * @return end work time from database
     */
    public String getCourierEndTime(final Courier courier) {
        reference.child("couriers").child(courier.getCourierID()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    courier.setEndTime(dataSnapshot.child("endTime").getValue().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return courier.getEndTime();
    }

    /**
     * This method gets the latitude of courier coordinates from database
     *
     * @param courier is the courier object with courier ID
     * @return latitude from database
     */
    public double getCourierLatitude(final Courier courier) {
        reference.child("couriers").child(courier.getCourierID()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    courier.setLatitude(Double.parseDouble(dataSnapshot.child("latitude").getValue().toString()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return courier.getLatitude();
    }

    /**
     * This method gets the longitude of courier coordinates from database
     *
     * @param courier is the courier object with courier ID
     * @return longitude from database
     */
    public double getCourierLongitude(final Courier courier) {
        reference.child("couriers").child(courier.getCourierID()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    courier.setLongitude(Double.parseDouble(dataSnapshot.child("longitude").getValue().toString()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return courier.getLongitude();
    }

    /**
     * This method checks if entered package number is correct. If it is correct then calls
     * the dialog service to start package code dialog. If the package number is correct but none
     * cf the couriers have a package then it shows appropriate notification. If the package number
     * is not correct then it shows appropriate notification.
     *
     * @param number is the package number to check
     */
    public void checkPackageNumber(String number) {
        final String packageNumber = number;
        reference.child("packages").child(packageNumber.toUpperCase().trim()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.child("courierID").getValue().equals("none")) {
                        Toast.makeText(context, "W tej chwili przesyłki nie ma żaden kurier", Toast.LENGTH_LONG).show();
                    } else {
                        dialogService.buildPackageCodeDialog(packageNumber);
                    }
                } else {
                    Toast.makeText(context, "Numer przesyłki jest nieprawidłowy lub nie istnieje w bazie", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /**
     * This method checks if the entered package code is the correct package code. If it's correct
     * then it starts the map activity. If it is not correct then it shows
     * appropriate notification.
     *
     * @param code          is the entered package code
     * @param packageNumber is the entered package number
     */
    public void checkPackageCodeAndStartMapActivity(final String code, final String packageNumber) {
        reference.child("packages").child(packageNumber.toUpperCase().trim()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("pinCode").getValue().equals(code)) {
                    Toast.makeText(context, "Śledzenie rozpoczęte", Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(context, ClientMapsActivity.class);
                    intent.putExtra("packageNumber", packageNumber);

                    fragment.startActivity(intent);
                } else {
                    Toast.makeText(context, "Niepoprawny kod odbioru!", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /**
     * This method checks if couriers credentials are correct. If correct then it calls Dialog Service
     * to create a dialog to enter additional work info. If it's not correct then it shows appropriate
     * notification
     *
     * @param login is the entered login
     * @param pin   is the entered pin (as a password)
     */
    public void checkCourierCredentials(final String login, final String pin) {
        reference.child("couriers").child(login.toUpperCase().trim()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.child("hhPin").getValue().toString().equals(pin)) {
                        String firstName = dataSnapshot.child("firstName").getValue().toString();
                        String lastName = dataSnapshot.child("lastName").getValue().toString();
                        String phoneNumber = dataSnapshot.child("phoneNumber").getValue().toString();
                        dialogService.buildDialogToUpdateCourierWorkInfo(login, pin, phoneNumber, firstName, lastName);
                    } else {
                        Toast.makeText(context, "Niepoprawny identyfikator lub PIN!", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(context, "Niepoprawny identyfikator lub PIN!", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /**
     * This method the info of a package from database and then adds the package to the list
     *
     * @param packageNumber is the package number
     * @param packageList   is the package list where the package will be added
     * @param adapter       is the adapter which converts the package objects for the list elements
     */
    public void addPackageToTheListAndGetDataFromDatabase(final String packageNumber, final List<Package> packageList, final RecyclerView.Adapter adapter) {
        reference.child("packages").child(packageNumber).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String packageAddress = dataSnapshot.child("address").getValue().toString();
                    String postCode = dataSnapshot.child("postCode").getValue().toString();
                    Package pack = new Package(packageNumber, packageAddress, postCode);
                    if (!packageList.contains(pack)) {
                        packageList.add(pack);
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(context, "Taka paczka już jest na liście", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(context, "Taka paczka nie istnieje w bazie", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /**
     * This method updates the database with the info about which courier has a package
     *
     * @param pack    is the package object
     * @param courier is the courier object with courier ID
     */
    public void changeCourierOfPackage(Package pack, Courier courier) {
        reference.child("packages").child(pack.getPackageNumber()).child("courierID").setValue(courier.getCourierID());
    }

    /**
     * This method updates the database with the courier localization
     *
     * @param courierID is the courier ID
     * @param latitude  is the latitude from coordinates
     * @param longitude is the longitude from coordinates
     */
    public void saveCourierLocation(String courierID, double latitude, double longitude) {
        reference.child("couriers").child(courierID).child("latitude").setValue(latitude);
        reference.child("couriers").child(courierID).child("longitude").setValue(longitude);
    }

    /**
     * This method updates the database with the client localization
     *
     * @param packageNumber is the clients package number
     * @param latitude      is the latitude from coordinates
     * @param longitude     is the longitude from coordinates
     */
    public void saveClientLocation(String packageNumber, double latitude, double longitude) {
        reference.child("packages").child(packageNumber.toUpperCase().trim()).child("clientLatitude").setValue(latitude);
        reference.child("packages").child(packageNumber.toUpperCase().trim()).child("clientLongitude").setValue(longitude);
    }

    /**
     * This method updates the database with couriers work time information
     *
     * @param courierID is the courier ID
     * @param startTime is the start work time of courier
     * @param endTime   is the end work time of courier
     */
    public void saveCourierWorkTimes(String courierID, String startTime, String endTime) {
        reference.child("couriers").child(courierID).child("startTime").setValue(startTime);
        reference.child("couriers").child(courierID).child("endTime").setValue(endTime);
    }

    /**
     * This method saves the couriers car information to the database
     *
     * @param courierID is the courier ID
     * @param car       is the couriers car information
     */
    public void saveCourierCarInfo(String courierID, String car) {
        reference.child("couriers").child(courierID).child("car").setValue(car);
    }

    /**
     * This method sets the value of packages courier ID to none (removes the courier ID from package)
     *
     * @param packageNumber is the package number
     */
    public void removeCourierIDFromPackage(String packageNumber) {
        reference.child("packages").child(packageNumber).child("courierID").setValue("none");
    }
}
