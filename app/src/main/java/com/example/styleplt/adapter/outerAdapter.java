package com.example.styleplt.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.styleplt.ImageDetailActivity;
import com.example.styleplt.PopupActivity;
import com.example.styleplt.R;
import com.example.styleplt.models.Outer;

import java.util.List;

public class outerAdapter extends RecyclerView.Adapter<outerAdapter.OuterViewHolder> {

    private List<Outer> datas;

    public outerAdapter(List<Outer> datas) {
        this.datas = datas;
    }
    @NonNull
    @Override
    public OuterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new OuterViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_outer, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull OuterViewHolder holder, int position) {
        Outer data = datas.get(position);
        Glide.with(holder.itemView)
                .load(datas.get(position).getImage())
                .into(holder.item_outer_image);
        holder.url.setText(data.getUrl());
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    class OuterViewHolder extends RecyclerView.ViewHolder {

        private ImageView item_outer_image;
        private TextView url;

        public OuterViewHolder(@NonNull View itemView) {
            super(itemView);

            item_outer_image = itemView.findViewById(R.id.item_outer_image);
            url = itemView.findViewById(R.id.url);
            item_outer_image.setOnClickListener(new View.OnClickListener() {
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
