package com.ugprojects.couriertracerdpd.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.ugprojects.couriertracerdpd.R;
import com.ugprojects.couriertracerdpd.model.Courier;
import com.ugprojects.couriertracerdpd.model.CourierBuilder;
import com.ugprojects.couriertracerdpd.model.PackagesListAdapter;
import com.ugprojects.couriertracerdpd.model.Package;
import com.ugprojects.couriertracerdpd.service.FirebaseService;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class CourierActivity extends AppCompatActivity {

    private RecyclerView.Adapter adapter;
    private List<Package> packageList;

    private String courierID;
    private String phoneNumber;
    private String car;
    private String startTime;
    private String endTime;
    private int hhPin;

    private FirebaseService firebaseService;

    private Courier courier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_courier);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firebaseService.checkAndAddPackageToList(packageList, adapter);
            }
        });
        Button goToMapButton = findViewById(R.id.goToMapButton);

        initializePackageListAdapter();
        getExtras();

        courier = new CourierBuilder()
                .withCourierID(courierID)
                .withHhPin(hhPin)
                .withStartTime(startTime)
                .withEndTime(endTime)
                .withCarInfo(car)
                .withPhoneNumber(phoneNumber)
                .build();

        firebaseService = new FirebaseService(CourierActivity.this);

        firebaseService.getCourierPhoneNumber(courier);
        firebaseService.getCourierCarInfo(courier);

        goToMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> packageAddresses = new ArrayList<>();
                ArrayList<String> packageNumbers = new ArrayList<>();
                preparePackagesInfoFromPackagesList(packageList, packageAddresses, packageNumbers);

                Toast.makeText(getApplicationContext(), "Dane zostały zaktualizowane", Toast.LENGTH_LONG).show();

                goToMapActivityWithPackagesInfo(packageAddresses, packageNumbers);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.courier_settings, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == R.id.courierSettings) {
            View dialogView = getLayoutInflater().inflate(R.layout.courier_settings_layout, null);
            dialogView.setPadding(60, 30, 60, 30);
            final EditText phoneNumberEditText = dialogView.findViewById(R.id.courierPhoneNumber);
            phoneNumberEditText.setText(courier.getPhoneNumber());
            final EditText carInfoEditText = dialogView.findViewById(R.id.carInfoEditText);
            carInfoEditText.setText(courier.getCarInfo());
            new AlertDialog.Builder(this)
                    .setTitle("Ustawienia")
                    .setView(dialogView)
                    .setPositiveButton("Zapisz", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            firebaseService.saveCourierSettings(courier.getCourierID(), phoneNumberEditText.getText().toString(), carInfoEditText.getText().toString());
                            courier.setPhoneNumber(firebaseService.getCourierPhoneNumber(courier));
                            courier.setCarInfo(firebaseService.getCourierCarInfo(courier));
                        }
                    })
                    .setNegativeButton("Anuluj", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();
            return true;
        }
        return false;
    }

    private void initializePackageListAdapter() {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        packageList = new ArrayList<>();
        adapter = new PackagesListAdapter(packageList);
        recyclerView.setAdapter(adapter);
    }

    private void getExtras() {
        Bundle extras = getIntent().getExtras();
        courierID = extras.getString("courierID");
        hhPin = Integer.parseInt(extras.getString("hhPin"));
        startTime = extras.getString("startTime");
        endTime = extras.getString("endTime");
        car = extras.getString("car");
        phoneNumber = extras.getString("phoneNumber");
    }

    private void goToMapActivityWithPackagesInfo(ArrayList<String> packageAddresses, ArrayList<String> packageNumbers) {
        Intent intent = new Intent(getApplicationContext(), CourierMapsActivity.class);
        intent.putExtra("courierID", courier.getCourierID());
        intent.putStringArrayListExtra("packageAddresses", packageAddresses);
        intent.putStringArrayListExtra("packageNumbers", packageNumbers);
        startActivity(intent);
    }

    private void preparePackagesInfoFromPackagesList(List<Package> packageList, ArrayList<String> packageAddresses, ArrayList<String> packageNumbers) {
        for (Package aPackage : packageList) {
            firebaseService.changeCourierOfPackage(aPackage, courier);
            String packageAddress = aPackage.getAddress() + ", " + aPackage.getPostCode();
            String packageNumber = aPackage.getPackageNumber();
            packageAddresses.add(packageAddress);
            packageNumbers.add(packageNumber);
        }
    }
}
