package com.ssg.aintstagram;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class HistoryRecyclerAdapter extends RecyclerView.Adapter<HistoryRecyclerAdapter.ItemViewHolder>  {
    private Context context;
    private ArrayList<HistoryCard> cards;
    private OnCardListener onCardListener;

    public HistoryRecyclerAdapter(ArrayList<HistoryCard> cards, Context context, OnCardListener onCardListener){
        this.cards = cards;
        this.context = context;
        this.onCardListener = onCardListener;
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView imageView;
        TextView history;

        OnCardListener onCardListener;

        public ItemViewHolder(View itemView, OnCardListener onCardListener){
            super(itemView);

            imageView = (ImageView) itemView.findViewById(R.id.history_user_profile);
            history = (TextView) itemView.findViewById(R.id.history_info);

            this.onCardListener = onCardListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onCardListener.onCardClick(getAdapterPosition());
        }
    }

    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_history_card,parent,false);
        return new ItemViewHolder(view, onCardListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position){
        holder.imageView.setImageBitmap(cards.get(position).getImg());
        holder.history.setText(cards.get(position).getTypeInfo()+cards.get(position).getDate());
    }

    @Override
    public int getItemCount(){
        return cards.size();
    }

    public void setItems(ArrayList<HistoryCard> cards){
        this.cards = cards;
    }

    public interface OnCardListener{
        void onCardClick(int pos);
    }
}
