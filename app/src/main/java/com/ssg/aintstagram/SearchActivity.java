package com.ssg.aintstagram;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import static android.view.View.VISIBLE;

public class SearchActivity extends FragmentActivity {
    private static final int REQUEST_TAKE_ALBUM = 2;
    private ImageButton btn_add;
    private ImageButton btn_profile;
    private ImageButton btn_home;
    private ImageButton btn_search;
    private Button btn_cancel;
    private EditText searchBar;

    private FragmentManager fragmentManager;
    private SearchResultFragment fragmentB;
    private FragmentTransaction transaction;


    String[] PERMISSIONS = {
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        fragmentManager = getSupportFragmentManager();
        fragmentB = new SearchResultFragment();
        transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.frame_search, fragmentB).commitAllowingStateLoss();

        searchBar = (EditText) findViewById(R.id.search_bar);
        searchBar.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                Button cancel = (Button) findViewById(R.id.button_cancel);
                if(hasFocus){
                    cancel.setVisibility(VISIBLE);
                } else {
                    cancel.setVisibility(View.INVISIBLE);
                }
            }
        });

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // 입력 전
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                fragmentB.RenewView(s.toString());
                // 입력되는 중
            }

            @Override
            public void afterTextChanged(Editable s) {
                // 입력 완료
            }
        });

        setBtn();
    }

    public void setBtn() {
        btn_add = (ImageButton) findViewById(R.id.button_to_add);
        btn_profile = (ImageButton) findViewById(R.id.button_to_info);
        btn_home = (ImageButton) findViewById(R.id.button_to_home);
        btn_search = (ImageButton) findViewById(R.id.button_to_search);
        btn_cancel = (Button) findViewById(R.id.button_cancel);

        View.OnClickListener Listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int permissionCheck;
                Intent intent = new Intent();
                switch (v.getId()) {
                    case R.id.button_to_home:
                        intent = new Intent(SearchActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                        break;

                    case R.id.button_to_search:
                        Intent searchintent = new Intent(SearchActivity.this, SearchActivity.class);
                        searchintent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(searchintent);
                        break;

                    case R.id.button_to_add:
                        permissionCheck = ContextCompat.checkSelfPermission(SearchActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);
                        if (permissionCheck == PackageManager.PERMISSION_DENIED) {
                            ActivityCompat.requestPermissions(SearchActivity.this, PERMISSIONS, 0);
                        } else {
                            Intent addintent = new Intent();
                            addintent.setAction(Intent.ACTION_GET_CONTENT);
                            addintent.setData(MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                            addintent.setType("image/*");
                            startActivityForResult(addintent, REQUEST_TAKE_ALBUM);
                        }
                        break;

                    case R.id.button_to_info:
                        Intent infointent = new Intent(SearchActivity.this, ProfileActivity.class);
                        infointent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(infointent);
                        break;

                    case R.id.button_cancel:
                        if(btn_cancel.getVisibility() == VISIBLE) {
                            searchBar.clearFocus();
                            searchBar.getText().clear();
                            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(searchBar.getWindowToken(), 0);
                        }
                        break;
                }
            }
        };
        btn_add.setOnClickListener(Listener);
        btn_profile.setOnClickListener(Listener);
        btn_home.setOnClickListener(Listener);
        btn_search.setOnClickListener(Listener);
        btn_cancel.setOnClickListener(Listener);
    }

}
