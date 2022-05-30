package com.example.joinus.model.repository;

import android.app.Application;
import android.net.Uri;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.UUID;

public class ImageRepository {
    private Application application;
    private FirebaseAuth mAuth;
    private FirebaseStorage storage;
    private MutableLiveData<Uri> uriMutableLiveData;
    private MutableLiveData<Boolean> isLoadingMutableLiveData;

    public ImageRepository(Application application){
        this.application = application;
        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        uriMutableLiveData = new MutableLiveData<>();
        isLoadingMutableLiveData = new MutableLiveData<>();
    }

    public MutableLiveData<Uri> getUriMutableLiveData() {
        return uriMutableLiveData;
    }

    public MutableLiveData<Boolean> getIsLoadingMutableLiveData() {
        return isLoadingMutableLiveData;
    }

    public void updateProfileImg(Uri updateImgUri){
        String uid = mAuth.getCurrentUser().getUid();
        StorageReference reference = storage.getReference();
        StorageReference profileReference = reference.child("images/" + uid);

        isLoadingMutableLiveData.postValue(true);

        profileReference.putFile(updateImgUri)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        Toast.makeText(application,"Cannot Upload", Toast.LENGTH_SHORT);
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        profileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                uriMutableLiveData.postValue(uri);
                                Toast.makeText(application,"Set Profile Image successfully!", Toast.LENGTH_SHORT).show();
                                isLoadingMutableLiveData.postValue(false);
                            }
                        });
                    }
        });
    }

    public void uploadImage(UUID uuid, Uri imgURi){
        StorageReference reference = storage.getReference();
        StorageReference profileReference = reference.child("images/" + uuid);

        isLoadingMutableLiveData.postValue(true);

        profileReference.putFile(imgURi)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        Toast.makeText(application,"Cannot Upload", Toast.LENGTH_SHORT).show();
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        profileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                uriMutableLiveData.postValue(uri);
                                isLoadingMutableLiveData.postValue(false);
                            }
                        });
                    }
        });
    }
}
