package com.example.blog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText loginEmaitext;
    private EditText loginpasswordtext;
    private Button loginbtn;
    private Button loginregbtn;

    private FirebaseAuth mauth;
    private ProgressBar pg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mauth=FirebaseAuth.getInstance();
        loginEmaitext=findViewById(R.id.login_email);
        loginpasswordtext=findViewById(R.id.login_password);
        loginbtn=findViewById(R.id.login_btn);
        loginregbtn=findViewById(R.id.login_reg_btn);
       pg=findViewById(R.id.login_pg);

        loginregbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent regintent=new Intent(LoginActivity.this,RegisterActivity.class);
                startActivity(regintent);

            }
        });

        loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email=loginEmaitext.getText().toString();
                String password=loginpasswordtext.getText().toString();
                pg.setVisibility(View.VISIBLE);
                if(!TextUtils.isEmpty(email)&&!TextUtils.isEmpty(password))
                {
                    mauth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull @org.jetbrains.annotations.NotNull Task<AuthResult> task) {

                            if(task.isSuccessful())
                            {

                                sendtoMain();


                            }
                            else{
                                String error=task.getException().getMessage();
                                Toast.makeText(LoginActivity.this,"Error :"+error,Toast.LENGTH_LONG).show();
                            }
                            pg.setVisibility(View.INVISIBLE);
                        }
                    });


                }


            }
        });

    }



    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentuser=mauth.getCurrentUser();
        if(currentuser!=null)
        {
            sendtoMain();

        }


    }
    private void sendtoMain() {
        Intent mainintent=new Intent(LoginActivity.this,MainActivity.class);
        startActivity(mainintent);
        finish();
    }
}