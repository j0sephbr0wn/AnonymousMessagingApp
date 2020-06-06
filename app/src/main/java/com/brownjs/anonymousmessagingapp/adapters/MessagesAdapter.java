package com.brownjs.anonymousmessagingapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.brownjs.anonymousmessagingapp.R;
import com.brownjs.anonymousmessagingapp.model.Message;
import com.brownjs.anonymousmessagingapp.model.User;
import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.ViewHolder> {

    private Context context;

    private String userId;
    private ArrayList<Message> messageList;

    private User otherUser;
    private boolean seen;

    public MessagesAdapter(Context context, String userId, ArrayList<Message> messageList) {
        this.context = context;
        this.userId = userId;
        this.messageList = messageList;
    }

    @Override
    public int getItemViewType(int position) {
        int type = 100;

        if (position < messageList.size() && position >= 0) {
            type = 0;
            if (messageList.get(position).getSender().equals(userId))
                type = 1;
        }

        return type;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 0) {
            return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_message_left, parent, false), viewType);
        }

        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_message_right, parent, false), viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Message message = messageList.get(position);

        holder.txtMessage.setText(message.getMessage());

        // display message times
        String messageTimeText = formatMessageTime(message);
        String nextTimeText = "";
        holder.txtTime.setVisibility(View.GONE);
        if (position + 1 != messageList.size())
            nextTimeText = formatMessageTime(messageList.get(position + 1));
        if (getItemViewType(position) != getItemViewType(position + 1)
                || !messageTimeText.equals(nextTimeText)) {
            holder.txtTime.setText(messageTimeText);
            holder.txtTime.setVisibility(View.VISIBLE);
        }

        if (getItemViewType(position) == 0) {
            if (getItemViewType(position - 1) == 1) {
                if (otherUser.isChampion()) {
                    Glide.with(context)
                            .load(otherUser.getImageURL())
                            .into(holder.imgProfile);
                    holder.txtName.setText(otherUser.getUsername());
                } else {
                    holder.txtName.setText(R.string.DEFAULT_USERNAME);
                }
                holder.imgProfile.setVisibility(View.VISIBLE);


                holder.txtName.setVisibility(View.VISIBLE);
            } else {
                holder.imgProfile.setVisibility(View.INVISIBLE);
                holder.txtName.setVisibility(View.GONE);
            }
        }
        // type 1
        else {
            if (position + 1 == messageList.size()) {
                String seenText = "Delivered";
                if (seen)
                    seenText = "Seen";
                holder.txtSeen.setText(seenText);
                holder.txtSeen.setVisibility(View.VISIBLE);
            } else {
                holder.txtSeen.setVisibility(View.GONE);
            }
        }
    }

    private String formatMessageTime(Message message) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.UK);
        SimpleDateFormat dayFormat = new SimpleDateFormat("dd MMM", Locale.UK);

        // format time
        String currentDay = dayFormat.format(System.currentTimeMillis());
        String timeText = dayFormat.format(message.getDate());

        if (currentDay.equals(timeText))
            timeText = timeFormat.format(message.getDate());

        return timeText;
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        CircleImageView imgProfile;
        TextView txtName;
        TextView txtMessage;
        TextView txtTime;
        TextView txtSeen;

        private ViewHolder(@NonNull View itemView, int viewType) {
            super(itemView);

            if (viewType == 0) {
                imgProfile = itemView.findViewById(R.id.profile_image);
                txtName = itemView.findViewById(R.id.textView_name);
                txtMessage = itemView.findViewById(R.id.textView_message);
                txtTime = itemView.findViewById(R.id.textView_time);
            } else {
                txtMessage = itemView.findViewById(R.id.textView_message);
                txtSeen = itemView.findViewById(R.id.textView_seen);
                txtTime = itemView.findViewById(R.id.textView_time);
            }
        }
    }

    public void updateMessages(ArrayList<Message> messageList) {
        this.messageList = messageList;
        notifyDataSetChanged();
    }

    public void setOtherUser(User otherUser) {
        this.otherUser = otherUser;
        notifyDataSetChanged();
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
        notifyDataSetChanged();
    }
}

