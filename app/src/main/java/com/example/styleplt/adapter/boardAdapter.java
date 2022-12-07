package com.example.styleplt.adapter;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.styleplt.CommentsActivity;
import com.example.styleplt.R;
import com.example.styleplt.models.board;

import java.util.List;

//리사이클러 뷰 사용을 위한 어댑터 생성
public class boardAdapter extends RecyclerView.Adapter<boardAdapter.boardViewHolder> {

    private List<board> datas;

    //어댑터에 대한 생성자
    public boardAdapter(List<board> datas) {
        this.datas = datas;
    }

    @NonNull
    @Override
    public boardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new boardViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_board, parent, false));
    }


    @Override
    public void onBindViewHolder(@NonNull boardViewHolder holder, int position) {

        // 각각의 홀더의 위치(position)에 데이터(컨텐츠, 타이틀)를 넣음
        // title, nickname, contents 섞임현상으로 임시로 수정
        board data = datas.get(position);
        holder.board_title.setText("✔" + data.getTitle());
        holder.board_contents.setText(data.getContents());
        holder.board_nickname.setText(data.getNickname());
        holder.board_uid.setText(data.getDocumentId());
        holder.board_collectionid.setText(data.getCollectionId());
        holder.board_timestamp.setText(data.getTimestamp());
    }

    private OnItemClickListener mListener = null ;

    public interface OnItemClickListener {
        void onItemClick(View v, int position) ;
    }

    // OnItemClickListener 리스너 객체 참조를 어댑터에 전달하는 메서드
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mListener = listener ;
    }

    //데이터의 길이를 전부 가져옴
    @Override
    public int getItemCount() {
        return datas.size();
    }

    class boardViewHolder extends RecyclerView.ViewHolder {

        private TextView board_title;
        private TextView board_contents;
        private TextView board_nickname;
        private TextView board_uid;
        private TextView board_timestamp;
        private TextView board_collectionid;

        //board view holder의 생성자
        public boardViewHolder(@NonNull View itemView) {
            super(itemView);

            // title, nickname, contents 섞임현상으로 임시로 수정
            board_title = itemView.findViewById(R.id.item_board_nickname);
            board_nickname = itemView.findViewById(R.id.item_board_contents);
            board_contents = itemView.findViewById(R.id.item_board_title);
            board_uid = itemView.findViewById(R.id.item_board_uid);
            board_timestamp = itemView.findViewById(R.id.item_board_timestamp);
            board_collectionid = itemView.findViewById(R.id.item_board_collectionid);
            Log.d("holder", "title = " + board_title.getText().toString());
            Log.d("holder", "title = " + board_nickname.getText().toString());

            // 리사이클러뷰 아이템 클릭 리스너
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION) {
                        if (mListener != null) {
                            mListener.onItemClick(view, position);
                        }
                    }


                    // 클릭한 아이템의 정보를 CommentActivity로 전송
                    Intent intent = new Intent(view.getContext(), CommentsActivity.class);
                    intent.putExtra("board_title", board_title.getText().toString());
                    intent.putExtra("board_contents", board_contents.getText().toString());
                    intent.putExtra("board_nickname", board_nickname.getText().toString());
                    intent.putExtra("board_documentID", board_uid.getText().toString());
                    intent.putExtra("board_timestamp", board_timestamp.getText().toString());
                    intent.putExtra("board_collectionID", board_collectionid.getText().toString());
                    Log.d("holder", "title = " + board_title.getText().toString());
                    view.getContext().startActivity(intent);

                }
            });


        }
    }

}
