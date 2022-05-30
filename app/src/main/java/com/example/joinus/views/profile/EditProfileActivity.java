package com.example.joinus.views.profile;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.joinus.R;
import com.example.joinus.Util.Utils;
import com.example.joinus.model.User;
import com.example.joinus.viewmodel.ImageViewModel;
import com.example.joinus.viewmodel.UserViewModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EditProfileActivity extends AppCompatActivity {

    public static final String TAG = "Edit Activity";

    private User currentUser;
    private Uri updateImgUri;
    private boolean isLoading;
    private Set<String> updateInterested;
    private UserViewModel userViewModel;
    private ImageViewModel imageViewModel;


    ImageView profileImage;
    EditText username;
    Button save;
    ProgressBar pb;
    ChipGroup chipGroup;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        imageViewModel = new ViewModelProvider(this).get(ImageViewModel.class);

        profileImage = findViewById(R.id.profile_userImage);
        username = findViewById(R.id.edit_profile_username);
        save = findViewById(R.id.edit_profile_save_btn);
        pb = findViewById(R.id.edit_profile_img_pb);
        chipGroup = findViewById(R.id.profile_interest_chipGroup);

        currentUser = null;
        updateInterested = new HashSet<>();
        isLoading = false;

        userViewModel.getCurrentUserMutableLiveData().observe(this, new Observer<User>() {
            @Override
            public void onChanged(User user) {
                if(user != null){
                    currentUser = user;
                    if(currentUser.getUsername() != null && currentUser.getEventList() != null){
                        initView();
                    }
                }
            }
        });

        //Select the photo from device
        ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri uri) {
                        updateImgUri = uri;
                        profileImage.setImageURI(updateImgUri);
                        updateProfileImg();
                    }
                });

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mGetContent.launch("image/*");
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateProfile();
                finish();
            }
        });
    }

    //init the view
    private void initView(){
        username.setText(currentUser.getUsername());

        if(currentUser.getProfileImg().equals(Utils.DEFAULTIMAGE)){
            AssetManager assetManager = getAssets();
            InputStream instr = null;
            try {
                instr = assetManager.open("defaultProfile.png");
            } catch (IOException e) {
                e.printStackTrace();
            }
            Bitmap bitmap = BitmapFactory.decodeStream(instr);
            try {
                instr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            profileImage.setImageBitmap(bitmap);
        }else{
            Picasso.get().load(currentUser.getProfileImg()).into(profileImage);
        }

        for(String topic : Utils.TOPICS){
            addChip(topic);
        }
    }

    //update the profile image from user selection
    private void updateProfileImg(){
        if(updateImgUri != null){
            isLoading = true;
            save.setClickable(false);
            pb.setVisibility(View.VISIBLE);
            imageViewModel.updateProfileImg(updateImgUri);
            imageViewModel.getUriMutableLiveData().observe(this, new Observer<Uri>() {
                @Override
                public void onChanged(Uri uri) {
                    if(uri != null){
                        updateImgUri = uri;
                    }
                }
            });
            imageViewModel.getIsLoadingMutableLiveData().observe(this, new Observer<Boolean>() {
                @Override
                public void onChanged(Boolean aBoolean) {
                    if(!aBoolean){
                        pb.setVisibility(View.INVISIBLE);
                        save.setClickable(true);
                        isLoading = aBoolean;
                    }
                }
            });
        }
    }

    //update the profile after the photo loading is done
    private void updateProfile() {
        String updateName = username.getText().toString().trim();
        if(!isLoading){
            userViewModel.updateUserData(updateName,updateImgUri,updateInterested);
        }
    }

    //add chip to group
    private void addChip(String value){
        Chip chip = (Chip) this.getLayoutInflater().inflate(R.layout.filter_interested_topics, null, false);
        chip.setText(value);
        chip.setClickable(true);
        for(String interested : currentUser.getInterestedTopics()){
            if(value.equals(interested)){
                chip.setChecked(true);
            }
            updateInterested.add(interested);
        }

        chip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(chip.isChecked()){
                    updateInterested.add(chip.getText().toString());
                }else{
                    chip.setChecked(false);
                    updateInterested.remove(chip.getText().toString());
                }
            }
        });
        chipGroup.addView(chip);
    }
}