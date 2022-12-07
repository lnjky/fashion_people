package com.example.styleplt;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.styleplt.utility.FirebaseID;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class UpdateProfile extends AppCompatActivity {

    private EditText  et_update_nickname, et_update_age;
    private ImageView iv_back;
    private Button btn_update;
    private TextView tv_set_current_nickname, tv_set_current_age;

    private String nickname, Uid, password, age, email;
    private Intent intent;

    private FirebaseFirestore mStore = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);

        tv_set_current_nickname = findViewById(R.id.tv_set_current_nickname);
        tv_set_current_age = findViewById(R.id.tv_set_current_age);
        et_update_nickname = findViewById(R.id.et_update_nickname);
        et_update_age = findViewById(R.id.et_update_age);
        btn_update = findViewById(R.id.btn_update);

        iv_back = findViewById(R.id.iv_back);
        findViewById(R.id.iv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(); // 인텐트 객체 생성하고
                setResult(RESULT_OK, intent); // 응답 보내기
                finish(); // 현재 액티비티 없애기
            }
        });

        intent = getIntent();// 인텐트 받아오기
        nickname = intent.getStringExtra("nickname"); //Adapter에서 받은 키값 연결
        Uid = intent.getStringExtra("Uid");
        password = intent.getStringExtra("password");
        age = intent.getStringExtra("age");

        //작성했던 글 텍스트 배치
        tv_set_current_nickname.setText(nickname);
        tv_set_current_age.setText(age);


        btn_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 닉네임 변경
                mStore.collection(FirebaseID.user).document(mAuth.getCurrentUser().getUid())
                        .update(FirebaseID.nickname, et_update_nickname.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Log.d("update_nick", "success");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("update_nickname", "faild");
                            }
                        });
                //나이 변경
                mStore.collection(FirebaseID.user).document(mAuth.getCurrentUser().getUid())
                        .update(FirebaseID.age, et_update_age.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Log.d("update_age", "success");
                                Toast.makeText(getApplicationContext(), "회원정보 수정 완료", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("update_age", "faild");
                            }
                        });
                // 이메일, 비밀번호 변경은 따로 인증을 좀 더 공부 후
                // 블록 주석 처리 : Ctrl + Shift + /

/*                //인증 서버의 이메일 변경 후 작업 성공시 firestore 내용 변경
                AuthCredential credential = EmailAuthProvider.getCredential(mAuth.getCurrentUser().getEmail(), password);

                //이메일 재인증 후 업데이트
                mUser.reauthenticate(credential)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    mUser.updateEmail(et_update_id.getText().toString())
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                mStore.collection(FirebaseID.user).document(mAuth.getCurrentUser().getUid())
                                                        .update(FirebaseID.email, et_update_id.getText().toString())
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void unused) {
                                                                Log.d("update_email", "success");
                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Log.d("update_email", "faild");
                                                            }
                                                        });
                                            }
                                        }
                                    });
                                } else {
                                    Log.d("authentication_email", "faild");
                                }
                            }
                        });
                mUser.reauthenticate(credential)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    mUser.updatePassword(et_update_pw.getText().toString())
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        mStore.collection(FirebaseID.user).document(mAuth.getCurrentUser().getUid())
                                                                .update(FirebaseID.password, et_update_pw.getText().toString())
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void unused) {
                                                                        Log.d("update_password", "success");
                                                                    }
                                                                })
                                                                .addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        Log.d("update_password", "faild");
                                                                    }
                                                                });
                                                    }
                                                }
                                            });
                                } else {
                                    Log.d("authentication_password", "faild");
                                }
                            }
                        });*/
                finish();

            }
        });
    }
}