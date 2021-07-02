 package com.example.blog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import id.zelory.compressor.Compressor;

 public class NewPostActivity extends AppCompatActivity {


     private ImageView newpostimg;
     private EditText newposttext;
     private Button newpostbtn;

     private Uri postImageuri = null;

     private Toolbar newPostToolbar;

     private FirebaseFirestore firebaseFirestore;
     private FirebaseAuth firebaseAuth;
     private StorageReference storageReference;
     private String current_user_id;

     private  Bitmap compressedImageFile;
     private ProgressBar pg;


     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_new_post);

         newPostToolbar = findViewById(R.id.new_post_toolbar);
         setSupportActionBar(newPostToolbar);
         newPostToolbar.setTitleTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
         getSupportActionBar().setTitle("Add new Post");
         getSupportActionBar().setDisplayHomeAsUpEnabled(true);

         newpostbtn = findViewById(R.id.new_post_btn);
         newposttext = findViewById(R.id.new_post_desc);
         newpostimg = findViewById(R.id.new_post_img);
         pg=findViewById(R.id.newpostpg);
         firebaseAuth = FirebaseAuth.getInstance();
         firebaseFirestore = FirebaseFirestore.getInstance();
         storageReference = FirebaseStorage.getInstance().getReference();
         current_user_id = firebaseAuth.getCurrentUser().getUid();


         if(firebaseAuth.getCurrentUser()!=null) {
             newpostimg.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View v) {
                     CropImage.activity(postImageuri)
                             .setGuidelines(CropImageView.Guidelines.ON)
                             .start(NewPostActivity.this);
                 }
             });

             newpostbtn.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View v) {
                     String desc = newposttext.getText().toString();
                     if (!TextUtils.isEmpty(desc) && postImageuri != null) {
                         pg.setVisibility(View.VISIBLE);
                         String Random = UUID.randomUUID().toString();
                         StorageReference filepath = storageReference.child("post_images").child(Random + ".jpg");

                         filepath.putFile(postImageuri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                             @Override
                             public void onComplete(@NonNull @NotNull Task<UploadTask.TaskSnapshot> task) {

                                 if (task.isSuccessful()) {

                                     File newimageFile = new File(postImageuri.getPath());

                                     try {
                                         compressedImageFile = new Compressor(NewPostActivity.this)
                                                 .setMaxWidth(300)
                                                 .setQuality(2)
                                                 .setMaxHeight(200)
                                                 .compressToBitmap(newimageFile);
                                     } catch (IOException e) {
                                         e.printStackTrace();
                                     }


                                     ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                     compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                     byte[] ThumbData = baos.toByteArray();

                                     UploadTask uploadTask = storageReference.child("post_images/thumb").child(Random + ".jpeg").putBytes(ThumbData);
                                     uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                         @Override
                                         public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                             filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                 @Override
                                                 public void onSuccess(Uri uri) {
                                                     Uri downloadUri = uri;
                                                     StorageReference st = storageReference.child("post_images").child("thumb").child(Random + ".jpeg");
                                                     st.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                         @Override
                                                         public void onSuccess(Uri uri) {
                                                             Uri downloadthumbUri = uri;
                                                             Map<String, Object> usermap = new HashMap<>();
                                                             usermap.put("desc", desc);
                                                             usermap.put("image_thumb", downloadthumbUri.toString());
                                                             usermap.put("user_id", current_user_id);
                                                             usermap.put("image_url", downloadUri.toString());
                                                             usermap.put("timestamp", FieldValue.serverTimestamp());


                                                             firebaseFirestore.collection("Posts").add(usermap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                                 @Override
                                                                 public void onComplete(@NonNull Task<DocumentReference> task) {

                                                                     if (task.isSuccessful()) {
                                                                         Toast.makeText(NewPostActivity.this, "Post added", Toast.LENGTH_LONG).show();
                                                                         Intent mainIntent = new Intent(NewPostActivity.this, MainActivity.class);
                                                                         startActivity(mainIntent);
                                                                         finish();

                                                                     } else {
                                                                         String error = task.getException().toString();
                                                                         Toast.makeText(NewPostActivity.this, "Error occured Post not added:" + error, Toast.LENGTH_LONG).show();


                                                                     }
                                                                     pg.setVisibility(View.INVISIBLE);


                                                                 }

                                                             });
                                                         }
                                                     });
                                                 }
                                             });


                                         }
                                     }).addOnFailureListener(new OnFailureListener() {
                                         @Override
                                         public void onFailure(@NonNull @NotNull Exception e) {
                                             String error = task.getException().toString();
                                             Toast.makeText(NewPostActivity.this, "Error :" + error, Toast.LENGTH_LONG).show();

                                         }
                                     });

                                 } else {
                                     String error = task.getException().toString();
                                     Toast.makeText(NewPostActivity.this, "Error occured:" + error, Toast.LENGTH_LONG).show();


                                 }
                                 pg.setVisibility(View.INVISIBLE);


                             }
                         });
                     } else {

                     }
                 }
             });
         }
         else
         {
             Intent logoutintent=new Intent(NewPostActivity.this,RegisterActivity.class);
             startActivity(logoutintent);
             finish();
         }


     }

     @Override
     protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
         if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
             CropImage.ActivityResult result = CropImage.getActivityResult(data);
             if (resultCode == RESULT_OK) {
                 postImageuri = result.getUri();
                 newpostimg.setImageURI(postImageuri);

             } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                 Exception error = result.getError();

             }
         }
     }

 }
