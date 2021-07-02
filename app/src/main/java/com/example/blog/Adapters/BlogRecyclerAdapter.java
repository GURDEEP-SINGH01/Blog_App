package com.example.blog.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.blog.BlogPost;
import com.example.blog.CommentsActivity;
import com.example.blog.R;
import com.example.blog.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class BlogRecyclerAdapter extends RecyclerView.Adapter<BlogRecyclerAdapter.ViewHolder> {

    public List<BlogPost>blog_list;
    public List<User>user_list;
    public Context context;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth mauth;

    public  BlogRecyclerAdapter(List<BlogPost>blog_list,List<User>user_list){

        this.user_list=user_list;
        this.blog_list=blog_list;
    }


    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {

        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.blog_list_item,parent,false);
        context=parent.getContext();
        firebaseFirestore=FirebaseFirestore.getInstance();
        mauth=FirebaseAuth.getInstance();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull BlogRecyclerAdapter.ViewHolder holder, int position) {

        holder.setIsRecyclable(false);
        String blogPostID=blog_list.get(position).blogPost;
        String currentUser=mauth.getCurrentUser().getUid();

        String desc=blog_list.get(position).getDesc();
        holder.descText(desc);

        String image_url=blog_list.get(position).getImage_url();
        String thumbUri=blog_list.get(position).getImage_thumb();
        holder.setBlogImage(image_url,thumbUri);


        String user_id=blog_list.get(position).getUser_id();
        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful())
                {
                    String username=task.getResult().getString("name");
                    String userImage=task.getResult().getString("image");
                    holder.setUserData(username,userImage);

                }else
                {

                }

            }
        });

        firebaseFirestore.collection("Posts/"+blogPostID+"/Comments").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable @org.jetbrains.annotations.Nullable QuerySnapshot value, @Nullable @org.jetbrains.annotations.Nullable FirebaseFirestoreException error) {
                if (error == null) {
                    if (!value.isEmpty()) {
                        int count = value.size();
                        holder.upDateCommentCount(count);
                    } else {
                        holder.upDateCommentCount(0);
                    }
                }
            }
        });


        long milliseconds=blog_list.get(position).getTimestamp().getTime();
        String dateTime=DateFormat.format("MM/dd/yy",new Date(milliseconds)).toString();
        holder.setTime(dateTime);

        firebaseFirestore.collection("Posts/"+blogPostID+"/Likes").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable @org.jetbrains.annotations.Nullable QuerySnapshot value, @Nullable @org.jetbrains.annotations.Nullable FirebaseFirestoreException error) {
                if (error == null) {
                    if (!value.isEmpty()) {
                        int count = value.size();
                        holder.upDatelikesCount(count);
                    } else {
                        holder.upDatelikesCount(0);
                    }
                }
            }
        });







        //Get Likes
        firebaseFirestore.collection("Posts/"+blogPostID+"/Likes").document(currentUser).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @SuppressLint("UseCompatLoadingForDrawables")
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onEvent(@Nullable @org.jetbrains.annotations.Nullable DocumentSnapshot value, @Nullable @org.jetbrains.annotations.Nullable FirebaseFirestoreException error) {

                if (error == null) {
                    if (value.exists()) {
                        holder.bloglikebtn.setImageDrawable(context.getDrawable(R.mipmap.action_like));

                    } else {
                        holder.bloglikebtn.setImageDrawable(context.getDrawable(R.mipmap.action_unlike));

                    }
                }
            }
        });


        //Likes Features
        holder.bloglikebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                firebaseFirestore.collection("Posts/"+blogPostID+"/Likes").document(currentUser)
                        .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                        if(!task.getResult().exists())
                        {
                            Map<String,Object> likesMap=new HashMap<>();
                            likesMap.put("timestamp", FieldValue.serverTimestamp());
                            firebaseFirestore.collection("Posts/"+blogPostID+"/Likes").document(currentUser).set(likesMap);

                        }else{
                            firebaseFirestore.collection("Posts/"+blogPostID+"/Likes").document(currentUser).delete();

                        }

                    }
                });


            }
        });

        holder.blogcommentbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent commentintent=new Intent(context, CommentsActivity.class);
                commentintent.putExtra("blog_post_id",blogPostID);
                context.startActivity(commentintent);
            }
        });



    }



    @Override
    public int getItemCount() {
        return blog_list.size()  ;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private View mview;

        private TextView descview;
        private TextView blogDate;
        private ImageView blogimage;

        private TextView blogusername;
        private CircleImageView blogUserimage;

        private ImageView bloglikebtn;
        private TextView bloglikecount;
        private ImageView blogcommentbtn;

        private TextView commentcount;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            mview=itemView;
            bloglikebtn=mview.findViewById(R.id.blog_like_btn);
            blogcommentbtn=mview.findViewById(R.id.blog_comment_btn);

        }
        public void descText(String descText)
        {
            descview=mview.findViewById(R.id.blog_desc);
            descview.setText(descText);
        }

        public void setBlogImage(String downloadUri,String thumbUri)
        {
            blogimage=mview.findViewById(R.id.blog_image);
            RequestOptions placeholderOptions=new RequestOptions();
            placeholderOptions.placeholder(R.drawable.defaultprofile);



            Glide.with(context).applyDefaultRequestOptions(placeholderOptions).load(downloadUri)
                    .thumbnail(Glide.with(context).load(thumbUri))
                .into(blogimage);
        }
        public void setTime(String date)
        {
            blogDate=mview.findViewById(R.id.blog_date);
            blogDate.setText(date);
        }
        public void setUserData(String name,String image)
        {
            blogUserimage=mview.findViewById(R.id.blog_user_image);
            blogusername=mview.findViewById(R.id.blog_user_name);

            blogusername.setText(name);
            RequestOptions placeholderOptions=new RequestOptions();
            placeholderOptions.placeholder(R.drawable.defaultprofile);
            Glide.with(context).applyDefaultRequestOptions(placeholderOptions).load(image).into(blogUserimage);
        }
        public void upDatelikesCount(int count){
            bloglikecount=mview.findViewById(R.id.blog_like_count);
            bloglikecount.setText(count + " Likes");
        }
        public void upDateCommentCount(int count){
            commentcount=mview.findViewById(R.id.blog_comment_count);
            commentcount.setText(count + " Comment");
        }



    }
}
