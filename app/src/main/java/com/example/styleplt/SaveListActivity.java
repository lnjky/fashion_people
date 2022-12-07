package com.example.styleplt;

import static com.example.styleplt.utility.FirebaseID.collectionId;
import static com.example.styleplt.utility.FirebaseID.documentId;
import static com.example.styleplt.utility.FirebaseID.time;
import static com.example.styleplt.utility.FirebaseID.timestamp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.example.styleplt.adapter.SaveAdapter;
import com.example.styleplt.adapter.UploadAdapter;
import com.example.styleplt.models.Save;
import com.example.styleplt.models.Upload;
import com.example.styleplt.utility.FirebaseID;
import com.example.styleplt.utility.RecyclerDecoration;
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

public class SaveListActivity extends AppCompatActivity {

    private ImageView iv_save_back;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore mStore = FirebaseFirestore.getInstance();
    private FirebaseStorage mStorage = FirebaseStorage.getInstance();

    private RecyclerView mSaveRecyclerView;
    private SaveAdapter mAdapter;
    private List<Save> mDatas;

    private String currentid = mAuth.getCurrentUser().getUid();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_list);

        iv_save_back = findViewById(R.id.iv_save_back);
        mSaveRecyclerView = findViewById(R.id.rv_save_image);

        iv_save_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(); // 인텐트 객체 생성하고
                setResult(RESULT_OK, intent); // 응답 보내기
                finish(); // 현재 액티비티 없애기
            }
        });

        RecyclerDecoration spaceDecoration = new RecyclerDecoration(20);
        mSaveRecyclerView.addItemDecoration(spaceDecoration);

    }

    @Override
    public void onStart() {
        super.onStart();

        // inent로 이미지 uri 받아오기 + 데이터 세팅하는거 수정
        mDatas = new ArrayList<>();
        mStore.collection(FirebaseID.save).document(currentid).collection("uploaded")
                .orderBy(time, Query.Direction.DESCENDING)    // DESCENDING = 오름차순, ASCENDING = 내림차순 정렬
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (value != null) {
                            mDatas.clear();
                            for (DocumentSnapshot snap : value.getDocuments()) {
                                Map<String, Object> shot = snap.getData();
                                String documentID = String.valueOf(shot.get((documentId)));
                                String nickname = String.valueOf(shot.get(FirebaseID.nickname));
                                String image = String.valueOf(shot.get(FirebaseID.image));
                                String contents = String.valueOf(shot.get(FirebaseID.contents));
                                String time = String.valueOf(shot.get(FirebaseID.time));
                                Save data = new Save(documentId, contents, image, nickname, timestamp, time);
                                mDatas.add(data);
                            }
                            mAdapter = new SaveAdapter(mDatas);
                            mSaveRecyclerView.setAdapter(mAdapter);
                        }
                    }
                });
    }
}