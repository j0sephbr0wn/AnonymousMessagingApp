package com.brownjs.anonymousmessagingapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.brownjs.anonymousmessagingapp.MessageActivity;
import com.brownjs.anonymousmessagingapp.ProfileActivity;
import com.brownjs.anonymousmessagingapp.R;
import com.brownjs.anonymousmessagingapp.model.Chat;
import com.brownjs.anonymousmessagingapp.model.User;
import com.bumptech.glide.Glide;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ViewHolder> {

    private Context context;

    private ArrayList<Chat> chatList;
    private ArrayList<Chat> archivedList;
    private ArrayList<User> userList;

    private ArrayList<Object> viewList;
    private ArrayList<Integer> typeList;

    private boolean archiveHidden;

    private boolean isChampion;

    public ChatsAdapter(Context context) {
        this.context = context;

        chatList = new ArrayList<>();
        archivedList = new ArrayList<>();
        userList = new ArrayList<>();

        viewList = new ArrayList<>();
        typeList = new ArrayList<>();

        buildViewLists();

        archiveHidden = true;
    }

    @Override
    public int getItemViewType(int position) {
        return typeList.get(position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case 0:
            case 2:
            case 3:
                return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_chat, parent, false), viewType);
            case 1:
                return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_divider_champions, parent, false), viewType);
            default:
                return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_divider_archive, parent, false), viewType);

        }
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        int viewType = typeList.get(position);

        PrettyTime p = new PrettyTime();

        switch (viewType) {
            // chat view
            case 0:
            case 3:
                final Chat chat = (Chat) viewList.get(position);

                final String otherUserId;


                boolean isLastRespondent;
                if (isChampion) {
                    otherUserId = chat.getInitiator();
                    isLastRespondent = chat.getLatestMessager().equals(chat.getChampion());
                } else {
                    otherUserId = chat.getChampion();
                    isLastRespondent = chat.getLatestMessager().equals(chat.getInitiator());
                }

                int chatHash = chat.getId().hashCode();

                // decide which profile image to use
                Glide.with(context)
                        .load(getDefaultImage(chatHash))
                        .into(holder.imgProfile);

                holder.txtMain.setText(chat.getSubject());

                if (!chat.isRead() && !isLastRespondent) {
                    holder.txtSub.setTypeface(holder.txtSub.getTypeface(), Typeface.BOLD);
                    String subText = "New message " + p.format(chat.getLatestMessageTime());
                    holder.txtSub.setText(subText);
                    holder.imgUnread.setVisibility(View.VISIBLE);
                } else {
                    holder.txtSub.setTypeface(holder.txtMain.getTypeface(), Typeface.NORMAL);
                    String subText = "Last message " + p.format(chat.getLatestMessageTime());
                    holder.txtSub.setText(subText);
                    holder.imgUnread.setVisibility(View.GONE);
                }

                holder.imgChevron.setVisibility(View.GONE);
                holder.imgOnline.setVisibility(View.GONE);
                holder.imgOffline.setVisibility(View.GONE);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, MessageActivity.class);
                        intent.putExtra("chatId", chat.getId());
                        intent.putExtra("subject", chat.getSubject());
                        intent.putExtra("otherUser", otherUserId);
                        context.startActivity(intent);
                    }
                });

//                if (viewType == 3) {
//                    if (archiveHidden) {
//                        holder.itemView.setVisibility(View.GONE);
//                    } else {
//                        holder.itemView.setVisibility(View.VISIBLE);
//                    }
//                }

                break;

            // user view
            case 2:
                final User user = (User) viewList.get(position);

                holder.txtMain.setText(user.getUsername());

                holder.imgUnread.setVisibility(View.GONE);
                holder.imgChevron.setVisibility(View.VISIBLE);

                if (user.getStatus().equals("online")) {
                    holder.imgOnline.setVisibility(View.VISIBLE);
                    holder.imgOffline.setVisibility(View.GONE);
                    String statusText = "Online now";
                    holder.txtSub.setText(statusText);
                } else {
                    holder.imgOnline.setVisibility(View.GONE);
                    holder.imgOffline.setVisibility(View.VISIBLE);
                    String statusText = "Offline, last seen " + p.format(user.getStatusOnlineTime());
                    holder.txtSub.setText(statusText);
                }

                if (user.getImageURL().equals("default")) {
                    Glide.with(context)
                            .load(getDefaultImage(user.getId().hashCode()))
                            .into(holder.imgProfile);
                } else {
                    Glide.with(context)
                            .load(user.getImageURL())
                            .into(holder.imgProfile);
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

            case 4:

                String dividerText;
                if (archiveHidden) {
                    dividerText = "Show archived chats (" + archivedList.size() + ")";
                } else {
                    dividerText = "Hide archived chats";
                }

                holder.txtDivider.setText(dividerText);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        archiveHidden = !archiveHidden;

                        refreshLists();
                    }
                });
        }
    }

    private int getDefaultImage(int uidHash) {
        switch (Math.abs(uidHash) % 3) {
            case 0:
                return R.drawable.spade_green;
            case 1:
                return R.drawable.spade_purple;
            default:
                return R.drawable.spade_red;
        }
    }

    @Override
    public int getItemCount() {
        return viewList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        View itemView;

        CircleImageView imgProfile;

        TextView txtMain;
        TextView txtSub;

        CircleImageView imgUnread;
        ImageView imgChevron;

        CircleImageView imgOnline;
        CircleImageView imgOffline;

        TextView txtDivider;


        private ViewHolder(@NonNull View itemView, int viewType) {
            super(itemView);
            this.itemView = itemView;
            if (viewType == 0 || viewType == 2 || viewType == 3) {

                imgProfile = itemView.findViewById(R.id.profile_image);

                txtMain = itemView.findViewById(R.id.main_text);
                txtSub = itemView.findViewById(R.id.sub_text);

                imgUnread = itemView.findViewById(R.id.unread);
                imgChevron = itemView.findViewById(R.id.chevron);

                imgOnline = itemView.findViewById(R.id.online);
                imgOffline = itemView.findViewById(R.id.offline);
            }
            else {
                txtDivider = itemView.findViewById(R.id.divider_text);
            }

        }
    }

    /**
     * type 0 = chat
     * type 1 = champions divider
     * type 2 = user
     * type 3 = archived chat
     * type 4 = archived chats divider
     */
    private void buildViewLists() {
        viewList.clear();
        typeList.clear();

        for (Chat chat : chatList) {
            viewList.add(chat);
            typeList.add(0);
        }

        if (!archivedList.isEmpty()) {
            viewList.add(4);
            typeList.add(4);

            if (!archiveHidden) {
                for (Chat chat : archivedList) {
                    viewList.add(chat);
                    typeList.add(3);
                }
            }
        }

        // if the list of champions is empty we can infer this is a champion logged in
        isChampion = userList.isEmpty();

        if (!isChampion) {

            viewList.add(1);
            typeList.add(1);

            for (User user : userList) {
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

    public void updateArchivedList(ArrayList<Chat> archivedList) {
        this.archivedList = archivedList;
        buildViewLists();
    }

    public void updateUserList(ArrayList<User> userList) {
        this.userList = userList;
        buildViewLists();
    }

    public void refreshLists() {
        buildViewLists();
    }
}

