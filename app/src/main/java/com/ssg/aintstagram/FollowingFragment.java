package com.ssg.aintstagram;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
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

import okhttp3.OkHttpClient;

public class FollowingFragment extends Fragment {
    private FollowingRecyclerAdapter adapter;
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

    @Override
    public void onStart() {
        super.onStart();
        getFollowingList();
    }

    public void getFollowingList(){
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);;
        v_recycle.setLayoutManager(linearLayoutManager);

        cards.clear();

        final OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
        final ApolloClient apolloClient = ApolloClient.builder().serverUrl(getString(R.string.api_url)).okHttpClient(okHttpClient).build();
        Token = Session.getCurrentSession().getTokenInfo().getAccessToken();
        final FollowTypeQuery p = FollowTypeQuery.builder().accessToken(Token).choice(2).build();

        apolloClient.query(p).enqueue(new ApolloCall.Callback<FollowTypeQuery.Data>() {
            @Override
            public void onResponse(@NotNull Response<FollowTypeQuery.Data> response) {
                cnt = response.data().follows().size();

                for (int i = 0; i < cnt; i++) {

                    String profile_url = getString(R.string.media_url) + response.data().follows().get(i).userTo().profile;
                    String name = response.data().follows().get(i).userTo().name;
                    String comment = response.data().follows().get(i).userTo().textComment;
                    Integer kakaoID = response.data().follows().get(i).userTo().kakaoID;

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
                                    FollowingRecyclerAdapter.OnCardListener mOnCardListener = new FollowingRecyclerAdapter.OnCardListener() {
                                        @Override
                                        public void onCardClick(int pos, int choice) {
                                            switch(choice) {
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
                                                    final Un_followMutation f = Un_followMutation.builder().accessToken(Token).fkakaoID(record2).build();
                                                    apolloClient.mutate(f).enqueue(new ApolloCall.Callback<Un_followMutation.Data>() {
                                                        @Override
                                                        public void onResponse(@NotNull Response<Un_followMutation.Data> response) {
                                                            getActivity().runOnUiThread(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    Toast.makeText(getContext(), "팔로우 취소를 완료하였습니다.", Toast.LENGTH_LONG).show();
                                                                    getFollowingList();
                                                                }
                                                            });
                                                        }

                                                        @Override
                                                        public void onFailure(@NotNull ApolloException e) {

                                                        }
                                                    });
                                                    break;
                                            }
                                        }
                                    };
                                    adapter = new FollowingRecyclerAdapter(cards, getContext(), mOnCardListener);
                                    v_recycle.setAdapter(adapter);
                                    Button btn_following = ((FollowActivity) getActivity()).findViewById(R.id.btn_following);
                                    btn_following.setText(String.valueOf(cnt) + " 팔로잉");

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

    public void filterSeq(CharSequence charsequence){
        adapter.getFilter().filter(charsequence);
    }

}
