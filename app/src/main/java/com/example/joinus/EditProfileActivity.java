package com.example.joinus;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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

import com.example.joinus.model.User;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EditProfileActivity extends AppCompatActivity {

    public static final Integer SELECT_IMG = 1;
    public static final String TAG = "Edit Activity";

    private FirebaseAuth mAuth;
    private User currentUser;
    private String uid;
    private Uri updateImgUri;
    private boolean isLoading;
    private Set<String> updateInterested;


    ImageView profileImage;
    EditText username;
    Button save;
    Handler handler;
    ProgressBar pb;
    ChipGroup chipGroup;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        profileImage = findViewById(R.id.profile_userImage);
        username = findViewById(R.id.edit_profile_username);
        save = findViewById(R.id.edit_profile_save_btn);
        pb = findViewById(R.id.edit_profile_img_pb);
        chipGroup = findViewById(R.id.profile_interest_chipGroup);

        mAuth = FirebaseAuth.getInstance();
        uid = mAuth.getCurrentUser().getUid();
        currentUser = null;
        handler = new Handler(Looper.getMainLooper());
        updateInterested = new HashSet<>();
        isLoading = false;

        new Thread(new Runnable() {
            @Override
            public void run() {
                currentUser = Utils.getUserData(uid,getApplicationContext());

                //System.out.println(currentUser.getUid());

                while(currentUser.getUid() == null){
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if(currentUser.getUsername() != null && currentUser.getEventList() != null){
                    initView();
                }
            }
        }).start();

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

    private void initView(){
        handler.post(new Runnable() {
            @Override
            public void run() {
                username.setText(currentUser.getUsername());
                Picasso.get().load(currentUser.getProfileImg()).into(profileImage);

                for(String topic : Utils.TOPICS){
                    addChip(topic);
                }

            }
        });
    }


    private void updateProfileImg(){
        //Upload image to firestorage
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference reference = storage.getReference();
        StorageReference profileReference = reference.child("images/" + currentUser.getUid());

        if(updateImgUri != null){
            isLoading = true;
            save.setClickable(false);
            pb.setVisibility(View.VISIBLE);
            profileReference.putFile(updateImgUri)
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle unsuccessful uploads
                            Toast.makeText(getApplicationContext(),"Cannot Upload", Toast.LENGTH_SHORT);
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            profileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    updateImgUri = uri;
                                    Toast.makeText(getApplicationContext(),"Set Profile Image successfully!", Toast.LENGTH_SHORT).show();
                                    Log.d(TAG,uri.toString());
                                    isLoading = false;
                                    pb.setVisibility(View.INVISIBLE);
                                    save.setClickable(true);
                                }
                            });
                        }
            });
        }
    }



    private void updateProfile() {
        String updateName = username.getText().toString().trim();

        if(!isLoading){
            //update to firestore
            FirebaseFirestore database = FirebaseFirestore.getInstance();
            DocumentReference userRef = database.collection("users").document(currentUser.getUid());
            database.runTransaction(new Transaction.Function<Void>() {
                @Override
                public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                    DocumentSnapshot snapshot = transaction.get(userRef);
                    transaction.update(userRef, "username", updateName);
                    if(updateImgUri != null){
                        transaction.update(userRef, "profileImg", updateImgUri.toString());
                    }
                    if(updateInterested != null){
                        List<String> aList = new ArrayList<String>(updateInterested);
                        transaction.update(userRef, "interestedTopics",aList);
                    }
                    return null;
                }
            }).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(EditProfileActivity.this,"Updated successfully!", Toast.LENGTH_SHORT).show();
                    Log.w(TAG, "Transaction successfully");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w(TAG, "Transaction failure.", e);
                }
            });
        }
    }

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