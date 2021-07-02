package com.example.blog.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.blog.BlogPost;
import com.example.blog.Comments;
import com.example.blog.CommentsActivity;
import com.example.blog.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class CommentsRecyclerAdapter extends RecyclerView.Adapter<CommentsRecyclerAdapter.ViewHolder> {


    public List<Comments> commentList;

    public Context context;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    public CommentsRecyclerAdapter(List<Comments> commentList)
    {
        this.commentList=commentList;
    }

    @Override
    public CommentsRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_list_item,parent,false);
        context=parent.getContext();
        firebaseAuth=FirebaseAuth.getInstance();
        firebaseFirestore=FirebaseFirestore.getInstance();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull CommentsRecyclerAdapter.ViewHolder holder, int position) {

        holder.setIsRecyclable(false);



        String comment=commentList.get(position).getMessage();
        holder.setCommentmessage(comment) ;


        String user=commentList.get(position).getUserid();

        firebaseFirestore.collection("Users").document(user).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful())
                {
                    String username=task.getResult().getString("name");
                    String userImage=task.getResult().getString("image");

                    holder.setCommentuserName(username);
                    holder.setCommentuserImage(userImage);

                }else
                {

                }

            }
        });








    }

    @Override
    public int getItemCount() {
        if(commentList!=null)
            return commentList.size();
        else
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView commentmessage;
        private TextView commentusername;
        private ImageView commentuserImage;
        private View mview;
        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            mview=itemView;
        }
        public void setCommentmessage(String message){
            commentmessage=mview.findViewById(R.id.comment_message);
            commentmessage.setText(message);

        }
        public void setCommentuserName(String username){
            commentusername=mview.findViewById(R.id.comment_username);
            commentusername.setText(username);

        }
        public void setCommentuserImage(String userimage){
            commentuserImage=mview.findViewById(R.id.comment_Image);
            Glide.with(context).load(userimage).into(commentuserImage);

        }
    }
}
