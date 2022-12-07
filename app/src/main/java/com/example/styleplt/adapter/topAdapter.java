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
import com.example.styleplt.models.Outer;
import com.example.styleplt.models.Top;

import java.util.List;

public class topAdapter extends RecyclerView.Adapter<topAdapter.topViewHolder> {

    private List<Top> datas;

    public topAdapter(List<Top> datas) {
        this.datas = datas;
    }

    @NonNull
    @Override
    public topAdapter.topViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new topViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_top, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull topAdapter.topViewHolder holder, int position) {
        Top data = datas.get(position);
        Glide.with(holder.itemView)
                .load(datas.get(position).getImage())
                .into(holder.item_top_image);
        holder.url.setText(data.getUrl());
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    public class topViewHolder extends RecyclerView.ViewHolder {

        private ImageView item_top_image;
        private TextView url;

        public topViewHolder(@NonNull View itemView) {
            super(itemView);

            item_top_image = itemView.findViewById(R.id.item_top_image);
            url = itemView.findViewById(R.id.url);
            item_top_image.setOnClickListener(new View.OnClickListener() {
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
