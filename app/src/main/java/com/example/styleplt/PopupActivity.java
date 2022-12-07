package com.example.styleplt;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.styleplt.utility.FirebaseID;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class PopupActivity extends AppCompatActivity {

    private RatingBar popup_ratingbar;
    private Button btn_shutdown;
    private TextView popup_starpoint, popup_documentid, popup_collectionid;
    private ImageView iv_popup_back;

    private Intent intent;
    private String documentid;
    private String collectionid;
    private String nickname;
    private String current_documentID;
    private String existingrating;
    private String existingcount;
    private String existingtotal;

    private float totalrating;
    private float totalscore;

    private FirebaseFirestore mStore = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popup);

        popup_ratingbar = findViewById(R.id.popup_ratingbar);
        btn_shutdown = findViewById(R.id.btn_shutdown);
        popup_starpoint = findViewById(R.id.popup_starpoint);
        iv_popup_back = findViewById(R.id.iv_popup_back);
        popup_documentid = findViewById(R.id.popup_documentid);
        popup_collectionid = findViewById(R.id.popup_collectionid);

        intent = getIntent();// 인텐트 받아오기
        documentid = intent.getStringExtra("upload_documentID"); //Adapter에서 받은 키값 연결
        collectionid = intent.getStringExtra("upload_collectionID");

        popup_documentid.setText(documentid);
        popup_collectionid.setText(collectionid);
        // 뒤로가기
        iv_popup_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(); // 인텐트 객체 생성하고
                setResult(RESULT_OK, intent); // 응답 보내기
                finish(); // 현재 액티비티 없애기
            }
        });

        //별점에 따라 숫자 표기
        popup_ratingbar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                popup_starpoint.setText(String.valueOf(rating));
            }
        });

        // 완료 클릭시
        btn_shutdown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Map<String, Object> data = new HashMap<>();
                // data에 데이터 추가(삽입) -> 별점을 부여한 사람의 컬렉션 새로 생성
                data.put(FirebaseID.documentId, mAuth.getCurrentUser().getUid());
                data.put(FirebaseID.nickname, nickname); // 현재 사용중인 사용자의 닉네임
                data.put(FirebaseID.timestamp, FieldValue.serverTimestamp());  // 서버 시간
                data.put(FirebaseID.rating, popup_ratingbar.getRating());
                mStore.collection(FirebaseID.upload).document(collectionid).collection(FirebaseID.uploadRating)
                        .document(documentid).set(data, SetOptions.merge());

                // 점수 부여시 파이어스토어 db의 카운트가 1씩 증가
                mStore.collection(FirebaseID.upload).document(collectionid).update("ratingcount",FieldValue.increment(1));

                // 총 부여받은 점수 계산식 = 이전까지 받은 점수 + 별점을 통해 받은 점숫
                totalscore = Float.parseFloat(existingtotal) + popup_ratingbar.getRating();
                // 총 별점 = 총 부여받은 점수 / 총 부여한 횟수
                totalrating = totalscore / Float.parseFloat(existingcount);

                //위에 계산한 식대로 db의 정보 업데이트
                mStore.collection(FirebaseID.upload).document(collectionid).update("TOTAL_SCORE", totalscore);
                mStore.collection(FirebaseID.upload).document(collectionid).update(FirebaseID.rating, totalrating);

                // 실패
                //UploadAdapter.UploadViewHolder.tv_ratingbar.setVisibility(View.INVISIBLE);
                finish();
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
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
                                    current_documentID = (String) document.getData().get(FirebaseID.documentId);
                                }
                                else
                                    Log.d("TAG","Document is not exists");
                            }
                        }
                    });
        }

        // 기존의 별점 및 카운트 횟수 가져오기
        mStore.collection(FirebaseID.upload).document(collectionid)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if(document.exists()) {
                                // (String) 이거 사용시 어플이 종료됨. String.valueof 사용
                                // null값 리턴함
                                existingtotal = String.valueOf(document.getData().get("TOTAL_SCORE"));
                                existingrating = String.valueOf(document.getData().get(FirebaseID.rating));
                                existingcount = String.valueOf(document.getData().get(FirebaseID.ratingcount));

                                //Toast.makeText(getApplicationContext(), "rating : " + existingrating, Toast.LENGTH_SHORT).show();
                                //Toast.makeText(getApplicationContext(), "count : " + existingcount, Toast.LENGTH_SHORT).show();
                            }
                            else
                                Toast.makeText(getApplicationContext(), "not document", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }
}