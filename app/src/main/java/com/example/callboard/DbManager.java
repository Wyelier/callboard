package com.example.callboard;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.callboard.adapter.DataSender;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DbManager {
    private Query mQuery;
    private List<NewPost> newPostList;
    private DataSender dataSender;
    private FirebaseDatabase db;
    private int cat_ads_counter = 0;
    private String[] category_ads = {"Машины", "Компьютеры", "Смартфоны", "Бытовая техника"};

    public void deleteItem(NewPost newPost)
    {
        DatabaseReference dbRef = db.getReference(newPost.getCat());
        dbRef.child(newPost.getKey()).removeValue();
    }
    public DbManager(DataSender dataSender) {
        this.dataSender = dataSender;
        newPostList = new ArrayList<>();
        db = FirebaseDatabase.getInstance();
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
