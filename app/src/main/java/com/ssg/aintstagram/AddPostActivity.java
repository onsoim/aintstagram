package com.ssg.aintstagram;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class AddPostActivity extends Activity {
    ImageView v_img;

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

        File postimg = new File(imgpath);
        if(postimg.exists()){
            Bitmap img = BitmapFactory.decodeFile(postimg.getAbsolutePath());
            ImageView v_img = (ImageView) findViewById(R.id.post_pic);
            v_img.setImageBitmap(img);
        }

    }
}
