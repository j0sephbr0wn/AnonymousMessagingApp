package com.brownjs.anonymousmessagingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class MessageActivity extends AppCompatActivity {

    private EditText editText_new_subject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        // setup common_toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("New message");
            getSupportActionBar().setSubtitle("You are anonymous");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // get ui elements
        editText_new_subject = findViewById(R.id.editText_new_subject);
        Button btn_new_message = findViewById(R.id.btn_new_message);

        btn_new_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String subject = editText_new_subject.getText().toString();

                if (subject.isEmpty()) {
                    Toast.makeText(MessageActivity.this, "Please enter a subject.", Toast.LENGTH_SHORT).show();
                } else {
                    if (getSupportActionBar() != null) {
                        getSupportActionBar().setTitle(subject);
                    }

                    findViewById(R.id.layout_subject).setVisibility(View.GONE);

                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

                    HashMap<String, Object> newChat = new HashMap<>();
                    newChat.put("initiator", currentUser.getUid());
                    newChat.put("respondent", "none");
                    newChat.put("subject", subject);

                    DatabaseReference chatsReference = FirebaseDatabase.getInstance().getReference().child("Chats");
                    String chatsKey = chatsReference.push().getKey();
                    chatsReference.child(chatsKey).setValue(newChat);

                    DatabaseReference chatListRef = FirebaseDatabase.getInstance().getReference("Users")
                            .child(currentUser.getUid())
                            .child("chatList");
                    HashMap<String, String> newChatListItem = new HashMap<>();
                    newChatListItem.put("id", chatsKey);
                    chatListRef.push().setValue(newChatListItem);

                }
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
}
