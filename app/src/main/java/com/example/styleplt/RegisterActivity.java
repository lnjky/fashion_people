package com.example.styleplt;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.styleplt.utility.FirebaseID;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText et_register_id, et_register_pw, et_register_age, et_register_nickname;
    private Button btn_register_button;

    //파이어베이스 초기화
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore mStore = FirebaseFirestore.getInstance();

    final String FIRESTORE_TAG = "[FIRESTORE_TAG]";

    @Override
    protected void onCreate(Bundle savedInstanceState) {   // 액티비티 시작시 처음으로 시작되는 생명주기
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // xml파일에서 선언한 아이디 연결
        et_register_id = findViewById(R.id.et_register_id);
        et_register_pw = findViewById(R.id.et_register_pw);
        et_register_nickname = findViewById(R.id.et_register_nickname);
        et_register_age = findViewById(R.id.et_register_age);

        btn_register_button = findViewById(R.id.btn_register_button);

        findViewById(R.id.btn_register_button).setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        final String email = et_register_id.getText().toString().trim();
        final String password = et_register_pw.getText().toString().trim();
        final String nickname = et_register_nickname.getText().toString().trim();
        final String age = et_register_age.getText().toString().trim();

        if ((email != null) && !email.isEmpty() && (password != null) && !password.isEmpty() && (nickname != null) && !nickname.isEmpty() && (age != null) && !age.isEmpty() ) {
            mAuth.createUserWithEmailAndPassword(et_register_id.getText().toString(), et_register_pw.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                FirebaseUser user = mAuth.getCurrentUser();

                                Map<String, Object> userMap = new HashMap<>();
                                userMap.put(FirebaseID.documentId, user.getUid());
                                userMap.put(FirebaseID.email, email);
                                userMap.put(FirebaseID.password, password);
                                userMap.put(FirebaseID.nickname, nickname);
                                userMap.put(FirebaseID.age, age);
                                //현재 유저의 Uid를 이름으로 한 document 생성. 이게 없으면 사용자 컨텐츠의 이륾과 사용자id이름이 달라 사용하기 힘듬
                                mStore.collection(FirebaseID.user).document(user.getUid()).set(userMap, SetOptions.merge());

                                //회원가입 성공시 로그인 액티비티로 화면 전환
                                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                startActivity(intent);
                                finish();

                            } else {
                                // If sign in fails, display a message to the user.
                                Toast.makeText(RegisterActivity.this, "회원가입 실패",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }
}