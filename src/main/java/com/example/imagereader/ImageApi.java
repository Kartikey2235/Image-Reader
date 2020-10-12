package com.example.imagereader;


import android.app.Application;

public class ImageApi extends Application {
    private String Text;
    private String userId;
    private static ImageApi instance;

    public ImageApi(){}

    public static ImageApi getInstance(){
        if(instance==null){
            instance=new ImageApi();
        }

        return instance;
    }

    public String getText() {
        return Text;
    }

    public void setText(String Text) {
        this.Text = Text;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
