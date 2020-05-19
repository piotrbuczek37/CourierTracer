/* Copyright (C) Piotr Buczek - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Piotr Buczek <piotr.buczek37@gmail.com>, May 2020
 */

package com.ugprojects.couriertracerdpd.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.ugprojects.couriertracerdpd.R;
import com.ugprojects.couriertracerdpd.model.Courier;
import com.ugprojects.couriertracerdpd.model.CourierBuilder;
import com.ugprojects.couriertracerdpd.model.PackagesListAdapter;
import com.ugprojects.couriertracerdpd.model.Package;
import com.ugprojects.couriertracerdpd.service.DialogService;
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
    private Activity activity;

    private String courierID;
    private String phoneNumber;
    private String car;
    private String startTime;
    private String endTime;
    private int hhPin;

    private FirebaseService firebaseService;
    private DialogService dialogService;

    private Courier courier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_courier);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        activity = this;

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogService.buildAddPackageToListDialog(activity, packageList, adapter);
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
        dialogService = new DialogService(CourierActivity.this);

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

    /**
     * This method creates the menu with courier settings
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.courier_settings, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * This method is called when menu with courier settings is selected.
     * It creates window with phone number input and car information input where courier can update
     * his information. After clicking saving button, it saves the information to the Firebase
     *
     * @param item is selected menu item
     */
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

    /**
     * This method creates the package list and connects it with the adapter. Thanks to that, elements
     * of layout allows to make actions with the package list
     */
    private void initializePackageListAdapter() {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        packageList = new ArrayList<>();
        adapter = new PackagesListAdapter(packageList);
        recyclerView.setAdapter(adapter);
    }

    /**
     * This method gets some information from the previous activity which are needed to create
     * courier object
     */
    private void getExtras() {
        Bundle extras = getIntent().getExtras();
        courierID = extras.getString("courierID");
        hhPin = Integer.parseInt(extras.getString("hhPin"));
        startTime = extras.getString("startTime");
        endTime = extras.getString("endTime");
        car = extras.getString("car");
        phoneNumber = extras.getString("phoneNumber");
    }

    /**
     * Start Courier Maps Activity with list of packages to deliver
     *
     * @param packageAddresses are the addresses from package list
     * @param packageNumbers   are the package numbers from the package list
     */
    private void goToMapActivityWithPackagesInfo(ArrayList<String> packageAddresses, ArrayList<String> packageNumbers) {
        Intent intent = new Intent(getApplicationContext(), CourierMapsActivity.class);
        intent.putExtra("courierID", courier.getCourierID());
        intent.putStringArrayListExtra("packageAddresses", packageAddresses);
        intent.putStringArrayListExtra("packageNumbers", packageNumbers);
        startActivity(intent);
    }

    /**
     * Converts every package from package list to list of package addresses and list of package numbers
     *
     * @param packageList      is the list of packages
     * @param packageAddresses is the list of package addresses
     * @param packageNumbers   is the list of package numbers
     */
    private void preparePackagesInfoFromPackagesList(List<Package> packageList, ArrayList<String> packageAddresses, ArrayList<String> packageNumbers) {
        for (Package aPackage : packageList) {
            firebaseService.changeCourierOfPackage(aPackage, courier);
            String packageAddress = aPackage.getAddress() + ", " + aPackage.getPostCode();
            String packageNumber = aPackage.getPackageNumber();
            packageAddresses.add(packageAddress);
            packageNumbers.add(packageNumber);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult Result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (Result != null) {
            if (Result.getContents() == null) {
                Toast.makeText(this, "Anulowano", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Zeskanowano -> " + Result.getContents(), Toast.LENGTH_SHORT).show();
                firebaseService.addPackageToTheListAndGetDataFromDatabase(Result.getContents().toUpperCase(), packageList, adapter);
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
