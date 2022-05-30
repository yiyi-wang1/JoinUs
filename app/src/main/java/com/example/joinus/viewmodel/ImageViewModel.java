package com.example.joinus.viewmodel;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.joinus.model.repository.ImageRepository;

import java.util.UUID;

public class ImageViewModel extends AndroidViewModel {
    private ImageRepository imageRepository;
    private MutableLiveData<Uri> uriMutableLiveData;
    private MutableLiveData<Boolean> isLoadingMutableLiveData;


    public ImageViewModel(@NonNull Application application) {
        super(application);
        imageRepository = new ImageRepository(application);
        uriMutableLiveData = imageRepository.getUriMutableLiveData();
        isLoadingMutableLiveData = imageRepository.getIsLoadingMutableLiveData();
    }

    public MutableLiveData<Uri> getUriMutableLiveData() {
        return uriMutableLiveData;
    }

    public MutableLiveData<Boolean> getIsLoadingMutableLiveData() {
        return isLoadingMutableLiveData;
    }

    public void updateProfileImg(Uri uri){
        imageRepository.updateProfileImg(uri);
    }

    public void uploadImg(UUID uuid, Uri imgURi){
        imageRepository.uploadImage(uuid, imgURi);
    }
}
