package com.ssg.aintstagram;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import okhttp3.OkHttpClient;


public class MainActivity extends AppCompatActivity{
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_TAKE_ALBUM = 2;
    private static final int REQUEST_EDIT_POST = 3;
    private static final int FAIL_EDIT = 5;
    private static final int SUCCESS_EDIT = 6;
    private String Token;

    private ArrayList<ImgUrlThread> threads;
    RecyclerView v_recycle;
    PostRecyclerAdapter adapter;

    private ImageButton btn_camera;
    private ImageButton btn_add;
    private ImageButton btn_profile;
    private ImageButton btn_search;
    private ImageButton btn_history;
    private URI mImageUri;

    private ArrayList<Post> posts;
    private int changed_post_pos;

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

        Intent historyService = new Intent(getApplicationContext(), HistoryService.class);
        startService(historyService);
    }

    @Override
    protected void onStart() {
        super.onStart();
        getPosts();
        setUserProfile();
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

        else if(requestCode == REQUEST_EDIT_POST && resultCode == SUCCESS_EDIT){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter.notifyItemChanged(changed_post_pos);
                }
            });
        }
    }

    public void setBtn() {
        btn_camera = (ImageButton) findViewById(R.id.button_to_camera);
        btn_add = (ImageButton) findViewById(R.id.button_to_add);
        btn_profile = (ImageButton) findViewById(R.id.button_to_info);
        btn_search = (ImageButton) findViewById(R.id.button_to_search);
        btn_history = (ImageButton) findViewById(R.id.button_to_history);

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

                    case R.id.button_to_history:
                        Intent historyIntent = new Intent(MainActivity.this, HistoryActivity.class);
                        historyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(historyIntent);
                        break;
                }
            }
        };

        btn_camera.setOnClickListener(Listener);
        btn_add.setOnClickListener(Listener);
        btn_profile.setOnClickListener(Listener);
        btn_search.setOnClickListener(Listener);
        btn_history.setOnClickListener(Listener);
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
        threads = new ArrayList<>();

        final OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
        final ApolloClient apolloClient = ApolloClient.builder().serverUrl(getString(R.string.api_url)).okHttpClient(okHttpClient).build();
        Token = Session.getCurrentSession().getTokenInfo().getAccessToken();

        final PostTypeQuery q = PostTypeQuery.builder().accessToken(Token).build();

        apolloClient.query(q).enqueue(new ApolloCall.Callback<PostTypeQuery.Data>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onResponse(@NotNull Response<PostTypeQuery.Data> response) {
                int cnt = response.data().posts().size();

                for(int i=0; i<cnt; i++){
                    String name = response.data().posts().get(i).user().name;
                    String place = response.data().posts().get(i).place;
                    String profile = getString(R.string.media_url) + response.data().posts().get(i).user().profile;
                    Integer postId = Integer.parseInt(response.data().posts().get(i).postId);
                    int likes = response.data().posts().get(i).likeCount;
                    int comments = response.data().posts().get(i).commentCount;
                    String textComment = response.data().posts().get(i).textComment;
                    String dt = response.data().posts().get(i).date.toString();
                    ZonedDateTime zdt = ZonedDateTime.parse(dt);
                    ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
                    long days = Duration.between(zdt ,now).toDays();
                    long hours = Duration.between(zdt, now).toHours();
                    long mins = Duration.between(zdt, now).toMinutes();

                    if(days>=1) {
                        posts.add(new Post(name, place, postId, textComment, likes, String.valueOf(days) + " 일", comments));
                    } else if(hours>=1){
                        posts.add(new Post(name, place, postId, textComment, likes, String.valueOf(hours) + " 시간", comments));
                    } else {
                        posts.add(new Post(name, place, postId, textComment, likes, String.valueOf(mins) + " 분", comments));
                    }

                    threads.add(new ImgUrlThread(i, postId, profile));
                }

                Thread mThread = new Thread() {
                    public void run() {
                        try {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    PostRecyclerAdapter.OnPostListener onPostListener = new PostRecyclerAdapter.OnPostListener() {
                                        @Override
                                        public void onPostClick(final int pos, int choice) {
                                            switch(choice){
                                                case 0:
                                                    String name = posts.get(pos).getName();
                                                    Intent profileIntent = new Intent(getApplicationContext(), SearchResultProfileActivity.class);
                                                    profileIntent.putExtra("username", name);
                                                    startActivity(profileIntent);
                                                    break;
                                                case 1:
                                                    final int record = posts.get(pos).get_post_id();
                                                    final String[] items = new String[]{"글 수정", "글 삭제"};
                                                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                                    builder.setTitle("옵션을 선택하세요.").setItems(items, new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {

                                                            switch(which){
                                                                case 0:
                                                                    Intent editPostIntent = new Intent(getApplicationContext(), EditPostActivity.class);
                                                                    changed_post_pos = record;
                                                                    editPostIntent.putExtra("record", record);
                                                                    startActivityForResult(editPostIntent, REQUEST_EDIT_POST);
                                                                    break;
                                                                case 1:
                                                                    final OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
                                                                    final ApolloClient apolloClient = ApolloClient.builder().serverUrl(getString(R.string.api_url)).okHttpClient(okHttpClient).build();

                                                                    final Remove_postMutation q = Remove_postMutation.builder().accessToken(Token).record(record).build();
                                                                    apolloClient.mutate(q).enqueue(new ApolloCall.Callback<Remove_postMutation.Data>() {
                                                                        @Override
                                                                        public void onResponse(@NotNull Response<Remove_postMutation.Data> response) {
                                                                            if(response.data().removePost().success){
                                                                                runOnUiThread(new Runnable() {
                                                                                    @Override
                                                                                    public void run() {
                                                                                        Toast.makeText(getApplicationContext(), "게시물 삭제가 완료되었습니다.", Toast.LENGTH_LONG).show();
                                                                                        adapter.notifyItemRemoved(pos);
                                                                                    }
                                                                                });
                                                                            }
                                                                        }
                                                                        @Override
                                                                        public void onFailure(@NotNull ApolloException e) {

                                                                        }
                                                                    });
                                                                    break;
                                                            }
                                                        }
                                                    });
                                                    AlertDialog dialog = builder.create();
                                                    dialog.show();
                                                    break;
                                                case 2:
                                                    final Boolean status = posts.get(pos).get_like_status();
                                                    final int rec = posts.get(pos).get_post_id();
                                                    final OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
                                                    final ApolloClient apolloClient = ApolloClient.builder().serverUrl(getString(R.string.api_url)).okHttpClient(okHttpClient).build();

                                                    if(status){
                                                        Un_likeMutation q = Un_likeMutation.builder().accessToken(Token).record(rec).typeinfo("P").build();
                                                        apolloClient.mutate(q).enqueue(new ApolloCall.Callback<Un_likeMutation.Data>() {
                                                            @Override
                                                            public void onResponse(@NotNull Response<Un_likeMutation.Data> response) {
                                                                if(response.data().unLike().success){
                                                                    posts.get(pos).set_like_status(false);
                                                                    posts.get(pos).setLikes(response.data().unLike().likes);
                                                                    runOnUiThread(new Runnable() {
                                                                        @Override
                                                                        public void run() {
                                                                            posts.get(pos).set_like_status(false);
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
                                                        Add_likeMutation q = Add_likeMutation.builder().accessToken(Token).record(rec).typeinfo("P").build();
                                                        apolloClient.mutate(q).enqueue(new ApolloCall.Callback<Add_likeMutation.Data>() {
                                                            @Override
                                                            public void onResponse(@NotNull Response<Add_likeMutation.Data> response) {
                                                                if(response.data().addLike().success){
                                                                    posts.get(pos).set_like_status(true);
                                                                    posts.get(pos).setLikes(response.data().addLike().likes);
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
                                                    break;
                                                case 3:
                                                    Intent commentIntent = new Intent(MainActivity.this, CommentActivity.class);
                                                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                                    posts.get(pos).get_profile_img().compress(Bitmap.CompressFormat.PNG, 100, stream);
                                                    byte[] bytes = stream.toByteArray();
                                                    commentIntent.putExtra("p_profile", bytes);
                                                    commentIntent.putExtra("p_name", posts.get(pos).getName());
                                                    commentIntent.putExtra("p_comment", posts.get(pos).get_text_comment());
                                                    commentIntent.putExtra("p_date", posts.get(pos).getDate());
                                                    commentIntent.putExtra("p_post_id", posts.get(pos).get_post_id());
                                                    startActivity(commentIntent);
                                                    break;
                                                case 4:
                                                    break;
                                                case 5:
                                                    break;
                                                default:
                                                    throw new IllegalStateException("Unexpected value: " + choice);
                                            }
                                        }
                                    };

                                    adapter = new PostRecyclerAdapter(posts, getApplicationContext(), onPostListener);
                                    v_recycle.setAdapter(adapter);
                                }
                            });
                        } catch (Exception e) {
                        }

                        for(ImgUrlThread thread : threads){
                            thread.run();
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

    public void setUserProfile(){
        final OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
        final ApolloClient apolloClient = ApolloClient.builder().serverUrl(getString(R.string.api_url)).okHttpClient(okHttpClient).build();

        Token = Session.getCurrentSession().getTokenInfo().getAccessToken();

        final UserTypeQuery u = UserTypeQuery.builder().accessToken(Token).build();

        apolloClient.query(u).enqueue(new ApolloCall.Callback<UserTypeQuery.Data>() {
            @Override
            public void onResponse(@NotNull Response<UserTypeQuery.Data> response) {
                final String url = getString(R.string.media_url) + response.data().users().get(0).profile;

                Thread mThread = new Thread() {
                    @Override
                    public void run() {
                        try {
                            final Bitmap bitmap = Glide
                                    .with(getApplicationContext())
                                    .asBitmap()
                                    .load(url)
                                    .submit().get();

                            runOnUiThread(new Runnable(){
                                public void run(){
                                    for(int idx=0; idx<posts.size(); idx++) {
                                        posts.get(idx).set_comment_img(bitmap);
                                        adapter.notifyItemChanged(idx);
                                    }
                                }
                            });
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
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


    public class ImgUrlThread implements Runnable {
        private String url;
        private String profile_url;
        private int idx;
        private Integer record;

        public ImgUrlThread(int idx, Integer record, String profile_url){
            this.idx = idx;
            this.record = record;
            this.profile_url = profile_url;
        }

        @Override
        public void run() {
            getImageUrl(record);

            try {
                addPostProfileImage(profile_url);
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            checkLike();
            checkPostUser();
        }

        private void getImageUrl(int record) {
            final OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
            final ApolloClient apolloClient = ApolloClient.builder().serverUrl(getString(R.string.api_url)).okHttpClient(okHttpClient).build();

            final PictureTypeQuery q = PictureTypeQuery.builder().accessToken(Token).record(record).build();

            apolloClient.query(q).enqueue(new ApolloCall.Callback<PictureTypeQuery.Data>() {
                @Override
                public void onResponse(@NotNull Response<PictureTypeQuery.Data> response) {
                    url = "http://10.0.2.2:8000/media/" + response.data().pics().get(0).pic;
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

        private void addPostProfileImage(String url) throws ExecutionException, InterruptedException {
            Bitmap bitmap = Glide
                    .with(getApplicationContext())
                    .asBitmap()
                    .load(url)
                    .submit().get();

            posts.get(idx).set_profile_img(bitmap);

            NotifyRunnable runnable = new NotifyRunnable();
            runnable.setIdx(idx);
            runOnUiThread(runnable);
        }

        private void checkLike() {
            final OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
            final ApolloClient apolloClient = ApolloClient.builder().serverUrl(getString(R.string.api_url)).okHttpClient(okHttpClient).build();

            final LikeTypeQuery l = LikeTypeQuery.builder().accessToken(Token).typeinfo("P").record(record).build();
            apolloClient.query(l).enqueue(new ApolloCall.Callback<LikeTypeQuery.Data>() {
                @Override
                public void onResponse(@NotNull Response<LikeTypeQuery.Data> response) {
                    if(response.data().likes().size() == 1) {
                        posts.get(idx).set_like_status(true);
                    } else {
                        posts.get(idx).set_like_status(false);
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

        private void checkPostUser(){
            final OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
            final ApolloClient apolloClient = ApolloClient.builder().serverUrl(getString(R.string.api_url)).okHttpClient(okHttpClient).build();

            final PostTypeQuery p = PostTypeQuery.builder().accessToken(Token).record(record).build();
            apolloClient.query(p).enqueue(new ApolloCall.Callback<PostTypeQuery.Data>() {
                @Override
                public void onResponse(@NotNull Response<PostTypeQuery.Data> response) {
                    if(response.data().posts().size() >= 1) {
                        posts.get(idx).setMine(true);
                    } else {
                        posts.get(idx).setMine(false);
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

