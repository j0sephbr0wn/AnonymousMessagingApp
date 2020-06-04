package com.brownjs.anonymousmessagingapp;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.brownjs.anonymousmessagingapp.adapters.ChatsAdapter;
import com.brownjs.anonymousmessagingapp.adapters.MessagesAdapter;
import com.brownjs.anonymousmessagingapp.model.Chat;
import com.brownjs.anonymousmessagingapp.model.Message;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
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

public class MessageActivity extends AppCompatActivity {

    private DatabaseReference reference;

    private String userId;
    private String chatId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        // get data from intent
        chatId = getIntent().getStringExtra("chatId");
        assert chatId != null;
        String subject = getIntent().getStringExtra("subject");

        // setup common_toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(subject);
//            getSupportActionBar().setSubtitle("You are anonymous");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        userId = FirebaseAuth.getInstance().getUid();

        reference = FirebaseDatabase.getInstance().getReference("Chats").child(chatId);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Chat chat = dataSnapshot.getValue(Chat.class);
                if (chat != null) {
                    // set the subject header
                    if (getSupportActionBar() != null)
                        getSupportActionBar().setTitle(chat.getSubject());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // setup chats recyclerView
        final RecyclerView recyclerViewMessages = findViewById(R.id.recyclerView_message);
        recyclerViewMessages.setHasFixedSize(false);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
//        linearLayoutManager.setStackFromEnd(true);
        recyclerViewMessages.setLayoutManager(linearLayoutManager);

        final ArrayList<Message> messagesList = new ArrayList<>();
        final MessagesAdapter messagesAdapter = new MessagesAdapter(this, userId, new Chat(), messagesList);
        recyclerViewMessages.setAdapter(messagesAdapter);

        reference.child("messages").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                messagesList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Message message = snapshot.getValue(Message.class);

                    messagesList.add(message);
                }

                messagesAdapter.updateMessageList(messagesList);
                recyclerViewMessages.scrollToPosition(messagesList.size() - 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        final EditText txtNewMessage = findViewById(R.id.editText_message);
        ImageButton btnSend = findViewById(R.id.btn_send);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newMessage = txtNewMessage.getText().toString();
                if (newMessage.isEmpty()) {
                    Toast.makeText(MessageActivity.this, "You can't send an empty message", Toast.LENGTH_SHORT).show();
                } else {
                    sendMessage(newMessage);
                }

                txtNewMessage.setText("");
            }
        });

        markAsRead();

    }

    private void markAsRead() {
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Chat chat = dataSnapshot.getValue(Chat.class);
                if (chat != null) {
                    // mark chat as read
                    if (!chat.getLatestMessager().equals(userId)) {
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("read", true);
                        reference.updateChildren(hashMap);
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

        String sendTime = sdf.format(System.currentTimeMillis());

        HashMap<String, Object> chatHash = new HashMap<>();
        chatHash.put("latest_message", message);
        chatHash.put("latest_message_time", sendTime);
        chatHash.put("latest_messager", userId);
        chatHash.put("read", false);
        reference.updateChildren(chatHash);

        DatabaseReference messageReference = reference.child("messages");

        HashMap<String, String> messageHash = new HashMap<>();
        messageHash.put("sender", userId);
        messageHash.put("message", message);
        messageHash.put("time", sendTime);
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
