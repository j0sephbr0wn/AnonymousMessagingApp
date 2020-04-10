package com.brownjs.anonymousmessagingapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * First screen user sees when they open the app. Given them the option of logging in or registering
 * as a new user, with a email and password (for Champions) or anonymously.
 */
public class StartActivity extends MyAppActivity {

    /**
     *
     */
    @Override
    protected void onStart() {
        super.onStart();

        // get the current user
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        // if the user is not null, they are already logged in
        if (firebaseUser != null) {

            Intent intent = new Intent(StartActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    /**
     * {@inheritDoc}
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        // get layout elements
        Button btn_login = findViewById(R.id.btn_login);
        Button btn_register = findViewById(R.id.btn_register);

        // set btn listeners
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(StartActivity.this, LoginActivity.class));
            }
        });

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(StartActivity.this, RegisterActivity.class));
            }
        });
    }
}
