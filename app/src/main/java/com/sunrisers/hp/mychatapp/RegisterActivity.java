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
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mauth;
    private DatabaseReference storeUserDefaultDataReference;

    private Toolbar mToolbar;
    private EditText etName,etEmail,etPassword;
    private Button btnRegister;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mauth= FirebaseAuth.getInstance();

        mToolbar = (Toolbar) findViewById(R.id.register_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Register");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        etName=(EditText) findViewById(R.id.etName);
        etEmail =(EditText) findViewById(R.id.etEmail);
        etPassword = (EditText) findViewById(R.id.etPassword);
        btnRegister = (Button) findViewById(R.id.btnRegister);
        loadingBar = new ProgressDialog(this);


        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = etName.getText().toString();
                String email = etEmail.getText().toString();
                String password = etPassword.getText().toString();

                registerAccount(name, email, password);
            }
        });
        
    }

    private void registerAccount(final String name, String email, String password) {

        if(name.length()==0)
        {
            Toast.makeText(this, "Please Enter your name", Toast.LENGTH_LONG).show();
            etName.requestFocus();
            return;
        }
        if(email.length()==0)
        {
            Toast.makeText(this, "Please Enter your email", Toast.LENGTH_LONG).show();
           etEmail.requestFocus();
           return;
        }
        if(password.length()==0)
        {
            Toast.makeText(this, "Please Enter your password", Toast.LENGTH_LONG).show();
            etPassword.requestFocus();
            return;
        }

        loadingBar.setTitle("Creating New Account");
        loadingBar.setMessage("Please Wait...");
        loadingBar.show();

        mauth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if(task.isSuccessful())
                        {
                            //get the device token
                            String device_Token = FirebaseInstanceId.getInstance().getToken();

                            String current_user_id = mauth.getCurrentUser().getUid();
                            storeUserDefaultDataReference = FirebaseDatabase.getInstance().getReference().child("Users").child(current_user_id);

                            storeUserDefaultDataReference.child("user_name").setValue(name);
                            storeUserDefaultDataReference.child("user_status").setValue("Hey There, I m using mychatapp");
                            storeUserDefaultDataReference.child("user_image").setValue("default_profile");
                            storeUserDefaultDataReference.child("device_token").setValue(device_Token);
                            storeUserDefaultDataReference.child("user_thumb_image").setValue("default_image")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful())
                                            {
                                                Intent mainIntent = new Intent(RegisterActivity.this,MainActivity.class);
                                                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK  | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(mainIntent);
                                                finish();
                                            }
                                        }
                                    });

                        }
                        else
                        {
                            Toast.makeText(RegisterActivity.this, "Error Occured, Try Again!!", Toast.LENGTH_SHORT).show();
                        }
                        loadingBar.dismiss();
                    }
                });




    }
}
