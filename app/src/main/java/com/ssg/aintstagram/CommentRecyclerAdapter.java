package com.ssg.aintstagram;

import android.content.Context;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;


public class CommentRecyclerAdapter extends RecyclerView.Adapter<CommentRecyclerAdapter.ItemViewHolder>{
    private Context context;
    private ArrayList<Comment> comments;
    private OnCommentListener onCommentListener;

    CommentRecyclerAdapter(ArrayList<Comment> comments, Context context, OnCommentListener onCommentListener){
        this.comments = comments;
        this.context = context;
        this.onCommentListener = onCommentListener;
    }


    public class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView comment_block;
        ImageView user_profile;
        TextView user_comment;
        TextView date;
        TextView likes;
        TextView add_reply;
        ImageButton btn_heart;

        OnCommentListener onCommentListener;

        public ItemViewHolder(View itemView, final OnCommentListener onCommentListener) {
            super(itemView);

            comment_block = (ImageView) itemView.findViewById(R.id.comment_block);
            user_profile = (ImageView) itemView.findViewById(R.id.user_profile);
            user_comment = (TextView) itemView.findViewById(R.id.user_comment);
            date = (TextView) itemView.findViewById(R.id.date);
            likes = (TextView) itemView.findViewById(R.id.likes);
            add_reply = (TextView) itemView.findViewById(R.id.add_reply);
            btn_heart = (ImageButton) itemView.findViewById(R.id.btn_heart);

            this.onCommentListener = onCommentListener;
            itemView.setOnClickListener(this);

            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (v.getId()) {
                        case R.id.add_reply:
                            onCommentListener.onCommentClick(getAdapterPosition(), 1);
                            break;
                        case R.id.btn_heart:
                            onCommentListener.onCommentClick(getAdapterPosition(), 2);
                            break;
                    }
                }
            };

            add_reply.setOnClickListener(listener);
            btn_heart.setOnClickListener(listener);

        }
        @Override
        public void onClick(View v) {
            onCommentListener.onCommentClick(getAdapterPosition(), 0);
        }
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_comment_detail,parent,false);
        return new ItemViewHolder(view, onCommentListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        holder.user_profile.setImageBitmap(comments.get(position).get_profile_img());
        holder.user_comment.setText(comments.get(position).getName() + " " + comments.get(position).getComment());
        holder.date.setText(comments.get(position).getDate());
        holder.likes.setText("좋아요 " + comments.get(position).getLikes() + "개");

        if(comments.get(position).isMine()){
            holder.btn_heart.setVisibility(View.GONE);
            holder.likes.setVisibility(View.GONE);
            holder.add_reply.setVisibility(View.GONE);
        } else {
            holder.btn_heart.setVisibility(View.VISIBLE);
        }

        if(comments.get(position).needBlock()){
            holder.comment_block.setVisibility(View.VISIBLE);
        } else {
            holder.comment_block.setVisibility(View.GONE);
        }

        if(comments.get(position).get_like_status()){
            holder.btn_heart.setImageResource(R.drawable.heartfull);
        } else {
            holder.btn_heart.setImageResource(R.drawable.heart);
        }

        if (comments.get(position).getLikes() > 0) {
            holder.likes.setVisibility(View.VISIBLE);
        } else {
            holder.likes.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public interface OnCommentListener{
        void onCommentClick(int pos, int choice);
    }
}
