package com.brownjs.anonymousmessagingapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

/**
 * Activity to allow new users to register for the application
 */
public class RegisterActivity extends MyAppActivity {

    private EditText txtRegEmail;
    private EditText txtRegPassword;
    private EditText txtRegUsername;
    private EditText txtChampionCode;
    private EditText txtSecretWord;

    /**
     * {@inheritDoc}
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // setup common_toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // get layout elements
        txtRegEmail = findViewById(R.id.editText_reg_email);
        txtRegPassword = findViewById(R.id.editText_reg_password);
        txtRegUsername = findViewById(R.id.editText_reg_username);
        txtChampionCode = findViewById(R.id.editText_champion_code);
        txtSecretWord = findViewById(R.id.editText_secret_word);
        TextView txtRegInfo = findViewById(R.id.textView_reg_info);
        Button btnRegCred = findViewById(R.id.btn_reg_cred);
        Button btnRegAnon = findViewById(R.id.btn_reg_anon);

        // set btn listeners
        btnRegCred.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = txtRegEmail.getText().toString();
                String password = txtRegPassword.getText().toString();
                String username = txtRegUsername.getText().toString();
                String code = txtChampionCode.getText().toString();

                // check if all fields are filled it
                if (email.isEmpty() || password.isEmpty() || username.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "All fields must be filled in.", Toast.LENGTH_SHORT).show();
                }
                // check password length
                else if (password.length() < 6) {
                    Toast.makeText(RegisterActivity.this, "Password must be at least 6 characters.", Toast.LENGTH_SHORT).show();
                }
                // check email is Capgemini email address
                else if (!email.endsWith(getChampionEmailSuffix())) {
                    Toast.makeText(RegisterActivity.this, "Email must be a Capgemini address.", Toast.LENGTH_SHORT).show();
                }
                else if (!code.equals(getChampionRegisterCode())) {
                    Toast.makeText(RegisterActivity.this, "Code does not match, please contact an admin.", Toast.LENGTH_SHORT).show();
                }
                else {
                    register(email, password, username);
                }
            }
        });

        btnRegAnon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String secretWord = txtSecretWord.getText().toString();

                if (secretWord.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Please choose a secret word.", Toast.LENGTH_SHORT).show();
                }
                else {
                    register(getPseudoId(), secretWord, null);
                }
            }
        });

        txtRegInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(RegisterActivity.this, "Some info", Toast.LENGTH_SHORT).show();
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
     * @param email    user wishes to register with
     * @param password user wishes to register with
     * @param username user wishes to register with
     */
    private void register(final String email, String password, final String username) {

        // display loading animation
        startLoadingAnimation();

        // get Firebase auth instance
        final FirebaseAuth auth = FirebaseAuth.getInstance();

        // create new account
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // if new account is created successfully, create new User in database
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = auth.getCurrentUser();
                            assert firebaseUser != null;

                            // get the userId of the user just created
                            String userId = firebaseUser.getUid();

                            // get reference to the User document
                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(userId);

                            // is the new user a champion?
                            boolean isChampion = email.endsWith(getChampionEmailSuffix());

                            HashMap<String, Object> hashMap = new HashMap<>();
                            if (isChampion) {
                                // build document
                                hashMap.put("id", userId);
                                hashMap.put("email", email);
                                hashMap.put("username", username);
                                hashMap.put("imageURL", "default");
                                hashMap.put("description", "Description not set");
                                hashMap.put("champion", true);
                                hashMap.put("phone", "Phone number not set");
                                hashMap.put("role", "Role not set");
                                hashMap.put("location", "Location not set");
                                hashMap.put("token", "default");
                            } else {
                                // build document
                                hashMap.put("id", userId);
                                hashMap.put("champion", false);
                                hashMap.put("token", "default");
                            }

                            // put document in table and
                            reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    // if user added successfully move to MainActivity
                                    if (task.isSuccessful()) {
                                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(RegisterActivity.this, "Authentication did not work.", Toast.LENGTH_SHORT).show();
                        }

                        // remover loading animation
                        endLoadingAnimation();
                    }
                });
    }
}
