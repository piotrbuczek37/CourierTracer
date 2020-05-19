/* Copyright (C) Piotr Buczek - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Piotr Buczek <piotr.buczek37@gmail.com>, May 2020
 */

package com.ugprojects.couriertracerdpd.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * This class is View model for Home section which sets main text displayed on the screen
 */
public class HomeViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public HomeViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Witaj! \nZa chwilę zobaczysz, w którym miejscu ukrywa się kurier z twoją paczką. Wystarczy, że wpiszesz poniżej numer przesyłki oraz kod odbioru wysłany na twój numer telefonu:");
    }

    public LiveData<String> getText() {
        return mText;
    }
}