package com.example.styleplt;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.styleplt.utility.FirebaseID;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class SaveActivity extends AppCompatActivity {


    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseStorage mStorage = FirebaseStorage.getInstance();
    private FirebaseFirestore mStore = FirebaseFirestore.getInstance();

    private StorageReference storageRef = mStorage.getReference();

    private String documentId = mAuth.getCurrentUser().getUid();
    long now = System.currentTimeMillis();
    java.util.Date mDate = new Date(now);
    // 날짜, 시간의 형식 설정
    SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss");
    String current_time = simpleDateFormat1.format(mDate);

    String uploadID = mStore.collection(FirebaseID.upload).document().getId();
    private String nickname;

    private ImageView iv_back, iv_save_image;
    private Button btn_save;
    private EditText et_save_comment;

    private Uri uri;
    private String type;
    private String saveID = mStore.collection(FirebaseID.post).document().getId();

    private UploadTask uploadTask = null; // 파일 업로드하는 객체
    private String imageFileName;// 파일 업로드하는 객체

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save);

        iv_back = findViewById(R.id.iv_back);
        btn_save = findViewById(R.id.btn_save);
        iv_save_image = findViewById(R.id.iv_save_image);
        et_save_comment = findViewById(R.id.et_save_comment);

        Intent intent = getIntent();
        uri = getIntent().getParcelableExtra("uri");
        Log.d("uri : ", String.valueOf(uri));
        // url을 통해 받아온 사진 띄우기
        Glide.with(this).load(uri).into(iv_save_image);

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(); // 인텐트 객체 생성하고
                setResult(RESULT_OK, intent); // 응답 보내기
                finish(); // 현재 액티비티 없애기
            }
        });

        // 현재 사용자의 데이터 가져오기
        if (mAuth.getCurrentUser() != null) {
            mStore.collection(FirebaseID.user).document(mAuth.getCurrentUser().getUid())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    Log.d("TAG", "Document is exists");
                                    nickname = (String) document.getData().get(FirebaseID.nickname);
                                } else
                                    Log.d("TAG", "Document is not exists");
                            }
                        }
                    });
        }

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //작성한 글, 닉네임 등을 파이어스토어의 upload 컬렉션에 올리는 코드
                Map<String, Object> data = new HashMap<>();
                data.put(FirebaseID.documentId, mAuth.getCurrentUser().getUid());
                data.put(FirebaseID.nickname, nickname);
                data.put(FirebaseID.contents, et_save_comment.getText().toString());
                data.put(FirebaseID.image, uri);
                data.put(FirebaseID.time, FieldValue.serverTimestamp());
                data.put(FirebaseID.timestamp, current_time);
                data.put(FirebaseID.collectionId, uploadID);
                mStore.collection(FirebaseID.save).document(documentId).collection("uploaded").document().set(data, SetOptions.merge());

                getIntent().getExtras().clear();
                finish();
            }
        });

    }

    // 지정한 경로(reference)에 대한 uri 을 다운로드하는 method
    // uri를 통해 이미지를 불러올 수 있음
    void downloadUri() {
        storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                //다운받은 uri를 인텐트에 넣어 다른 액티비티로 이동
                //Log.d(TAG, "onSuccess: download");
                //imageUri = uri;
                //Intent intent = new Intent(UploadActivity.this, HomeFragment.class);
                //intent.putExtra("path", uri.toString()); // 다운로드한 uri, String 형으로 바꿔 인텐트에 넣기
                //startActivity(intent);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("TAG", "onFailure: download");
            }
        });
    }
}