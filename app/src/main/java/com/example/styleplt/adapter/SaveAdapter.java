package com.example.styleplt.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.styleplt.R;
import com.example.styleplt.models.Save;
import com.example.styleplt.models.Upload;

import org.w3c.dom.Text;

import java.util.List;

public class SaveAdapter extends RecyclerView.Adapter<SaveAdapter.SaveViewHolder> {

    private List<Save> datas;

    public SaveAdapter(List<Save> datas) {
        this.datas = datas;
    }
    @NonNull
    @Override
    public SaveAdapter.SaveViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SaveAdapter.SaveViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_save, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull SaveAdapter.SaveViewHolder holder, int position) {
        Save data = datas.get(position);
        holder.item_save_comments.setText(data.getContents());
        holder.item_save_time.setText(data.getTimestamp());
        Glide.with(holder.itemView)
                .load(datas.get(position).getImage())
                .into(holder.item_save_image);
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    public class SaveViewHolder extends RecyclerView.ViewHolder {

        private ImageView item_save_image;
        private TextView item_save_time;
        private TextView item_save_comments;
        public SaveViewHolder(@NonNull View itemView) {
            super(itemView);

            item_save_image = itemView.findViewById(R.id.item_save_image);
            item_save_time = itemView.findViewById(R.id.item_save_time);
            item_save_comments = itemView.findViewById(R.id.item_save_comments);


        }
    }
}
