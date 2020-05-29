package com.ssg.aintstagram;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
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
    private int chatID;
    private Boolean sendMessage = false;

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
                chatID = extras.getInt("chatID");
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

        new_message.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    sendMessage = false;

                    final OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
                    final ApolloClient apolloClient = ApolloClient.builder().serverUrl(getString(R.string.api_url)).okHttpClient(okHttpClient).build();

                    String Token = Session.getCurrentSession().getTokenInfo().getAccessToken();

                    final Send_messageMutation s = Send_messageMutation.builder().accessToken(Token).username(username).chatid(chatID).msg(new_message.getText().toString()).build();


                    apolloClient.mutate(s).enqueue(new ApolloCall.Callback<Send_messageMutation.Data>() {
                        @Override
                        public void onResponse(@NotNull Response<Send_messageMutation.Data> response) {
                            sendMessage = response.data().sendMessage.success;
                            if (sendMessage) {
                                new_message.getText().clear();
                                getMessages();
                            }
                        }

                        @Override
                        public void onFailure(@NotNull ApolloException e) {

                        }
                    });
                    return sendMessage;
                }
                return false;
            }
        });
    }


    public void getMessages() {
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
                    int messageID = Integer.parseInt(response.data().messages.get(i).messageId);
                    String name = response.data().messages().get(i).sender().name;
                    String textMessage = response.data().messages().get(i).textMessage;
                    messages.add(new Message(messageID, textMessage, username.equals(name)));
                }

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable(){
                            public void run(){
                                MessageRecyclerAdapter.OnMessageListener onMessageListener = new MessageRecyclerAdapter.OnMessageListener() {
                                    @Override
                                    public void onMessageClick(int pos) {
                                        final int record = messages.get(pos).getMessageID();


                                        String[] items = new String[]{"삭제", "취소"};
                                        AlertDialog.Builder builder = new AlertDialog.Builder(MessageActivity.this);
                                        builder.setTitle("옵션을 선택하세요.").setItems(items, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                switch (which) {
                                                    case 0:
                                                        deleteMessage(record);
                                                        break;
                                                    case 1:
                                                        break;
                                                }
                                            }
                                        });
                                        AlertDialog dialog = builder.create();
                                        dialog.show();
                                    }
                                };

                                adapter = new MessageRecyclerAdapter(messages, getApplicationContext(), onMessageListener);
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

    public void deleteMessage(int record) {
        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
        ApolloClient apolloClient = ApolloClient.builder().serverUrl(getString(R.string.api_url)).okHttpClient(okHttpClient).build();

        Delete_messageMutation delete_messageMutation = Delete_messageMutation.builder().accessToken(Token).chatid(chatID).record(record).build();

        apolloClient.mutate(delete_messageMutation).enqueue(new ApolloCall.Callback<Delete_messageMutation.Data>() {
            @Override
            public void onResponse(@NotNull Response<Delete_messageMutation.Data> response) {
                if (response.data().deleteMessage.success) {
                    getMessages();
                }
            }

            @Override
            public void onFailure(@NotNull ApolloException e) {

            }
        });
    }
}

