package com.sunrisers.hp.mychatapp;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageViewholder>
{
    private List<Messages> userMessagesList;

    private FirebaseAuth mauth;
    private DatabaseReference usersReference;

    public MessagesAdapter(List<Messages> userMessagesList)
    {
        this.userMessagesList = userMessagesList;
    }

    @NonNull
    @Override
    public MessageViewholder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
    {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.messages_layout_of_users,viewGroup,false);

        mauth = FirebaseAuth.getInstance();

        return new MessageViewholder(v);

    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewholder messageViewholder, int i)
    {
        String message_sender_id = mauth.getCurrentUser().getUid().toString();

        Messages messages = userMessagesList.get(i);

        String fromUserId = messages.getFrom();
        String fromMessageType = messages.getType();

        usersReference = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserId);
        usersReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                String userName = dataSnapshot.child("user_name").getValue().toString();
                String userImage = dataSnapshot.child("user_thumb_image").getValue().toString();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if(fromMessageType.equals("text"))
        {
            messageViewholder.messageImageView.setVisibility(View.INVISIBLE);
            if(fromUserId.equals(message_sender_id))
            {
                messageViewholder.messageText.setBackgroundResource(R.drawable.message_text_background_two);

                messageViewholder.messageText.setTextColor(Color.BLACK);

                messageViewholder.messageText.setGravity(Gravity.RIGHT);
            }
            else
            {
                messageViewholder.messageText.setBackgroundResource(R.drawable.message_text_background);

                messageViewholder.messageText.setTextColor(Color.WHITE);

                messageViewholder.messageText.setGravity(Gravity.LEFT);
            }
        }
        else
        {
            messageViewholder.messageText.setVisibility(View.INVISIBLE);
            messageViewholder.messageText.setPadding(0,0,0,0);
            Picasso.get().load(messages.getMessage()).placeholder(R.drawable.default_account_image).into(messageViewholder.messageImageView);

        }


        messageViewholder.messageText.setText(messages.getMessage());


    }

    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }

    public class MessageViewholder extends RecyclerView.ViewHolder
    {
        public TextView messageText;
        public CircleImageView userProfileImage;
        public ImageView messageImageView;

        public MessageViewholder(@NonNull View itemView) {
            super(itemView);

            messageText = (TextView) itemView.findViewById(R.id.tvMessageText);
            userProfileImage = (CircleImageView) itemView.findViewById(R.id.messagesProfileImage);
            messageImageView =(ImageView) itemView.findViewById(R.id.msg_image_view);

        }
    }



}
