package com.brownjs.anonymousmessagingapp;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.brownjs.anonymousmessagingapp.adapters.MessagesAdapter;
import com.brownjs.anonymousmessagingapp.model.Chat;
import com.brownjs.anonymousmessagingapp.model.Message;
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

public class MessageActivity extends AppCompatActivity {

    private String chatId;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        // get data from intent
        chatId = getIntent().getStringExtra("chatId");
        String subject = getIntent().getStringExtra("subject");

        // set current user id
        userId = FirebaseAuth.getInstance().getUid();

        // only proceed if data from the intent is present
        if (chatId != null && subject != null) {

            // setup common_toolbar
            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(subject);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }

            // setup messages recyclerView
            final RecyclerView recyclerViewMessages = findViewById(R.id.recyclerView_message);
            recyclerViewMessages.setHasFixedSize(false);
            recyclerViewMessages.setLayoutManager(new LinearLayoutManager(this));

            final ArrayList<Message> messagesList = new ArrayList<>();
            final MessagesAdapter messagesAdapter = new MessagesAdapter(this, userId, messagesList);
            recyclerViewMessages.setAdapter(messagesAdapter);

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
                            messagesAdapter.updateMessageList(messagesList);

                            // scroll to most recent message
                            recyclerViewMessages.scrollToPosition(messagesList.size() - 1);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

            // set listener for sending new message
            ImageButton btnSend = findViewById(R.id.btn_send);
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

    private void markAsRead() {
        final DatabaseReference chatReference = FirebaseDatabase.getInstance().getReference("Chats")
                .child(chatId);

        chatReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Chat chat = dataSnapshot.getValue(Chat.class);
                if (chat != null) {
                    // mark chat as read
                    if (!chat.getLatestMessager().equals(userId)) {
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("read", true);
                        chatReference.updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

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

    @Override
    protected void onPause() {
        super.onPause();
        markAsRead();
    }
}
