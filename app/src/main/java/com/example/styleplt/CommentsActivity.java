package com.example.styleplt;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.styleplt.adapter.CommentsAdapter;
import com.example.styleplt.models.Comments;
import com.example.styleplt.utility.FirebaseID;
import com.example.styleplt.utility.RecyclerDecoration;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommentsActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseFirestore mStore = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private ImageView iv_comment_back;
    private TextView tv_post_title;
    private TextView tv_post_contents;
    private TextView tv_tool_title;
    private TextView bar_nickname;
    private TextView bar_time;
    private EditText et_write_comments;
    private ImageView btn_write_comments;
    private Button btn_post_delete;

    private Intent intent;

    private String title;
    private String board_nickname;
    private String nickname;
    private String contents;
    private String board_documentID;
    private String documentID;
    private String board_timestamp;
    private String board_collectionID;
    // 리사이클러뷰
    private RecyclerView mCommentsRecyclerView;
    private CommentsAdapter mAdapter;
    private List<Comments> mDatas;

    long now = System.currentTimeMillis();
    Date mDate = new Date(now);
    // 날짜, 시간의 형식 설정
    SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss");
    String current_time = simpleDateFormat1.format(mDate);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        tv_post_title = findViewById(R.id.tv_post_title);
        tv_post_contents = findViewById(R.id.tv_post_contents);
        tv_tool_title = findViewById(R.id.tv_tool_title);
        et_write_comments = findViewById(R.id.et_write_comments);
        bar_nickname = findViewById(R.id.bar_nickname);
        bar_time = findViewById(R.id.bar_time);
        btn_write_comments = findViewById(R.id.btn_write_comments);
        iv_comment_back = findViewById(R.id.iv_comment_back);
        btn_post_delete = findViewById(R.id.btn_post_delete);

        //글 작성자의 닉네임, 내용, uID, 제목
        intent = getIntent();// 인텐트 받아오기
        title = intent.getStringExtra("board_title"); //Adapter에서 받은 키값 연결
        board_nickname = intent.getStringExtra("board_nickname");
        contents = intent.getStringExtra("board_contents");
        board_documentID = intent.getStringExtra("board_documentID");
        board_timestamp = intent.getStringExtra("board_timestamp");
        board_collectionID = intent.getStringExtra("board_collectionID");

        //작성했던 글 텍스트 배치
        // 데이터 섞임으로 인해 컨텐츠, 닉네임, 타이틀 순서 섞임
        tv_tool_title.setText(contents);
        tv_post_title.setText(contents);
        tv_post_contents.setText(board_nickname);
        bar_time.setText(" 작성 시간 : " + board_timestamp);
        bar_nickname.setText(" 작성자 : " + title);

        //Toast.makeText(getApplicationContext(), "uid : " + mAuth.getCurrentUser().getUid(), Toast.LENGTH_SHORT).show();

        // CommentsActivity에서 CommentsAdapter에 board_documentID intent
        Intent intent1 = new Intent(CommentsActivity.this, CommentsAdapter.class);
        intent1.putExtra("board_documentID", board_documentID);

        findViewById(R.id.iv_comment_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(); // 인텐트 객체 생성하고
                setResult(RESULT_OK, intent); // 응답 보내기
                finish(); // 현재 액티비티 없애기
            }
        });

        // 삭제
        if((mAuth.getCurrentUser().getUid()).equals(board_documentID)) {
            btn_post_delete.setVisibility(View.VISIBLE);
            btn_post_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(CommentsActivity.this);
                    builder.setMessage("작성한 글을 삭제하시겠습니까?");
                    builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {      // 버튼1 (직접 작성)
                        public void onClick(DialogInterface dialog, int which) {
                            mStore.collection(FirebaseID.post).document(board_collectionID)
                                    .delete()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Log.d("delete : ", "success");
                                            finish();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.d("delete : ", "failed");
                                        }
                                    });
                        }
                    });
                    builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    builder.show();// TestActivity 부분에는 현재 Activity의 이름 입력.
                }
            });

        }
        else
            btn_post_delete.setVisibility(View.INVISIBLE); // 작성자와 현재 사용자가 다르면 삭제버튼 안보이게 설정

        mCommentsRecyclerView = findViewById(R.id.rv_comments);
        btn_write_comments.setOnClickListener(this);

        RecyclerDecoration spaceDecoration = new RecyclerDecoration(20);
        mCommentsRecyclerView.addItemDecoration(spaceDecoration);

    }

    //작성 이미지 클릭시 행동
    @Override
    public void onClick(View view) {
        if(mAuth.getCurrentUser() != null) {
            String commentId = mStore.collection(FirebaseID.post).document(board_collectionID).collection(FirebaseID.comments).getId();
            Map<String, Object> data = new HashMap<>();
            // data에 데이터 추가(삽입)
            data.put(FirebaseID.documentId, mAuth.getCurrentUser().getUid());
            data.put(FirebaseID.nickname, nickname); // 현재 사용중인 사용자의 닉네임
            data.put(FirebaseID.time, FieldValue.serverTimestamp());  // 서버 시간
            data.put(FirebaseID.comments, et_write_comments.getText().toString());  // 작성한 댓글 내용 입력
            data.put(FirebaseID.timestamp, current_time);
            mStore.collection(FirebaseID.post).document(board_collectionID).collection(FirebaseID.documentId)
                    .document(et_write_comments.getText().toString()).set(data, SetOptions.merge());
            Log.d("board_collectionID2", board_collectionID);
        }
        et_write_comments.setText(null); // 댓글 작성 후 Edit Text 비우기
    }

    @Override
    protected void onStart() {
        super.onStart();
        //현재 사용자의 uid, 닉네임 가져오기
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
                                    documentID = (String) document.getData().get(FirebaseID.documentId);
                                }
                                else
                                    Log.d("TAG","Document is not exists");
                            }
                        }
                    });
        }
        //댓글 불러오기
        mDatas = new ArrayList<>();
        mStore.collection(FirebaseID.post).document(board_collectionID).collection(FirebaseID.documentId)
                .orderBy(FirebaseID.time, Query.Direction.DESCENDING)    // DESCENDING = 오름차순, ASCENDING = 내림차순 정렬
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (value != null) {
                            mDatas.clear();
                            for (DocumentSnapshot snap : value.getDocuments()) {
                                Map<String, Object> shot = snap.getData();
                                // 작성한 댓글 불러오기
                                String documentID = String.valueOf(shot.get((FirebaseID.documentId)));  // 작성한 사용자의 uID
                                String nickname = String.valueOf(shot.get(FirebaseID.nickname));        // 작성한 사용자의 닉네임
                                String timestamp = String.valueOf(shot.get(FirebaseID.time));      // 작성한 시간
                                String comments = String.valueOf(shot.get(FirebaseID.comments));        // 작성한 댓글 내용
                                String collectionID = String.valueOf(shot.get(FirebaseID.collectionId));
                                String time = String.valueOf(shot.get(FirebaseID.timestamp));
                                Comments data = new Comments(documentID, nickname, comments, time, collectionID);
                                mDatas.add(data);
                            }
                            mAdapter = new CommentsAdapter(mDatas);
                            mCommentsRecyclerView.setAdapter(mAdapter);
                        }
                    }
                });


    }

}