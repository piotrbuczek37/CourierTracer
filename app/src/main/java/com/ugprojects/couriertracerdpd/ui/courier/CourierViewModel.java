/* Copyright (C) Piotr Buczek - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Piotr Buczek <piotr.buczek37@gmail.com>, May 2020
 */

package com.ugprojects.couriertracerdpd.ui.courier;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * This class is View model for Courier section which sets main text displayed on the screen
 */
public class CourierViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public CourierViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Witaj w sekcji dla kuriera!\nZaloguj się za pomocą twojego identyfikatora oraz PINu do HH");
    }

    public LiveData<String> getText() {
        return mText;
    }
}