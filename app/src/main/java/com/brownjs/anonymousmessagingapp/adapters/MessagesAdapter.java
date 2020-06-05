package com.brownjs.anonymousmessagingapp.adapters;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.brownjs.anonymousmessagingapp.R;
import com.brownjs.anonymousmessagingapp.model.Message;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.ViewHolder> {

    private Context context;

    private String userId;
    private ArrayList<Message> messageList;

    public MessagesAdapter(Context context, String userId, ArrayList<Message> messageList) {
        this.context = context;
        this.userId = userId;
        this.messageList = messageList;
    }

    @Override
    public int getItemViewType(int position) {
        int type = 0;

        if (messageList.get(position).getSender().equals(userId))
            type = 1;

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

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.UK);
        SimpleDateFormat dayFormat = new SimpleDateFormat("dd MMM", Locale.UK);

        Message message = messageList.get(position);

        holder.txtMessage.setText(message.getMessage());

        // format time
        String currentDay = dayFormat.format(System.currentTimeMillis());
        String timeText = dayFormat.format(message.getDate());

        if (currentDay.equals(timeText))
            timeText = timeFormat.format(message.getDate());

        holder.txtTime.setText(timeText);

        if (getItemViewType(position) == 0) {
            if (getItemViewType(position - 1) == 1) {
                holder.imgProfile.setVisibility(View.VISIBLE);
                holder.txtName.setVisibility(View.VISIBLE);
            } else {
                holder.imgProfile.setVisibility(View.INVISIBLE);
                holder.txtName.setVisibility(View.GONE);
            }
        } else {
            if (position + 1 == messageList.size()) {
                holder.txtSeen.setVisibility(View.VISIBLE);
            } else {
                holder.txtSeen.setVisibility(View.GONE);
            }
        }
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

    public void updateMessageList(ArrayList<Message> messageList) {
        this.messageList = messageList;
        notifyDataSetChanged();
    }
}

