package com.ssg.aintstagram;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;


public class MessageRecyclerAdapter extends RecyclerView.Adapter<MessageRecyclerAdapter.ItemViewHolder> {
    private Context context;
    private ArrayList<Message> messages;
    private OnMessageListener onMessageListener;

    MessageRecyclerAdapter(ArrayList<Message> messages, Context context, OnMessageListener onMessageListener){
        this.messages = messages;
        this.context = context;
        this.onMessageListener = onMessageListener;
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView message;
        OnMessageListener onMessageListener;

        public ItemViewHolder(View itemView, final OnMessageListener onMessageListener){
            super(itemView);

            message = (TextView) itemView.findViewById(R.id.message);

            this.onMessageListener = onMessageListener;

            View.OnLongClickListener onLongClickListener = new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    onMessageListener.onMessageClick(getAdapterPosition());
                    return false;
                }
            };

            message.setOnLongClickListener(onLongClickListener);
        }
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_message_detail, parent,false);
        return new ItemViewHolder(view, onMessageListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        holder.message.setText(messages.get(position).getText());

        if (messages.get(position).getMine()) {
            holder.message.setGravity(Gravity.LEFT);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public interface OnMessageListener {
        void onMessageClick(int pos);
    }
}
