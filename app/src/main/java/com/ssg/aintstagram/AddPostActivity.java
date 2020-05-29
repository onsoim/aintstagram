package com.ssg.aintstagram;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.FileUpload;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.kakao.auth.Session;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import okhttp3.OkHttpClient;

public class AddPostActivity extends Activity {
    ImageView v_img;
    File postimg;
    EditText v_comment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);
        String imgpath;

        if(savedInstanceState == null){
            Bundle extras = getIntent().getExtras();
            if(extras == null){
                imgpath = null;
            } else {
                imgpath = extras.getString("imgpath");
            }
        } else {
            imgpath = (String) savedInstanceState.getSerializable("imgpath");
        }

        if(imgpath == null){
            finish();
        }

        postimg = new File(imgpath);
        if(postimg.exists()){
            Bitmap img = BitmapFactory.decodeFile(postimg.getAbsolutePath());
            v_img = (ImageView) findViewById(R.id.post_pic);
            v_img.setImageBitmap(img);
        }

        v_comment = (EditText) findViewById(R.id.text_content);
        this.setBtn();
    }

    public void setBtn(){
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
                        String text_comment = v_comment.getText().toString();
                        final Add_postMutation addPost = Add_postMutation.builder().img(new FileUpload("image/jpg", new File(postimg.getAbsolutePath()))).accessToken(Token).comment(text_comment).build();

                        apolloClient.mutate(addPost).enqueue(new ApolloCall.Callback<Add_postMutation.Data>() {
                            @Override
                            public void onResponse(@NotNull Response<Add_postMutation.Data> response) {
                                if(response.data().addPost.success == false){
                                    Toast.makeText(getApplicationContext(), "알 수 없는 이유로 실패하였습니다.", Toast.LENGTH_LONG).show();
                                } else {
                                    finish();
                                }
                            }

                            @Override
                            public void onFailure(@NotNull ApolloException e) {
                                e.printStackTrace();
                                Toast.makeText(getApplicationContext(), "알 수 없는 이유로 실패하였습니다.", Toast.LENGTH_LONG).show();
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
