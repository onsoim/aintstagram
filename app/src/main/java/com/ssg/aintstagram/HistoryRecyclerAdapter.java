package com.ssg.aintstagram;

import android.content.Context;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class HistoryRecyclerAdapter extends RecyclerView.Adapter<HistoryRecyclerAdapter.ItemViewHolder>{
    private Context context;
    private ArrayList<HistoryCard> cards;

    public HistoryRecyclerAdapter(ArrayList<HistoryCard> cards, Context context){
        this.cards = cards;
        this.context = context;
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView history;
        TextView btn_remove;

        public ItemViewHolder(View itemView){
            super(itemView);

            imageView = (ImageView) itemView.findViewById(R.id.history_user_profile);
            history = (TextView) itemView.findViewById(R.id.history_info);
            btn_remove = (TextView) itemView.findViewById(R.id.history_remove);
        }
    }

    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_history_card,parent,false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position){
        holder.imageView.setImageBitmap(cards.get(position).getImg());
        holder.history.setText(cards.get(position).getUsername() + cards.get(position).getText() + " " + cards.get(position).getDate());

        if(cards.get(position).getBtn()) {
            holder.btn_remove.setVisibility(View.VISIBLE);
        } else {
            holder.btn_remove.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount(){
        return cards.size();
    }

    public void setItems(ArrayList<HistoryCard> cards){
        this.cards = cards;
    }
}
