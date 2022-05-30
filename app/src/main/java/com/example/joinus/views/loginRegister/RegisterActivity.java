package com.example.joinus.views.loginRegister;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.joinus.views.MainActivity;
import com.example.joinus.R;
import com.example.joinus.viewmodel.LoginRegisterViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

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
    private LoginRegisterViewModel loginRegisterViewModel;
    private String fcmToken;

//    private FirebaseAuth mAuth;
//    private FirebaseFirestore database;

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


        fcmToken = getIntent().getExtras().getString("fcmToken");
        loginRegisterViewModel = new ViewModelProvider(this).get(LoginRegisterViewModel.class);
        loginRegisterViewModel.getUserMutableLiveData().observe(this, new Observer<FirebaseUser>() {
            @Override
            public void onChanged(FirebaseUser user) {
                if(user != null){
                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }else{
                    Toast.makeText(getApplicationContext(), "Cannot create new user", Toast.LENGTH_SHORT).show();
                }
            }
        });


        register_button.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.P)
            @Override
            public void onClick(View view) {
                email = register_email_text.getText().toString().trim();
                password = register_password_text.getText().toString().trim();
                confirmedPassword = register_confirm_password_text.getText().toString().trim();
                username= register_username_text.getText().toString().trim();

                if(checkDetails(email, password, confirmedPassword, username)){
                    loginRegisterViewModel.register(email,password,username,fcmToken);
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
}