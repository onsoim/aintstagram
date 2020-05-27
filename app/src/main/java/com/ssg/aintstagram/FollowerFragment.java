package com.ssg.aintstagram;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

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
    private ArrayList<Boolean> mutuality = new ArrayList<>();

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

    @Override
    public void onStart() {
        super.onStart();
        getFollowerList();
    }

    public void getFollowerList(){
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);;
        v_recycle.setLayoutManager(linearLayoutManager);

        cards.clear();

        final OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
        ApolloClient apolloClient = ApolloClient.builder().serverUrl(getString(R.string.api_url)).okHttpClient(okHttpClient).build();
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
                    Integer kakaoID = response.data().follows().get(i).userFrom().kakaoID;

                    Bitmap bitmap = null;
                    try {
                        bitmap = Glide
                                .with(getActivity())
                                .asBitmap()
                                .load(profile_url)
                                .submit().get();
                    } catch (Exception e) {
                        bitmap = ((BitmapDrawable)getResources().getDrawable(R.drawable.userinfo)).getBitmap();
                    }
                    cards.add(new FollowCard(bitmap, name, comment, kakaoID, false));

                }

                Thread mThread = new Thread() {
                    public void run() {
                        try {
                            getActivity().runOnUiThread(new Runnable() {
                                public void run() {
                                    FollowerRecyclerAdapter.OnCardListener mOnCardListener = new FollowerRecyclerAdapter.OnCardListener(){
                                        @Override
                                        public void onCardClick(int pos, int choice) {
                                            switch(choice){
                                                case 1:
                                                    String record = cards.get(pos).getName();
                                                    Intent profileIntent = new Intent(getContext().getApplicationContext(), SearchResultProfileActivity.class);
                                                    profileIntent.putExtra("username", record);
                                                    startActivity(profileIntent);
                                                    break;
                                                case 2:
                                                    Integer record2 = cards.get(pos).getKakaoID();
                                                    final OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
                                                    final ApolloClient apolloClient = ApolloClient.builder().serverUrl(getString(R.string.api_url)).okHttpClient(okHttpClient).build();
                                                    final Un_followMutation f = Un_followMutation.builder().accessToken(Token).fkakaoID(record2).choice(1).build();
                                                    apolloClient.mutate(f).enqueue(new ApolloCall.Callback<Un_followMutation.Data>() {
                                                        @Override
                                                        public void onResponse(@NotNull Response<Un_followMutation.Data> response) {
                                                            getActivity().runOnUiThread(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    Toast.makeText(getContext(), "팔로우 취소를 완료하였습니다.", Toast.LENGTH_LONG).show();
                                                                    getFollowerList();
                                                                }
                                                            });
                                                        }

                                                        @Override
                                                        public void onFailure(@NotNull ApolloException e) {

                                                        }
                                                    });
                                                    break;
                                                case 3:
                                                    break;
                                            }
                                        }
                                    };

                                    adapter = new FollowerRecyclerAdapter(cards, getContext(), mOnCardListener);
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

    private void setMutuality(){
        for(int i=0; i<cards.size(); i++){
            String name = cards.get(i).getName();

            final OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
            ApolloClient apolloClient = ApolloClient.builder().serverUrl(getString(R.string.api_url)).okHttpClient(okHttpClient).build();
            Token = Session.getCurrentSession().getTokenInfo().getAccessToken();
            final FollowTypeQuery ps = FollowTypeQuery.builder().accessToken(Token).username(name).build();
            apolloClient.query(ps).enqueue(new ApolloCall.Callback<FollowTypeQuery.Data>() {
                @Override
                public void onResponse(@NotNull Response<FollowTypeQuery.Data> response) {
                    if (response.data().follows().size() >=1){
                        final Boolean res = true;
                        mutuality.add(true);
                    } else {
                        final Boolean res = false;
                        mutuality.add(false);
                    }
                    if(mutuality.size() == cards.size()){
                        for(int i=0; i<mutuality.size(); i++){
                            cards.get(i).setIsMutual(mutuality.get(i));
                        }
                    }
                }

                @Override
                public void onFailure(@NotNull ApolloException e) {

                }
            });
        }
    }

    public void filterSeq(CharSequence charsequence){
        adapter.getFilter().filter(charsequence);

    }

}
