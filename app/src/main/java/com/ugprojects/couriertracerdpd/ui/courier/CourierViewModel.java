package com.ugprojects.couriertracerdpd.ui.courier;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

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