package com.brownjs.anonymousmessagingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private EditText editText_login_email;
    private EditText editText_login_password;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Login");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // get layout elements
        editText_login_email = findViewById(R.id.editText_login_email);
        editText_login_password = findViewById(R.id.editText_login_password);
        Button btn_login_cred = findViewById(R.id.btn_login_cred);
        Button btn_login_anon = findViewById(R.id.btn_login_anon);

        // get Firebase auth instance
        firebaseAuth = FirebaseAuth.getInstance();

        // set btn listeners
        btn_login_cred.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // read the user input
                String email = editText_login_email.getText().toString();
                String password = editText_login_password.getText().toString();

                // check if both are filled in
                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Please enter all fields", Toast.LENGTH_SHORT).show();
                } else {
                    // fields are valid, proceed to login
                    login(email, password);
                }

            }
        });

        btn_login_anon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // build a (nearly) unique id from the users hardware
                String pseudoId =
                        Build.BOARD.length() % 10 + Build.BRAND.length() % 10 +
                                Build.CPU_ABI.length() % 10 + Build.DEVICE.length() % 10 +
                                Build.DISPLAY.length() % 10 + Build.HOST.length() % 10 +
                                Build.ID.length() % 10 + Build.MANUFACTURER.length() % 10 +
                                Build.MODEL.length() % 10 + Build.PRODUCT.length() % 10 +
                                Build.TAGS.length() % 10 + Build.TYPE.length() % 10 +
                                Build.USER.length() % 10 + "@capgemini.com";

                String password = "default_password";

                login(pseudoId, password);
            }
        });
    }

    private void login(String email, String password) {

        //TODO add loading text/animation

        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Authentication unsuccessful.", Toast.LENGTH_LONG).show();
                }
            }

            //TODO remove loading text/animation
        });
    }
}
