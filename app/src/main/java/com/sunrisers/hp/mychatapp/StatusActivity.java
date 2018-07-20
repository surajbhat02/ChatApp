package com.sunrisers.hp.mychatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {

    private FirebaseAuth mauth;
    private DatabaseReference getUserDataReference;

    private Toolbar mToolbar;
    private EditText etChangeStatus;
    private Button btnChangeStatus;

    ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        mauth = FirebaseAuth.getInstance();
        getUserDataReference=FirebaseDatabase.getInstance().getReference();

        mToolbar = (Toolbar) findViewById(R.id.status_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Change Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        etChangeStatus = (EditText) findViewById(R.id.etChangeStatus);
        btnChangeStatus=(Button) findViewById(R.id.btnChangeStatus);

        //to display the old status in the changestatus textview
        Intent i = getIntent();
        String old_status = i.getExtras().get("user_status").toString();
        etChangeStatus.setText(old_status);

        loadingBar = new ProgressDialog(this);

        btnChangeStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String new_status=etChangeStatus.getText().toString();

                changeProfileStatus(new_status);

            }
        });


    }

    private void changeProfileStatus(String new_status) {

        if(new_status.length()==0)
        {
            etChangeStatus.setError("Please Write your Status");
            etChangeStatus.requestFocus();
            return;
        }
        String user_id = mauth.getCurrentUser().getUid();
        DatabaseReference onlineUserReference=getUserDataReference.child("Users").child(user_id);

        loadingBar.setTitle("Change Status");
        loadingBar.setMessage("Please Wait...");
        loadingBar.show();

        onlineUserReference.child("user_status").setValue(new_status)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            loadingBar.dismiss();
                            Intent settingsIntent = new Intent(StatusActivity.this,SettingsActivity.class);
                            startActivity(settingsIntent);
                            finish();

                            Toast.makeText(StatusActivity.this, "Profile Status Updated", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            Toast.makeText(StatusActivity.this, "Error Occured!!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });


    }
}
