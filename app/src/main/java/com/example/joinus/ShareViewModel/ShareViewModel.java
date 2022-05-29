package com.example.joinus.ShareViewModel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.joinus.model.User;

public class ShareViewModel extends ViewModel {
    private MutableLiveData<User> currentUser = new MutableLiveData<>();

    public void setUser(User user) {
        currentUser.setValue(user);
    }

    public MutableLiveData<User> getCurrentUser() {
        return currentUser;
    }
}
