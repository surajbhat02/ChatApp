package com.sunrisers.hp.mychatapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String messageReceiverId;
    private String messageReceiverName;

    private Toolbar mToolbar;
    private TextView tvChatUsername,tvChatLastSeen;
    private CircleImageView ivChatProfileImage;

    private ImageButton ibSendMessage,ibSendImage;
    private EditText etSendMessage;

    private DatabaseReference rootReference;

    private FirebaseAuth mauth;
    private String messageSenderId;

    private RecyclerView messageListOfUsers;

    private final List<Messages> messagesList = new ArrayList<>();

    private LinearLayoutManager linearLayoutManager;
    private MessagesAdapter messagesAdapter;

    private static int Gallery_Pick=1;

    private StorageReference msgImageReference;

    private ProgressDialog loadingBar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        rootReference = FirebaseDatabase.getInstance().getReference();

        mauth=FirebaseAuth.getInstance();
        messageSenderId = mauth.getCurrentUser().getUid();

        Intent i = getIntent();
        messageReceiverId = i.getExtras().get("visit_user_id").toString();
        messageReceiverName = i.getExtras().get("user_name").toString();

        msgImageReference = FirebaseStorage.getInstance().getReference().child("Messages_Pictures");

        mToolbar = (Toolbar) findViewById(R.id.chat_bar_layout);
        setSupportActionBar(mToolbar);

        ActionBar actionBar =getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);


        LayoutInflater layoutInflater= (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = layoutInflater.inflate(R.layout.chat_custom_bar,null);

        //connect the custom chat with action bar
        actionBar.setCustomView(action_bar_view);

        tvChatUsername = (TextView) findViewById(R.id.tvChatUsername);
        tvChatLastSeen = (TextView) findViewById(R.id.tvChatLastSeen);
        ivChatProfileImage = (CircleImageView) findViewById(R.id.ivChatProfileImage);

        ibSendImage=(ImageButton) findViewById(R.id.ibSendImage);
        ibSendMessage = (ImageButton) findViewById(R.id.ibSendMessage);
        etSendMessage = (EditText) findViewById(R.id.etSendMessage);

        loadingBar = new ProgressDialog(this);

        messagesAdapter = new MessagesAdapter(messagesList);

        //users message list
        messageListOfUsers = (RecyclerView) findViewById(R.id.messageListOfUsers);

        linearLayoutManager = new LinearLayoutManager(this);

        messageListOfUsers.setHasFixedSize(true);
        messageListOfUsers.setLayoutManager(linearLayoutManager);
        messageListOfUsers.setAdapter(messagesAdapter);

        fetchMessages();


        tvChatUsername.setText(messageReceiverName);

        rootReference.child("Users").child(messageReceiverId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String online = dataSnapshot.child("online").getValue().toString();
                final String image = dataSnapshot.child("user_thumb_image").getValue().toString();



                Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_account_image)
                        .into(ivChatProfileImage, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError(Exception e) {
                                Picasso.get().load(image).placeholder(R.drawable.default_account_image).into(ivChatProfileImage);

                            }
                        });

                if(online.equals("true"))
                {
                    tvChatLastSeen.setText("Online");
                }
                else
                {
                    LastSeenTime getTime = new LastSeenTime();
                    long lastSeen = Long.parseLong(online);

                    String lastSeenDisplayTime = getTime.getTimeAgo(lastSeen,getApplicationContext()).toString();
                    tvChatLastSeen.setText(lastSeenDisplayTime);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        ibSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                sendMessage();

            }
        });

        ibSendImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, Gallery_Pick);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == Gallery_Pick && resultCode==RESULT_OK && data!=null)
        {
            loadingBar.setTitle("Sending Image");
            loadingBar.setMessage("Please Wait...");
            loadingBar.show();

            Uri ImageUri = data.getData();


            //get that image and store it in firebase

            final String message_sender_ref = "Messages/" + messageSenderId + "/" +messageReceiverId;
            final String message_receiver_ref = "Messages/" + messageReceiverId + "/" +messageSenderId;

            DatabaseReference user_message_key = rootReference.child("Messages").child(messageSenderId).child(messageReceiverId).push();

            final String message_push_id = user_message_key.getKey();

            StorageReference filepath = msgImageReference.child(message_push_id + ".jpg");
            filepath.putFile(ImageUri)
                    .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
                        {
                            if(task.isSuccessful())
                            {
                                String downloadUrl = task.getResult().getDownloadUrl().toString();

                                //Image message body
                                Map messageTextBody = new HashMap<>();

                                messageTextBody.put("message",downloadUrl);
                                messageTextBody.put("seen",false);
                                messageTextBody.put("type","image");
                                messageTextBody.put("time", ServerValue.TIMESTAMP);
                                messageTextBody.put("from", messageSenderId);

                                Map messageBodyDetails = new HashMap();

                                messageBodyDetails.put(message_sender_ref+"/"+message_push_id,messageTextBody);

                                messageBodyDetails.put(message_receiver_ref+"/"+message_push_id,messageTextBody);

                                rootReference.updateChildren(messageBodyDetails, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference)
                                    {
                                        if(databaseError!=null)
                                        {
                                            Log.d("Chat_Log",databaseError.getMessage().toString());
                                        }
                                        etSendMessage.setText("");
                                        loadingBar.dismiss();
                                    }
                                });
                                loadingBar.dismiss();
                            }
                            else
                            {
                                Toast.makeText(ChatActivity.this, "Server Issue!!Try Again...", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }


                        }
                    });




        }
    }

    private void fetchMessages()
    {
        rootReference.child("Messages").child(messageSenderId).child(messageReceiverId)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s)
                    {
                        Messages messages = dataSnapshot.getValue(Messages.class);

                        messagesList.add(messages);
                        messagesAdapter.notifyDataSetChanged();

                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void sendMessage()
    {
        final String messageText= etSendMessage.getText().toString();

        if(messageText.length()==0)
        {
            etSendMessage.setError("Please write something...");
            etSendMessage.requestFocus();
            return;
        }
        else
        {
            String message_sender_ref = "Messages/" + messageSenderId + "/" +messageReceiverId;
            String message_receiver_ref = "Messages/" + messageReceiverId + "/" +messageSenderId;

            //this is used for generating unique msg key and will be overwritten afterwards by (rootref.updatechildren(messagebodydetails))
            DatabaseReference user_message_key = rootReference.child("Messages").child(messageSenderId).child(messageReceiverId).push();

            String message_push_id = user_message_key.getKey();

            //message body
            Map messageTextBody = new HashMap<>();

            messageTextBody.put("message",messageText);
            messageTextBody.put("seen",false);
            messageTextBody.put("type","text");
            messageTextBody.put("time", ServerValue.TIMESTAMP);
            messageTextBody.put("from", messageSenderId);

            Map messageBodyDetails = new HashMap();

            messageBodyDetails.put(message_sender_ref+"/"+message_push_id,messageTextBody);

            messageBodyDetails.put(message_receiver_ref+"/"+message_push_id,messageTextBody);

            rootReference.updateChildren(messageBodyDetails, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference)
                {
                    if(databaseError!=null)
                    {
                        Log.d("Chat_Log", databaseError.getMessage().toString());
                    }
                    etSendMessage.setText("");


                }
            });

        }
    }
}
