package com.ssg.aintstagram;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

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
    private Comment post;
    private Boolean myPost;
    private Button button_to_cancel;
    private ImageButton button_to_sms;
    private EditText new_comment;
    private RecyclerView v_recycle;
    private String Token;

    private ArrayList<Comment> comments;
    private ArrayList<CommentInfoThread> threads;
    CommentRecyclerAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        Token = Session.getCurrentSession().getTokenInfo().getAccessToken();

        if(savedInstanceState == null){
            Bundle extras = getIntent().getExtras();
            if(extras == null){
                post = null;
            } else {
                byte[] bytes = getIntent().getByteArrayExtra("p_profile");
                Bitmap img = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                post = new Comment(extras.getInt("p_post_id"), -1, img, extras.getString("p_name"), extras.getString("p_comment"), extras.getString("p_date"), null, null, 0);
                post.set_mine(true);
                myPost = extras.getBoolean("p_mine");
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

        threads = new ArrayList<>();

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
                    int record = Integer.parseInt(response.data().comments().get(i).commentId);
                    int likeCount = response.data().comments().get(i).likeCount;
                    String url = getString(R.string.media_url) + response.data().comments().get(i).user().profile;
                    Integer parent = response.data().comments().get(i).parent;
                    ZonedDateTime zdt = ZonedDateTime.parse(response.data().comments().get(i).date.toString());
                    ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
                    long days = Duration.between(zdt, now).toDays();
                    long hours = Duration.between(zdt, now).toHours();
                    long mins = Duration.between(zdt, now).toMinutes();
                    String timestamp = "";
                    if (days >= 1) {
                        timestamp = String.valueOf(days) + " 일";
                    } else if (hours >= 1) {
                        timestamp = String.valueOf(hours) + " 시간";
                    } else {
                        timestamp = String.valueOf(mins) + " 분";
                    }
                    String textComment = response.data().comments().get(i).textComment;
                    String p_name = response.data().comments().get(i).user.name;

                    Boolean flag_end = true;
                    if (parent != null) {
                        Boolean flag = false;
                        for (Comment c : comments) {
                            if (flag && c.getParent() == null) {
                                comments.add(comments.indexOf(c), new Comment(postId, record, p_name, textComment, timestamp, parent, url, likeCount));
                                flag_end = false;
                                break;
                            } else if (c.getRecord() == parent) {
                                flag = true;
                            }
                        }
                    }
                    if (flag_end) {
                        comments.add(new Comment(postId, record, p_name, textComment, timestamp, parent, url, likeCount));
                    }
                    threads.add(new CommentInfoThread(i + 1));
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        CommentRecyclerAdapter.OnCommentListener onCommentListener = new CommentRecyclerAdapter.OnCommentListener(){
                            @Override
                            public void onCommentClick(final int pos, int choice) {
                                switch(choice){
                                    case 1:
                                        Integer parent = comments.get(pos).getParent() != null ? comments.get(pos).getParent() : comments.get(pos).getRecord();
                                        new_comment.setOnKeyListener(new ReplyCommentListener(parent));
                                        new_comment.requestFocus();
                                        break;
                                    case 2:
                                        final Boolean status = comments.get(pos).get_like_status();
                                        final int rec = comments.get(pos).getRecord();
                                        String Token = Session.getCurrentSession().getTokenInfo().getAccessToken();
                                        final OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
                                        final ApolloClient apolloClient = ApolloClient.builder().serverUrl(getString(R.string.api_url)).okHttpClient(okHttpClient).build();

                                        if(status){
                                            Un_likeMutation q = Un_likeMutation.builder().accessToken(Token).record(rec).typeinfo("C").build();
                                            apolloClient.mutate(q).enqueue(new ApolloCall.Callback<Un_likeMutation.Data>() {
                                                @Override
                                                public void onResponse(@NotNull Response<Un_likeMutation.Data> response) {
                                                    if(response.data().unLike().success){
                                                        comments.get(pos).set_like_status(false);
                                                        comments.get(pos).setLikes(response.data().unLike().likes);
                                                        runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                comments.get(pos).set_like_status(false);
                                                                adapter.notifyItemChanged(pos);
                                                            }
                                                        });
                                                    }
                                                }

                                                @Override
                                                public void onFailure(@NotNull ApolloException e) {

                                                }
                                            });
                                        } else {
                                            Add_likeMutation q = Add_likeMutation.builder().accessToken(Token).record(rec).typeinfo("C").build();
                                            apolloClient.mutate(q).enqueue(new ApolloCall.Callback<Add_likeMutation.Data>() {
                                                @Override
                                                public void onResponse(@NotNull Response<Add_likeMutation.Data> response) {
                                                    if(response.data().addLike().success){
                                                        comments.get(pos).set_like_status(true);
                                                        comments.get(pos).setLikes(response.data().addLike().likes);
                                                        runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                adapter.notifyItemChanged(pos);
                                                            }
                                                        });
                                                    }
                                                }

                                                @Override
                                                public void onFailure(@NotNull ApolloException e) {

                                                }
                                            });

                                        }
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                adapter.notifyItemChanged(pos);
                                            }
                                        });
                                }
                            }
                        };
                        adapter = new CommentRecyclerAdapter(comments, getApplicationContext(), onCommentListener);
                        v_recycle.setAdapter(adapter);
                    }
                });

                for(CommentInfoThread thread : threads){
                    thread.run();
                }
            }
            
            @Override
            public void onFailure(@NotNull ApolloException e) {

            }


        });
    }

    public void setBtn() {
        button_to_cancel = (Button) findViewById(R.id.button_to_cancel);
        button_to_sms = (ImageButton) findViewById(R.id.button_to_sms);
        new_comment = (EditText) findViewById(R.id.new_comment);

        View.OnClickListener Listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch(v.getId()){
                    case R.id.button_to_cancel:
                        finish();
                        break;
                    case R.id.button_to_sms:
                        if(myPost) break;
                        moveToChatroom(post.getName());
                        break;
                }
            }
        };

        button_to_cancel.setOnClickListener(Listener);
        button_to_sms.setOnClickListener(Listener);
    }

    private class CommentInfoThread implements Runnable{
        private int idx;

        public CommentInfoThread(int idx){
            this.idx = idx;
        }

        @Override
        public void run() {
            try {
                comments.get(idx).set_profile_img(getProfile());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyItemChanged(idx);
                    }
                });
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            checkLike();
        }

        Bitmap getProfile() throws ExecutionException, InterruptedException {
            Bitmap bitmap = null;
            try {
                bitmap = Glide
                        .with(getApplicationContext())
                        .asBitmap()
                        .load(comments.get(idx).getUrl())
                        .submit().get();
            } catch (Exception e) {
                bitmap = ((BitmapDrawable)getResources().getDrawable(R.drawable.userinfo)).getBitmap();
            }

            return bitmap;
        };

        private void checkLike(){
            final OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
            final ApolloClient apolloClient = ApolloClient.builder().serverUrl(getString(R.string.api_url)).okHttpClient(okHttpClient).build();
            String Token = Session.getCurrentSession().getTokenInfo().getAccessToken();
            final LikeTypeQuery l = LikeTypeQuery.builder().accessToken(Token).typeinfo("C").record(comments.get(idx).getRecord()).build();
            apolloClient.query(l).enqueue(new ApolloCall.Callback<LikeTypeQuery.Data>() {
                @Override
                public void onResponse(@NotNull Response<LikeTypeQuery.Data> response) {
                    if(response.data().likes().size() == 1) {
                        comments.get(idx).set_like_status(true);
                    } else {
                        comments.get(idx).set_like_status(false);
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyItemChanged(idx);
                        }
                    });
                }

                @Override
                public void onFailure(@NotNull ApolloException e) {

                }
            });
        }
    }

    public class ReplyCommentListener implements View.OnKeyListener{
        Integer parent;

        public ReplyCommentListener(Integer parent){
            this.parent = parent;
        }

        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                String comment = String.valueOf(new_comment.getText());
                if (comment.substring(0) != "\n") {
                    OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
                    ApolloClient apolloClient = ApolloClient.builder().serverUrl(getString(R.string.api_url)).okHttpClient(okHttpClient).build();

                    String Token = Session.getCurrentSession().getTokenInfo().getAccessToken();
                    if(this.parent == null){
                        final Add_commentMutation addComment = Add_commentMutation.builder().accessToken(Token).record(comments.get(0).get_post_id()).text(comment).build();
                        apolloClient.mutate(addComment).enqueue(new ApolloCall.Callback<Add_commentMutation.Data>() {
                            @Override
                            public void onResponse(@NotNull Response<Add_commentMutation.Data> response) {
                                if (response.data().addComment.success) {
                                    new_comment.getText().clear();

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            new_comment.clearFocus();
                                        }
                                    });

                                    InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
                                    imm.hideSoftInputFromWindow( new_comment.getWindowToken(), 0);

                                    getCommentsList();
                                }
                            }

                            @Override
                            public void onFailure(@NotNull ApolloException e) {
                                e.printStackTrace();
                                Toast.makeText(getApplicationContext(), "알 수 없는 이유로 실패하였습니다.", Toast.LENGTH_LONG).show();
                            }
                        });
                    } else {
                        final Add_commentMutation addComment = Add_commentMutation.builder().accessToken(Token).record(comments.get(0).get_post_id()).parent(this.parent).text(comment).build();
                        apolloClient.mutate(addComment).enqueue(new ApolloCall.Callback<Add_commentMutation.Data>() {
                            @Override
                            public void onResponse(@NotNull Response<Add_commentMutation.Data> response) {
                                if (response.data().addComment.success) {
                                    new_comment.getText().clear();

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            new_comment.clearFocus();
                                        }
                                    });

                                    InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
                                    imm.hideSoftInputFromWindow( new_comment.getWindowToken(), 0);

                                    getCommentsList();
                                }
                            }

                            @Override
                            public void onFailure(@NotNull ApolloException e) {
                                e.printStackTrace();
                                Toast.makeText(getApplicationContext(), "알 수 없는 이유로 실패하였습니다.", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                    return true;
                }
                return false;
            }
            return false;
        }
    }

    private void moveToChatroom(final String username){
        final OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
        final ApolloClient apolloClient = ApolloClient.builder().serverUrl(getString(R.string.api_url)).okHttpClient(okHttpClient).build();

        ChatroomTypeQuery q = ChatroomTypeQuery.builder().accessToken(Token).username(username).build();
        apolloClient.query(q).enqueue(new ApolloCall.Callback<ChatroomTypeQuery.Data>() {
            @Override
            public void onResponse(@NotNull Response<ChatroomTypeQuery.Data> response) {
                if(response.data().chatrooms().size() == 0){
                    createChatroom(username);
                }
                else {
                    gotoChatroom(username);
                }
            }

            @Override
            public void onFailure(@NotNull ApolloException e) {

            }
        });
    }

    private void createChatroom(final String username){
        final OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
        final ApolloClient apolloClient = ApolloClient.builder().serverUrl(getString(R.string.api_url)).okHttpClient(okHttpClient).build();

        Create_chatroomMutation q = Create_chatroomMutation.builder().accessToken(Token).username(username).build();
        apolloClient.mutate(q).enqueue(new ApolloCall.Callback<Create_chatroomMutation.Data>() {
            @Override
            public void onResponse(@NotNull Response<Create_chatroomMutation.Data> response) {
                if(response.data().createChatroom.success){
                    gotoChatroom(username);
                }
                else{
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "잠시 후 다시 시도해주세요.", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }

            @Override
            public void onFailure(@NotNull ApolloException e) {

            }
        });
    }

    private void gotoChatroom(final String username){
        Intent chatroomIntent = new Intent(CommentActivity.this, MessageActivity.class);
        chatroomIntent.putExtra("username", username);
        startActivity(chatroomIntent);
    };
}
