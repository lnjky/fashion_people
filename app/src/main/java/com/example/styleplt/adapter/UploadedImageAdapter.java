package com.example.styleplt.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.styleplt.R;
import com.example.styleplt.models.UploadedImage;

import java.util.List;

public class UploadedImageAdapter extends RecyclerView.Adapter<UploadedImageAdapter.UploadedImageViewHolder> {

    private List<String> imageDatas;

    public UploadedImageAdapter(List<String> imageDatas) {
        this.imageDatas = imageDatas;
    }

    @NonNull
    @Override
    public UploadedImageAdapter.UploadedImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new UploadedImageViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_uploaded_image, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull UploadedImageAdapter.UploadedImageViewHolder holder, int position) {
        Glide.with(holder.itemView.getContext()).load(imageDatas.get(position)).into(holder.Uploaded_image);
    }

    @Override
    public int getItemCount() {
        return imageDatas.size();
    }

    public class UploadedImageViewHolder extends RecyclerView.ViewHolder {

        private ImageView Uploaded_image;
        public UploadedImageViewHolder(@NonNull View itemView) {
            super(itemView);
            Uploaded_image = itemView.findViewById(R.id.item_uploaded_image);
        }
    }
}
