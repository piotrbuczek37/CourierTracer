package com.ugprojects.couriertracerdpd.ui.courier;

import android.os.Bundle;
import android.text.InputFilter;
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
 * This Fragment uses CourierViewModel to display text on the screen. It also creates inputs to
 * allow courier to enter courier ID and PIN to HH to login and then it's calling Firebase database
 * to check credentials
 */
public class CourierFragment extends Fragment {

    public View onCreateView(@NonNull final LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        CourierViewModel courierViewModel = ViewModelProviders.of(this).get(CourierViewModel.class);
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
        courierPinEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});

        final DialogService dialogService = new DialogService(inflater, getContext(), CourierFragment.this);
        final FirebaseService firebaseService = new FirebaseService(getContext(), CourierFragment.this, dialogService);

        logInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String login = courierIdEditText.getText().toString();
                final String pin = courierPinEditText.getText().toString();
                firebaseService.checkCourierCredentials(login, pin);
            }
        });
        return root;
    }
}