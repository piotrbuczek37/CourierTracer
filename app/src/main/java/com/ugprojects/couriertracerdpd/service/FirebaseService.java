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

    public void saveCourierSettings(String courierID, String phoneNumber, String carInfo) {
        reference.child("couriers").child(courierID).child("phoneNumber").setValue(phoneNumber);
        reference.child("couriers").child(courierID).child("car").setValue(carInfo);
    }

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

    public void addPackageToTheListAndUpdateDatabase(final String packageNumber, final List<Package> packageList, final RecyclerView.Adapter adapter) {
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

    public void changeCourierOfPackage(Package pack, Courier courier) {
        reference.child("packages").child(pack.getPackageNumber()).child("courierID").setValue(courier.getCourierID());
    }

    public void saveCourierLocation(String courierID, double latitude, double longitude) {
        reference.child("couriers").child(courierID).child("latitude").setValue(latitude);
        reference.child("couriers").child(courierID).child("longitude").setValue(longitude);
    }

    public void saveClientLocation(String packageNumber, double latitude, double longitude) {
        reference.child("packages").child(packageNumber.toUpperCase().trim()).child("clientLatitude").setValue(latitude);
        reference.child("packages").child(packageNumber.toUpperCase().trim()).child("clientLongitude").setValue(longitude);
    }

    public void saveCourierWorkTimes(String courierID, String startTime, String endTime) {
        reference.child("couriers").child(courierID).child("startTime").setValue(startTime);
        reference.child("couriers").child(courierID).child("endTime").setValue(endTime);
    }

    public void saveCourierCarInfo(String courierID, String car) {
        reference.child("couriers").child(courierID).child("car").setValue(car);
    }

    public void removeCourierIDFromPackage(String packageNumber) {
        reference.child("packages").child(packageNumber).child("courierID").setValue("none");
    }
}
