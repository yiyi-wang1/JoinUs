package com.example.joinus.viewmodel;

import android.app.Application;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.joinus.model.repository.AppRepository;
import com.google.firebase.auth.FirebaseUser;

public class LoginRegisterViewModel extends AndroidViewModel {
    private AppRepository appRepository;
    private MutableLiveData<FirebaseUser> userMutableLiveData;
    private MutableLiveData<Boolean> isLoggingMutableLiveData;

    public LoginRegisterViewModel(@NonNull Application application) {
        super(application);
        appRepository = new AppRepository(application);
        userMutableLiveData = appRepository.getUserMutableLiveData();
        isLoggingMutableLiveData = appRepository.getIsLoggingMutableLiveData();
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    public void register(String email, String password, String username, String fcmToken){
        appRepository.register(email,password,username,fcmToken);
    }

    public void login(String email, String password, String fcmToken){
        appRepository.login(email, password, fcmToken);
    }

    public MutableLiveData<FirebaseUser> getUserMutableLiveData() {
        return userMutableLiveData;
    }

    public MutableLiveData<Boolean> getIsLoggingMutableLiveData() {
        return isLoggingMutableLiveData;
    }
}
