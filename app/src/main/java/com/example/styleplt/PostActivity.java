package com.example.styleplt;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.styleplt.utility.FirebaseID;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class PostActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText et_title;
    private EditText et_contents;
    private ImageView iv_back;


    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore mStore = FirebaseFirestore.getInstance();

    private String nickname;
    private String documentId;

    long now = System.currentTimeMillis();
    Date mDate = new Date(now);

    // 날짜, 시간의 형식 설정
    SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss");
    String current_time = simpleDateFormat1.format(mDate);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        et_title = findViewById(R.id.et_post_title);
        et_contents = findViewById(R.id.et_post_contents);
        iv_back = findViewById(R.id.iv_back);

        findViewById(R.id.btn_post).setOnClickListener(this);
        findViewById(R.id.iv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(); // 인텐트 객체 생성하고
                setResult(RESULT_OK, intent); // 응답 보내기
                finish(); // 현재 액티비티 없애기
            }
        });

        if (mAuth.getCurrentUser() != null) {
            mStore.collection(FirebaseID.user).document(mAuth.getCurrentUser().getUid())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if(document.exists()) {
                                    Log.d("TAG","Document is exists");
                                    nickname = (String) document.getData().get(FirebaseID.nickname);
                                    documentId = (String) document.getData().get(FirebaseID.documentId);
                                }
                                else
                                    Log.d("TAG","Document is not exists");
                            }
                        }
                    });
        }
    }
    //버튼 클릭시
    @Override
    public void onClick(View view) {
        if(mAuth.getCurrentUser() != null) {
            // 겹쳐도 상관없게 하는 구문
            String postID = mStore.collection(FirebaseID.post).document().getId();
            Map<String, Object> data = new HashMap<>();
            data.put(FirebaseID.documentId, mAuth.getCurrentUser().getUid());
            data.put(FirebaseID.title , et_title.getText().toString());
            data.put(FirebaseID.nickname, nickname);
            data.put(FirebaseID.contents , et_contents.getText().toString());
            data.put(FirebaseID.timestamp, current_time);
            data.put(FirebaseID.time, FieldValue.serverTimestamp());
            data.put(FirebaseID.collectionId, postID);
            mStore.collection(FirebaseID.post).document(postID).set(data, SetOptions.merge());
            finish();
        }
    }

    public void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.iv_back, fragment);
        fragmentTransaction.commit();
    }
}

