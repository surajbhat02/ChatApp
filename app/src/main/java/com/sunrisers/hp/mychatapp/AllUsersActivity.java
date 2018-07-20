package com.sunrisers.hp.mychatapp;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AllUsersActivity extends AppCompatActivity {


    private Toolbar mToolbar;
    private RecyclerView allUsersList;
    //ListView allUsersList;
    List<String> usersList;
    private DatabaseReference allDatabaseUserReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users);

        mToolbar = (Toolbar) findViewById(R.id.all_users_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        allDatabaseUserReference = FirebaseDatabase.getInstance().getReference().child("Users");
        //this will load the data offline
        allDatabaseUserReference.keepSynced(true);




        allUsersList = (RecyclerView) findViewById(R.id.allUsersList);
        allUsersList.setHasFixedSize(true);
        allUsersList.setLayoutManager(new LinearLayoutManager(this));



    }

    @Override
    protected void onStart() {
        super.onStart();




        //we need to have a firebase recycler adapter to display the users
        FirebaseRecyclerAdapter<AllUsers,AllUsersViewholder> firebaseRecyclerAdapter
                =new FirebaseRecyclerAdapter<AllUsers, AllUsersViewholder>(AllUsers.class,R.layout.all_users_display_layout,AllUsersViewholder.class,allDatabaseUserReference) {
            @Override
            protected void populateViewHolder(AllUsersViewholder viewHolder, AllUsers model, final int position) {

                viewHolder.setUser_name(model.getUser_name());
                viewHolder.setUser_status(model.getUser_status());
                viewHolder.setUser_thumb_image(getApplicationContext(),model.getUser_thumb_image());

                //which view(user) is been clicked, set an on click listener
                viewHolder.mview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //we will get the position of the user which has been clicked and with that position we will retrieve the ID
                        String visit_user_id = getRef(position).getKey();

                        Intent profileIntent = new Intent(AllUsersActivity.this,ProfileActivity.class);
                        profileIntent.putExtra("visit_user_id",visit_user_id);
                        startActivity(profileIntent);
                    }
                });

            }
        };


        allUsersList.setAdapter(firebaseRecyclerAdapter);



    }


    public static class AllUsersViewholder extends RecyclerView.ViewHolder
    {
        View mview;

        public AllUsersViewholder(@NonNull View itemView) {
            super(itemView);
            mview=itemView;
        }

        public  void setUser_name(String user_name)
        {
            TextView name = (TextView) mview.findViewById(R.id.all_users_username);
            name.setText(user_name);
        }
        public void setUser_status(String user_status) {
            TextView status = (TextView) mview.findViewById(R.id.all_users_user_status);
            status.setText(user_status);
        }
        public void setUser_thumb_image(Context ctx, final String user_thumb_image) {
            final CircleImageView thumb_image = (CircleImageView) mview.findViewById(R.id.all_users_profile_image);


            //Picasso.get().load(user_thumb_image).placeholder(R.drawable.default_account_image).into(thumb_image);
            Picasso.get().load(user_thumb_image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_account_image)
                    .into(thumb_image, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.get().load(user_thumb_image).placeholder(R.drawable.default_account_image).into(thumb_image);

                        }
                    });

        }
    }
}
