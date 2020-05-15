package com.ugprojects.couriertracerdpd.service;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.github.javiersantos.materialstyleddialogs.enums.Style;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.ugprojects.couriertracerdpd.R;
import com.ugprojects.couriertracerdpd.activity.ClientMapsActivity;
import com.ugprojects.couriertracerdpd.activity.CourierActivity;
import com.ugprojects.couriertracerdpd.model.Courier;
import com.ugprojects.couriertracerdpd.model.Package;

import java.util.List;

public class FirebaseService {
    private DatabaseReference reference;
    private LayoutInflater inflater;
    private Context context;
    private Fragment fragment;

    public FirebaseService() {
        reference = FirebaseDatabase.getInstance().getReference();
    }

    public FirebaseService(Context context) {
        reference = FirebaseDatabase.getInstance().getReference();
        this.context = context;
    }

    public FirebaseService(LayoutInflater inflater, Context context, Fragment fragment) {
        reference = FirebaseDatabase.getInstance().getReference();
        this.inflater = inflater;
        this.context = context;
        this.fragment = fragment;
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
                        View dialogView = inflater.inflate(R.layout.client_code_layout, null);
                        final EditText clientPackageCodeEditText = dialogView.findViewById(R.id.clientPackageCodeEditText);
                        new MaterialStyledDialog.Builder(context)
                                .setTitle("Wprowadź kod odbioru")
                                .setDescription("Kod odbioru znajduje się w wiadomości SMS")
                                .setStyle(Style.HEADER_WITH_TITLE)
                                .setCustomView(dialogView)
                                .setPositiveText("OK")
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        String code = clientPackageCodeEditText.getText().toString().toUpperCase().trim();
                                        if (dataSnapshot.child("pinCode").getValue().equals(code)) {
                                            Toast.makeText(context, "Śledzenie rozpoczęte", Toast.LENGTH_LONG).show();

                                            Intent intent = new Intent(context, ClientMapsActivity.class);
                                            intent.putExtra("packageNumber", packageNumber);

                                            fragment.startActivity(intent);
                                        } else {
                                            Toast.makeText(context, "Niepoprawny kod odbioru!", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                })
                                .setNegativeText("Anuluj")
                                .onNegative(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        dialog.dismiss();
                                    }
                                })
                                .show();
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

    public void checkCourierCredentials(final String login, final String pin) {
        reference.child("couriers").child(login.toUpperCase().trim()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.child("hhPin").getValue().toString().equals(pin)) {
                        final String phoneNumber = dataSnapshot.child("phoneNumber").getValue().toString();
                        View dialogView = inflater.inflate(R.layout.start_day_layout, null);
                        final TimePicker picker = dialogView.findViewById(R.id.dateStartPicker);
                        picker.setIs24HourView(true);
                        final TimePicker picker2 = dialogView.findViewById(R.id.dateEndPicker);
                        picker2.setIs24HourView(true);
                        final EditText carEditText = dialogView.findViewById(R.id.carEditText);
                        new MaterialStyledDialog.Builder(context)
                                .setTitle("Zalogowano jako: " + dataSnapshot.child("firstName").getValue() + " " + dataSnapshot.child("lastName").getValue())
                                .setDescription("Teraz możesz wpisać szczegóły dotyczące twojego dzisiejszego dnia pracy:")
                                .setStyle(Style.HEADER_WITH_TITLE)
                                .setScrollable(true)
                                .setCustomView(dialogView)
                                .setPositiveText("Zapisz i przejdź dalej")
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        String startTime = String.format("%02d", picker.getHour()) + ":" + String.format("%02d", picker.getMinute());
                                        String endTime = String.format("%02d", picker2.getHour()) + ":" + String.format("%02d", picker2.getMinute());
                                        String car = carEditText.getText().toString();
                                        reference.child("couriers").child(login.toUpperCase().trim()).child("startTime").setValue(startTime);
                                        reference.child("couriers").child(login.toUpperCase().trim()).child("endTime").setValue(endTime);
                                        reference.child("couriers").child(login.toUpperCase().trim()).child("car").setValue(car);
                                        Toast.makeText(context, "Dane zostały zapisane", Toast.LENGTH_LONG).show();
                                        Intent intent = new Intent(context, CourierActivity.class);
                                        intent.putExtra("courierID", login.toUpperCase().trim());
                                        intent.putExtra("hhPin", pin);
                                        intent.putExtra("startTime", startTime);
                                        intent.putExtra("endTime", endTime);
                                        intent.putExtra("car", car);
                                        intent.putExtra("phoneNumber", phoneNumber);
                                        fragment.startActivity(intent);
                                    }
                                })
                                .setNegativeText("Anuluj i wyloguj")
                                .onNegative(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        dialog.dismiss();
                                    }
                                })
                                .show();
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

    public void checkAndAddPackageToList(final List<Package> packageList, final RecyclerView.Adapter adapter, final Activity activity) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.add_package_layout, null);
        final EditText packageNumberEditText = dialogView.findViewById(R.id.packageNumberEditText);
        final Button scannerButton;
        scannerButton = dialogView.findViewById(R.id.scannerButton);
        scannerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentIntegrator intentIntegrator = new IntentIntegrator(activity);
                intentIntegrator.setDesiredBarcodeFormats(intentIntegrator.ALL_CODE_TYPES);
                intentIntegrator.setBeepEnabled(true);
                intentIntegrator.setCameraId(0);
                intentIntegrator.setOrientationLocked(false);
                intentIntegrator.setPrompt("SCAN");
                intentIntegrator.setBarcodeImageEnabled(false);
                intentIntegrator.initiateScan();
            }
        });

        new AlertDialog.Builder(context)
                .setIcon(android.R.drawable.ic_input_add)
                .setTitle("Dodaj paczkę")
                .setView(dialogView)
                .setPositiveButton("Dodaj", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final String packageNumber = packageNumberEditText.getText().toString().toUpperCase().trim();
                        addPackageToTheListAndUpdateDatabase(packageNumber,packageList,adapter);
                    }
                })
                .setNegativeButton("Anuluj", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    public void addPackageToTheListAndUpdateDatabase(final String packageNumber, final List<Package> packageList, final RecyclerView.Adapter adapter){
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

    public void removeCourierIDFromPackage(String packageNumber) {
        reference.child("packages").child(packageNumber).child("courierID").setValue("none");
    }
}
