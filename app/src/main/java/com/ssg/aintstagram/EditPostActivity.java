package com.ssg.aintstagram;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.bumptech.glide.Glide;
import com.kakao.auth.Session;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutionException;

import okhttp3.OkHttpClient;

public class EditPostActivity extends Activity {
    private static final int FAIL_EDIT = 5;
    private static final int SUCCESS_EDIT = 6;
    Bitmap bitmap;
    ImageView v_img;
    EditText v_comment;
    EditText v_place;
    int record;
    String comment;
    String place;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_post);

        if(savedInstanceState == null){
            Bundle extras = getIntent().getExtras();
            if(extras == null){
               record  = -1;
            } else {
                record = extras.getInt("record");
            }
        } else {
            record = (Integer) savedInstanceState.getSerializable("record");
        }

        if(record == -1){
            setResult(FAIL_EDIT);
            finish();
        }
        setBtn();
        getPost();
    }

    public void getPost(){
        final OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
        final ApolloClient apolloClient = ApolloClient.builder().serverUrl(getString(R.string.api_url)).okHttpClient(okHttpClient).build();

        String Token = Session.getCurrentSession().getTokenInfo().getAccessToken();

        final PostTypeQuery q = PostTypeQuery.builder().accessToken(Token).record(record).build();
        apolloClient.query(q).enqueue(new ApolloCall.Callback<PostTypeQuery.Data>() {
            @Override
            public void onResponse(@NotNull Response<PostTypeQuery.Data> response) {
                if(response.data().posts().size() == 1){
                    place = response.data().posts().get(0).place;
                    comment = response.data().posts().get(0).textComment;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            v_comment.setText(place);
                            v_place.setText(comment);
                        }
                    });
                    getImageUrl(record);
                }
            }

            @Override
            public void onFailure(@NotNull ApolloException e) {

            }
        });
    }

    private void getImageUrl(int record) {
        final OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
        final ApolloClient apolloClient = ApolloClient.builder().serverUrl(getString(R.string.api_url)).okHttpClient(okHttpClient).build();

        String Token = Session.getCurrentSession().getTokenInfo().getAccessToken();

        final PictureTypeQuery q = PictureTypeQuery.builder().accessToken(Token).record(record).build();

        apolloClient.query(q).enqueue(new ApolloCall.Callback<PictureTypeQuery.Data>() {
            @Override
            public void onResponse(@NotNull Response<PictureTypeQuery.Data> response) {
                String url = "http://10.0.2.2:8000/media/" + response.data().pics().get(0).pic;
                try {
                    addPostImage(url);

                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(@NotNull ApolloException e) {

            }
        });
    }

    private void addPostImage(String url) throws ExecutionException, InterruptedException {
        bitmap = Glide
                .with(getApplicationContext())
                .asBitmap()
                .load(url)
                .submit().get();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                v_img.setImageBitmap(bitmap);
            }
        });
    }

    public void setBtn(){
        v_comment = (EditText) findViewById(R.id.text_content);
        v_place = (EditText) findViewById(R.id.text_place);
        v_img = (ImageView) findViewById(R.id.post_pic);
        Button btn_cancel = (Button) findViewById(R.id.button_to_cancel);
        Button btn_addpost = (Button) findViewById(R.id.button_to_ok);

        View.OnClickListener Listener = new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                switch(v.getId()){
                    case R.id.button_to_cancel:
                        finish();
                        break;
                    case R.id.button_to_ok:
                        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
                        ApolloClient apolloClient = ApolloClient.builder().serverUrl(getString(R.string.api_url)).okHttpClient(okHttpClient).build();

                        String Token = Session.getCurrentSession().getTokenInfo().getAccessToken();

                        final Edit_postMutation p = Edit_postMutation.builder().accessToken(Token).record(record).place(String.valueOf(v_place.getText())).comment(String.valueOf(v_comment.getText())).build();
                        apolloClient.mutate(p).enqueue(new ApolloCall.Callback<Edit_postMutation.Data>() {
                            @Override
                            public void onResponse(@NotNull Response<Edit_postMutation.Data> response) {
                                if(response.data().editPost().success){
                                    setResult(SUCCESS_EDIT);
                                    finish();
                                }
                                else {
                                    setResult(FAIL_EDIT);
                                    finish();
                                }
                            }

                            @Override
                            public void onFailure(@NotNull ApolloException e) {

                            }
                        });
                        break;
                }
            }
        };

        btn_cancel.setOnClickListener(Listener);
        btn_addpost.setOnClickListener(Listener);
    }
}
