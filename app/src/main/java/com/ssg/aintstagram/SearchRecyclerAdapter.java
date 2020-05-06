package com.ssg.aintstagram;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Dictionary;

public class SearchRecyclerAdapter extends RecyclerView.Adapter<SearchRecyclerAdapter.ItemViewHolder>  {
    private Context context;
    private ArrayList<SearchCard> cards;
    private int lastPosition = -1;

    public SearchRecyclerAdapter(ArrayList<SearchCard> cards, Context context){
        this.cards = cards;
        this.context = context;
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView name;
        TextView comment;

        public ItemViewHolder(View itemView){
            super(itemView);

            imageView = (ImageView) itemView.findViewById(R.id.user_profile);
            name = (TextView) itemView.findViewById(R.id.user_name);
            comment = (TextView) itemView.findViewById(R.id.user_comment);
        }
    }

    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_search_card,parent,false);
        return new ItemViewHolder(view);
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

}
