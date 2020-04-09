package com.brownjs.anonymousmessagingapp;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private EditText editText_reg_email;
    private EditText editText_reg_password;
    private EditText editText_reg_username;
    private LinearLayout loading_page;

    private FirebaseAuth auth;
    private DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Register");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // get layout elements
        editText_reg_email = findViewById(R.id.editText_reg_email);
        editText_reg_password = findViewById(R.id.editText_reg_password);
        editText_reg_username = findViewById(R.id.editText_reg_username);
        loading_page = findViewById(R.id.loading_page);
//        spinner_business_unit = findViewById(R.id.spinner_business_unit);
        //    private Spinner spinner_business_unit;
        Button btn_reg_cred = findViewById(R.id.btn_reg_cred);
        Button btn_reg_anon = findViewById(R.id.btn_reg_anon);

        //populate spinner
//        addBusinessUnitsToSpinner();

        // get Firebase auth instance
        auth = FirebaseAuth.getInstance();

        // set btn listeners
        btn_reg_cred.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editText_reg_email.getText().toString();
                String password = editText_reg_password.getText().toString();
                String username = editText_reg_username.getText().toString();

                // check if all fields are filled it
                if (email.isEmpty() || password.isEmpty() || username.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "All fields must be filled in.", Toast.LENGTH_SHORT).show();
                }
                // check password length
                else if (password.length() < 6) {
                    Toast.makeText(RegisterActivity.this, "Password must be at least 6 characters.", Toast.LENGTH_SHORT).show();
                }
                // check email is Capgemini email address
                else if (!email.endsWith("@capgemini.com")) {
                    Toast.makeText(RegisterActivity.this, "Email must be a Capgemini address.", Toast.LENGTH_SHORT).show();
                }
                else{
                    register(email, password, username);
                }
            }
        });

        btn_reg_anon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // build a (nearly) unique id from the users hardware
                String pseudoId = "00" +
                        Build.BOARD.length() % 10 + Build.BRAND.length() % 10 +
                                Build.CPU_ABI.length() % 10 + Build.DEVICE.length() % 10 +
                                Build.DISPLAY.length() % 10 + Build.HOST.length() % 10 +
                                Build.ID.length() % 10 + Build.MANUFACTURER.length() % 10 +
                                Build.MODEL.length() % 10 + Build.PRODUCT.length() % 10 +
                                Build.TAGS.length() % 10 + Build.TYPE.length() % 10 +
                                Build.USER.length() % 10 + "@capgemini.com";

                // password doesn't matter, set it to a default string
                String password = "default_password";

                // user is anonymous
                String username = "Anonymous User";

                register(pseudoId, password, username);
            }
        });
    }

    private void register(final String email, String password, final String username) {

        startLoading();

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = auth.getCurrentUser();
                            assert firebaseUser != null;
                            String userid = firebaseUser.getUid();

                            reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

                            HashMap<String, String> hashMap = new HashMap<>();
                            hashMap.put("id", userid);
                            hashMap.put("email", email);
                            hashMap.put("username", username);
                            hashMap.put("imageURL", "default");
                            hashMap.put("description", "No description set");
                            hashMap.put("businessUnit", "No BU set");
                            hashMap.put("status", "offline");

                            reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(RegisterActivity.this, "Anonymous authentication did not work", Toast.LENGTH_SHORT).show();
                        }

                        finishLoading();
                    }
                });
    }

    private void startLoading() {
        loading_page.setVisibility(View.VISIBLE);

        RotateAnimation rotate = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(1000);
        rotate.setRepeatCount(Animation.INFINITE);
        rotate.setInterpolator(new LinearInterpolator());

        ImageView loadingSpade = findViewById(R.id.loading_spade);
        loadingSpade.startAnimation(rotate);
    }

    private void finishLoading() {
        loading_page.setVisibility(View.GONE);
    }
}
