package com.brownjs.anonymousmessagingapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.brownjs.anonymousmessagingapp.R;
import com.brownjs.anonymousmessagingapp.model.Chat;
import com.brownjs.anonymousmessagingapp.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ViewHolder> {

    private Context context;

    private ArrayList<Chat> chatList;
    private ArrayList<User> userList;

    private ArrayList<Object> viewList;
    private ArrayList<Integer> typeList;

    public ChatsAdapter(Context context, ArrayList<Chat> chatList, ArrayList<User> userList) {
        this.context = context;
        this.chatList = chatList;
        this.userList = userList;

        viewList = new ArrayList<>();
        typeList = new ArrayList<>();

        buildViewLists();
    }

    @Override
    public int getItemViewType(int position) {
        return typeList.get(position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 0 || viewType == 2) {
            return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_chat, parent, false), viewType);
        }

        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_divider, parent, false), viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int viewType = typeList.get(position);

        switch (viewType) {
            case 0:
                Chat chat = (Chat) viewList.get(position);

                holder.username.setText(chat.getSubject());
                holder.lastMessage.setText(chat.getInitiator());
                holder.status.setText(chat.getRespondent());

                holder.status.setVisibility(View.VISIBLE);
                holder.chevron.setVisibility(View.GONE);
                holder.online.setVisibility(View.GONE);
                holder.offline.setVisibility(View.GONE);

                break;

            case 2:
                User user = (User) viewList.get(position);

                holder.username.setText(user.getUsername());
                holder.lastMessage.setText(user.getEmail());

                holder.status.setVisibility(View.GONE);
                holder.chevron.setVisibility(View.VISIBLE);

                if (user.getStatus().equals("online")) {
                    holder.online.setVisibility(View.VISIBLE);
                    holder.offline.setVisibility(View.GONE);
                } else {
                    holder.online.setVisibility(View.GONE);
                    holder.offline.setVisibility(View.VISIBLE);
                }

                break;
        }

//        if (viewType != 1) {
//            holder.username.setText("Joseph Brown" + position);
//
//            if (position < 6) {
//
//            } else {
//
//            }
//
//            if (viewType == 0) {
//                holder.lastMessage.setText("Some last message");
//                holder.status.setText("14:33");
//                holder.chevron.setVisibility(View.GONE);
//            }
//
//            if (viewType == 2) {
//                holder.lastMessage.setText("Tap for more information");
//                holder.status.setText("");
//                holder.chevron.setVisibility(View.VISIBLE);
//            }
//        }
    }

    @Override
    public int getItemCount() {
        return viewList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView username;
        TextView status;
        TextView lastMessage;

        ImageView chevron;

        CircleImageView online;
        CircleImageView offline;


        private ViewHolder(@NonNull View itemView, int viewType) {
            super(itemView);
            if (viewType == 0 || viewType == 2) {
                username = itemView.findViewById(R.id.username);
                status = itemView.findViewById(R.id.status);
                lastMessage = itemView.findViewById(R.id.last_message);

                chevron = itemView.findViewById(R.id.chevron);

                online = itemView.findViewById(R.id.online);
                offline = itemView.findViewById(R.id.offline);
            }

        }
    }

    /**
     * type 0 = chat
     * type 1 = divider
     * type 2 = user
     */
    private void buildViewLists() {
        viewList.clear();
        typeList.clear();

        for (Chat chat: chatList) {
            viewList.add(chat);
            typeList.add(0);
        }

        viewList.add(1);
        typeList.add(1);

        for (User user: userList) {
            viewList.add(user);
            typeList.add(2);
        }

        notifyDataSetChanged();
    }

    public void updateChatList(ArrayList<Chat> chatList) {
        this.chatList = chatList;
        buildViewLists();
    }

    public void updateUserList(ArrayList<User> userList) {
        this.userList = userList;
        buildViewLists();
    }
}

