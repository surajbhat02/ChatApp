package com.sunrisers.hp.mychatapp;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mauth;
    private Toolbar mToolbar;

    private ViewPager myViewPager;
    private TabLayout myTabLayout;
    private TabsPagerAdapter myTabsPagerAdapter;

    FirebaseUser currentUser;
    private DatabaseReference usersReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //checks if the user is signned in or not
        mauth = FirebaseAuth.getInstance();

        currentUser = mauth.getCurrentUser();


        if(currentUser !=null)
        {
            String online_user_id = mauth.getCurrentUser().getUid();
            usersReference = FirebaseDatabase.getInstance().getReference().child("Users").child(online_user_id);
        }


        //Tabs for MainActivity////////////////////
        myViewPager = (ViewPager) findViewById(R.id.main_tabs_pager);    //fragment texts will be displayed
        myTabsPagerAdapter = new TabsPagerAdapter(getSupportFragmentManager());
        myViewPager.setAdapter(myTabsPagerAdapter);

        myTabLayout = (TabLayout) findViewById(R.id.main_tabs);
        myTabLayout.setupWithViewPager(myViewPager);

        //////////////////////////////////////////



        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("MyChat");
    }

    @Override
    protected void onStart() {
        super.onStart();

        currentUser = mauth.getCurrentUser();

        if(currentUser==null)
        {
            logoutUser();
        }
        else if (currentUser!=null)
        {
            usersReference.child("online").setValue("true");

        }
    }

    //if user minimize the app


    @Override
    protected void onStop() {
        super.onStop();

        if (currentUser!=null)
        {
            //becomes offline
            //usersReference.child("online").setValue(false);

            //to add the concept of last seen we need to store the time when the user signned in
            usersReference.child("online").setValue(ServerValue.TIMESTAMP);

        }

    }

    private void logoutUser() {
        Intent startIntent=new Intent(MainActivity.this,StartPageActivity.class);
        startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK  | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(startIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId() == R.id.main_logout_button)
        {
            if(currentUser!=null)
            {
                usersReference.child("online").setValue(ServerValue.TIMESTAMP);
            }
            mauth.signOut();
            logoutUser();
        }
        if(item.getItemId() == R.id.main_account_settings_button)
        {
            Intent settingsIntent = new Intent(MainActivity.this,SettingsActivity.class);
            startActivity(settingsIntent);
        }
        if(item.getItemId() == R.id.main_all_users_button)
        {
            Intent allUsersIntent = new Intent(MainActivity.this,AllUsersActivity.class);
            startActivity(allUsersIntent);
        }
        return true;
    }
}
