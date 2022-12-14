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
    // ??????, ????????? ?????? ??????
    SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss");
    String current_time = simpleDateFormat1.format(mDate);

    private UploadTask uploadTask = null; // ?????? ??????????????? ??????
    private String imageFileName = "IMAGE_" + documentId + "_" + uploadID + "_.png"; // ?????????
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
                Intent intent = new Intent(); // ????????? ?????? ????????????
                setResult(RESULT_OK, intent); // ?????? ?????????
                finish(); // ?????? ???????????? ?????????
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

        // ?????? ???????????? ????????? ????????????
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
                //????????? ??????????????? ????????? ??????
                // ????????? ?????? ?????? ?????? (/item/????????? documentId / IAMGE_DOCUMENTID_UPLOADID_.png)
                storageRef = mStorage.getReference().child("image").child(documentId).child(imageFileName);
                uploadTask = storageRef.putFile(imageUri); // ???????????? ????????? ???????????? ?????? ??????
                //?????? ????????? ??????
                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //????????? ?????? ??? ???????????? ?????? url ????????????
                        Log.d(TAG, "onSuccess: upload");
                        downloadUri(); // ????????? ?????? ??? ???????????? ?????? Uri ????????????
                        storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                url = uri.toString();
                                Log.d("uri : ", uri.toString());

                                //????????? ???, ????????? ?????? ????????????????????? upload ???????????? ????????? ??????
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
                      //????????? ?????? ??? ??????
                        Log.d(TAG, "onFailure: upload");
                    }
                });

            }
        });


    }

    // ????????? ???????????? ???????????? ??????
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

    // ????????? ??????(reference)??? ?????? uri ??? ?????????????????? method
    // uri??? ?????? ???????????? ????????? ??? ??????
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

