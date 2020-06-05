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

import java.util.ArrayList;

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
            return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_message_left, parent, false));
        }

        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_message_right, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

//        PrettyTime p = new PrettyTime();

        Message message = messageList.get(position);

        holder.txtMessage.setText(message.getMessage());

//        if (position + 1 == messageList.size()) {
//            holder.txtSeen.setText(message.getSender());
//            holder.txtSeen.setVisibility(View.VISIBLE);
//        } else {
//            holder.txtSeen.setVisibility(View.GONE);
//        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtMessage;
        TextView txtSeen;

        private ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtMessage = itemView.findViewById(R.id.textView_message);
            txtSeen = itemView.findViewById(R.id.textView_seen);

        }
    }

    public void updateMessageList(ArrayList<Message> messageList) {
        this.messageList = messageList;
        notifyDataSetChanged();
    }
}

