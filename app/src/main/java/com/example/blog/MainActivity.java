package com.example.blog;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.blog.mainactivity.AccountFragment;
import com.example.blog.mainactivity.HomeFragment;
import com.example.blog.mainactivity.NotificationFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;

public class MainActivity extends AppCompatActivity {


    private Toolbar mainToolbar;
    private FirebaseAuth mauth;
    private FirebaseFirestore firebaseFirestore;

    private String currentuserid;
    private FloatingActionButton addpostbtn;
    private BottomNavigationView mainbottomnav;

    private HomeFragment homeFragment;
    private AccountFragment accountFragment;
    private NotificationFragment notificationFragment;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseFirestore=FirebaseFirestore.getInstance();

        mainToolbar= findViewById(R.id.main_toolbar);
        setSupportActionBar(mainToolbar);
        mainToolbar.setTitleTextColor(ContextCompat.getColor(getApplicationContext(),R.color.white));
        getSupportActionBar().setTitle("Blog");

        mauth=FirebaseAuth.getInstance();
        addpostbtn=findViewById(R.id.add_post_btn);



        if(mauth.getCurrentUser()!=null) {

            mainbottomnav=findViewById(R.id.main_bottom_nav);

            //Fragments
            homeFragment = new HomeFragment();
            notificationFragment = new NotificationFragment();
            accountFragment = new AccountFragment();

            replaceFragment(homeFragment);

            mainbottomnav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull @NotNull MenuItem item) {

                    switch (item.getItemId()) {
                        case R.id.bottom_home:
                            replaceFragment(homeFragment);
                            return true;
                        case R.id.bottom_account:
                            Intent intent=new Intent(MainActivity.this,SetupActivity.class);
                            startActivity(intent);
                            return true;

                        default:
                            //replaceFragment(homeFragment);
                            return false;
                    }


                }
            });


            addpostbtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent newpostIntent = new Intent(MainActivity.this, NewPostActivity.class);
                    startActivity(newpostIntent);
                }
            });
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user==null)
        {

            sendToLogin();
        }
        else
        {
            currentuserid=mauth.getCurrentUser().getUid();
            firebaseFirestore.collection("Users").document(currentuserid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {

                    if(task.isSuccessful())
                    {
                        if(!task.getResult().exists())
                        {
                            Intent setUpintent=new Intent(MainActivity.this,SetupActivity.class);
                            startActivity(setUpintent);
                        }
                    }
                    else {
                        String error=task.getException().toString();
                        Toast.makeText(MainActivity.this,"Error :"+error,Toast.LENGTH_LONG).show();


                    }

                }
            });
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId())
        {
            //case R.id.action_searchbtn:break;
            case R.id.action_logoutbtn:
                logout();
                return true;
            case R.id.action_accountbtn:
                Intent settingIntent=new Intent(MainActivity.this,SetupActivity.class);
                startActivity(settingIntent);
                return true;


            default:
                return false;
        }



    }

    private void logout() {
        mauth.signOut();
        sendToLogin();
    }

    private void sendToLogin() {
        Intent loginintent=new Intent(MainActivity.this,LoginActivity.class);
        startActivity(loginintent);
        finish();
    }

    private void replaceFragment(Fragment fragment)
    {
        FragmentTransaction fragmentTransaction=getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_container,fragment);
        fragmentTransaction.commit();
    }
}