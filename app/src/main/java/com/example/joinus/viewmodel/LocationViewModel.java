package com.example.joinus.viewmodel;

import android.app.Application;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.joinus.model.repository.LocationRepository;

public class LocationViewModel extends AndroidViewModel {
    private LocationRepository locationRepository;
    private MutableLiveData<Location> locationMutableLiveData;


    public LocationViewModel(@NonNull Application application) {
        super(application);
        locationRepository = new LocationRepository(application);
        locationMutableLiveData = locationRepository.getLocationMutableLiveData();
    }

    public MutableLiveData<Location> getLocationMutableLiveData() {
        return locationMutableLiveData;
    }

    public void getLocation(){
        locationRepository.getLocation();
    }
}
