package com.ssg.aintstagram;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;


public class ChatroomRecyclerAdapter extends RecyclerView.Adapter<ChatroomRecyclerAdapter.ItemViewHolder> {
    private Context context;
    private ArrayList<Chatroom> chatrooms;
    private OnChatroomListener onChatroomListener;

    ChatroomRecyclerAdapter(ArrayList<Chatroom> chatrooms, Context context, OnChatroomListener onChatroomListener){
        this.chatrooms = chatrooms;
        this.context = context;
        this.onChatroomListener = onChatroomListener;
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        Button btn_user;

        OnChatroomListener onChatroomListener;

        public ItemViewHolder(View itemView, final OnChatroomListener onChatroomListener){
            super(itemView);

            btn_user = (Button) itemView.findViewById(R.id.btn_user);

            this.onChatroomListener = onChatroomListener;

            View.OnClickListener listener = new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    switch (v.getId()){
                        case R.id.btn_user:
                            onChatroomListener.onChatroomClick(getAdapterPosition());
                            break;
                    }
                }
            };

            btn_user.setOnClickListener(listener);
        }
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_chatroom_detail, parent,false);
        return new ItemViewHolder(view, onChatroomListener);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
//        holder.btn_user.setCompoundDrawablesRelativeWithIntrinsicBounds(new BitmapDrawable(chatrooms.get(position).getProfile()), null, null, null);
        holder.btn_user.setText(chatrooms.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return chatrooms.size();
    }

    public interface OnChatroomListener{
        void onChatroomClick(int pos);
    }
}
