package com.ssg.aintstagram;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.FileUpload;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.bumptech.glide.Glide;
import com.kakao.auth.Session;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import okhttp3.OkHttpClient;


public class MainActivity extends AppCompatActivity{
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_TAKE_ALBUM = 2;
    private String Token;

    RecyclerView v_recycle;
    PostRecyclerAdapter adapter;

    private ImageButton btn_camera;
    private ImageButton btn_add;
    private ImageButton btn_profile;
    private ImageButton btn_search;
    private URI mImageUri;

    private ArrayList<Post> posts;

    String[] PERMISSIONS = {
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        v_recycle = (RecyclerView) findViewById(R.id.scroll);

        this.setBtn();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);;
        v_recycle.setLayoutManager(linearLayoutManager);

    }

    @Override
    protected void onStart() {
        super.onStart();

        getPosts();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_TAKE_ALBUM && resultCode == RESULT_OK) {
            try {
                InputStream in = getContentResolver().openInputStream(data.getData());

                Bitmap img = BitmapFactory.decodeStream(in);
                in.close();

                Uri uri = data.getData();
                String newProfile = PathUtils.getPath(this, uri);

                Intent intent = new Intent(MainActivity.this, AddPostActivity.class);
                intent.putExtra("imgpath", newProfile);
                startActivity(intent);

            } catch (Exception e) {
            }
        }

        else if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Intent intent = new Intent(MainActivity.this, AddPostActivity.class);
            intent.putExtra("imgpath", imageFilePath);
            startActivity(intent);
        }
    }

    public void setBtn() {
        btn_camera = (ImageButton) findViewById(R.id.button_to_camera);
        btn_add = (ImageButton) findViewById(R.id.button_to_add);
        btn_profile = (ImageButton) findViewById(R.id.button_to_info);
        btn_search = (ImageButton) findViewById(R.id.button_to_search);

        View.OnClickListener Listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int permissionCheck;
                switch (v.getId()) {
                    case R.id.button_to_camera:
                        permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA);
                        if (permissionCheck == PackageManager.PERMISSION_DENIED) {
                            ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS, 0);
                        } else {
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            if (intent.resolveActivity(getPackageManager()) != null) {
                                File photoFile = null;
                                try {
                                    photoFile = createImageFile();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                if(photoFile != null){
                                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    Uri photoUri = Uri.fromFile(photoFile);
                                    intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                                    startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
                                }
                            }
                        }
                        break;

                    case R.id.button_to_search:
                        Intent searchintent = new Intent(MainActivity.this, SearchActivity.class);
                        searchintent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(searchintent);
                        break;

                    case R.id.button_to_add:
                        permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);
                        if (permissionCheck == PackageManager.PERMISSION_DENIED) {
                            ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS, 0);
                        } else {
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setData(MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                            intent.setType("image/*");
                            startActivityForResult(intent, REQUEST_TAKE_ALBUM);
                        }
                        break;

                    case R.id.button_to_info:
                        Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                        break;
                }
            }
        };

        btn_camera.setOnClickListener(Listener);
        btn_add.setOnClickListener(Listener);
        btn_profile.setOnClickListener(Listener);
        btn_search.setOnClickListener(Listener);
    }

    String imageFilePath;
    private File mFile;
    private File createImageFile() throws IOException {
        String timeStamp =
                new SimpleDateFormat("yyyyMMdd_HHmmss",
                        Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";

        File storageDir  = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "aintstagram");
        if(!storageDir.exists()) storageDir.mkdirs();

        mFile = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        imageFilePath = mFile.getAbsolutePath();
        return mFile;
    }

    public void getPosts(){
        posts = new ArrayList<>();

        final OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
        final ApolloClient apolloClient = ApolloClient.builder().serverUrl(getString(R.string.api_url)).okHttpClient(okHttpClient).build();
        Token = Session.getCurrentSession().getTokenInfo().getAccessToken();

        final PostTypeQuery q = PostTypeQuery.builder().accessToken(Token).build();

        apolloClient.query(q).enqueue(new ApolloCall.Callback<PostTypeQuery.Data>() {
            @Override
            public void onResponse(@NotNull Response<PostTypeQuery.Data> response) {
                int cnt = response.data().posts().size();

                for(int i=0; i<cnt; i++){
                    String name = response.data().posts().get(i).user().name;
                    String place = response.data().posts().get(i).place;
                    Integer postId = Integer.parseInt(response.data().posts().get(i).postId);
                    String textComment = response.data().posts().get(i).textComment;

                    posts.add(new Post(name, place, postId, textComment));


                    // FIXME :: It should be run after setAdapter, unless null deref
                    ImgUrlThread imgThread = new ImgUrlThread(i, postId);
                    imgThread.run();
                }

                Thread mThread = new Thread() {
                    public void run() {
                        try {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    adapter = new PostRecyclerAdapter(posts, getApplicationContext());
                                    v_recycle.setAdapter(adapter);
                                }
                            });
                        } catch (Exception e) {
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

    public class ImgUrlThread implements Runnable {
        private String url;
        private int idx;
        private Integer record;

        public ImgUrlThread(int idx, Integer record){
            this.idx = idx;
            this.record = record;
        }

        @Override
        public void run() {
            Log.e("CALLED", "THREAD_RUN");
            getImageUrl(record);
        }

        private void getImageUrl(int record) {
            Log.e("CALLED", "GET_IMAGE_URL");
            final OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
            final ApolloClient apolloClient = ApolloClient.builder().serverUrl(getString(R.string.api_url)).okHttpClient(okHttpClient).build();

            final PictureTypeQuery q = PictureTypeQuery.builder().accessToken(Token).record(record).build();

            apolloClient.query(q).enqueue(new ApolloCall.Callback<PictureTypeQuery.Data>() {
                @Override
                public void onResponse(@NotNull Response<PictureTypeQuery.Data> response) {
                    url = "http://10.0.2.2:8000/media/" + response.data().pics().get(0).pic;
                    try {
                        addAlbum(url, idx);

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

        private void addAlbum(String url, int idx) throws ExecutionException, InterruptedException {
            Bitmap bitmap = Glide
                    .with(getApplicationContext())
                    .asBitmap()
                    .load(url)
                    .submit().get();

            posts.get(idx).set_post_img(bitmap);

            NotifyRunnable runnable = new NotifyRunnable();
            runnable.setIdx(idx);
            runOnUiThread(runnable);
        }
    }

    public class NotifyRunnable implements Runnable {
        private int idx;
        public void setIdx(int idx) {
            this.idx = idx;
        }

        public void run() {
            if(adapter.getItemCount()>=idx)
                adapter.notifyItemChanged(idx);
        }
    }
}

