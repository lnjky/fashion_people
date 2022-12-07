package com.example.styleplt;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class UploadActivity extends AppCompatActivity {

    private ImageView iv_upload_back, iv_upload_image;
    private Button btn_upload, btn_upload_gallery;
    private EditText et_upload_contents;

    final int GET_GALLERY_IMAGE = 200;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore mStore = FirebaseFirestore.getInstance();
    private FirebaseStorage mStorage = FirebaseStorage.getInstance();

    private String nickname, documentId;

    private Uri imageUri;
    private String pathUri;
    private String TAG = "IMAGE";

    String uploadID = mStore.collection(FirebaseID.upload).document().getId();
    private StorageReference storageRef = mStorage.getReference();

    long now = System.currentTimeMillis();
    java.util.Date mDate = new Date(now);
    // 날짜, 시간의 형식 설정
    SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss");
    String current_time = simpleDateFormat1.format(mDate);

    private UploadTask uploadTask = null; // 파일 업로드하는 객체
    private String imageFileName = "IMAGE_" + documentId + "_" + uploadID + "_.png"; // 파일명
    private String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        iv_upload_back = findViewById(R.id.iv_upload_back);
        btn_upload = findViewById(R.id.btn_upload);

        iv_upload_image = findViewById(R.id.iv_upload_image);
        btn_upload_gallery = findViewById(R.id.btn_upload_gallery);
        et_upload_contents = findViewById(R.id.et_upload_contents);


        iv_upload_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(); // 인텐트 객체 생성하고
                setResult(RESULT_OK, intent); // 응답 보내기
                finish(); // 현재 액티비티 없애기
            }
        });

        btn_upload_gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityResult.launch(intent);
            }
        });

        iv_upload_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityResult.launch(intent);
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
                                    documentId = (String) document.getData().get(FirebaseID.documentId);
                                } else
                                    Log.d("TAG", "Document is not exists");
                            }
                        }
                    });
        }

        btn_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //사진을 스토리지에 올리는 코드
                // 이미지 파일 경로 지정 (/item/사용자 documentId / IAMGE_DOCUMENTID_UPLOADID_.png)
                storageRef = mStorage.getReference().child("image").child(documentId).child(imageFileName);
                uploadTask = storageRef.putFile(imageUri); // 업로드할 파일과 업로드할 위치 설정
                //파일 업로드 시작
                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //업로드 성공 시 이미지를 올린 url 가져오기
                        Log.d(TAG, "onSuccess: upload");
                        downloadUri(); // 업로드 성공 시 업로드한 파일 Uri 다운받기
                        storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                url = uri.toString();
                                Log.d("uri : ", uri.toString());

                                //작성한 글, 닉네임 등을 파이어스토어의 upload 컬렉션에 올리는 코드
                                Map<String, Object> data = new HashMap<>();
                                data.put(FirebaseID.documentId, mAuth.getCurrentUser().getUid());
                                data.put(FirebaseID.nickname, nickname);
                                data.put(FirebaseID.contents, et_upload_contents.getText().toString());
                                data.put(FirebaseID.collectionId, uploadID);
                                data.put(FirebaseID.image, imageUri);
                                data.put(FirebaseID.time, FieldValue.serverTimestamp());
                                data.put(FirebaseID.timestamp, current_time);
                                data.put("TOTAL_SCORE", 0);
                                data.put(FirebaseID.ratingcount, 1);
                                data.put(FirebaseID.rating, 0.0F);
                                data.put("url", url);
                                mStore.collection(FirebaseID.upload).document(uploadID).set(data, SetOptions.merge());
                                finish();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                      //업로드 실패 시 동작
                        Log.d(TAG, "onFailure: upload");
                    }
                });

            }
        });


    }

    // 클릭시 갤러리로 이동하는 구문
    ActivityResultLauncher<Intent> startActivityResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if ( result.getResultCode() == RESULT_OK && result.getData() != null) {
                imageUri = result.getData().getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                    iv_upload_image.setImageBitmap(bitmap);
                }
                catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    });

    // 지정한 경로(reference)에 대한 uri 을 다운로드하는 method
    // uri를 통해 이미지를 불러올 수 있음
    void downloadUri() {
        storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: download");
            }
        });
    }
}

