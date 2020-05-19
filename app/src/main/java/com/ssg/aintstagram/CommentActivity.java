package com.ssg.aintstagram;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.Nullable;

public class CommentActivity extends Activity {
    Comment post;
    private Button button_to_cancel;
    private ImageButton button_to_sms;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        if(savedInstanceState == null){
            Bundle extras = getIntent().getExtras();
            if(extras == null){
                post = null;
            } else {
                byte[] bytes = getIntent().getByteArrayExtra("p_profile");
                Bitmap img = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                post = new Comment(img, extras.getString("p_name"), extras.getString("p_comment"), extras.getString("p_date"));
            }
        }

        if(post == null){
            finish();
        }

        setBtn();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    public void setBtn() {
        button_to_cancel = (Button) findViewById(R.id.button_to_cancel);
        button_to_sms = (ImageButton) findViewById(R.id.button_to_sms);

        View.OnClickListener Listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch(v.getId()){
                    case R.id.button_to_cancel:
                        finish();
                        break;
                    case R.id.button_to_sms:
                        break;
                }
            }
        };

        button_to_cancel.setOnClickListener(Listener);
        button_to_sms.setOnClickListener(Listener);
    }
}
