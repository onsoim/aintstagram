package com.ssg.aintstagram;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.FileUpload;
import com.apollographql.apollo.api.Input;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.kakao.auth.Session;
import com.ssg.aintstagram.R;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import okhttp3.OkHttpClient;

public class EditProfileActivity extends Activity {
    private static final int REQUEST_TAKE_ALBUM = 2;

    String[] PERMISSIONS = {
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    };


    private Button btn_cancel;
    private Button btn_change_profile;
    private Button btn_ok;
    private Bitmap bitmap;
    private String newProfile;
    EditText t_name;
    EditText t_comment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        setBtn();

        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
        ApolloClient apolloClient = ApolloClient.builder().serverUrl(getString(R.string.api_url)).okHttpClient(okHttpClient).build();
        String Token = Session.getCurrentSession().getTokenInfo().getAccessToken();

        final UserTypeQuery u = UserTypeQuery.builder().accessToken(Token).build();

        apolloClient.query(u).enqueue(new ApolloCall.Callback<UserTypeQuery.Data>() {
            @Override
            public void onResponse(@NotNull Response<UserTypeQuery.Data> response) {
                final String profile_img = getString(R.string.media_url) + response.data().users().get(0).profile;
                final String username = response.data().users().get(0).name;
                final String usercomment = response.data().users().get(0).textComment;

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
                                    t_name = (EditText) findViewById(R.id.text_name);
                                    t_comment = (EditText) findViewById(R.id.text_comment);
                                    t_name.setText(username);
                                    t_comment.setText(usercomment);
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

    @Override
    protected void onStart() {
        super.onStart();

    }

    public void setBtn() {
        btn_cancel = (Button) findViewById(R.id.button_to_cancel);
        btn_change_profile = (Button) findViewById(R.id.btn_change_profile);
        btn_ok = (Button) findViewById(R.id.button_to_ok);

        View.OnClickListener Listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int permissionCheck;
                Intent intent = new Intent();
                switch (v.getId()) {
                    case R.id.button_to_cancel:
                        finish();
                        break;
                    case R.id.btn_change_profile:
                        permissionCheck = ContextCompat.checkSelfPermission(EditProfileActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);
                        if (permissionCheck == PackageManager.PERMISSION_DENIED) {
                            ActivityCompat.requestPermissions(EditProfileActivity.this, PERMISSIONS, 0);
                        } else {
                            intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            intent.setType("image/*");
                            startActivityForResult(intent, REQUEST_TAKE_ALBUM);
                        }
                        break;
                    case R.id.button_to_ok:
                        // TODO : have to implement is_ok button for user's privacy choice.
                        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
                        ApolloClient apolloClient = ApolloClient.builder().serverUrl(getString(R.string.api_url)).okHttpClient(okHttpClient).build();

                        String Token = Session.getCurrentSession().getTokenInfo().getAccessToken();

                        final Edit_profileMutation editProfile;
                        if (newProfile != null) {
                             editProfile = Edit_profileMutation.builder().img(
                                    new FileUpload("image/jpg", new File(newProfile))).accessToken(Token).name(t_name.getText().toString()).textComment(t_comment.getText().toString()).build();
                        }
                        else {
                            editProfile = Edit_profileMutation.builder().accessToken(Token).name(t_name.getText().toString()).textComment(t_comment.getText().toString()).build();
                        }
                        apolloClient.mutate(editProfile).enqueue(new ApolloCall.Callback<Edit_profileMutation.Data>() {
                            @Override
                            public void onResponse(@NotNull Response<Edit_profileMutation.Data> response) {
                                if(response.data().editProfile.success == false){
                                    Toast.makeText(getApplicationContext(), "알 수 없는 이유로 실패하였습니다.", Toast.LENGTH_LONG).show();
                                }
                                else {
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
        btn_change_profile.setOnClickListener(Listener);
        btn_ok.setOnClickListener(Listener);
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_TAKE_ALBUM && resultCode == RESULT_OK) {
            try {
                InputStream in = getContentResolver().openInputStream(data.getData());

                Bitmap img = BitmapFactory.decodeStream(in);
                in.close();

                ImageView v_profile = (ImageView) findViewById(R.id.user_profile);
                v_profile.setImageBitmap(img);

                Uri uri = data.getData();
                newProfile = PathUtils.getPath(this, uri);
            } catch (Exception e) {
            }
        }
    }
}

