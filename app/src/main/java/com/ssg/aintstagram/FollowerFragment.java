package com.ssg.aintstagram;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.bumptech.glide.Glide;
import com.kakao.auth.Session;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import okhttp3.OkHttpClient;

public class FollowerFragment extends Fragment {
    private FollowerRecyclerAdapter adapter;
    private ArrayList<FollowCard> cards = new ArrayList<>();

    RecyclerView v_recycle;
    private String Token;
    private int cnt;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_search_result, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        v_recycle = (RecyclerView) view.findViewById(R.id.recycle_card);

    }

    public void getFollowerList(){
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);;
        v_recycle.setLayoutManager(linearLayoutManager);

        cards.clear();

        final OkHttpClient okHttpClient2 = new OkHttpClient.Builder().build();
        ApolloClient apolloClient = ApolloClient.builder().serverUrl(getString(R.string.api_url)).okHttpClient(okHttpClient2).build();
        Token = Session.getCurrentSession().getTokenInfo().getAccessToken();
        final FollowTypeQuery p = FollowTypeQuery.builder().accessToken(Token).choice(1).build();

        apolloClient.query(p).enqueue(new ApolloCall.Callback<FollowTypeQuery.Data>() {
            @Override
            public void onResponse(@NotNull Response<FollowTypeQuery.Data> response) {
                cnt = response.data().follows().size();

                for (int i = 0; i < cnt; i++) {

                    String profile_url = getString(R.string.media_url) + response.data().follows().get(i).userFrom().profile;
                    String name = response.data().follows().get(i).userFrom().name;
                    String comment = response.data().follows().get(i).userFrom().textComment;

                    try {
                        Bitmap bitmap = Glide
                                .with(getActivity())
                                .asBitmap()
                                .load(profile_url)
                                .submit().get();

                        cards.add(new FollowCard(bitmap, name, comment));
                    } catch (ExecutionException e) {
                        cards.add(new FollowCard(name, comment));
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                Thread mThread = new Thread() {
                    public void run() {
                        try {
                            getActivity().runOnUiThread(new Runnable() {
                                public void run() {
                                    adapter = new FollowerRecyclerAdapter(cards, getContext());
                                    v_recycle.setAdapter(adapter);
                                    Button btn_follower = ((FollowActivity) getActivity()).findViewById(R.id.btn_follower);
                                    btn_follower.setText(String.valueOf(cnt) + " 팔로워");

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

}
