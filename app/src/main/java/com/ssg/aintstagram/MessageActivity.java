package com.ssg.aintstagram;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.kakao.auth.Session;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import okhttp3.OkHttpClient;


public class MessageActivity extends AppCompatActivity{
    private String Token;
    private String username;

    RecyclerView v_recycle;
    MessageRecyclerAdapter adapter;

    private TextView button_to_cancel;
    private TextView view_username;
    private EditText new_message;

    private ArrayList<Message> messages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                username = extras.getString("username");
            }
        }

        v_recycle = (RecyclerView) findViewById(R.id.recycle_message);

        this.setBtn();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        linearLayoutManager.setStackFromEnd(true);
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
        getMessages();
    }

    public void setBtn() {
        button_to_cancel = (TextView) findViewById(R.id.button_to_cancel);
        view_username = (TextView) findViewById(R.id.user_name);
        new_message = (EditText) findViewById(R.id.new_message);

        View.OnClickListener Listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.button_to_cancel:
                        finish();
                        break;
                }
            }
        };

        button_to_cancel.setOnClickListener(Listener);
        view_username.setText(username);
    }


    public void getMessages(){
        messages = new ArrayList<Message>();

        final OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
        final ApolloClient apolloClient = ApolloClient.builder().serverUrl(getString(R.string.api_url)).okHttpClient(okHttpClient).build();
        Token = Session.getCurrentSession().getTokenInfo().getAccessToken();

        final MessageTypeQuery m = MessageTypeQuery.builder().accessToken(Token).username(username).build();

        apolloClient.query(m).enqueue(new ApolloCall.Callback<MessageTypeQuery.Data>() {
            @Override
            public void onResponse(@NotNull Response<MessageTypeQuery.Data> response) {
                int cnt = response.data().messages().size();

                for(int i = 0; i < cnt; i++){
                    String name = response.data().messages().get(i).sender().name;
                    String textMessage = response.data().messages().get(i).textMessage;
                    messages.add(new Message(name, textMessage, username.equals(name)));
                }

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable(){
                            public void run(){
                                adapter = new MessageRecyclerAdapter(messages, getApplicationContext());
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

