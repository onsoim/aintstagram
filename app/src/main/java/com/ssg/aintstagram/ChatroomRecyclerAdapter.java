package com.ssg.aintstagram;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
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
                            onChatroomListener.onChatroomClick(getAdapterPosition(), 1);
                            break;
                    }
                }
            };

            View.OnLongClickListener longClickListener = new View.OnLongClickListener(){
                @Override
                public boolean onLongClick(View v) {
                    onChatroomListener.onChatroomClick(getAdapterPosition(), 2);
                    return false;
                }
            };

            btn_user.setOnClickListener(listener);
            btn_user.setOnLongClickListener(longClickListener);

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
        holder.btn_user.setText(chatrooms.get(position).getName());
        BitmapDrawable drawableLeft = new BitmapDrawable(chatrooms.get(position).getImg());
        holder.btn_user.setCompoundDrawablesWithIntrinsicBounds(drawableLeft, null, null, null);
    }

    @Override
    public int getItemCount() {
        return chatrooms.size();
    }

    public interface OnChatroomListener{
        void onChatroomClick(int pos, int choice);
    }
}
