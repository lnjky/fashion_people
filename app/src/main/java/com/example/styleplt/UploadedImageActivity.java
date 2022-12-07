package com.example.styleplt;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.example.styleplt.adapter.UploadedImageAdapter;
import com.example.styleplt.adapter.outerAdapter;
import com.example.styleplt.models.Outer;
import com.example.styleplt.utility.RecyclerDecoration;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class UploadedImageActivity extends AppCompatActivity {

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore mStore = FirebaseFirestore.getInstance();
    private FirebaseStorage mStorage = FirebaseStorage.getInstance();

    private ImageView iv_uploaded_image_back;

    private RecyclerView mImageRecyclerview;
    private UploadedImageAdapter UploadedImageAdapter;
    private List<String> ImageDatas;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uploaded_image);

        iv_uploaded_image_back = findViewById(R.id.iv_uploaded_image_back);

        iv_uploaded_image_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(); // 인텐트 객체 생성하고
                setResult(RESULT_OK, intent); // 응답 보내기
                finish(); // 현재 액티비티 없애기
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mImageRecyclerview = findViewById(R.id.Uploaded_Recyclerview);
        mImageRecyclerview.setLayoutManager(layoutManager);
        ImageDatas = new ArrayList<>();

        StorageReference listRef = FirebaseStorage.getInstance().getReference().child("image").child(mAuth.getCurrentUser().getUid());
        listRef.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                for(StorageReference file:listResult.getItems()){
                    file.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            ImageDatas.add(uri.toString());
                            Log.e("Itemvalue",uri.toString());
                        }
                    }).addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            mImageRecyclerview.setAdapter(UploadedImageAdapter);
                        }
                    });
                }
            }
        });

        UploadedImageAdapter = new UploadedImageAdapter(ImageDatas);
        mImageRecyclerview.setAdapter(UploadedImageAdapter);

        RecyclerDecoration spaceDecoration = new RecyclerDecoration(20);
        mImageRecyclerview.addItemDecoration(spaceDecoration);
    }
}