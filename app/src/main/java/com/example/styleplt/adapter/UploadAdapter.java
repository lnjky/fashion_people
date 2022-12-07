package com.example.styleplt.adapter;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.LongDef;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.styleplt.MainActivity;
import com.example.styleplt.PopupActivity;
import com.example.styleplt.R;
import com.example.styleplt.SaveActivity;
import com.example.styleplt.UploadActivity;
import com.example.styleplt.fragment.HomeFragment;
import com.example.styleplt.models.Upload;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class UploadAdapter extends RecyclerView.Adapter<UploadAdapter.UploadViewHolder>{

    private FirebaseFirestore mStore = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private List<Upload> datas;

    private String collectionid2;
    private Uri uri;

    public UploadAdapter(List<Upload> datas) {
        this.datas = datas;
    }

    @NonNull
    @Override
    public UploadViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new UploadViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_upload, parent, false));
    }


    @Override
    public void onBindViewHolder(@NonNull UploadViewHolder holder, int position) {
        Upload data = datas.get(position);
        holder.nickname.setText(data.getNickname());
        holder.contents.setText(data.getContents());
        holder.documentid.setText(data.getDocumentId());
        holder.collectionid.setText(data.getCollectionId());
        holder.upload_time.setText(data.getTimestamp());
        holder.upload_ratingbar.setRating(Float.parseFloat(data.getRating().toString()));
        holder.item_upload_url.setText(data.getUrl());
        Glide.with(holder.itemView)
                .load(datas.get(position).getImage())
                .into(holder.image);
    }

    @Override
    public int getItemCount() {
        return (datas != null ? datas.size() : 0);
    }

    class UploadViewHolder extends RecyclerView.ViewHolder {

        private TextView nickname;
        private TextView contents;
        private TextView tv_ratingbar;
        private TextView documentid;
        private TextView collectionid;
        private TextView upload_time;
        private TextView item_upload_url;
        private ImageView image;
        private RatingBar upload_ratingbar;
        private ImageView item_upload_star;
        private ImageView item_upload_delete;

        public UploadViewHolder(@NonNull View itemView) {
            super(itemView);

            nickname = itemView.findViewById(R.id.item_upload_nickname);
            contents = itemView.findViewById(R.id.item_upload_contents);
            image = itemView.findViewById(R.id. item_upload_image);
            tv_ratingbar = itemView.findViewById(R.id.tv_ratingbar);
            upload_ratingbar = itemView.findViewById(R.id.upload_ratingbar);
            documentid = itemView.findViewById(R.id.item_upload_documentid);
            collectionid = itemView.findViewById(R.id.item_upload_collectionid);
            upload_time = itemView.findViewById(R.id.upload_time);
            item_upload_star = itemView.findViewById(R.id.item_upload_star);
            item_upload_delete = itemView.findViewById(R.id.item_upload_delete);
            item_upload_url = itemView.findViewById(R.id.item_upload_url);

            // 별점주기 클릭시 화면 전환 및 docuemntid, collectionid popupactivity로 전송
            tv_ratingbar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(view.getContext(), PopupActivity.class);
                    intent.putExtra("upload_documentID", documentid.getText().toString());
                    intent.putExtra("upload_collectionID", collectionid.getText().toString());
                    intent.putExtra("upload_rating", upload_ratingbar.getRating());

                    view.getContext().startActivity(intent);
                }
            });

            //
            item_upload_delete.setVisibility(View.INVISIBLE);
/*
            if((mAuth.getCurrentUser().getUid()).equals(documentid.getText().toString())) {
                Log.d("documentid : ", documentid.getText().toString());
                item_upload_delete.setVisibility(View.VISIBLE);
                DialogInterface.OnClickListener confirm = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                };
                DialogInterface.OnClickListener cancle = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                };
 *//*               new AlertDialog.Builder(view.getContext())
                        .setTitle("삭제하시겠습니까?")
                        .setPositiveButton("네", confirm)
                        .setNegativeButton("아니오", cancle)
                        .show();*//*
            }
            else {
                item_upload_delete.setVisibility(View.INVISIBLE);
            }*/



            item_upload_star.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(view.getContext(), SaveActivity.class);
                    Uri uri = Uri.parse(item_upload_url.getText().toString());
                    intent.putExtra("uri", uri); // contents:// 이렇게 나와서 이걸 url로 바꾸는 방법을 찾아내야 함
                    view.getContext().startActivity(intent);
                }
            });



        }
    }
}
