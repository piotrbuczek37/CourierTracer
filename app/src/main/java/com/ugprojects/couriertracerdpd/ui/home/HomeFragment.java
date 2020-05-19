/* Copyright (C) Piotr Buczek - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Piotr Buczek <piotr.buczek37@gmail.com>, May 2020
 */

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
import com.ugprojects.couriertracerdpd.service.DialogService;
import com.ugprojects.couriertracerdpd.service.FirebaseService;

/**
 * This Fragment uses HomeViewModel to display text on the screen. It also creates inputs to
 * allow client to enter package number and then it's calling Firebase database to check credentials
 */
public class HomeFragment extends Fragment {

    private FirebaseService firebaseService;
    private DialogService dialogService;

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

        dialogService = new DialogService(inflater, getContext(), HomeFragment.this);
        firebaseService = new FirebaseService(getContext(), HomeFragment.this, dialogService);

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