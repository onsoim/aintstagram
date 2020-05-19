package com.ssg.aintstagram;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CommentActivity extends Activity {
    Comment post;
    private Button button_to_cancel;
    private ImageButton button_to_sms;
    private RecyclerView v_recycle;

    private ArrayList<Comment> comments;
    CommentRecyclerAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        if(savedInstanceState == null){
            Bundle extras = getIntent().getExtras();
            if(extras == null){
                post = null;
            } else {
                byte[] bytes = getIntent().getByteArrayExtra("p_profile");
                Bitmap img = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                post = new Comment(img, extras.getString("p_name"), extras.getString("p_comment"), extras.getString("p_date"));
                post.set_mine(true);
            }
        }

        if(post == null){
            finish();
        }

        v_recycle = (RecyclerView) findViewById(R.id.scroll);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);;
        v_recycle.setLayoutManager(linearLayoutManager);

        setBtn();
    }

    @Override
    protected void onStart() {
        super.onStart();
        getComments();
    }

    private void getComments(){
        comments = new ArrayList<>();
        comments.add(post);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                CommentRecyclerAdapter.OnCommentListener onCommentListener = new CommentRecyclerAdapter.OnCommentListener(){

                    @Override
                    public void onCommentClick(int pos, int choice) {

                    }
                };
                adapter = new CommentRecyclerAdapter(comments, getApplicationContext(), onCommentListener);
                Log.e("DEBUG", String.valueOf(comments.size()));
                v_recycle.setAdapter(adapter);
            }
        });

    }


    public void setBtn() {
        button_to_cancel = (Button) findViewById(R.id.button_to_cancel);
        button_to_sms = (ImageButton) findViewById(R.id.button_to_sms);

        View.OnClickListener Listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch(v.getId()){
                    case R.id.button_to_cancel:
                        finish();
                        break;
                    case R.id.button_to_sms:
                        break;
                }
            }
        };

        button_to_cancel.setOnClickListener(Listener);
        button_to_sms.setOnClickListener(Listener);
    }
}
