package com.example.blog.mainactivity;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.blog.Adapters.BlogRecyclerAdapter;
import com.example.blog.BlogPost;
import com.example.blog.Comments;
import com.example.blog.R;
import com.example.blog.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A simple {@link Fragment} subclass
 */

public class HomeFragment extends Fragment {

    private RecyclerView bloglistview;
    private List<BlogPost>blog_list;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth mauth;
    private BlogRecyclerAdapter blogRecyclerAdapter;

    private List<User>user_List;

    private DocumentSnapshot lastvisible;
    private Boolean isFirstPageload=false;

    public HomeFragment() {
        // Required empty public constructor
    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view=inflater.inflate(R.layout.fragment_home,container,false);

        blog_list=new ArrayList<>();
        user_List=new ArrayList<>();
        bloglistview=view.findViewById(R.id.blog_recycler_view);
        mauth=FirebaseAuth.getInstance();


        blogRecyclerAdapter=new BlogRecyclerAdapter(blog_list,user_List);
        bloglistview.setLayoutManager(new LinearLayoutManager(getActivity()));
        bloglistview.setAdapter(blogRecyclerAdapter);

        if(mauth.getCurrentUser()!=null) {
            firebaseFirestore = FirebaseFirestore.getInstance();

            bloglistview.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull @NotNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    boolean reachBottom=!recyclerView.canScrollVertically(1);

                    if(reachBottom){

                        Toast.makeText(container.getContext(),"Reached Bottom",Toast.LENGTH_LONG).show();

                        LoadMorePosts();

                    }

                }
            });

            Query firstQuery=firebaseFirestore.collection("Posts")
                    .orderBy("timestamp", Query.Direction.ASCENDING);


            firstQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable @org.jetbrains.annotations.Nullable QuerySnapshot value,
                                    @Nullable @org.jetbrains.annotations.Nullable FirebaseFirestoreException error) {

                    if (error == null) {
                        if (value != null) {
                            if (isFirstPageload) {

                                lastvisible = value.getDocuments().get(value.size() - 1);
                            }
                            else
                                lastvisible=value.getDocuments().get(0);

                            for (DocumentChange doc : value.getDocumentChanges()) {
                                if (doc.getType() == DocumentChange.Type.ADDED) {

                                    String blogPostId = doc.getDocument().getId();
                                    BlogPost blogPost = doc.getDocument().toObject(BlogPost.class).withId(blogPostId);

                                    String userid = doc.getDocument().getString("user_id");

                                    firebaseFirestore.collection("Users").document(userid).get()
                                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                                                    if (task.isSuccessful()) {
                                                        User user = task.getResult().toObject(User.class);
                                                        if (isFirstPageload) {
                                                            {
                                                                user_List.add(user);

                                                                blog_list.add(blogPost);
                                                            }
                                                        } else {
                                                            user_List.add(user);
                                                            blog_list.add(0, blogPost);
                                                        }

                                                        Collections.sort(blog_list, new Comparator<BlogPost>() {
                                                            @Override
                                                            public int compare(BlogPost o1, BlogPost o2) {
                                                                if (o1.getTimestamp() != null && o2.getTimestamp() != null)
                                                                    return o2.getTimestamp().compareTo(o1.getTimestamp());
                                                                else
                                                                    return 0;
                                                            }


                                                        });
                                                        blogRecyclerAdapter.notifyDataSetChanged();


                                                    }

                                                }

                                            });


                                }

                            }
                            isFirstPageload = true;
                        }
                    }
                }

            });

        }

        return view;
    }

    private void LoadMorePosts()
    {
        Query nextQuery=firebaseFirestore.collection("Posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .startAfter(lastvisible);



        nextQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable @org.jetbrains.annotations.Nullable QuerySnapshot value, @Nullable @org.jetbrains.annotations.Nullable FirebaseFirestoreException error) {

                if( value != null) {
                    if (!value.isEmpty()) {
                        lastvisible = value.getDocuments().get(value.size() - 1);

                        for (DocumentChange doc : value.getDocumentChanges()) {
                            if (doc.getType() == DocumentChange.Type.ADDED) {
                                String blogPostId=doc.getDocument().getId();
                                BlogPost blogPost = doc.getDocument().toObject(BlogPost.class).withId(blogPostId);
                                String userid=doc.getDocument().getString("user_id");


                                firebaseFirestore.collection("Users").document(userid).get()
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                                                if(task.isSuccessful())
                                                {
                                                    User user=task.getResult().toObject(User.class);

                                                    user_List.add(user);
                                                    blog_list.add(blogPost);

                                                    Collections.sort(blog_list, new Comparator<BlogPost>() {
                                                        @Override
                                                        public int compare(BlogPost o1, BlogPost o2) {
                                                            if(o1.getTimestamp()!=null&&o2.getTimestamp()!=null)
                                                                return o2.getTimestamp().compareTo(o1.getTimestamp());
                                                            else
                                                                return 0;
                                                        }


                                                    });

                                                    blogRecyclerAdapter.notifyDataSetChanged();


                                                }
                                            }

                                        });


                            }

                        }
                    }
                }
            }
            

        });

    }


}