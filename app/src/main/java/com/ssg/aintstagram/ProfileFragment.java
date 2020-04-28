package com.ssg.aintstagram;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.kakao.auth.Session;

import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import okhttp3.OkHttpClient;

public class ProfileFragment extends Fragment {
    RecyclerView v_recycle;
    ProfileRecyclerAdapter adapter;
    ArrayList<Album> albums = new ArrayList<>();
    private String Token;
    int cnt;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        v_recycle = (RecyclerView) view.findViewById(R.id.recycle_pic);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());

        v_recycle.setLayoutManager(linearLayoutManager);

        adapter = new ProfileRecyclerAdapter(albums, getContext());
        v_recycle.setAdapter(adapter);
        return view;
    }


    @Override
    public void onStart() {
        super.onStart();

        final OkHttpClient okHttpClient2 = new OkHttpClient.Builder().build();
        ApolloClient apolloClient2 = ApolloClient.builder().serverUrl(getString(R.string.api_url)).okHttpClient(okHttpClient2).build();

        Token = Session.getCurrentSession().getTokenInfo().getAccessToken();
        final PostTypeQuery p = PostTypeQuery.builder().accessToken(Token).build();

        apolloClient2.query(p).enqueue(new ApolloCall.Callback<PostTypeQuery.Data>() {
            @Override
            public void onResponse(@NotNull Response<PostTypeQuery.Data> response) {
                cnt = response.data().posts.size();

                for (int i = 0; i < cnt; i++) {
                    int record_no = Integer.parseInt(response.data().posts().get(i).postId);

                    OkHttpClient okHttpClient3 = new OkHttpClient.Builder().build();
                    ApolloClient apolloClient3 = ApolloClient.builder().serverUrl(getString(R.string.api_url)).okHttpClient(okHttpClient3).build();

                    PictureTypeQuery c = PictureTypeQuery.builder().accessToken(Token).record(record_no).build();
                    apolloClient3.query(c).enqueue(new ApolloCall.Callback<PictureTypeQuery.Data>() {
                        @Override
                        public void onResponse(@NotNull Response<PictureTypeQuery.Data> response) {
                            String album_url = getString(R.string.media_url) + response.data().pics().get(0).pic;
                            Log.e("DEBUG PIC", response.data().pics().get(0).pic);

                            try {
                                URL img_url = new URL(album_url);
                                HttpURLConnection conn = (HttpURLConnection) img_url.openConnection();
                                conn.setDoInput(true);
                                conn.connect();

                                InputStream is = conn.getInputStream();
                                Bitmap bitmap = BitmapFactory.decodeStream(is);

                                albums.add(new Album(bitmap));
                            } catch (Exception e) {
                            }
                        }

                        @Override
                        public void onFailure(@NotNull ApolloException e) {

                        }
                    });
                    adapter.setItems(albums);
                }
            }

            @Override
            public void onFailure(@NotNull ApolloException e) {

            }
        });

        adapter.setItems(albums);
    }
}
