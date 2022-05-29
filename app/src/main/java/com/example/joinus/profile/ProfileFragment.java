package com.example.joinus.profile;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.joinus.R;
import com.example.joinus.Util.Utils;
import com.example.joinus.login.LoginActivity;
import com.example.joinus.model.User;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;


public class ProfileFragment extends Fragment {

    Button logout_btn;
    TextView username;
    TextView email;
    TextView location;
    ImageView profileImg;
    ImageButton profileEdit;
    ChipGroup chipGroup;

    private FirebaseAuth mAuth;
    private String uid;
    private User currentUser;
    private Handler handler;
    private String city;
    public static final String TAG = "AccountFragment";
    public static final String USERNAME = "username: ";
    public static final String EMAIL = "email address: ";
    public static final String LOCATION = "location: ";

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_account, container, false);
        logout_btn = view.findViewById(R.id.account_logout_btn);
        username = view.findViewById(R.id.profile_username);
        email = view.findViewById(R.id.profile_email);
        location = view.findViewById(R.id.profile_location);
        profileImg = view.findViewById(R.id.profile_userImage);
        profileEdit = view.findViewById(R.id.profile_edit);
        chipGroup = view.findViewById(R.id.profile_interest_chipGroup);

        mAuth = FirebaseAuth.getInstance();
        uid = mAuth.getCurrentUser().getUid();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        profileEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity().getApplicationContext(), EditProfileActivity.class);
                startActivity(intent);
            }
        });

        logout_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                Intent intent = new Intent(getActivity().getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        handler = new Handler(Looper.getMainLooper());
        currentUser = null;

        new Thread(new Runnable() {
            @Override
            public void run() {
                currentUser = Utils.getUserData(uid);

                while(currentUser.getUid() == null){
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if(currentUser.getUsername() != null){

                    if(currentUser.getLocation() == null){
                        city = "N/A";
                    }else{
                        //get the city name from current location
                        double lat = currentUser.getLocation().getLatitude();
                        double lon = currentUser.getLocation().getLongitude();
                        try {
                            Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
                            List<Address> addresses = geocoder.getFromLocation(lat, lon,1);
                            city = addresses.get(0).getLocality();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    initView();
                }
            }
        }).start();
    }

    public void initView(){

        handler.post(new Runnable() {
            @Override
            public void run() {

                //set the textView
                username.setText(USERNAME + currentUser.getUsername());
                email.setText(EMAIL + currentUser.getEmail());

                if(city == null){
                    location.setText(LOCATION + "Cannot get location");
                }else{
                    location.setText(LOCATION + city);
                }

                //set the ImageView
                if(currentUser.getProfileImg().equals(Utils.DEFAULTIMAGE)){
                    AssetManager assetManager = getContext().getAssets();
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
                    profileImg.setImageBitmap(bitmap);
                }else{
                    Picasso.get().load(currentUser.getProfileImg()).into(profileImg);
                }

                //set the chipView
                for(String topic : currentUser.getInterestedTopics()){
                    addChip(topic);
                }
            }
        });
    }

    private void addChip(String value){
        Chip chip = new Chip(getContext());
        chip.setText(value);
        chipGroup.addView(chip);
    }
}