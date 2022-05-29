package com.example.joinus.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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

import com.example.joinus.MainActivity;
import com.example.joinus.R;
import com.example.joinus.Util.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;

public class LoginActivity extends AppCompatActivity {

    EditText email_text;
    EditText password_text;
    Button login_btn;
    TextView register_text;
    ProgressBar progressBar;

    public final static String LOGIN_SUCCESS = "Login Successfully!";
    public final static String LOGIN_FAILED = "Invalid username or password. Please check!";
    public final static String TAG = "REGISTER";

    private String user_email;
    private String user_password;
    private String fcmToken;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        email_text = findViewById(R.id.login_email);
        password_text = findViewById(R.id.login_password);
        login_btn = findViewById(R.id.login_button);
        register_text = findViewById(R.id.login_register_txt);
        progressBar = findViewById(R.id.login_pb);

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

                        // Log and toast
                        Log.d(TAG + "token", fcmToken);
                    }
                });

        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                user_email = email_text.getText().toString().trim();
                user_password = password_text.getText().toString().trim();
                    if(checkDetails(user_email, user_password)){
                        progressBar.setVisibility(View.VISIBLE);
                        login_btn.setVisibility(View.INVISIBLE);
                        mAuth.signInWithEmailAndPassword(user_email, user_password)
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            // Sign in success, update UI with the signed-in user's information
                                            Log.d(TAG, "signInWithEmail:success");
                                            FirebaseUser user = mAuth.getCurrentUser();
                                            Toast.makeText(getApplicationContext(),LOGIN_SUCCESS, Toast.LENGTH_SHORT).show();
                                            Utils.updateToken(fcmToken);
                                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            // If sign in fails, display a message to the user.
                                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                                            Toast.makeText(getApplicationContext(),LOGIN_FAILED, Toast.LENGTH_SHORT).show();
                                            progressBar.setVisibility(View.INVISIBLE);
                                            login_btn.setVisibility(View.VISIBLE);
                                        }
                                    }
                                });
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

    private boolean checkDetails(String email, String password){
        if(TextUtils.isEmpty(email)){
            Toast.makeText(getApplicationContext(),"The username is invalid.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(TextUtils.isEmpty(password)){
            Toast.makeText(getApplicationContext(),"Please enter password and confirmed password.", Toast.LENGTH_SHORT).show();
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