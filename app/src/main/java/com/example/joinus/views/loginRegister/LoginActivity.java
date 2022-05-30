package com.example.joinus.views.loginRegister;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.joinus.views.MainActivity;
import com.example.joinus.R;
import com.example.joinus.viewmodel.LoginRegisterViewModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;

public class LoginActivity extends AppCompatActivity {

    EditText email_text;
    EditText password_text;
    Button login_btn;
    TextView register_text;
    ProgressBar progressBar;

    public final static String TAG = "LOGIN";

    private String user_email;
    private String user_password;
    private String fcmToken;
    private FirebaseAuth mAuth;
    private LoginRegisterViewModel loginRegisterViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //get the view
        email_text = findViewById(R.id.login_email);
        password_text = findViewById(R.id.login_password);
        login_btn = findViewById(R.id.login_button);
        register_text = findViewById(R.id.login_register_txt);
        progressBar = findViewById(R.id.login_pb);

        //get the current token from device
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        fcmToken = task.getResult();
                        Log.d(TAG + "token", fcmToken);
                    }
                });

        //Update UI
        loginRegisterViewModel = new ViewModelProvider(this).get(LoginRegisterViewModel.class);
        loginRegisterViewModel.getUserMutableLiveData().observe(this, new Observer<FirebaseUser>() {
            @Override
            public void onChanged(FirebaseUser user) {
                if(user != null){
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
        loginRegisterViewModel.getIsLoggingMutableLiveData().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(!aBoolean){
                    progressBar.setVisibility(View.INVISIBLE);
                    login_btn.setVisibility(View.VISIBLE);
                }
            }
        });

        //Login User
        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                user_email = email_text.getText().toString().trim();
                user_password = password_text.getText().toString().trim();
                    if(checkDetails(user_email, user_password)){
                        progressBar.setVisibility(View.VISIBLE);
                        login_btn.setVisibility(View.INVISIBLE);
                        loginRegisterViewModel.login(user_email,user_password, fcmToken);
                    }
            }
        });

        //Start the register activity
        register_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                intent.putExtra("fcmToken",fcmToken);
                startActivity(intent);
            }
        });
    }

    //Check input
    private boolean checkDetails(String email, String password){
        if(TextUtils.isEmpty(email)){
            Toast.makeText(LoginActivity.this,"The username is invalid.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(TextUtils.isEmpty(password)){
            Toast.makeText(LoginActivity.this,"Please enter password and confirmed password.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("email", email_text.getText().toString());
        outState.putString("password", password_text.getText().toString());
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        email_text.setText(savedInstanceState.getString("email"));
        password_text.setText(savedInstanceState.getString("password"));
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser != null){
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
        }
    }
}