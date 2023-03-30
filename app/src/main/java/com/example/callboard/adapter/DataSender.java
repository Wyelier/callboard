package com.example.callboard.adapter;

import com.example.callboard.NewPost;

import java.util.List;

public interface DataSender {

    public void OnDataReceived(List<NewPost> listData);
}
