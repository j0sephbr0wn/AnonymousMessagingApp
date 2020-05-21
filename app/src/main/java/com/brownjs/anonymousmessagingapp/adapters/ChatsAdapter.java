package com.brownjs.anonymousmessagingapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.brownjs.anonymousmessagingapp.MainActivity;
import com.brownjs.anonymousmessagingapp.MessageActivity;
import com.brownjs.anonymousmessagingapp.ProfileActivity;
import com.brownjs.anonymousmessagingapp.R;
import com.brownjs.anonymousmessagingapp.model.Chat;
import com.brownjs.anonymousmessagingapp.model.User;
import com.bumptech.glide.Glide;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ViewHolder> {

    private Context context;

    private ArrayList<Chat> chatList;
    private ArrayList<User> userList;

    private ArrayList<Object> viewList;
    private ArrayList<Integer> typeList;

    private boolean isChampion;

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

        PrettyTime p = new PrettyTime();

        switch (viewType) {
            // chat view
            case 0:
                Chat chat = (Chat) viewList.get(position);

                int userHash;
                boolean isLastRespondent;
                if (isChampion) {
                    userHash = chat.getInitiator().hashCode();
                    isLastRespondent = chat.getLatestMessager().equals(chat.getChampion());
                } else {
                    userHash = chat.getChampion().hashCode();
                    isLastRespondent = chat.getLatestMessager().equals(chat.getInitiator());
                }

                // decide which profile image to use
                Glide.with(context)
                        .load(getDefaultImage(userHash))
                        .into(holder.profileImage);

                holder.mainText.setText(chat.getSubject());
//                holder.smallText.setText();

                if (!chat.isRead() && !isLastRespondent) {
//                    holder.mainText.setTypeface(holder.mainText.getTypeface(), Typeface.BOLD);
                    holder.subText.setTypeface(holder.subText.getTypeface(), Typeface.BOLD);
                    String subText = "New message " + p.format(chat.getLatestMessageTime());
                    holder.subText.setText(subText);
                    holder.unread.setVisibility(View.VISIBLE);
                } else {
//                    holder.mainText.setTypeface(holder.mainText.getTypeface(), Typeface.NORMAL);
                    holder.subText.setTypeface(holder.mainText.getTypeface(), Typeface.NORMAL);
                    String subText = "Last updated " + p.format(chat.getLatestMessageTime());
                    holder.subText.setText(subText);
                    holder.unread.setVisibility(View.GONE);
                }

//                holder.smallText.setVisibility(View.GONE);
                holder.chevron.setVisibility(View.GONE);
                holder.online.setVisibility(View.GONE);
                holder.offline.setVisibility(View.GONE);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //TODO
                    }
                });

                break;

            // user view
            case 2:
                final User user = (User) viewList.get(position);

                holder.mainText.setText(user.getUsername());

//                holder.smallText.setVisibility(View.GONE);
                holder.unread.setVisibility(View.GONE);
                holder.chevron.setVisibility(View.VISIBLE);

                if (user.getStatus().equals("online")) {
                    holder.online.setVisibility(View.VISIBLE);
                    holder.offline.setVisibility(View.GONE);
                    String statusText = "Online now";
                    holder.subText.setText(statusText);
                } else {
                    holder.online.setVisibility(View.GONE);
                    holder.offline.setVisibility(View.VISIBLE);
                    String statusText = "Offline";
                    holder.subText.setText(statusText);
                }

                if (user.getImageURL().equals("default")) {
                    Glide.with(context)
                            .load(getDefaultImage(user.getId().hashCode()))
                            .into(holder.profileImage);
                } else {
                    Glide.with(context)
                            .load(user.getImageURL())
                            .into(holder.profileImage);
                }

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, ProfileActivity.class);
                        intent.putExtra("userId", user.getId());
                        context.startActivity(intent);
                    }
                });

                break;
        }
    }

    private int getDefaultImage(int uidHash) {
        switch (Math.abs(uidHash) % 3) {
            case 0: return R.drawable.spade_green;
            case 1: return R.drawable.spade_purple;
            default: return R.drawable.spade_red;
        }
    }

    @Override
    public int getItemCount() {
        return viewList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        CircleImageView profileImage;

        TextView mainText;
        TextView subText;
//        TextView smallText;

        CircleImageView unread;
        ImageView chevron;

        CircleImageView online;
        CircleImageView offline;


        private ViewHolder(@NonNull View itemView, int viewType) {
            super(itemView);
            if (viewType == 0 || viewType == 2) {
                profileImage = itemView.findViewById(R.id.profile_image);

                mainText = itemView.findViewById(R.id.main_text);
                subText = itemView.findViewById(R.id.sub_text);
//                smallText = itemView.findViewById(R.id.small_text);

                unread = itemView.findViewById(R.id.unread);
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

        // if the list of champions is empty we can infer this is a champion logged in
        isChampion = userList.isEmpty();

        if (!isChampion) {

            viewList.add(1);
            typeList.add(1);

            for (User user: userList) {
                viewList.add(user);
                typeList.add(2);
            }
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

    public void refreshLists() {
        notifyDataSetChanged();
    }
}

