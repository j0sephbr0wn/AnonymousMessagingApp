package com.brownjs.anonymousmessagingapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.brownjs.anonymousmessagingapp.R;
import com.brownjs.anonymousmessagingapp.adapters.ChatsAdapter;
import com.brownjs.anonymousmessagingapp.model.Chat;
import com.brownjs.anonymousmessagingapp.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    private FirebaseUser firebaseUser;
//    private DatabaseReference reference;

    private boolean isChampion;

    private ArrayList<Chat> chatList = new ArrayList<>();
    private ArrayList<Chat> archivedList = new ArrayList<>();
    private ArrayList<User> userList = new ArrayList<>();

    private ChatsAdapter chatsAdapter;

    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chats, container, false);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        assert firebaseUser != null;

        String email = firebaseUser.getEmail();
        assert email != null;
        isChampion = (email.endsWith("capgemini.com"));

        getChatList();
        if (!isChampion) getUserList();

        // setup chats recyclerView
        RecyclerView recyclerViewChats = view.findViewById(R.id.recyclerView_chats);
        recyclerViewChats.setHasFixedSize(true);
        recyclerViewChats.setLayoutManager(new LinearLayoutManager(getContext()));

        chatsAdapter = new ChatsAdapter(getContext());
        recyclerViewChats.setAdapter(chatsAdapter);

        return view;
    }

    private void getChatList() {

        assert firebaseUser != null;
        String userId = firebaseUser.getUid();

        Query query;

        if (isChampion) {
            query = FirebaseDatabase.getInstance().getReference("Chats")
                    .orderByChild("champion")
                    .equalTo(userId);
        } else {
            query = FirebaseDatabase.getInstance().getReference("Chats")
                    .orderByChild("initiator")
                    .equalTo(userId);
        }

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatList.clear();
                archivedList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    assert chat != null;

                    if (chat.isArchived()) {
                        archivedList.add(chat);
                    }
                    else {
                        chatList.add(chat);
                    }
                }

                Collections.sort(chatList);
                Collections.sort(archivedList);

                chatsAdapter.updateChatList(chatList);
                chatsAdapter.updateArchivedList(archivedList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getUserList() {
        Query query = FirebaseDatabase.getInstance().getReference("Users")
                .orderByChild("champion")
                .equalTo(true);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<User> onlineList = new ArrayList<>();
                ArrayList<User> offlineList = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    assert user != null;
                    if (user.getStatus().equals("online")) {
                        onlineList.add(user);
                    } else {
                        offlineList.add(user);
                    }
                }

                userList.clear();
                userList.addAll(onlineList);
                userList.addAll(offlineList);

                chatsAdapter.updateUserList(userList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        chatsAdapter.refreshLists();
    }
}
