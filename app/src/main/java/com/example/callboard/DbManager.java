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

    public DbManager(DataSender dataSender) {
        this.dataSender = dataSender;
        newPostList = new ArrayList<>();
    }

    public void getDataFromDb(String path)
    {

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference dbRef = db.getReference(path);
        mQuery = dbRef.orderByChild("ad/time");
        readDataUpdate();


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
}
