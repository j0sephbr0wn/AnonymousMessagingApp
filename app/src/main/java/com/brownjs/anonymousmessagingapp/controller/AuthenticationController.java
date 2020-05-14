package com.brownjs.anonymousmessagingapp.controller;

import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.brownjs.anonymousmessagingapp.LoginActivity;
import com.brownjs.anonymousmessagingapp.MainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class AuthenticationController {

    private static AuthenticationController single_instance = null;

    private FirebaseAuth firebaseAuth;

    private static final String DEFAULT_PASSWORD = "default_password";
    private static final String PSEUDO_ID = "00" +
            Build.BOARD.length() % 10 + Build.BRAND.length() % 10 +
            Build.CPU_ABI.length() % 10 + Build.DEVICE.length() % 10 +
            Build.DISPLAY.length() % 10 + Build.HOST.length() % 10 +
            Build.ID.length() % 10 + Build.MANUFACTURER.length() % 10 +
            Build.MODEL.length() % 10 + Build.PRODUCT.length() % 10 +
            Build.TAGS.length() % 10 + Build.TYPE.length() % 10 +
            Build.USER.length() % 10 + "@anonymous.com";

    private AuthenticationController() {

        firebaseAuth = FirebaseAuth.getInstance();

    }

    public static AuthenticationController getInstance() {
        if (single_instance == null) {
            single_instance = new AuthenticationController();
        }

        return single_instance;
    }

    public boolean register(String email, String username, String password, Boolean anon) {

        return true;
    }

//    public boolean login(String email, String password, Boolean anon) {
//        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
//            @Override
//            public void onComplete(@NonNull Task<AuthResult> task) {
//                if (task.isSuccessful()) {
//                    // move to MainActivity, clearing the back stack
//                    return true;
//                } else {
//                    return false;
//                }
//            }
//        });
//    }

}
