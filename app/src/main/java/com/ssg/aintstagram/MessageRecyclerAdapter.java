package com.ssg.aintstagram;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;


public class MessageRecyclerAdapter extends RecyclerView.Adapter<MessageRecyclerAdapter.ItemViewHolder> {
    private Context context;
    private ArrayList<Message> messages;

    MessageRecyclerAdapter(ArrayList<Message> messages, Context context){
        this.messages = messages;
        this.context = context;
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView message;
        LinearLayout message_side;

        public ItemViewHolder(View itemView){
            super(itemView);

            message = (TextView) itemView.findViewById(R.id.message);
            message_side = (LinearLayout) itemView.findViewById(R.id.message_side);
        }
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_message_detail, parent,false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        holder.message.setText(messages.get(position).getText());

        if (messages.get(position).getMine()) {
            holder.message_side.setGravity(Gravity.LEFT);
            holder.message.setBackgroundColor(R.color.skyblue);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }
}
