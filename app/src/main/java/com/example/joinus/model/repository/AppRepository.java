package com.example.joinus.model.repository;

import android.app.Application;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.MutableLiveData;

import com.example.joinus.Util.Utils;
import com.example.joinus.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class AppRepository {
    private Application application;
    private FirebaseAuth mAuth;
    private FirebaseFirestore database;
    private MutableLiveData<FirebaseUser> userMutableLiveData;
    private MutableLiveData<Boolean> isLoggingMutableLiveData;

    public final static String LOGIN_SUCCESS = "Login Successfully!";
    public final static String LOGIN_FAILED = "Invalid username or password. Please check!";

    public AppRepository(Application application){
        this.application = application;
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseFirestore.getInstance();
        userMutableLiveData = new MutableLiveData<>();
        isLoggingMutableLiveData = new MutableLiveData<>();
    }

    public MutableLiveData<FirebaseUser> getUserMutableLiveData() {
        return userMutableLiveData;
    }

    public MutableLiveData<Boolean> getIsLoggingMutableLiveData() {
        return isLoggingMutableLiveData;
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    public void register(String email, String password, String username, String fcmToken){
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(application.getMainExecutor(),new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            createUser(email,username,fcmToken);
                        }else{
                            Toast.makeText(application, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    private void createUser(String email, String username, String fcmToken){
        String uid = mAuth.getCurrentUser().getUid();
        User user = new User(email,uid,username,fcmToken);

        database.collection("users").document(uid).set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(application, "User Created", Toast.LENGTH_SHORT).show();
                userMutableLiveData.postValue(mAuth.getCurrentUser());
            }
        });
    }

    public void login(String email, String password, String fcmToken){
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Toast.makeText(application,LOGIN_SUCCESS, Toast.LENGTH_SHORT).show();
                            userMutableLiveData.postValue(mAuth.getCurrentUser());
                            isLoggingMutableLiveData.postValue(true);
                            Utils.updateToken(fcmToken);
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(application,LOGIN_FAILED, Toast.LENGTH_SHORT).show();
                            isLoggingMutableLiveData.postValue(false);
                        }
                    }
                });
    }
}
