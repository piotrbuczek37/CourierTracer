package com.ugprojects.couriertracerdpd.ui.courierSection;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.github.javiersantos.materialstyleddialogs.enums.Style;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ugprojects.couriertracerdpd.CourierActivity;
import com.ugprojects.couriertracerdpd.MainActivity;
import com.ugprojects.couriertracerdpd.R;

public class CourierFragment extends Fragment {

    private CourierViewModel courierViewModel;

    public View onCreateView(@NonNull final LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        courierViewModel =
                ViewModelProviders.of(this).get(CourierViewModel.class);
        final View root = inflater.inflate(R.layout.fragment_courier, container, false);
        final TextView textView = root.findViewById(R.id.text_courier);
        courierViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        Button logInButton = root.findViewById(R.id.logInButton);
        final EditText courierIdEditText = root.findViewById(R.id.courierIdEditText);
        final EditText courierPinEditText = root.findViewById(R.id.courierPinEditText);
        courierPinEditText.setFilters(new InputFilter[] {new InputFilter.LengthFilter(4)});
        logInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String login = courierIdEditText.getText().toString();
                final String pin = courierPinEditText.getText().toString();
                final DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                ref.child("couriers").child(login.toUpperCase().trim()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            if(dataSnapshot.child("hhPin").getValue().toString().equals(pin)){
                                final String phoneNumber = dataSnapshot.child("phoneNumber").getValue().toString();
                                View dialogView = inflater.inflate(R.layout.start_day_layout,null);
                                final TimePicker picker= dialogView.findViewById(R.id.dateStartPicker);
                                picker.setIs24HourView(true);
                                final TimePicker picker2= dialogView.findViewById(R.id.dateEndPicker);
                                picker2.setIs24HourView(true);
                                final EditText carEditText = dialogView.findViewById(R.id.carEditText);
                                new MaterialStyledDialog.Builder(getContext())
                                        .setTitle("Zalogowano jako: "+dataSnapshot.child("firstName").getValue()+" "+dataSnapshot.child("lastName").getValue())
                                        .setDescription("Teraz możesz wpisać szczegóły dotyczące twojego dzisiejszego dnia pracy:")
                                        .setStyle(Style.HEADER_WITH_TITLE)
                                        .setScrollable(true)
                                        .setCustomView(dialogView)
                                        .setPositiveText("Zapisz i przejdź dalej")
                                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                String startTime = String.format("%02d",picker.getHour())+":"+String.format("%02d",picker.getMinute());
                                                String endTime = String.format("%02d",picker2.getHour())+":"+String.format("%02d",picker2.getMinute());
                                                String car = carEditText.getText().toString();
                                                ref.child("couriers").child(login.toUpperCase().trim()).child("startTime").setValue(startTime);
                                                ref.child("couriers").child(login.toUpperCase().trim()).child("endTime").setValue(endTime);
                                                ref.child("couriers").child(login.toUpperCase().trim()).child("car").setValue(car);
                                                Toast.makeText(getContext(),"Dane zostały zapisane",Toast.LENGTH_LONG).show();
                                                Intent intent = new Intent(getContext(), CourierActivity.class);
                                                intent.putExtra("courierID",login.toUpperCase().trim());
                                                intent.putExtra("hhPin",pin);
                                                intent.putExtra("startTime",startTime);
                                                intent.putExtra("endTime",endTime);
                                                intent.putExtra("car",car);
                                                intent.putExtra("phoneNumber",phoneNumber);
                                                startActivity(intent);
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
                            }
                            else {
                                Toast.makeText(root.getContext(),"Niepoprawny identyfikator lub PIN!",Toast.LENGTH_LONG).show();
                            }
                        }
                        else{
                            Toast.makeText(root.getContext(),"Niepoprawny identyfikator lub PIN!",Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
        return root;
    }
}