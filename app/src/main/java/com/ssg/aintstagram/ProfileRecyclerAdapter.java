package com.ssg.aintstagram;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Dictionary;

public class ProfileRecyclerAdapter extends RecyclerView.Adapter<ProfileRecyclerAdapter.ItemViewHolder>  {
    private Context context;
    private ArrayList<Album> albums;
    private int lastPosition = -1;

    public ProfileRecyclerAdapter(ArrayList<Album> albums, Context context){
        this.albums = albums;
        this.context = context;
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ItemViewHolder(View itemView){
            super(itemView);

            imageView = (ImageView) itemView.findViewById(R.id.imgview_picture);
        }
    }

    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_img,parent,false);
        return new ItemViewHolder(view);
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
}
