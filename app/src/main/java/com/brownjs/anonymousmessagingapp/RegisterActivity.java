package com.brownjs.anonymousmessagingapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

    private EditText editText_reg_email;
    private EditText editText_reg_password;
    private EditText editText_reg_username;
    private ImageView loading_spade;
    private LinearLayout loading_page;

    /**
     * {@inheritDoc}
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // get layout elements
        editText_reg_email = findViewById(R.id.editText_reg_email);
        editText_reg_password = findViewById(R.id.editText_reg_password);
        editText_reg_username = findViewById(R.id.editText_reg_username);
        loading_spade = findViewById(R.id.loading_spade);
        loading_page = findViewById(R.id.loading_page);
        Button btn_reg_cred = findViewById(R.id.btn_reg_cred);
        Button btn_reg_anon = findViewById(R.id.btn_reg_anon);

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
                } else {
                    register(email, password, username);
                }
            }
        });

        btn_reg_anon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // get pseudoId
                String pseudoId = buildPseudoId();

                // password doesn't matter, set it to a default string
                String password = "default_password";

                // user is anonymous
                String username = "Anonymous User";

                register(pseudoId, password, username);
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
        startLoading(loading_spade, loading_page);

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

                            // build document
                            HashMap<String, String> hashMap = new HashMap<>();
                            hashMap.put("id", userId);
                            hashMap.put("email", email);
                            hashMap.put("username", username);
                            hashMap.put("imageURL", "default");
                            hashMap.put("description", "No description set");
                            hashMap.put("status", "offline");

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
                        finishLoading(loading_spade, loading_page);
                    }
                });
    }
}
