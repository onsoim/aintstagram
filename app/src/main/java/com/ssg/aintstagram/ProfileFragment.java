package com.ssg.aintstagram;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.bumptech.glide.Glide;
import com.kakao.auth.Session;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import okhttp3.OkHttpClient;

public class ProfileFragment extends Fragment {
    RecyclerView v_recycle;
    ProfileRecyclerAdapter adapter;
    ArrayList<Album> albums = new ArrayList<>();
    ArrayList<String> album_urls = new ArrayList<>();
    private String Token;
    int cnt;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        v_recycle = (RecyclerView) view.findViewById(R.id.recycle_pic);

        LinearLayoutManager linearLayoutManager = new GridLayoutManager(getActivity(), 4);
        v_recycle.setLayoutManager(linearLayoutManager);
        
        album_urls = new ArrayList<>();

        final OkHttpClient okHttpClient2 = new OkHttpClient.Builder().build();
        ApolloClient apolloClient2 = ApolloClient.builder().serverUrl(getString(R.string.api_url)).okHttpClient(okHttpClient2).build();

        Token = Session.getCurrentSession().getTokenInfo().getAccessToken();
        final PictureTypeQuery p = PictureTypeQuery.builder().accessToken(Token).build();

        apolloClient2.query(p).enqueue(new ApolloCall.Callback<PictureTypeQuery.Data>() {
            @Override
            public void onResponse(@NotNull Response<PictureTypeQuery.Data> response) {
                cnt = response.data().pics().size();

                for (int i = 0; i < cnt; i++) {
                    String album_url = getString(R.string.media_url) + response.data().pics().get(0).pic;
                    album_urls.add(album_url);
                }

                try {
                    addAlbum();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Thread mThread = new Thread() {
                    public void run() {
                        try {
                            getActivity().runOnUiThread(new Runnable() {
                                public void run() {
                                    adapter = new ProfileRecyclerAdapter(albums, getContext());
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

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void addAlbum() throws ExecutionException, InterruptedException {
        for(int i=0; i<cnt; i++) {
            String album_url = album_urls.get(i);
            Bitmap bitmap = Glide
                    .with(getActivity())
                    .asBitmap()
                    .load(album_url)
                    .submit().get();
            albums.add(new Album(bitmap));
        }
    }
}
