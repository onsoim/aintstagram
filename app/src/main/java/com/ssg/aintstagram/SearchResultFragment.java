package com.ssg.aintstagram;

import android.content.Intent;
import android.graphics.Bitmap;
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
import com.bumptech.glide.Glide;
import com.kakao.auth.Session;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import okhttp3.OkHttpClient;

public class SearchResultFragment extends Fragment implements SearchRecyclerAdapter.OnCardListener {
    RecyclerView v_recycle;
    private SearchRecyclerAdapter adapter;
    private ArrayList<SearchCard> cards = new ArrayList<>();
    private SearchRecyclerAdapter.OnCardListener onCardListener;
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

    public void RenewView(String input){
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);;
        v_recycle.setLayoutManager(linearLayoutManager);

        cards.clear();

        final OkHttpClient okHttpClient2 = new OkHttpClient.Builder().build();
        ApolloClient apolloClient = ApolloClient.builder().serverUrl(getString(R.string.api_url)).okHttpClient(okHttpClient2).build();
        Token = Session.getCurrentSession().getTokenInfo().getAccessToken();
        final UserTypeQuery p = UserTypeQuery.builder().name(input).accessToken(Token).search(1).build();

        apolloClient.query(p).enqueue(new ApolloCall.Callback<UserTypeQuery.Data>() {
            @Override
            public void onResponse(@NotNull Response<UserTypeQuery.Data> response) {
                cnt = response.data().users().size();

                for (int i = 0; i < cnt; i++) {
                    String profile_url = getString(R.string.media_url) + response.data().users().get(i).profile;
                    String name =  response.data().users().get(i).name;
                    String comment = response.data().users().get(i).textComment;
                    try {
                        Bitmap bitmap = Glide
                                .with(getActivity())
                                .asBitmap()
                                .load(profile_url)
                                .submit().get();

                        cards.add(new SearchCard(bitmap, name, comment));
                    } catch (ExecutionException e) {
                        cards.add(new SearchCard(name, comment));
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
                                    SearchRecyclerAdapter.OnCardListener mCardListener = new SearchRecyclerAdapter.OnCardListener() {
                                        @Override
                                        public void onCardClick(int pos) {
                                            String name = cards.get(pos).getName();

                                            Intent profileIntent = new Intent(getContext().getApplicationContext(), SearchResultProfileActivity.class);
                                            profileIntent.putExtra("username", name);
                                            startActivity(profileIntent);

                                        }
                                    };

                                    adapter = new SearchRecyclerAdapter(cards, getContext(), mCardListener);
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
    public void onCardClick(int pos) {

    }
}