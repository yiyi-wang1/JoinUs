package com.example.joinus.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.joinus.MainActivity;
import com.example.joinus.R;
import com.example.joinus.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegisterActivity extends AppCompatActivity {

    EditText register_email_text;
    EditText register_password_text;
    EditText register_confirm_password_text;
    EditText register_username_text;
    Button register_button;

    private String email;
    private String password;
    private String confirmedPassword;
    private String username;

    private FirebaseAuth mAuth;
    private FirebaseFirestore database;

    public final static String TAG = "REGISTER";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        register_email_text = findViewById(R.id.register_email);
        register_password_text = findViewById(R.id.register_password1);
        register_confirm_password_text = findViewById(R.id.register_password2);
        register_username_text = findViewById(R.id.register_username);
        register_button = findViewById(R.id.register_button);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseFirestore.getInstance();

        register_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                email = register_email_text.getText().toString().trim();
                password = register_password_text.getText().toString().trim();
                confirmedPassword = register_confirm_password_text.getText().toString().trim();
                username= register_username_text.getText().toString().trim();

                if(checkDetails(email, password, confirmedPassword, username)){
                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()){
                                        Log.d(TAG,"createUserWithEmail:success");
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        registerUser(email,username);
                                    }else{
                                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                        Toast.makeText(getApplicationContext(), "Authentication failed.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser != null){
            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
            startActivity(intent);
        }
    }

    //Check if the email, username are valid and passwords match,
    //return true if email and username are valid and passwords match, otherwise return false
    private boolean checkDetails(String email, String password, String confirmedPassword, String username){
        if(TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(getApplicationContext(),"The email is invalid.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(TextUtils.isEmpty(username)){
            Toast.makeText(getApplicationContext(),"The username is invalid.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmedPassword)){
            Toast.makeText(getApplicationContext(),"Please enter password and confirmed password.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(password.length() < 6 || confirmedPassword.length() < 6){
            Toast.makeText(getApplicationContext(),"Password should be at least 6 digit.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(!password.equals(confirmedPassword)){
            Toast.makeText(getApplicationContext(),"Passwords do not match.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void registerUser(String email, String username){
        String uid = mAuth.getCurrentUser().getUid();
//        Map<String, Object> user = new HashMap<>();
//        user.put("email", email);
//        user.put("username", username);
//        user.put("uid",uid);
//        user.put("profileImgUrl", Utils.DEFAULTIMAGE);
//        user.put("eventList",null);
//        user.put("chatList",null);
//        user.put("location",null);
//        user.put("interestedTopics",null);
//        user.put("verified",false);
        User user = new User(email,uid,username);
        
        database.collection("users").document(uid).set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}