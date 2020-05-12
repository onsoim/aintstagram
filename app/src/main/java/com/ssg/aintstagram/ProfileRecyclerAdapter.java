package com.ssg.aintstagram;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ProfileRecyclerAdapter extends RecyclerView.Adapter<ProfileRecyclerAdapter.ItemViewHolder>  {
    private Context context;
    private ArrayList<Album> albums;
    private OnPostListener onPostListener;

    public ProfileRecyclerAdapter(ArrayList<Album> albums, Context context, OnPostListener onPostListener){
        this.albums = albums;
        this.context = context;
        this.onPostListener = onPostListener;
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView imageView;

        OnPostListener onPostListener;

        public ItemViewHolder(View itemView, OnPostListener onPostListener){
            super(itemView);

            imageView = (ImageView) itemView.findViewById(R.id.imgview_picture);

            this.onPostListener = onPostListener;
            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            onPostListener.onPostClick(getAdapterPosition());
        }
    }

    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_post_img,parent,false);
        return new ItemViewHolder(view, onPostListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position){
        holder.imageView.setImageBitmap(albums.get(position).getImg());
    }

    @Override
    public int getItemCount(){
        return albums.size();
    }

    public void setItems(ArrayList<Album> albums){
        this.albums = albums;
    }

    public interface OnPostListener{
        void onPostClick(int pos);
    }
}
