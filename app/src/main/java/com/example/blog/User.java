package com.example.blog;

public class User {

    public String user,image;

    public User(){}
    public User(String user, String image) {
        this.user = user;
        this.image = image;
    }


    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }


}
