package com.ssg.aintstagram;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.ContentResolver;
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
import com.kakao.auth.Session;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.OkHttpClient;


public class MainActivity extends AppCompatActivity{
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_TAKE_ALBUM = 2;
    private ImageButton btn_camera;
    private ImageButton btn_add;
    private ImageButton btn_profile;
    private URI mImageUri;

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

        this.setBtn();

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
//        if(requestCode == REQUEST_IMAGE_CAPTURE) {
//            User user = new User();
//            String Token = Session.getCurrentSession().getTokenInfo().getAccessToken();
//            user.Token = Token;
//            user = user.uploadProfile(user, getString(R.string.api_url), imageFilePath);
////
////            OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
////            ApolloClient apolloClient = ApolloClient.builder().serverUrl(getString(R.string.api_url)).okHttpClient(okHttpClient).build();
////
////
////            final Upload_profileMutation uploadProfile = Upload_profileMutation.builder().img(new FileUpload("image/jpg", new File(imageFilePath))).accessToken(Token).build();
////            apolloClient.mutate(uploadProfile).enqueue(new ApolloCall.Callback<Upload_profileMutation.Data>() {
////                @Override
////                public void onResponse(@NotNull Response<Upload_profileMutation.Data> response) {
////                }
////
////                @Override
////                public void onFailure(@NotNull ApolloException e) {
////                    e.printStackTrace();
////                }
////            });
//        }
    }

    public void setBtn() {
        btn_camera = (ImageButton) findViewById(R.id.button_to_camera);
        btn_add = (ImageButton) findViewById(R.id.button_to_add);
        btn_profile = (ImageButton) findViewById(R.id.button_to_info);

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
//                                    Uri photoUri = FileProvider.getUriForFile(MainActivity.this, "com.ssg.aintstagram.fileprovider", photoFile);
                                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    Uri photoUri = Uri.fromFile(photoFile);
                                    intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                                    startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
                                }
                            }
                        }
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
                }
            }
        };

        btn_camera.setOnClickListener(Listener);
        btn_add.setOnClickListener(Listener);
        btn_profile.setOnClickListener(Listener);
    }

    String imageFilePath;
    private File mFile;
    private File createImageFile() throws IOException {
        String timeStamp =
                new SimpleDateFormat("yyyyMMdd_HHmmss",
                        Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";
//        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

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

}

