package com.example.blog;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    private CircleImageView setupimg;
    private Toolbar setup;

    private EditText setupname;
    private Button setupbtn;
    private StorageReference storageReference;
    private FirebaseAuth mauth;
    private FirebaseFirestore firebaseFirestore;

    private String user_id;
    private Boolean ischanged=false;

    private ProgressBar  pgbar;
    private Uri download_uri=null;

    private Uri mainimageUri=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        setup=findViewById(R.id.setuptoolbar);
        setSupportActionBar(setup);
        setup.setTitleTextColor(ContextCompat.getColor(getApplicationContext(),R.color.white));

        getSupportActionBar().setTitle("Account Settings");

        mauth=FirebaseAuth.getInstance();
        user_id=mauth.getCurrentUser().getUid();
        firebaseFirestore=FirebaseFirestore.getInstance();
        storageReference= FirebaseStorage.getInstance().getReference();


        pgbar= findViewById(R.id.progressBar);
        setupimg=findViewById(R.id.setupimage);
        setupbtn=findViewById(R.id.setup_btn);
        setupname=findViewById(R.id.setup_name);

        //customdialog customdialog=new customdialog(SetupActivity.this);

        pgbar.setVisibility(View.VISIBLE);
        setupbtn.setEnabled(false);

        firebaseFirestore.collection("Users").document(user_id).get().
                addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful())
                {
                    if(task.getResult().exists())
                    {
                        String name=task.getResult().getString("name");
                        String image=task.getResult().getString("image");

                        setupname.setText(name);
                        RequestOptions placeHolderRequest=new RequestOptions();
                        placeHolderRequest.placeholder(R.drawable.ac);
                        Glide.with(SetupActivity.this).setDefaultRequestOptions(placeHolderRequest).load(image).into(setupimg);
                    }


                }
                else
                {
                    String error=task.getException().getMessage();
                    Toast.makeText(SetupActivity.this,"FireStore Error :"+error,Toast.LENGTH_LONG).show();
                }
                pgbar.setVisibility(View.INVISIBLE);
                setupbtn.setEnabled(true);
            }
        });


        setupbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = setupname.getText().toString();
                if (!TextUtils.isEmpty(username) && mainimageUri != null) {

                    pgbar.setVisibility(View.VISIBLE);
                    if (ischanged) {
                        if (!TextUtils.isEmpty(username)) {

                            user_id = mauth.getCurrentUser().getUid();
                            //customdialog.Loadingdialog();
                            pgbar.setVisibility(View.VISIBLE);
                            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);


                            StorageReference image_path = storageReference.child("profile_images").child(user_id + ".jpg");
                            image_path.putFile(mainimageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull @NotNull Task<UploadTask.TaskSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        storeFirestore(task, image_path, username);
                                    } else {
                                        String error = task.getException().getMessage();
                                        Toast.makeText(SetupActivity.this, "Error :" + error, Toast.LENGTH_LONG).show();
                                    }

                                    //customdialog.dismissDialog();
                                    pgbar.setVisibility(View.INVISIBLE);


                                }
                            });


                        } else {
                            Toast.makeText(SetupActivity.this, "Name is empty :", Toast.LENGTH_LONG).show();

                        }

                    } else {
                        // StorageReference image_path=storageReference.child("profile_images").child(user_id + ".jpg");
                        storeFirestore(null, null, username);
                    }

                }
            }
        });



        setupimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(SetupActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED)
                {
                    Toast.makeText(SetupActivity.this,"Permission denied",Toast.LENGTH_LONG).show();
                    ActivityCompat.requestPermissions(SetupActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
                }
                else {
                    bringimagepicker();

                }


            }
        });

    }

    private void storeFirestore(Task<UploadTask.TaskSnapshot> task,StorageReference image_path,String username) {



            image_path.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                     download_uri = uri;

                    Map<String, String> usermap = new HashMap<>();
                    usermap.put("name", username);
                    usermap.put("image", download_uri.toString());

                    firebaseFirestore.collection("Users").document(user_id).set(usermap).
                            addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull @NotNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(SetupActivity.this,"The user setting is updated",Toast.LENGTH_LONG).show();
                                        Intent mainIntent = new Intent(SetupActivity.this, MainActivity.class);
                                        startActivity(mainIntent);
                                        finish();
                                    } else {
                                        String error = task.getException().getMessage();
                                        Toast.makeText(SetupActivity.this, "FireStore Error :" + error, Toast.LENGTH_LONG).show();

                                    }
                                    pgbar.setVisibility(View.INVISIBLE);
                                }

                            });

                }
            });
            //Toast.makeText(SetupActivity.this,"Task Successful",Toast.LENGTH_LONG).show();




    }

    private void bringimagepicker() {
        CropImage.activity(mainimageUri)
                .setAspectRatio(1,1)
                .start(SetupActivity.this);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
               mainimageUri= result.getUri();
               setupimg.setImageURI(mainimageUri);
               ischanged=true;
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}