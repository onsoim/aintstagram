package com.ssg.aintstagram;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.kakao.auth.Session;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import okhttp3.OkHttpClient;


public class ChatActivity extends AppCompatActivity{
    private String Token;

    RecyclerView v_recycle;
    ChatroomRecyclerAdapter adapter;

    private TextView button_to_cancel;
    private ImageButton new_video_chat;
    private ImageButton new_chatroom;
    private TextView button_to_camera;

    private ArrayList<Chatroom> chatrooms;

    private int kakaoID = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        v_recycle = (RecyclerView) findViewById(R.id.recycle_chatroom);

        this.setBtn();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);;
        v_recycle.setLayoutManager(linearLayoutManager);

        v_recycle.setOnScrollListener(new RecyclerView.OnScrollListener(){
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!v_recycle.canScrollVertically(-1)) {
                    Log.i("RECYCLERVIEW", "Top of list");
                } else if (!v_recycle.canScrollVertically(1)) {
                    Log.i("RECYCLERVIEW", "End of list");
                } else {
                    Log.i("RECYCLERVIEW", "idle");
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        getChatrooms();
    }

    public void setBtn() {
        button_to_cancel = (TextView) findViewById(R.id.button_to_cancel);
        new_video_chat = (ImageButton) findViewById(R.id.new_video_chat);
        new_chatroom = (ImageButton) findViewById(R.id.new_chatroom);
        button_to_camera = (TextView) findViewById(R.id.button_to_camera);

        View.OnClickListener Listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.button_to_cancel:
                        finish();
                        break;

                    case R.id.new_video_chat:
                        break;

                    case R.id.new_chatroom:
                        break;

                    case R.id.button_to_camera:
                        break;
                }
            }
        };

        button_to_cancel.setOnClickListener(Listener);
        new_video_chat.setOnClickListener(Listener);
        new_chatroom.setOnClickListener(Listener);
        button_to_camera.setOnClickListener(Listener);
    }


    public void getChatrooms(){
        chatrooms = new ArrayList<Chatroom>();

        final OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
        final ApolloClient apolloClient = ApolloClient.builder().serverUrl(getString(R.string.api_url)).okHttpClient(okHttpClient).build();
        Token = Session.getCurrentSession().getTokenInfo().getAccessToken();

        final UserTypeQuery u = UserTypeQuery.builder().accessToken(Token).build();
        final ChatroomTypeQuery m = ChatroomTypeQuery.builder().accessToken(Token).build();

        apolloClient.query(u).enqueue(new ApolloCall.Callback<UserTypeQuery.Data>() {
            @Override
            public void onResponse(@NotNull Response<UserTypeQuery.Data> response) {
                kakaoID = response.data().users().get(0).kakaoID;
            }

            @Override
            public void onFailure(@NotNull ApolloException e) {

            }
        });

        apolloClient.query(m).enqueue(new ApolloCall.Callback<ChatroomTypeQuery.Data>() {
            @Override
            public void onResponse(@NotNull Response<ChatroomTypeQuery.Data> response) {
                int cnt = response.data().chatrooms().size();

                for(int i=0; i<cnt; i++){
                    Boolean flag = response.data().chatrooms().get(i).userTo.kakaoID != kakaoID;

                    int chatroomId = Integer.parseInt(response.data().chatrooms().get(i).chatroomId);
                    String name = flag ? response.data().chatrooms().get(i).userTo.name : response.data().chatrooms().get(i).userFrom.name;
                    int kakaoID = flag ? response.data().chatrooms().get(i).userTo.kakaoID : response.data().chatrooms().get(i).userFrom.kakaoID;
                    String profile = flag ? response.data().chatrooms().get(i).userTo.profile : response.data().chatrooms().get(i).userFrom.profile;

                    chatrooms.add(new Chatroom(chatroomId, name, kakaoID, profile));
                }

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable(){
                            public void run(){
                                ChatroomRecyclerAdapter.OnChatroomListener onChatroomListener = new ChatroomRecyclerAdapter.OnChatroomListener() {
                                    @Override
                                    public void onChatroomClick(int pos) {
                                        Intent chatroomIntent = new Intent(ChatActivity.this, MessageActivity.class);
                                        chatroomIntent.putExtra("username", chatrooms.get(pos).getName());
                                        startActivity(chatroomIntent);
                                    }

                                };
                                adapter = new ChatroomRecyclerAdapter(chatrooms, getApplicationContext(), onChatroomListener);
                                v_recycle.setAdapter(adapter);
                            }
                        });
                    }
                }).start();
            }

            @Override
            public void onFailure(@NotNull ApolloException e) {

            }
        });
    }
}

