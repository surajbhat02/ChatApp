package com.sunrisers.hp.mychatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {

    private CircleImageView ivProfileImage;
    private TextView tvUsername,tvUserStatus;
    private Button btnChangeImage,btnChangeStatus;

    private final static int Gallery_Pick = 1;
    private StorageReference storeProfileImageStorageReference;
    private StorageReference thumbImageReference;

    private DatabaseReference getUserDataReference;
    private FirebaseAuth mauth;

    private ProgressDialog loadingBar;

    Bitmap thumb_bitmap=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mauth =FirebaseAuth.getInstance();

        ivProfileImage = (CircleImageView) findViewById(R.id.ivProfileImage);
        tvUsername = (TextView) findViewById(R.id.tvUsername);
        tvUserStatus = (TextView) findViewById(R.id.tvUserStatus);
        btnChangeImage = (Button) findViewById(R.id.btnChangeImage);
        btnChangeStatus= (Button) findViewById(R.id.btnChangeStatus);

        String online_user_id=mauth.getCurrentUser().getUid();
        getUserDataReference = FirebaseDatabase.getInstance().getReference().child("Users").child(online_user_id);
        //make it available offline
        getUserDataReference.keepSynced(true);

        storeProfileImageStorageReference = FirebaseStorage.getInstance().getReference().child("Profile_images");
        thumbImageReference = FirebaseStorage.getInstance().getReference().child("Thumb_Images");

        loadingBar= new ProgressDialog(this);

        getUserDataReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("user_name").getValue().toString();
                String status = dataSnapshot.child("user_status").getValue().toString();
                final String image = dataSnapshot.child("user_image").getValue().toString();
                String thumb_image = dataSnapshot.child("user_thumb_image").getValue().toString();

                tvUsername.setText(name);
                tvUserStatus.setText(status);

                //
                if(!image.equals("defaul_profile"))
                {
                    //getting the image from db and setting it in imageview using picasso
                    Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_account_image)
                            .into(ivProfileImage, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError(Exception e) {
                                    Picasso.get().load(image).placeholder(R.drawable.default_account_image).into(ivProfileImage);

                                }
                            });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        btnChangeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, Gallery_Pick);
            }
        });

        btnChangeStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String old_status = tvUserStatus.getText().toString();
                Intent statusIntent = new Intent(SettingsActivity.this,StatusActivity.class);
                statusIntent.putExtra("user_status",old_status);
                startActivity(statusIntent);

            }
        });



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == Gallery_Pick && resultCode==RESULT_OK && data!=null)
        {
            Uri ImageUri = data.getData();

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
            //get that cropped image and store it in firebase

        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK)
            {
                loadingBar.setTitle("Updating Profile photo");
                loadingBar.setMessage("Please wait...");
                loadingBar.show();

                //what user selects from gallery(cropped image)
                Uri resultUri = result.getUri();

                //original copy is stored in this thumb_filepath
                File thumb_filePathUri =new File(resultUri.getPath());

                String user_id = mauth.getCurrentUser().getUid();

                try {
                    thumb_bitmap= new Compressor(this)
                            .setMaxWidth(200)
                            .setMaxHeight(200)
                            .setQuality(50)
                            .compressToBitmap(thumb_filePathUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
                final byte[] thumb_byte = byteArrayOutputStream.toByteArray();

                //creating the name of the image
                StorageReference filepath = storeProfileImageStorageReference.child(user_id + ".jpg");
                //creating the name of thumnb image
                final StorageReference thumbFilePath = thumbImageReference.child(user_id+".jpg");

                //map this name to the image
                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful())
                        {
                            //Toast.makeText(SettingsActivity.this, "Saving your profile photo in firebase storage...", Toast.LENGTH_SHORT).show();
                            //after saving the image in storage, now storing the image in the database

                            final String downloadUrl = task.getResult().getDownloadUrl().toString();

                            UploadTask uploadTask =thumbFilePath.putBytes(thumb_byte);
                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {

                                    String thumb_downloadUrl = thumb_task.getResult().getDownloadUrl().toString();

                                    if(thumb_task.isSuccessful())
                                    {
                                        Map update_user_data = new HashMap();
                                        update_user_data.put("user_image",downloadUrl);
                                        update_user_data.put("user_thumb_image",thumb_downloadUrl);

                                        getUserDataReference.updateChildren(update_user_data)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful())
                                                        {
                                                            loadingBar.dismiss();
                                                            Toast.makeText(SettingsActivity.this, "Profile Photo Updated", Toast.LENGTH_SHORT).show();
                                                        }

                                                    }
                                                });

                                    }



                                }
                            });
                            /*
                            getUserDataReference.child("user_image").setValue(downloadUrl)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful())
                                            {
                                                Toast.makeText(SettingsActivity.this, "Profile Photo Updated", Toast.LENGTH_SHORT).show();
                                            }

                                        }
                                    });*/

                        }
                        else
                        {
                            Toast.makeText(SettingsActivity.this, "Error Occured!!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                //map the name to the thumb image



            }
            else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE)
            {
                Exception error = result.getError();
            }
        }

    }
}
