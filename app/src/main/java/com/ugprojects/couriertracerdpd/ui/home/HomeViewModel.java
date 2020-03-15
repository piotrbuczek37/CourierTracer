package com.ugprojects.couriertracerdpd.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HomeViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public HomeViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Witaj! Za chwilę zobaczysz, w którym miejscu ukrywa się kurier z twoją paczką. Wystarczy, że wpiszesz poniżej numer przesyłki oraz kod odbioru wysłany na twoją wiadomość e-mail:");
    }

    public LiveData<String> getText() {
        return mText;
    }
}