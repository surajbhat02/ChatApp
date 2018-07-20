package com.sunrisers.hp.mychatapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth mauth;

    private ImageView profile_visit_userImage;
    private TextView tvDisplayUserName,tvDisplayUserStatus;
    private Button btnSendFriendRequest,btnDeclineFriendRequest;

    private DatabaseReference userDatabaseReference;

    private String CURRENT_STATE;
    private DatabaseReference friendRequestReference;
    String sender_user_id;
    String visit_user_id;

    private DatabaseReference friendsReference;

    private DatabaseReference notificationsReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mauth= FirebaseAuth.getInstance();
        sender_user_id=mauth.getCurrentUser().getUid();

        profile_visit_userImage = (ImageView) findViewById(R.id.profile_visit_userImage);
        tvDisplayUserName = (TextView) findViewById(R.id.tvDisplayUserName);
        tvDisplayUserStatus = (TextView) findViewById(R.id.tvDisplayUserStatus);
        btnSendFriendRequest = (Button) findViewById(R.id.btnSendFriendRequest);
        btnDeclineFriendRequest = (Button) findViewById(R.id.btnDeclineFriendRequest);

        CURRENT_STATE="not_friends";


        //get the user id from previous activity
        Intent i = getIntent();
        visit_user_id = i.getExtras().get("visit_user_id").toString();

        userDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(visit_user_id);

        //create another node that stores the state of the user
        friendRequestReference = FirebaseDatabase.getInstance().getReference().child("Friend_Requests");
        //make it available offline
        friendRequestReference.keepSynced(true);

        //create another node to store friends
        friendsReference = FirebaseDatabase.getInstance().getReference().child("Friends");
        //make it available offline
        friendsReference.keepSynced(true);

        //for notifications
        notificationsReference = FirebaseDatabase.getInstance().getReference().child("Notification");
        notificationsReference.keepSynced(true);

        userDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name= dataSnapshot.child("user_name").getValue().toString();
                String status = dataSnapshot.child("user_status").getValue().toString();
                String image = dataSnapshot.child("user_image").getValue().toString();

                tvDisplayUserName.setText(name);
                tvDisplayUserStatus.setText(status);

                Picasso.get().load(image).placeholder(R.drawable.default_account_image).into(profile_visit_userImage);

                friendRequestReference.child(sender_user_id)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                //if it has a child(reciever
                                if(dataSnapshot.hasChild(visit_user_id))
                                {
                                    String req_type = dataSnapshot.child(visit_user_id).child("request_type").getValue().toString();
                                    if(req_type.equals("sent"))
                                    {
                                        CURRENT_STATE="request_sent";
                                        btnSendFriendRequest.setText("Cancel Friend Request");

                                        //declineFriend Request button should be invisible
                                        btnDeclineFriendRequest.setVisibility(View.INVISIBLE);
                                        btnDeclineFriendRequest.setEnabled(false);
                                    }
                                    else if (req_type.equals("received"))
                                    {
                                        CURRENT_STATE="request_received";
                                        btnSendFriendRequest.setText("Accept Friend Request");

                                        btnDeclineFriendRequest.setVisibility(View.VISIBLE);
                                        btnDeclineFriendRequest.setEnabled(true);

                                        btnDeclineFriendRequest.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                declineFriendRequest();
                                            }
                                        });


                                    }
                                }
                                else
                                {
                                    friendsReference.child(sender_user_id)
                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    if(dataSnapshot.hasChild(visit_user_id))
                                                    {
                                                        CURRENT_STATE = "friends";
                                                        btnSendFriendRequest.setText("Unfriend this person");

                                                        //declineFriend Request button should be invisible
                                                        btnDeclineFriendRequest.setVisibility(View.INVISIBLE);
                                                        btnDeclineFriendRequest.setEnabled(false);
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        btnDeclineFriendRequest.setVisibility(View.INVISIBLE);
        btnDeclineFriendRequest.setEnabled(false);
        //a user cannot send friend request to himself
        if(!sender_user_id.equals(visit_user_id))
        {
            btnSendFriendRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    btnSendFriendRequest.setEnabled(false);


                    if(CURRENT_STATE.equals("not_friends"))
                    {
                        sendFriendRequest();
                    }
                    if(CURRENT_STATE.equals("request_sent"))
                    {
                        cancelFriendRequest();
                    }
                    if(CURRENT_STATE.equals("request_received"))
                    {
                        acceptFriendRequest();
                    }
                    if(CURRENT_STATE.equals("friends"))
                    {
                        unfriend();
                    }
                }
            });
        }
        else
        {
            btnSendFriendRequest.setVisibility(View.INVISIBLE);
            btnDeclineFriendRequest.setVisibility(View.INVISIBLE);
        }


    }


    private void sendFriendRequest() {

        friendRequestReference.child(sender_user_id).child(visit_user_id).child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    friendRequestReference.child(visit_user_id).child(sender_user_id).child("request_type").setValue("received")
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful())
                                    {
                                        //as we are stpring complex data hence we will use HashMap
                                        HashMap<String, String> notificationsData = new HashMap<>();
                                        notificationsData.put("from",sender_user_id);
                                        notificationsData.put("type","request");

                                        notificationsReference.child(visit_user_id).push().setValue(notificationsData)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful())
                                                        {
                                                            btnSendFriendRequest.setEnabled(true);
                                                            CURRENT_STATE="request_sent";
                                                            btnSendFriendRequest.setText("Cancel Friend Request");

                                                            btnDeclineFriendRequest.setVisibility(View.INVISIBLE);
                                                            btnDeclineFriendRequest.setEnabled(false);
                                                        }




                                                    }
                                                });





                                    }
                                }
                            });

                }
            }
        });
    }


    private void cancelFriendRequest() {

        friendRequestReference.child(sender_user_id).child(visit_user_id).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            friendRequestReference.child(visit_user_id).child(sender_user_id).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful())
                                            {
                                                btnSendFriendRequest.setEnabled(true);
                                                CURRENT_STATE="not_friends";
                                                btnSendFriendRequest.setText("Send Friend Request");

                                                btnDeclineFriendRequest.setVisibility(View.INVISIBLE);
                                                btnDeclineFriendRequest.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }


    private void acceptFriendRequest() {

        Calendar callForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-YYYY");
        final String saveCurrentDate = currentDate.format(callForDate.getTime());

        friendsReference.child(sender_user_id).child(visit_user_id).child("date").setValue(saveCurrentDate)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        friendsReference.child(visit_user_id).child(sender_user_id).child("date").setValue(saveCurrentDate)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid)
                                    {
                                        //cancel the request because they will become friends now
                                        friendRequestReference.child(sender_user_id).child(visit_user_id).removeValue()
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful())
                                                        {
                                                            friendRequestReference.child(visit_user_id).child(sender_user_id).removeValue()
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if(task.isSuccessful())
                                                                            {
                                                                                btnSendFriendRequest.setEnabled(true);
                                                                                CURRENT_STATE="friends";
                                                                                btnSendFriendRequest.setText("Unfriend this Person");

                                                                                btnDeclineFriendRequest.setVisibility(View.INVISIBLE);
                                                                                btnDeclineFriendRequest.setEnabled(false);


                                                                            }
                                                                        }
                                                                    });
                                                        }
                                                    }
                                                });
                                        /////////////////////////////////////////////////////

                                    }
                                });
                    }
                });

    }

    private void unfriend() {

        friendsReference.child(sender_user_id).child(visit_user_id).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            friendsReference.child(visit_user_id).child(sender_user_id).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful())
                                            {
                                                btnSendFriendRequest.setEnabled(true);
                                                CURRENT_STATE="not_friends";
                                                btnSendFriendRequest.setText("Send Friend Request");

                                                btnDeclineFriendRequest.setVisibility(View.INVISIBLE);
                                                btnDeclineFriendRequest.setEnabled(false);
                                            }

                                        }
                                    });
                        }
                    }
                });

    }


    private void declineFriendRequest() {

        friendRequestReference.child(sender_user_id).child(visit_user_id).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            friendRequestReference.child(visit_user_id).child(sender_user_id).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful())
                                            {
                                                btnSendFriendRequest.setEnabled(true);
                                                CURRENT_STATE="not_friends";
                                                btnSendFriendRequest.setText("Send Friend Request");

                                                btnDeclineFriendRequest.setVisibility(View.INVISIBLE);
                                                btnDeclineFriendRequest.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });

    }
}
