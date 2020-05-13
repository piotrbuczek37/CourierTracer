package com.ugprojects.couriertracerdpd.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.ugprojects.couriertracerdpd.R;
import com.ugprojects.couriertracerdpd.service.FirebaseService;

public class HomeFragment extends Fragment {

    private FirebaseService firebaseService;

    public View onCreateView(@NonNull final LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel = ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        final TextView textView = root.findViewById(R.id.text_home);
        homeViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        firebaseService = new FirebaseService(inflater, getContext(), HomeFragment.this);

        final EditText clientPackageNumberEditText = root.findViewById(R.id.clientPackageNumberEditText);
        Button searchForPackageButton = root.findViewById(R.id.searchForPackageButton);

        searchForPackageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseService.checkPackageNumber(clientPackageNumberEditText.getText().toString());
            }
        });

        return root;
    }
}