package com.ssg.aintstagram;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.bumptech.glide.Glide;
import com.kakao.auth.Session;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import okhttp3.OkHttpClient;

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
                post = new Comment(extras.getInt("p_post_id"), img, extras.getString("p_name"), extras.getString("p_comment"), extras.getString("p_date"), false);
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
        getCommentsList();
    }

    private void getComments(){
        comments = new ArrayList<>();
        comments.add(post);

    }

    public void getCommentsList() {
        getComments();

        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
        ApolloClient apolloClient = ApolloClient.builder().serverUrl(getString(R.string.api_url)).okHttpClient(okHttpClient).build();
        String Token = Session.getCurrentSession().getTokenInfo().getAccessToken();
        CommentTypeQuery c = CommentTypeQuery.builder().accessToken(Token).record(post.get_post_id()).build();

        apolloClient.query(c).enqueue(new ApolloCall.Callback<CommentTypeQuery.Data>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onResponse(@NotNull Response<CommentTypeQuery.Data> response) {
                for (int i = 0; i < response.data().comments().size(); i++) {
                    int postId = response.data().comments().get(i).postId;
                    int likeCount = response.data().comments().get(i).likeCount;
                    ZonedDateTime zdt = ZonedDateTime.parse(response.data().comments().get(i).date.toString());
                    ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
                    long days = Duration.between(zdt ,now).toDays();
                    long hours = Duration.between(zdt, now).toHours();
                    long mins = Duration.between(zdt, now).toMinutes();
                    String timestamp = "";
                    if(days>=1) {
                        timestamp = String.valueOf(days) + " 일";
                    } else if(hours>=1){
                        timestamp = String.valueOf(hours) + " 시";
                    } else {
                        timestamp = String.valueOf(mins) + " 분";
                    }
                    String textComment = response.data().comments().get(i).textComment;
                    String p_name = response.data().comments().get(i).user.name;

                    try {
                        Bitmap img = Glide
                                .with(getApplicationContext())
                                .asBitmap()
                                .load(response.data().comments().get(i).user.profile)
                                .submit().get();

                        if(response.data().comments().get(i).parent == null) {
                            comments.add(new Comment(postId, img, p_name, textComment, timestamp, false));
                        } else {
                            comments.add(new Comment(postId, img, p_name, textComment, timestamp, false));
                        }
                    } catch (ExecutionException e) {
                        if(response.data().comments().get(i).parent == null) {
                            comments.add(new Comment(postId, p_name, textComment, timestamp, false));
                        } else {
                            comments.add(new Comment(postId, p_name, textComment, timestamp, true));
                        }
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

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

            @Override
            public void onFailure(@NotNull ApolloException e) {

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
