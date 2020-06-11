package com.brownjs.anonymousmessagingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.brownjs.anonymousmessagingapp.adapters.ChatsAdapter;
import com.brownjs.anonymousmessagingapp.model.Chat;
import com.brownjs.anonymousmessagingapp.model.User;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupChatActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_chat);

        // setup common_toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }


        // get champion id from intent
        final String championId = getIntent().getStringExtra("championId");
        assert championId != null;

        // get ui elements
        final EditText txtSubject = findViewById(R.id.editText_new_subject);
        Button btnNewMessage = findViewById(R.id.btn_new_message);

        // get champion information
        FirebaseDatabase.getInstance().getReference("Users")
                .child(championId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        User champion = dataSnapshot.getValue(User.class);
                        assert champion != null;

                        CircleImageView imgProfile = findViewById(R.id.profile_image);
                        TextView txtChampionName = findViewById(R.id.champion_name);

                        Glide.with(getApplicationContext())
                                .load(champion.getImageURL())
                                .into(imgProfile);

                        txtChampionName.setText(champion.getUsername());

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


        // set on click listener
        btnNewMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get subject
                String subject = txtSubject.getText().toString();

                // check if empty
                if (subject.isEmpty()) {
                    Toast.makeText(SetupChatActivity.this, "Please enter a subject.", Toast.LENGTH_SHORT).show();
                }
                // if not empty create new chat
                else {
                    // change title of toolbar
                    if (getSupportActionBar() != null) {
                        getSupportActionBar().setTitle(subject);
                    }

                    // hide the 'choose subject' layout
                    findViewById(R.id.layout_subject).setVisibility(View.GONE);

                    // get the current Firebase user
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    assert currentUser != null;

                    // date formatter
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.UK);

                    // get next chat key
                    DatabaseReference chatsReference = FirebaseDatabase.getInstance().getReference().child("Chats");
                    String chatKey = chatsReference.push().getKey();
                    assert chatKey != null;

                    // build new document
                    HashMap<String, Object> newChat = new HashMap<>();
                    newChat.put("id", chatKey);
                    newChat.put("champion", championId);
                    newChat.put("initiator", currentUser.getUid());
                    newChat.put("latest_message", "No messages yet");
                    newChat.put("latest_message_time", sdf.format(System.currentTimeMillis()));
                    newChat.put("latest_messager", championId);
                    newChat.put("read", false);
                    newChat.put("subject", subject);

                    // put new chat in document store
                    chatsReference.child(chatKey).setValue(newChat);

                    // add chat to user documents
                    DatabaseReference currentUserChatListRef = FirebaseDatabase.getInstance().getReference("Users")
                            .child(currentUser.getUid())
                            .child("chatList");
                    DatabaseReference championChatListRef = FirebaseDatabase.getInstance().getReference("Users")
                            .child(championId)
                            .child("chatList");

                    HashMap<String, String> newChatListItem = new HashMap<>();
//                    newChatListItem.put("id", chatKey);

                    currentUserChatListRef.push().setValue(newChatListItem);
                    championChatListRef.push().setValue(newChatListItem);

                    // start new activity and clear from back-stack
                    Intent intent = new Intent(SetupChatActivity.this, MessageActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
                    intent.putExtra("chatId", chatKey);
                    intent.putExtra("subject", subject);
                    intent.putExtra("otherUser", championId);
                    startActivity(intent);
                    finish();
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
