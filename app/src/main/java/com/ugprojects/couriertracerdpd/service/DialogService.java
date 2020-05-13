package com.ugprojects.couriertracerdpd.service;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.github.javiersantos.materialstyleddialogs.enums.Style;
import com.ugprojects.couriertracerdpd.activity.ClientMapsActivity;

public class DialogService {
    private MaterialStyledDialog materialStyledDialog;
    private boolean isCorrect;

    public boolean showDialogToEnterPackageCode(View dialogView ,final Context context){
        new MaterialStyledDialog.Builder(context)
                .setTitle("Wprowadź kod odbioru")
                .setDescription("Kod odbioru znajduje się w wiadomości SMS")
                .setStyle(Style.HEADER_WITH_TITLE)
                .setCustomView(dialogView)
                .setPositiveText("OK")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        isCorrect = true;
                    }
                })
                .setNegativeText("Anuluj")
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        isCorrect = false;
                        dialog.dismiss();
                    }
                })
                .show();
        return isCorrect;
    }
}
