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
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class requestsFragment extends Fragment {

    private RecyclerView requestList;

    private View myMainView;

    private FirebaseAuth mauth;

    private DatabaseReference friendRequestReference;
    private DatabaseReference usersReference;

    private DatabaseReference friendsReference;
    private DatabaseReference friendRequestDatabaseReference;

    String online_user_id;


    public requestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        myMainView=inflater.inflate(R.layout.fragment_requests, container, false);

        requestList = (RecyclerView) myMainView.findViewById(R.id.requestList);

        requestList.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        requestList.setLayoutManager(linearLayoutManager);

        mauth = FirebaseAuth.getInstance();
        online_user_id = mauth.getCurrentUser().getUid();
        friendRequestReference = FirebaseDatabase.getInstance().getReference().child("Friend_Requests").child(online_user_id);
        usersReference = FirebaseDatabase.getInstance().getReference().child("Users");

        friendsReference = FirebaseDatabase.getInstance().getReference().child("Friends");
        friendRequestDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Friend_Requests");


        return myMainView;
    }

    @Override
    public void onStart() {
        super.onStart();


        FirebaseRecyclerAdapter<Requests,RequestViewholder> firebaseRecyclerAdapter
                =new FirebaseRecyclerAdapter<Requests, RequestViewholder>(Requests.class,R.layout.friend_request_all_users_layout,RequestViewholder.class,friendRequestReference) {
            @Override
            protected void populateViewHolder(final RequestViewholder viewHolder, Requests model, int position)
            {
                final String list_users_id = getRef(position).getKey();

                DatabaseReference get_type_ref= getRef(position).child("request_type").getRef();

                get_type_ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        if(dataSnapshot.exists())
                        {
                            String request_type = dataSnapshot.getValue().toString();

                            if(request_type.equals("received"))
                            {
                                //if received then showa the accept and cancel button
                                usersReference.child(list_users_id).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot)
                                    {
                                        final String username = dataSnapshot.child("user_name").getValue().toString();
                                        final String thumb_user_image = dataSnapshot.child("user_thumb_image").getValue().toString();
                                        final String userStatus  = dataSnapshot.child("user_status").getValue().toString();

                                        viewHolder.setUsername(username);
                                        viewHolder.setThumbImage(thumb_user_image);
                                        viewHolder.setUserstatus(userStatus);

                                        viewHolder.mview.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view)
                                            {
                                                CharSequence options[] = new CharSequence[]
                                                        {
                                                                "Accept Friend Request",
                                                                "Cancel Friend Request"
                                                        };
                                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                builder.setTitle("Friend Request Options");
                                                //after selecting any one option
                                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int position) {

                                                        if(position==0)
                                                        {
                                                            Calendar callForDate = Calendar.getInstance();
                                                            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-YYYY");
                                                            final String saveCurrentDate = currentDate.format(callForDate.getTime());

                                                            friendsReference.child(online_user_id).child(list_users_id).child("date").setValue(saveCurrentDate)
                                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void aVoid) {
                                                                            friendsReference.child(list_users_id).child(list_users_id).child("date").setValue(saveCurrentDate)
                                                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                        @Override
                                                                                        public void onSuccess(Void aVoid)
                                                                                        {
                                                                                            //cancel the request because they will become friends now
                                                                                            friendRequestDatabaseReference.child(online_user_id).child(list_users_id).removeValue()
                                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                        @Override
                                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                                            if(task.isSuccessful())
                                                                                                            {
                                                                                                                friendRequestDatabaseReference.child(list_users_id).child(online_user_id).removeValue()
                                                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                            @Override
                                                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                                                if(task.isSuccessful())
                                                                                                                                {
                                                                                                                                    Toast.makeText(getContext(), "Friend Request Accepted", Toast.LENGTH_SHORT).show();


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
                                                        if(position==1)
                                                        {
                                                            friendRequestDatabaseReference.child(online_user_id).child(list_users_id).removeValue()
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if(task.isSuccessful())
                                                                            {
                                                                                friendRequestDatabaseReference.child(list_users_id).child(online_user_id).removeValue()
                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                if(task.isSuccessful())
                                                                                                {
                                                                                                    Toast.makeText(getContext(), "Request Cancelled", Toast.LENGTH_SHORT).show();
                                                                                                }
                                                                                            }
                                                                                        });
                                                                            }
                                                                        }
                                                                    });

                                                        }


                                                    }
                                                });
                                                builder.show();

                                            }
                                        });

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                            }
                            else if (request_type.equals("sent"))
                            {

                                Button req_sent_button = viewHolder.mview.findViewById(R.id.btnRequestAccept);
                                req_sent_button.setText("Request Sent");


                                viewHolder.mview.findViewById(R.id.btnRequestCancel).setVisibility(View.INVISIBLE);


                                usersReference.child(list_users_id).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot)
                                    {
                                        final String username = dataSnapshot.child("user_name").getValue().toString();
                                        final String thumb_user_image = dataSnapshot.child("user_thumb_image").getValue().toString();
                                        final String userStatus  = dataSnapshot.child("user_status").getValue().toString();

                                        viewHolder.setUsername(username);
                                        viewHolder.setThumbImage(thumb_user_image);
                                        viewHolder.setUserstatus(userStatus);

                                        viewHolder.mview.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view)
                                            {
                                                CharSequence options[] = new CharSequence[]
                                                        {
                                                                "Cancel Freind Request"
                                                        };
                                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                builder.setTitle("Request Sent");
                                                //after selecting any one option
                                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int position) {

                                                        if(position==0)
                                                        {
                                                            friendRequestDatabaseReference.child(online_user_id).child(list_users_id).removeValue()
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if(task.isSuccessful())
                                                                            {
                                                                                friendRequestDatabaseReference.child(list_users_id).child(online_user_id).removeValue()
                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                if(task.isSuccessful())
                                                                                                {
                                                                                                    Toast.makeText(getContext(), "Request Cancelled", Toast.LENGTH_SHORT).show();
                                                                                                }
                                                                                            }
                                                                                        });
                                                                            }
                                                                        }
                                                                    });

                                                        }
                                                    }
                                                });
                                                builder.show();


                                            }
                                        });

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                            }

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });




            }
        };

        requestList.setAdapter(firebaseRecyclerAdapter);

    }

    public  static class RequestViewholder extends RecyclerView.ViewHolder
    {
        View mview;

        public RequestViewholder(@NonNull View itemView) {
            super(itemView);

            mview=itemView;
        }


        public void setUsername(String username)
        {
            TextView user_name = (TextView) mview.findViewById(R.id.tvRequestUsername);
            user_name.setText(username);
        }

        public void setThumbImage(final String thumb_user_image)
        {
            final CircleImageView userThumbImage = (CircleImageView) mview.findViewById(R.id.requestProfileImage);
            Picasso.get().load(thumb_user_image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_account_image)
                    .into(userThumbImage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.get().load(thumb_user_image).placeholder(R.drawable.default_account_image).into(userThumbImage);

                        }
                    });
        }

        public void setUserstatus(String userStatus)
        {
            TextView status = (TextView) mview.findViewById(R.id.tvRequestUserstatus);
            status.setText(userStatus);
        }
    }


}
