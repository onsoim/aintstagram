package com.ssg.aintstagram;

import android.content.Context;
import android.media.Image;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class PostRecyclerAdapter extends RecyclerView.Adapter<PostRecyclerAdapter.ItemViewHolder> {
    private Context context;
    private ArrayList<Post> posts;
    private OnPostListener onPostListener;

    PostRecyclerAdapter(ArrayList<Post> posts, Context context, OnPostListener onPostListener){
        this.posts = posts;
        this.context = context;
        this.onPostListener = onPostListener;
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageButton btn_edit_post;
        ImageButton btn_heart;
        ImageButton btn_comment;
        ImageButton btn_message;
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

        OnPostListener onPostListener;

        public ItemViewHolder(View itemView, final OnPostListener onPostListener){
            super(itemView);

            btn_edit_post = (ImageButton) itemView.findViewById(R.id.btn_edit_post);
            btn_heart = (ImageButton) itemView.findViewById(R.id.btn_heart);
            btn_comment = (ImageButton) itemView.findViewById(R.id.btn_comment);
            btn_message = (ImageButton) itemView.findViewById(R.id.btn_message);
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

            this.onPostListener = onPostListener;
            itemView.setOnClickListener(this);

            View.OnClickListener listener = new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    switch (v.getId()){
                        case R.id.btn_edit_post:
                            onPostListener.onPostClick(getAdapterPosition(), 1);
                            break;
                        case R.id.btn_heart:
                            onPostListener.onPostClick(getAdapterPosition(), 2);
                            break;
                        case R.id.btn_comment:
                            onPostListener.onPostClick(getAdapterPosition(), 3);
                            break;
                        case R.id.btn_message:
                            onPostListener.onPostClick(getAdapterPosition(), 4);
                            break;
                        case R.id.post_img:
                            onPostListener.onPostClick(getAdapterPosition(), 5);
                    }
                }
            };

            btn_edit_post.setOnClickListener(listener);
            btn_heart.setOnClickListener(listener);
            btn_comment.setOnClickListener(listener);
            btn_message.setOnClickListener(listener);
            post_img.setOnClickListener(listener);
        }


        @Override
        public void onClick(View v) {
            onPostListener.onPostClick(getAdapterPosition(), 0);
        }
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_post_detail,parent,false);
        return new ItemViewHolder(view, onPostListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        holder.user_profile.setImageBitmap(posts.get(position).get_profile_img());
        holder.post_img.setImageBitmap(posts.get(position).get_post_img());
        holder.user_name.setText(posts.get(position).getName());
        holder.place.setText(posts.get(position).getPlace());
        holder.likes.setText("좋아요 " + String.valueOf(posts.get(position).getLikes()) + "개");
        holder.user_name_comment.setText(posts.get(position).getName());
        holder.user_comment.setText(posts.get(position).get_text_comment());
        holder.viewer_profile_img.setImageBitmap(posts.get(position).get_profile_img());
        holder.date.setText(posts.get(position).getDate());

        if(posts.get(position).get_like_status()){
            holder.btn_heart.setImageResource(R.drawable.heartfull);
        } else {
            holder.btn_heart.setImageResource(R.drawable.heart);
        }

        if(posts.get(position).getMine()){
            holder.btn_edit_post.setVisibility(View.VISIBLE);
        } else {
            holder.btn_edit_post.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public interface OnPostListener{
        void onPostClick(int pos, int choice);
    }
}
