package com.sunrisers.hp.mychatapp;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class chatsFragment extends Fragment
{
    private FirebaseAuth mauth;

    private RecyclerView chatsList;
    private DatabaseReference friendsReference;
    private DatabaseReference usersReference;

    String online_user_id;

    private View myMainView;




    public chatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        myMainView = inflater.inflate(R.layout.fragment_chats, container, false);

        chatsList = (RecyclerView) myMainView.findViewById(R.id.chatsList);

        mauth = FirebaseAuth.getInstance();
        online_user_id = mauth.getCurrentUser().getUid();

        friendsReference = FirebaseDatabase.getInstance().getReference().child("Friends").child(online_user_id);
        friendsReference.keepSynced(true);

        usersReference = FirebaseDatabase.getInstance().getReference().child("Users");
        usersReference.keepSynced(true);

        chatsList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        chatsList.setLayoutManager(linearLayoutManager);




        return myMainView;
    }

    @Override
    public void onStart() {
        super.onStart();


        FirebaseRecyclerAdapter<Chats,chatsFragment.ChatsViewHolder> firebaseRecyclerAdapter
                =new FirebaseRecyclerAdapter<Chats, ChatsViewHolder>(Chats.class,R.layout.all_users_display_layout,chatsFragment.ChatsViewHolder.class,friendsReference) {
            @Override
            protected void populateViewHolder(final chatsFragment.ChatsViewHolder viewHolder, Chats model, int position)
            {

                //get the user key
                final String list_user_id = getRef(position).getKey();
                usersReference.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {
                        final String username = dataSnapshot.child("user_name").getValue().toString();
                        String thumb_user_image = dataSnapshot.child("user_thumb_image").getValue().toString();
                        String userStatus  = dataSnapshot.child("user_status").getValue().toString();


                        if(dataSnapshot.hasChild("online"))
                        {
                            String online_status = dataSnapshot.child("online").getValue().toString();

                            viewHolder.setUserOnline(online_status);
                        }

                        viewHolder.setUserName(username);
                        viewHolder.setThumbImage(thumb_user_image);
                        viewHolder.setUserStatus(userStatus);

                        //when a user clicks on a particular user it will show a box stating visit profile and send message
                        viewHolder.mview.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view)
                            {

                                if(dataSnapshot.child("online").exists())
                                {
                                    Intent chatIntent = new Intent(getContext(),ChatActivity.class);
                                    chatIntent.putExtra("visit_user_id",list_user_id);
                                    chatIntent.putExtra("user_name",username);
                                    startActivity(chatIntent);
                                }
                                else
                                {
                                    usersReference.child(list_user_id).child("online")
                                            .setValue(ServerValue.TIMESTAMP)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                    Intent chatIntent = new Intent(getContext(),ChatActivity.class);
                                                    chatIntent.putExtra("visit_user_id",list_user_id);
                                                    chatIntent.putExtra("user_name",username);
                                                    startActivity(chatIntent);

                                                }
                                            });
                                }
                            }
                        });

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };

        chatsList.setAdapter(firebaseRecyclerAdapter);


    }



    public static class ChatsViewHolder extends RecyclerView.ViewHolder
    {
        View mview;

        public ChatsViewHolder(@NonNull View itemView) {
            super(itemView);
            mview=itemView;
        }

        public void setUserName(String userName)
        {
            TextView username = (TextView) mview.findViewById(R.id.all_users_username);
            username.setText(userName);
        }
        public void setThumbImage(final String thumbImage)
        {
            final CircleImageView userThumbImage = (CircleImageView) mview.findViewById(R.id.all_users_profile_image);
            Picasso.get().load(thumbImage).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_account_image)
                    .into(userThumbImage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.get().load(thumbImage).placeholder(R.drawable.default_account_image).into(userThumbImage);

                        }
                    });
        }

        public void setUserOnline(String online_status) {
            ImageView onlineStatusView =(ImageView) mview.findViewById(R.id.online_status);

            if(online_status.equals("true"))
            {
                onlineStatusView.setVisibility(View.VISIBLE);
            }
            else
            {
                onlineStatusView.setVisibility(View.INVISIBLE);
            }
        }

        public void setUserStatus(String userStatus)
        {
            TextView user_status = (TextView) mview.findViewById(R.id.all_users_user_status);
            user_status.setText(userStatus);
        }
    }
}
