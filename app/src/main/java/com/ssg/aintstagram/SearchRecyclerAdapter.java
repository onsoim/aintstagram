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

public class SearchRecyclerAdapter extends RecyclerView.Adapter<SearchRecyclerAdapter.ItemViewHolder>  {
    private Context context;
    private ArrayList<SearchCard> cards;
    private OnCardListener onCardListener;
    private int lastPosition = -1;

    public SearchRecyclerAdapter(ArrayList<SearchCard> cards, Context context, OnCardListener onCardListener){
        this.cards = cards;
        this.context = context;
        this.onCardListener = onCardListener;
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        ImageView imageView;
        TextView name;
        TextView comment;

        OnCardListener onCardListener;

        public ItemViewHolder(View itemView, OnCardListener onCardListener){
            super(itemView);


            imageView = (ImageView) itemView.findViewById(R.id.user_profile);
            name = (TextView) itemView.findViewById(R.id.user_name);
            comment = (TextView) itemView.findViewById(R.id.user_comment);

            this.onCardListener = onCardListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onCardListener.onCardClick(getAdapterPosition());
        }
    }

    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_search_card,parent,false);
        return new ItemViewHolder(view, onCardListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position){
        holder.imageView.setImageBitmap(cards.get(position).getImg());
        holder.name.setText(cards.get(position).getName());
        holder.comment.setText(cards.get(position).getComment());
    }

    @Override
    public int getItemCount(){
        return cards.size();
    }

    public void setItems(ArrayList<SearchCard> cards){
        this.cards = cards;
    }

    public interface OnCardListener{
        void onCardClick(int pos);
    }
}
