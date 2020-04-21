package com.ugprojects.couriertracerdpd;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ugprojects.couriertracerdpd.layoutElements.PackagesListAdapter;
import com.ugprojects.couriertracerdpd.model.Courier;
import com.ugprojects.couriertracerdpd.model.Package;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
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

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;

    private List<Package> packageList;
    DatabaseReference ref;
    String courierID;
    String phoneNumber;
    String car;
    String phoneNumberFromDb;
    String carFromDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_courier);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ref = FirebaseDatabase.getInstance().getReference();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View dialogView = LayoutInflater.from(CourierActivity.this).inflate(R.layout.add_package_layout,null);
                final EditText packageNumberEditText = dialogView.findViewById(R.id.packageNumberEditText);
                new AlertDialog.Builder(CourierActivity.this)
                        .setIcon(android.R.drawable.ic_input_add)
                        .setTitle("Dodaj paczkę")
                        .setView(dialogView)
                        .setPositiveButton("Dodaj", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final String packageNumber = packageNumberEditText.getText().toString().toUpperCase().trim();
                                ref.child("packages").child(packageNumber).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.exists()){
                                            String packageAddress = dataSnapshot.child("address").getValue().toString();
                                            String postCode = dataSnapshot.child("postCode").getValue().toString();
                                            Package pack = new Package(packageNumber,packageAddress,postCode);
                                            if(!packageList.contains(pack)){
                                                packageList.add(pack);
                                                adapter.notifyDataSetChanged();
                                            }
                                            else{
                                                Toast.makeText(CourierActivity.this,"Taka paczka już jest na liście",Toast.LENGTH_LONG).show();
                                            }
                                        }
                                        else {
                                            Toast.makeText(CourierActivity.this,"Taka paczka nie istnieje w bazie",Toast.LENGTH_LONG).show();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
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
        });

        recyclerView = findViewById(R.id.recyclerView);
        Button goToMapButton = findViewById(R.id.goToMapButton);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        packageList = new ArrayList<>();
        Bundle extras = getIntent().getExtras();
        courierID = extras.getString("courierID");
        String hhPin  =extras.getString("hhPin");
        int hhPinInt = Integer.parseInt(hhPin);
        String startTime = extras.getString("startTime");
        String endTime = extras.getString("endTime");
        car = extras.getString("car");
        phoneNumber = extras.getString("phoneNumber");

        ref.child("couriers").child(courierID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    phoneNumberFromDb = dataSnapshot.child("phoneNumber").getValue().toString();
                    carFromDb = dataSnapshot.child("car").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        Courier courier = new Courier(courierID,startTime,endTime,car,hhPinInt);
//        Package pack = new Package("00005126125U","Piotrkowsa 10");
//        packageList.add(pack);

        adapter = new PackagesListAdapter(packageList,this);
        recyclerView.setAdapter(adapter);

        goToMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> packageAddresses = new ArrayList<>();
                ArrayList<String> packageNumbers = new ArrayList<>();
                for (Package aPackage : packageList) {
                    ref.child("packages").child(aPackage.getPackageNumber()).child("courierID").setValue(courierID);
                    String packageAddress = aPackage.getAddress() + ", " + aPackage.getPostCode();
                    String packageNumber = aPackage.getPackageNumber();
                    packageAddresses.add(packageAddress);
                    packageNumbers.add(packageNumber);
                }
                Toast.makeText(getApplicationContext(),"Dane zostały zaktualizowane",Toast.LENGTH_LONG).show();

                Intent intent = new Intent(getApplicationContext(),CourierMapsActivity.class);
                intent.putExtra("courierID",courierID);
                intent.putStringArrayListExtra("packageAddresses",packageAddresses);
                intent.putStringArrayListExtra("packageNumbers",packageNumbers);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.courier_settings,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item){
        super.onOptionsItemSelected(item);
        if(item.getItemId()==R.id.courierSettings){
            View dialogView = getLayoutInflater().inflate(R.layout.courier_settings_layout,null);
            dialogView.setPadding(60,30,60,30);
            final EditText phoneNumberEditText = dialogView.findViewById(R.id.courierPhoneNumber);
            phoneNumberEditText.setText(phoneNumberFromDb);
            final EditText carInfoEditText = dialogView.findViewById(R.id.carInfoEditText);
            carInfoEditText.setText(carFromDb);
            new AlertDialog.Builder(this)
                    .setTitle("Ustawienia")
                    .setView(dialogView)
                    .setPositiveButton("Zapisz", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ref.child("couriers").child(courierID).child("phoneNumber").setValue(phoneNumberEditText.getText().toString());
                            ref.child("couriers").child(courierID).child("car").setValue(carInfoEditText.getText().toString());

                            ref.child("couriers").child(courierID).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.exists()){
                                        phoneNumberFromDb = dataSnapshot.child("phoneNumber").getValue().toString();
                                        carFromDb = dataSnapshot.child("car").getValue().toString();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
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
}