package com.ugprojects.couriertracerdpd.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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
import com.ugprojects.couriertracerdpd.ClientMapsActivity;
import com.ugprojects.couriertracerdpd.R;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;

    public View onCreateView(@NonNull final LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        final TextView textView = root.findViewById(R.id.text_home);
        homeViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        final EditText clientPackageNumberEditText = root.findViewById(R.id.clientPackageNumberEditText);
        Button searchForPackageButton = root.findViewById(R.id.searchForPackageButton);
        searchForPackageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                final String packageNumber = clientPackageNumberEditText.getText().toString();
                reference.child("packages").child(packageNumber.toUpperCase().trim()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            if(dataSnapshot.child("courierID").getValue().equals("none")){
                                Toast.makeText(getContext(),"W tej chwili przesyłki nie ma żaden kurier",Toast.LENGTH_LONG).show();
                            } else {
                                View dialogView = inflater.inflate(R.layout.client_code_layout,null);
                                final EditText clientPackageCodeEditText = dialogView.findViewById(R.id.clientPackageCodeEditText);
                                new MaterialStyledDialog.Builder(getContext())
                                        .setTitle("Wprowadź kod odbioru")
                                        .setDescription("Kod odbioru znajduje się w wiadomości e-mail")
                                        .setStyle(Style.HEADER_WITH_TITLE)
                                        .setCustomView(dialogView)
                                        .setPositiveText("OK")
                                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                String code = clientPackageCodeEditText.getText().toString().toUpperCase().trim();
                                                if(dataSnapshot.child("pinCode").getValue().equals(code)){
                                                    Toast.makeText(getContext(),"Śledzenie rozpoczęte",Toast.LENGTH_LONG).show();

                                                    Intent intent = new Intent(getContext(), ClientMapsActivity.class);
                                                    intent.putExtra("packageNumber",packageNumber);

                                                    startActivity(intent);
                                                }
                                                else {
                                                    Toast.makeText(getContext(),"Niepoprawny kod odbioru!",Toast.LENGTH_LONG).show();
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
                            Toast.makeText(getContext(),"Numer przesyłki jest nieprawidłowy lub nie istnieje w bazie",Toast.LENGTH_LONG).show();
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