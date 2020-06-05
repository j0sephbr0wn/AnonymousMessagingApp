package com.brownjs.anonymousmessagingapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Activity to allow existing users to login to the application
 */
public class LoginActivity extends MyAppActivity {

    private EditText txtLoginEmail;
    private EditText txtLoginPassword;

    /**
     * {@inheritDoc}
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // setup common_toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // get layout elements
        txtLoginEmail = findViewById(R.id.editText_login_email);
        txtLoginPassword = findViewById(R.id.editText_login_password);
        Button btnLoginCred = findViewById(R.id.btn_login_cred);
        Button btnLoginAnon = findViewById(R.id.btn_login_anon);

        // set btn listeners
        btnLoginCred.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // read the user input
                String email = txtLoginEmail.getText().toString();
                String password = txtLoginPassword.getText().toString();

                // check if both are filled in
                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Please enter all fields", Toast.LENGTH_SHORT).show();
                } else {
                    // fields are valid, proceed to login
                    login(email, password);
                }

            }
        });

        btnLoginAnon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                login(getPseudoId(), getDefaultPassword());
            }
        });
    }

    /**
     * Function is overwritten to give the same animation as using the device 'Back' command
     * {@inheritDoc}
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        onBackPressed();
        return true;
    }

    /**
     *
     * @param email user wishes to login with
     * @param password user wishes to login with
     */
    private void login(String email, String password) {

        startLoadingAnimation();

        // login with credentials
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // move to MainActivity, clearing the back stack
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Authentication unsuccessful.", Toast.LENGTH_LONG).show();
                }

                endLoadingAnimation();
            }
        });
    }
}
