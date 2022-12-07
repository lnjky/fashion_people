package com.example.styleplt.adapter;

import android.content.Intent;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.styleplt.ImageDetailActivity;
import com.example.styleplt.R;
import com.example.styleplt.models.Bottom;
import com.example.styleplt.models.Top;

import java.util.List;

public class bottomAdapter extends RecyclerView.Adapter<bottomAdapter.bottomViewHolder> {

    private List<Bottom> datas;

    public bottomAdapter(List<Bottom> datas) {
        this.datas = datas;
    }

    @NonNull
    @Override
    public bottomAdapter.bottomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new bottomAdapter.bottomViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bottom, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull bottomAdapter.bottomViewHolder holder, int position) {
        Bottom data = datas.get(position);
        Glide.with(holder.itemView)
                .load(datas.get(position).getImage())
                .into(holder.item_bottom_image);
        holder.url.setText(data.getUrl());
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    public class bottomViewHolder extends RecyclerView.ViewHolder {

        private ImageView item_bottom_image;
        private TextView url;

        public bottomViewHolder(@NonNull View itemView) {
            super(itemView);

            item_bottom_image = itemView.findViewById(R.id.item_bottom_image);
            url = itemView.findViewById(R.id.url);
            item_bottom_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(view.getContext(), ImageDetailActivity.class);
                    intent.putExtra("url", url.getText().toString());
                    view.getContext().startActivity(intent);
                }
            });
        }
    }
}
