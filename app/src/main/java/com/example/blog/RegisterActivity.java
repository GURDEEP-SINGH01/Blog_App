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

import org.jetbrains.annotations.NotNull;

public class RegisterActivity extends AppCompatActivity {

    private EditText regemail;
    private EditText regpassword;
    private EditText regconfirmpassword;
    private Button regbtn;
    private Button regloginbtn;
    private FirebaseAuth mauth;

    private ProgressBar regpg;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mauth=FirebaseAuth.getInstance();

        regemail=findViewById(R.id.reg_email);
        regpassword=findViewById(R.id.reg_password);
        regconfirmpassword=findViewById(R.id.reg_confirm_password);
        regbtn=findViewById(R.id.reg_btn);
        regloginbtn=findViewById(R.id.reg_login_btn);
        regpg=findViewById(R.id.reg_pg);

        regloginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        regbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                regpg.setVisibility(View.VISIBLE);
                String email=regemail.getText().toString();
                String password=regpassword.getText().toString();
                String confirmpassword=regconfirmpassword.getText().toString();

                if(!TextUtils.isEmpty(email)&&!TextUtils.isEmpty(password)&&!TextUtils.isEmpty(confirmpassword))
                {
                    if(password.equals(confirmpassword))
                    {
                        mauth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull @NotNull Task<AuthResult> task) {

                                if(task.isSuccessful())
                                {
                                    regpg.setVisibility(View.INVISIBLE);
                                    Intent setupintent=new Intent(RegisterActivity .this,SetupActivity.class);
                                    startActivity(setupintent);
                                    finish();
                                }
                                else {
                                    String error=task.getException().toString();
                                    Toast.makeText(RegisterActivity.this,"Error :"+error,Toast.LENGTH_LONG).show();
                                }
                                regpg.setVisibility(View.INVISIBLE);
                            }
                        });
                    }
                    else{

                        Toast.makeText(RegisterActivity.this,"Error : Password Fields dont match",Toast.LENGTH_LONG).show();
                    }
                    regpg.setVisibility(View.INVISIBLE);

                }
                else
                {
                    Toast.makeText(RegisterActivity.this,"Error : Fields might be empty",Toast.LENGTH_LONG).show();
                }
                regpg.setVisibility(View.VISIBLE);
            }
        });






    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser curruser=mauth.getCurrentUser();
        if(curruser!=null)
        {
            sendToMain();
        }
    }

    private void sendToMain() {
        Intent mainintent=new Intent(RegisterActivity .this,MainActivity.class);
        startActivity(mainintent);
        finish();
    }


}












