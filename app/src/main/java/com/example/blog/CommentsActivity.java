package com.example.blog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.blog.Adapters.CommentsRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommentsActivity extends AppCompatActivity {

    private Toolbar commentToolbar;
    private EditText comment_field;
    private ImageView comment_postbtn;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth mauth;
    private RecyclerView commentlist;

    private String blog_post_id;
    private String currentuserid;
    private List<Comments>comment_list;
    private CommentsRecyclerAdapter commentsRecyclerAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        commentToolbar=findViewById(R.id.comment_toolbar);
        setSupportActionBar(commentToolbar);
        commentToolbar.setTitleTextColor(ContextCompat.getColor(getApplicationContext(),R.color.white));

        getSupportActionBar().setTitle("Comment");

        firebaseFirestore=FirebaseFirestore.getInstance();
        mauth=FirebaseAuth.getInstance();
        currentuserid=mauth.getCurrentUser().getUid();
        blog_post_id=getIntent().getStringExtra("blog_post_id");



        comment_field=findViewById(R.id.comment_Field);
        comment_postbtn=findViewById(R.id.comment_btn);
        commentlist=findViewById(R.id.comment_list);

        comment_list=new ArrayList<>();
        commentsRecyclerAdapter=new CommentsRecyclerAdapter(comment_list);
        commentlist.setLayoutManager(new LinearLayoutManager(this));
        commentlist.setAdapter(commentsRecyclerAdapter);





        firebaseFirestore.collection("Posts/"+blog_post_id+"/Comments").
                addSnapshotListener(CommentsActivity.this,new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable @org.jetbrains.annotations.Nullable QuerySnapshot value,
                                @Nullable @org.jetbrains.annotations.Nullable FirebaseFirestoreException error) {

                if (error == null) {
                    if (!value.isEmpty()) {
                        for (DocumentChange doc : value.getDocumentChanges()) {
                            if (doc.getType() == DocumentChange.Type.ADDED) {
                                String commentid = doc.getDocument().getId();
                                Comments comments = doc.getDocument().toObject(Comments.class);
                                comment_list.add(comments);



                                Collections.sort(comment_list, new Comparator<Comments>() {
                                    @Override
                                    public int compare(Comments o1, Comments o2) {
                                        if(o1.getTimestamp()!=null&&o2.getTimestamp()!=null)
                                        return o2.getTimestamp().compareTo(o1.getTimestamp());
                                        else
                                        return 0;
                                    }


                                });
                                commentsRecyclerAdapter.notifyDataSetChanged();

                            }
                        }
                    }
                }
            }
        });

        comment_postbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String comment_message=comment_field.getText().toString();
                if(!comment_message.isEmpty())
                {
                    Map<String,Object> map=new HashMap<>();
                    map.put("message",comment_message);
                    map.put("user_id",currentuserid);
                    map.put("timestamp", FieldValue.serverTimestamp());
                    firebaseFirestore.collection("Posts/"+blog_post_id+"/Comments").add(map).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull @NotNull Task<DocumentReference> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(CommentsActivity.this,"Post Added",Toast.LENGTH_LONG).show();
                                comment_field.setText("");
                            }else{
                                String error=task.getException().toString();
                                Toast.makeText(CommentsActivity.this,"Error:"+error,Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }

            }
        });


    }
}