package com.ssg.aintstagram;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.kakao.auth.Session;
import com.ssg.aintstagram.R;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import okhttp3.OkHttpClient;

public class EditProfileActivity extends Activity {
    private Bitmap bitmap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
    }

    @Override
    protected void onStart() {
        super.onStart();

        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
        ApolloClient apolloClient = ApolloClient.builder().serverUrl(getString(R.string.api_url)).okHttpClient(okHttpClient).build();
        String Token = Session.getCurrentSession().getTokenInfo().getAccessToken();

        final UserTypeQuery u = UserTypeQuery.builder().accessToken(Token).build();

        apolloClient.query(u).enqueue(new ApolloCall.Callback<UserTypeQuery.Data>() {
            @Override
            public void onResponse(@NotNull Response<UserTypeQuery.Data> response) {
                final String profile_img = getString(R.string.media_url) + response.data().users().get(0).profile;
                final String name = response.data().users().get(0).name;
                final String comment = response.data().users().get(0).textComment;

                Thread mThread = new Thread() {
                    @Override
                    public void run() {
                        try {
                            URL img_url = new URL(profile_img);
                            HttpURLConnection conn = (HttpURLConnection) img_url.openConnection();
                            conn.setDoInput(true);
                            conn.connect();

                            InputStream is = conn.getInputStream();
                            bitmap = BitmapFactory.decodeStream(is);

                            runOnUiThread(new Runnable() {
                                public void run() {
                                    ImageView v_profile = (ImageView) findViewById(R.id.user_profile);
                                    v_profile.setImageBitmap(bitmap);
                                    TextView t_name = (TextView) findViewById(R.id.text_name);
                                    TextView t_comment = (TextView) findViewById(R.id.text_comment);
                                    t_name.setText(name);
                                    t_comment.setText(comment);
                                }
                            });
                        } catch (
                                MalformedURLException e) {
                            e.printStackTrace();

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                };
                mThread.start();

            }

            @Override
            public void onFailure(@NotNull ApolloException e) {

            }
        });
    }
}

