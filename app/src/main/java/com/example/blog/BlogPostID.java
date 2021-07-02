package com.example.blog;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.Exclude;

public class BlogPostID {

    @Exclude
    public String blogPost;

    public<T extends BlogPostID>T withId(@NonNull final String id){
        this.blogPost=id;
        return (T)this;

    }
}
