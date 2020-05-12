package com.ssg.aintstagram;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class PostRecyclerAdapter extends RecyclerView.Adapter<PostRecyclerAdapter.ItemViewHolder> {
    private Context context;
    private ArrayList<Post> posts;

    PostRecyclerAdapter(ArrayList<Post> posts, Context context){
        this.posts = posts;
        this.context = context;
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        ImageView user_profile;
        ImageView post_img;
        TextView user_name;
        TextView place;
        TextView likes;
        TextView user_name_comment;
        TextView user_comment;
        TextView comments;
        ImageView viewer_profile_img;
        EditText viewer_comment;
        TextView date;


        public ItemViewHolder(View itemView){
            super(itemView);

            user_profile = (ImageView) itemView.findViewById(R.id.user_profile);
            post_img = (ImageView) itemView.findViewById(R.id.post_img);
            user_name = (TextView) itemView.findViewById(R.id.user_name);
            place = (TextView) itemView.findViewById(R.id.place);
            likes = (TextView) itemView.findViewById(R.id.likes);
            user_name_comment = (TextView) itemView.findViewById(R.id.user_name_comment);
            user_comment = (TextView) itemView.findViewById(R.id.user_comment);
            comments = (TextView) itemView.findViewById(R.id.comments);
            viewer_comment = (EditText) itemView.findViewById(R.id.viewer_comment);
            viewer_profile_img = (ImageView) itemView.findViewById(R.id.viewer_profile_img);
            date = (TextView) itemView.findViewById(R.id.date);
        }

    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_post_detail,parent,false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        holder.user_profile.setImageBitmap(posts.get(position).get_profile_img());
        holder.post_img.setImageBitmap(posts.get(position).get_post_img());
        holder.user_name.setText(posts.get(position).getName());
        holder.place.setText(posts.get(position).getPlace());
        holder.likes.setText(String.valueOf(posts.get(position).getLikes()));
        holder.user_name_comment.setText(posts.get(position).getName());
        holder.user_comment.setText(posts.get(position).get_text_comment());
        // date
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }
}
