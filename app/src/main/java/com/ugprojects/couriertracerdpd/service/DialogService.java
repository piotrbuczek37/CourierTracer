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
import com.google.zxing.integration.android.IntentIntegrator;
import com.ugprojects.couriertracerdpd.R;
import com.ugprojects.couriertracerdpd.activity.CourierActivity;
import com.ugprojects.couriertracerdpd.model.Package;

import java.util.List;

public class DialogService {
    private LayoutInflater inflater;
    private Context context;
    private FirebaseService firebaseService;
    private Fragment fragment;

    public DialogService(Context context) {
        this.context = context;
        this.firebaseService = new FirebaseService(this.context);
    }

    public DialogService(LayoutInflater inflater, Context context, Fragment fragment) {
        this.inflater = inflater;
        this.context = context;
        this.fragment = fragment;
        this.firebaseService = new FirebaseService(this.context, this.fragment);
    }

    public void buildAddPackageToListDialog(final Activity activity, final List<Package> packageList, final RecyclerView.Adapter adapter) {
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
                        String packageNumber = packageNumberEditText.getText().toString().toUpperCase().trim();
                        firebaseService.addPackageToTheListAndUpdateDatabase(packageNumber, packageList, adapter);
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

    public void buildPackageCodeDialog(final String packageNumber) {
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
                        firebaseService.checkPackageCodeAndStartMapActivity(code, packageNumber);
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

    public void buildDialogToUpdateCourierWorkInfo(final String login, final String pin, final String phoneNumber, String firstName, String lastName) {
        View dialogView = inflater.inflate(R.layout.start_day_layout, null);
        final TimePicker picker = dialogView.findViewById(R.id.dateStartPicker);
        picker.setIs24HourView(true);
        final TimePicker picker2 = dialogView.findViewById(R.id.dateEndPicker);
        picker2.setIs24HourView(true);
        final EditText carEditText = dialogView.findViewById(R.id.carEditText);
        new MaterialStyledDialog.Builder(context)
                .setTitle("Zalogowano jako: " + firstName + " " + lastName)
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
                        firebaseService.saveCourierWorkTimes(login.toUpperCase().trim(), startTime, endTime);
                        firebaseService.saveCourierCarInfo(login.toUpperCase().trim(), car);
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
    }
}
