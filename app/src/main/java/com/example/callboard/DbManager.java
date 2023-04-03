package com.example.callboard;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.callboard.adapter.DataSender;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class DbManager {
    private Context context;
    private Query mQuery;
    private List<NewPost> newPostList;
    private DataSender dataSender;
    private FirebaseDatabase db;
    private FirebaseStorage fbs;
    private int cat_ads_counter = 0;
    private String[] category_ads = {"Машины", "Компьютеры", "Смартфоны", "Бытовая техника"};

    public void deleteItem(final NewPost newPost)
    {
        StorageReference sRef = fbs.getReferenceFromUrl(newPost.getImageId());
        sRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                DatabaseReference dbRef = db.getReference(newPost.getCat());
                dbRef.child(newPost.getKey()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(context, R.string.item_deleted, Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Произошла ошибка. Объявление не удалилось", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Произошла ошибка. Фото не удалилось", Toast.LENGTH_SHORT).show();
            }
        });
    }
    public DbManager(DataSender dataSender, Context context) {
        this.dataSender = dataSender;
        this.context = context;
        newPostList = new ArrayList<>();
        db = FirebaseDatabase.getInstance();
        fbs = FirebaseStorage.getInstance();
    }

    public void getDataFromDb(String path)
    {

        DatabaseReference dbRef = db.getReference(path);
        mQuery = dbRef.orderByChild("ad/time");
        readDataUpdate();

    }

    public void getMyAdsFromDb(String uid)
    {

        if(newPostList.size() > 0) newPostList.clear();
        DatabaseReference dbRef = db.getReference(category_ads[0]);
        mQuery = dbRef.orderByChild("ad/uid").equalTo(uid);
        readMyAdsDataUpdate(uid);
        cat_ads_counter++;

    }

    public void readDataUpdate()
    {
        mQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if(newPostList.size() > 0) newPostList.clear();
                for(DataSnapshot ds : snapshot.getChildren())
                {

                    NewPost newPost = ds.child("ad").getValue(NewPost.class);
                    newPostList.add(newPost);
                }

                dataSender.OnDataReceived(newPostList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {

            }
        });
    }
    public void readMyAdsDataUpdate(final String uid)
    {
        mQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {

                for(DataSnapshot ds : snapshot.getChildren())
                {

                    NewPost newPost = ds.child("ad").getValue(NewPost.class);
                    newPostList.add(newPost);
                }
                if(cat_ads_counter > 3)
                {
                    dataSender.OnDataReceived(newPostList);
                    newPostList.clear();
                    cat_ads_counter = 0;
                }
                else
                {
                    DatabaseReference dbRef = db.getReference(category_ads[cat_ads_counter]);
                    mQuery = dbRef.orderByChild("ad/uid").equalTo(uid);
                    readMyAdsDataUpdate(uid);
                    cat_ads_counter++;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {

            }
        });
    }
}
