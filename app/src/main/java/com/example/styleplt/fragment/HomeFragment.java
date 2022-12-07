package com.example.styleplt.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.styleplt.utility.FirebaseID;
import com.example.styleplt.ViewModel.HomeFragmentViewModel;
import com.example.styleplt.R;
import com.example.styleplt.UploadActivity;
import com.example.styleplt.WeatherActivity;
import com.example.styleplt.adapter.UploadAdapter;
import com.example.styleplt.models.Upload;
import com.example.styleplt.utility.RecyclerDecoration;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {
    private View view;

    private TextView tv_home_weather;

    private FloatingActionButton floatingActionButton;

    private HomeFragmentViewModel mViewModel;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore mStore = FirebaseFirestore.getInstance();
    private FirebaseStorage mStorage = FirebaseStorage.getInstance();

    private RecyclerView mUploadRecyclerView;
    private UploadAdapter mAdapter;
    private List<Upload> mDatas;

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @org.jetbrains.annotations.NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.frag_home, container, false);

        tv_home_weather = (TextView) view.findViewById(R.id.tv_home_weather);
        tv_home_weather.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), WeatherActivity.class); //fragment라서 activity intent와는 다른 방식
                // intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);   화면 전환 애니메이션 제거
                startActivity(intent);
            }
        });


        floatingActionButton = view.findViewById(R.id.board_write);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), UploadActivity.class); //fragment라서 activity intent와는 다른 방식
                // intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });
        mUploadRecyclerView = view.findViewById(R.id.home_RecyclerView);
        mDatas = new ArrayList<>();

        return view;
    }

    // 시작시 리사이클러뷰를 통해 작성한 글 나열
    @Override
    public void onStart() {
        super.onStart();

        // inent로 이미지 uri 받아오기 + 데이터 세팅하는거 수정
        mDatas = new ArrayList<>();
        mStore.collection(FirebaseID.upload)
                .orderBy(FirebaseID.timestamp, Query.Direction.DESCENDING)    // DESCENDING = 오름차순, ASCENDING = 내림차순 정렬
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (value != null) {
                            mDatas.clear();
                            for (DocumentSnapshot snap : value.getDocuments()) {
                                Map<String, Object> shot = snap.getData();
                                String documentID = String.valueOf(shot.get((FirebaseID.documentId)));
                                String nickname = String.valueOf(shot.get(FirebaseID.nickname));
                                String image = String.valueOf(shot.get(FirebaseID.image));
                                String contents = String.valueOf(shot.get(FirebaseID.contents));
                                String collectionID = String.valueOf(shot.get(FirebaseID.collectionId));
                                String rating = String.valueOf(shot.get(FirebaseID.rating));
                                String time = String.valueOf(shot.get(FirebaseID.timestamp));
                                String url = String.valueOf(shot.get("url"));
                                Upload data = new Upload(documentID, contents, nickname, image, collectionID, rating, time, url);
                                mDatas.add(data);
                            }
                            mAdapter = new UploadAdapter(mDatas);
                            mUploadRecyclerView.setAdapter(mAdapter);

                            RecyclerDecoration spaceDecoration = new RecyclerDecoration(20);
                            mUploadRecyclerView.addItemDecoration(spaceDecoration);
                        }
                    }
                });
    }

    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(HomeFragmentViewModel.class);
        // TODO: Use the ViewModel
    }
}