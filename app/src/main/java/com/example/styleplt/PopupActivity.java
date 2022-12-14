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

        intent = getIntent();// ????????? ????????????
        documentid = intent.getStringExtra("upload_documentID"); //Adapter?????? ?????? ?????? ??????
        collectionid = intent.getStringExtra("upload_collectionID");

        popup_documentid.setText(documentid);
        popup_collectionid.setText(collectionid);
        // ????????????
        iv_popup_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(); // ????????? ?????? ????????????
                setResult(RESULT_OK, intent); // ?????? ?????????
                finish(); // ?????? ???????????? ?????????
            }
        });

        //????????? ?????? ?????? ??????
        popup_ratingbar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                popup_starpoint.setText(String.valueOf(rating));
            }
        });

        // ?????? ?????????
        btn_shutdown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Map<String, Object> data = new HashMap<>();
                // data??? ????????? ??????(??????) -> ????????? ????????? ????????? ????????? ?????? ??????
                data.put(FirebaseID.documentId, mAuth.getCurrentUser().getUid());
                data.put(FirebaseID.nickname, nickname); // ?????? ???????????? ???????????? ?????????
                data.put(FirebaseID.timestamp, FieldValue.serverTimestamp());  // ?????? ??????
                data.put(FirebaseID.rating, popup_ratingbar.getRating());
                mStore.collection(FirebaseID.upload).document(collectionid).collection(FirebaseID.uploadRating)
                        .document(documentid).set(data, SetOptions.merge());

                // ?????? ????????? ?????????????????? db??? ???????????? 1??? ??????
                mStore.collection(FirebaseID.upload).document(collectionid).update("ratingcount",FieldValue.increment(1));

                // ??? ???????????? ?????? ????????? = ???????????? ?????? ?????? + ????????? ?????? ?????? ??????
                totalscore = Float.parseFloat(existingtotal) + popup_ratingbar.getRating();
                // ??? ?????? = ??? ???????????? ?????? / ??? ????????? ??????
                totalrating = totalscore / Float.parseFloat(existingcount);

                //?????? ????????? ????????? db??? ?????? ????????????
                mStore.collection(FirebaseID.upload).document(collectionid).update("TOTAL_SCORE", totalscore);
                mStore.collection(FirebaseID.upload).document(collectionid).update(FirebaseID.rating, totalrating);

                // ??????
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

        // ????????? ?????? ??? ????????? ?????? ????????????
        mStore.collection(FirebaseID.upload).document(collectionid)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if(document.exists()) {
                                // (String) ?????? ????????? ????????? ?????????. String.valueof ??????
                                // null??? ?????????
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