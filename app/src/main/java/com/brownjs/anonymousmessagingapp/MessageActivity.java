package com.brownjs.anonymousmessagingapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.brownjs.anonymousmessagingapp.adapters.MessagesAdapter;
import com.brownjs.anonymousmessagingapp.model.Chat;
import com.brownjs.anonymousmessagingapp.model.Message;
import com.brownjs.anonymousmessagingapp.model.User;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
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

public class MessageActivity extends MyAppActivity {

    private String chatId;
    private String userId;

    // vars for setting and releasing listener to read messages
    private DatabaseReference markAsReadReference;
    private ValueEventListener newMessageListener;

    private MessagesAdapter messagesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        // get data from intent
        chatId = getIntent().getStringExtra("chatId");
        final String subject = getIntent().getStringExtra("subject");
        final String otherUserId = getIntent().getStringExtra("otherUser");

        // set current user id
        userId = FirebaseAuth.getInstance().getUid();

        // only proceed if all data from the intent is present
        if (chatId != null && subject != null && otherUserId != null) {

            // setup messages recyclerView
            final RecyclerView recyclerViewMessages = findViewById(R.id.recyclerView_message);
            recyclerViewMessages.setHasFixedSize(false);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            linearLayoutManager.setStackFromEnd(true);
//            recyclerViewMessages.setLayoutManager(new LinearLayoutManager(this));
            recyclerViewMessages.setLayoutManager(linearLayoutManager);

            final ArrayList<Message> messagesList = new ArrayList<>();
            messagesAdapter = new MessagesAdapter(this, userId, messagesList);

            // set listener for other user
            FirebaseDatabase.getInstance().getReference("Users")
                    .child(otherUserId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            final User otherUser = dataSnapshot.getValue(User.class);
                            assert otherUser != null;

                            messagesAdapter.setOtherUser(otherUser);
                            recyclerViewMessages.setAdapter(messagesAdapter);
                            String otherUsername = null;

                            if (otherUser.isChampion()) {
                                otherUsername = "Talking to " + otherUser.getUsername();
                            }
                            CircleImageView imgProfile = findViewById(R.id.profile_image);

                            if (otherUser.isChampion()) {
                                Glide.with(getApplicationContext())
                                        .load(otherUser.getImageURL())
                                        .into(imgProfile);

                                // set listener for profile image
                                imgProfile.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent = new Intent(MessageActivity.this, ProfileActivity.class);
                                        intent.putExtra("userId", otherUserId);
                                        startActivity(intent);
                                    }
                                });
                            }

                            // setup common_toolbar
                            Toolbar toolbar = findViewById(R.id.toolbar);
                            setSupportActionBar(toolbar);
                            if (getSupportActionBar() != null) {
                                getSupportActionBar().setTitle(subject);
                                getSupportActionBar().setSubtitle(otherUsername);
                                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

            // set listener for other user status changes
            FirebaseDatabase.getInstance().getReference("Users")
                    .child(otherUserId)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            User otherUser = dataSnapshot.getValue(User.class);
                            assert otherUser != null;

                            if (otherUser.isChampion()) {

                                CircleImageView imgOnline = findViewById(R.id.online);
                                CircleImageView imgOffline = findViewById(R.id.offline);

                                if (otherUser.getStatus().equals("online")) {
                                    imgOnline.setVisibility(View.VISIBLE);
                                    imgOffline.setVisibility(View.GONE);
                                } else {
                                    imgOnline.setVisibility(View.GONE);
                                    imgOffline.setVisibility(View.VISIBLE);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

            // set listener for receiving new messages
            FirebaseDatabase.getInstance().getReference("Chats")
                    .child(chatId)
                    .child("messages")
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            messagesList.clear();

                            // add each message to array
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                Message message = snapshot.getValue(Message.class);
                                messagesList.add(message);
                            }

                            // update recyclerView
                            messagesAdapter.updateMessages(messagesList);

                            // scroll to most recent message
                            recyclerViewMessages.scrollToPosition(messagesList.size() - 1);

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

            // set listener for sending new message
            ImageView btnSend = findViewById(R.id.btn_send);
            btnSend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // get text from user
                    EditText txtNewMessage = findViewById(R.id.editText_message);
                    String newMessage = txtNewMessage.getText().toString();

                    //check if anything to send
                    if (!newMessage.isEmpty())
                        sendMessage(newMessage);

                    //reset for next message
                    txtNewMessage.setText("");
                }
            });

            // mark this chat as read
            markAsRead();
        }
    }

    /**
     * Setup a listen to mark the chat as read. Any message received while in this activity will also be marked as read.
     */
    private void markAsRead() {
        // assign reference
        markAsReadReference = FirebaseDatabase.getInstance().getReference("Chats")
                .child(chatId);

        // assign listener
        newMessageListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Chat chat = dataSnapshot.getValue(Chat.class);
                if (chat != null) {
                    // mark chat as read if current user did not send the most recent message
                    if (!chat.getLatestMessager().equals(userId)) {
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("read", true);
                        markAsReadReference.updateChildren(hashMap);

                    } else {
                        // if current user sent the latest message, update adapter to the read value
                        messagesAdapter.setSeen(chat.isRead());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        // apply listener to reference
        markAsReadReference.addValueEventListener(newMessageListener);
    }

    /**
     * Build the necessary data structures and put them in the document store
     *
     * @param message to be inserted
     */
    private void sendMessage(String message) {

        // date formatter
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.UK);

        // get current time
        String sendTime = sdf.format(System.currentTimeMillis());

        //get database references
        DatabaseReference chatReference = FirebaseDatabase.getInstance().getReference("Chats").child(chatId);
        DatabaseReference messageReference = chatReference.child("messages");

        // build update for chat
        HashMap<String, Object> chatHash = new HashMap<>();
        chatHash.put("latest_message", message);
        chatHash.put("latest_message_time", sendTime);
        chatHash.put("latest_messager", userId);
        chatHash.put("read", false);

        // build new message
        HashMap<String, String> messageHash = new HashMap<>();
        messageHash.put("sender", userId);
        messageHash.put("message", message);
        messageHash.put("time", sendTime);

        // put new data
        chatReference.updateChildren(chatHash);
        messageReference.push().setValue(messageHash);
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
     * {@inheritDoc}
     */
    @Override
    protected void onPause() {
        super.onPause();

        // remove listener to mark as read when leaving this activity
        markAsReadReference.removeEventListener(newMessageListener);
    }
}
