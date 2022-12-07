package com.example.styleplt.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.styleplt.SaveActivity;
import com.example.styleplt.SaveListActivity;
import com.example.styleplt.UploadActivity;
import com.example.styleplt.UploadedImageActivity;
import com.example.styleplt.adapter.UploadedImageAdapter;
import com.example.styleplt.utility.FirebaseID;
import com.example.styleplt.ViewModel.ProfileFragmentViewModel;
import com.example.styleplt.R;
import com.example.styleplt.UpdateProfile;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {
    private View view;

    private ProfileFragmentViewModel mViewModel;
    private TextView tv_profile_id;
    private TextView tv_profile_pass;
    private ImageView profile_settings;
    private LinearLayout profile_uploaded_image;
    private LinearLayout profile_save_image;


    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore mStore = FirebaseFirestore.getInstance();

    private String nickname;
    private String Uid;
    private String password;
    private String age;

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @org.jetbrains.annotations.NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.frag_profile, container, false);

        tv_profile_id = (TextView) view.findViewById(R.id.tv_profile_id);
        tv_profile_pass = (TextView) view.findViewById(R.id.tv_profile_pass);
        profile_settings = (ImageView) view.findViewById(R.id.profile_settings);
        profile_uploaded_image = (LinearLayout) view.findViewById(R.id.profile_uploaded_image);
        profile_save_image = (LinearLayout) view.findViewById(R.id.profile_save_image);

        profile_uploaded_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), UploadedImageActivity.class); //fragment라서 activity intent와는 다른 방식
                // intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });

        profile_save_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), SaveListActivity.class); //fragment라서 activity intent와는 다른 방식
                // intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });

        profile_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), UpdateProfile.class); //fragment라서 activity intent와는 다른 방식
                // intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);   화면 전환 애니메이션 제거
                intent.putExtra("nickname", nickname);
                intent.putExtra("Uid", Uid);
                intent.putExtra("password", password);
                intent.putExtra("age", age);
                startActivity(intent);
                Log.d("intent","nickname ,uid, password, age" + nickname + Uid + password + age);
                //Toast.makeText(getActivity(), "uid = " + Uid, Toast.LENGTH_SHORT).show();
            }
        });
        return view;

    }

    @Override
    public void onStart() {
        super.onStart();

        // 파이어 스토어에서 현재 유저의 필드에 저장된 닉네임, 유저 아이디, 이메일, 비밀번호 등 가져오는 구문
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
                                    Uid = (String) document.getData().get(FirebaseID.documentId);
                                    password = (String) document.getData().get(FirebaseID.password);
                                    age = (String) document.getData().get(FirebaseID.age);
                                    tv_profile_id.setText(nickname);
                                }
                                else
                                    Log.d("TAG","Document is not exists");
                            }
                        }
                    });
        }

    }

    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(ProfileFragmentViewModel.class);
        // TODO: Use the ViewModel
    }
}