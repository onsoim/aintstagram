package com.ssg.aintstagram;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;


public class FollowerRecyclerAdapter extends RecyclerView.Adapter<FollowerRecyclerAdapter.ItemViewHolder> implements Filterable {
    private Context context;
    private ArrayList<FollowCard> cards;
    private OnCardListener onCardListener;

    ArrayList<FollowCard> unFilteredlist;
    ArrayList<FollowCard> filteredList;

    public FollowerRecyclerAdapter(ArrayList<FollowCard> cards, Context context, OnCardListener onCardListener){
        this.cards = cards;
        this.context = context;
        this.onCardListener = onCardListener;
        this.unFilteredlist = cards;
        this.filteredList = cards;
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView imageView;
        TextView name;
        TextView comment;
        Button btn_mutual;
        Button btn_remove;

        OnCardListener onCardListener;

        public ItemViewHolder(View itemView, final OnCardListener onCardListener){
            super(itemView);

            imageView = (ImageView) itemView.findViewById(R.id.user_profile);
            name = (TextView) itemView.findViewById(R.id.user_name);
            comment = (TextView) itemView.findViewById(R.id.user_comment);
            btn_mutual = (Button) itemView.findViewById(R.id.btn_follow);
            btn_remove = (Button) itemView.findViewById(R.id.btn_remove);

            this.onCardListener = onCardListener;
            itemView.setOnClickListener(this);

            View.OnClickListener Listener = new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    switch(v.getId()){
                        case R.id.btn_remove:
                            onCardListener.onCardClick(getAdapterPosition(), 2);
                            break;
                        case R.id.btn_follow:
                            onCardListener.onCardClick(getAdapterPosition(), 3);
                            break;
                        default:
                            throw new IllegalStateException("Unexpected value: " + v.getId());
                    }
                }
            };

            btn_remove.setOnClickListener(Listener);
            btn_mutual.setOnClickListener(Listener);
        }

        @Override
        public void onClick(View v) {
            onCardListener.onCardClick(getAdapterPosition(), 1);
        }
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_follower_card, parent, false);
        return new ItemViewHolder(view, onCardListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        holder.imageView.setImageBitmap(cards.get(position).getImg());
        holder.name.setText(cards.get(position).getName());
        holder.comment.setText(cards.get(position).getComment());
    }

    @Override
    public int getItemCount() {
        return cards.size();
    }

    public void setItem(ArrayList<FollowCard> cards){ this.cards = cards; }

    public interface OnCardListener{
        void onCardClick(int pos, int choice);
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String charString = constraint.toString();
                if(charString.isEmpty()) {
                    filteredList = unFilteredlist;
                } else {
                    ArrayList<FollowCard> filteringList = new ArrayList<>();
                    for(int i=0; i<unFilteredlist.size(); i++) {
                        if(unFilteredlist.get(i).getName().toLowerCase().contains(charString.toLowerCase())) {
                            filteringList.add(unFilteredlist.get(i));
                        }
                    }
                    filteredList = filteringList;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = filteredList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredList = (ArrayList<FollowCard>)results.values;
                setItem(filteredList);
                notifyDataSetChanged();
            }
        };
    }


}
