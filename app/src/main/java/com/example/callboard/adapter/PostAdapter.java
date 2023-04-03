package com.example.callboard.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.callboard.DbManager;
import com.example.callboard.EditActivity;
import com.example.callboard.MainActivity;
import com.example.callboard.NewPost;
import com.example.callboard.R;
import com.example.callboard.Utils.MyConstants;
import com.squareup.picasso.Picasso;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolderData> {
    private List<NewPost> arrayPost;
    private Context context;
    private OnItemClickCustom onItemClickCustom;
    private DbManager dbManager;

    public PostAdapter(List<NewPost> arrayPost, Context context, OnItemClickCustom onItemClickCustom) {
        this.arrayPost = arrayPost;
        this.context = context;
        this.onItemClickCustom = onItemClickCustom;
    }

    @NonNull
    @Override
    public ViewHolderData onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_ads,parent,false);
        return new ViewHolderData(view, onItemClickCustom);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderData holder, int position) {
        holder.setData(arrayPost.get(position));
    }

    @Override
    public int getItemCount() {
        return arrayPost.size();
    }

    public class ViewHolderData extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        private TextView tvPriceTel, tvDesc, tvTitle;
        private ImageView imAds;
        private LinearLayout edit_layout;
        private ImageButton deleteButton, editButton;
        private OnItemClickCustom onItemClickCustom;

        public ViewHolderData(@NonNull View itemView, OnItemClickCustom onItemClickCustom) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvPriceTel = itemView.findViewById(R.id.tvPriceTel);
            tvDesc = itemView.findViewById(R.id.tvDesc);
            imAds = itemView.findViewById(R.id.imageAds);
            edit_layout = itemView.findViewById(R.id.edit_layout);
            deleteButton = itemView.findViewById(R.id.DeleteAd);
            editButton = itemView.findViewById(R.id.EditAd);
            itemView.setOnClickListener(this);
            this.onItemClickCustom = onItemClickCustom;
        }
        public void setData(NewPost newPost)
        {
            if(newPost.getUid().equals(MainActivity.MAUTH))
            {
                edit_layout.setVisibility(View.VISIBLE);
            }
            else
            {
                edit_layout.setVisibility(View.GONE);
            }
            Picasso.get().load(newPost.getImageId()).into(imAds);
            tvTitle.setText(newPost.getTitle());
            String price_tel = "Цена: " + newPost.getPrice() + " Тел: " + newPost.getPhone();
            tvPriceTel.setText(price_tel);
            String textDesc = null;
            if (newPost.getDesc().length() > 50)
            {
                textDesc = newPost.getDesc().substring(0, 50) + "...";
            }
            else
            {
                textDesc = newPost.getDesc();
            }
            tvDesc.setText(textDesc);

            deleteButton.setOnClickListener(v -> {
                deleteDialog(newPost,getAdapterPosition());
            });
            editButton.setOnClickListener(v -> {
                Intent i = new Intent(context, EditActivity.class);
                i.putExtra(MyConstants.IMAGE_ID,newPost.getImageId());
                i.putExtra(MyConstants.TITLE,newPost.getTitle());
                i.putExtra(MyConstants.PRICE,newPost.getPrice());
                i.putExtra(MyConstants.PHONE,newPost.getPhone());
                i.putExtra(MyConstants.DESC,newPost.getDesc());
                i.putExtra(MyConstants.KEY,newPost.getKey());
                i.putExtra(MyConstants.UID,newPost.getUid());
                i.putExtra(MyConstants.TIME,newPost.getTime());
                i.putExtra(MyConstants.CAT,newPost.getCat());
                i.putExtra(MyConstants.EDIT_STATE,true);
                context.startActivity(i);
            });
        }

        @Override
        public void onClick(View v) {
            onItemClickCustom.onItemSelected(getAdapterPosition());
        }
    }
    private void deleteDialog(final NewPost newPost, int position)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.delete_title);
        builder.setMessage(R.string.delete_msg);
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {

            }
        });
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dbManager.deleteItem(newPost);
                arrayPost.remove(position);
                notifyItemRemoved(position);
            }
        });
        builder.show();
    }
    public interface OnItemClickCustom
    {
        void onItemSelected(int position);
    }
    public void updateAdapter(List<NewPost> listData)
    {
        arrayPost.clear();
        arrayPost.addAll(listData);
        notifyDataSetChanged();
    }
    public void setDbManager(DbManager dbManager)
    {
        this.dbManager = dbManager;
    }
}
