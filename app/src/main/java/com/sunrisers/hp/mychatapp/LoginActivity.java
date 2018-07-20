package com.sunrisers.hp.mychatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {

    FirebaseAuth mauth;

    private Toolbar mToolbar;
    private EditText etEmail,etPassword;
    private Button btnLogin;

    private ProgressDialog loadingBar;

    private DatabaseReference usersReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mauth=FirebaseAuth.getInstance();

        usersReference = FirebaseDatabase.getInstance().getReference().child("Users");

        //ActionBar
        mToolbar = (Toolbar) findViewById(R.id.login_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Sign In");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        etEmail =(EditText) findViewById(R.id.etEmail);
        etPassword = (EditText) findViewById(R.id.etPassword);
        btnLogin = (Button) findViewById(R.id.btnLogin);


        //Loading BAr
        loadingBar=new ProgressDialog(this);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = etEmail.getText().toString();
                String password = etPassword.getText().toString();

                loginUser(email, password);
            }
        });


    }

    private void loginUser(String email, String password) {
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

        loadingBar.setTitle("Logging You in");
        loadingBar.setMessage("Please wait...");
        loadingBar.show();

        mauth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful())
                        {
                            String online_user_id = mauth.getCurrentUser().getUid();
                            //get the device token
                            String device_Token = FirebaseInstanceId.getInstance().getToken();

                            usersReference.child(online_user_id).child("device_token").setValue(device_Token)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid)
                                        {

                                            Intent mainIntent = new Intent(LoginActivity.this,MainActivity.class);
                                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(mainIntent);
                                            finish();

                                        }
                                    });
                        }
                        else
                        {
                            Toast.makeText(LoginActivity.this, "Please check your email and password!!", Toast.LENGTH_SHORT).show();
                        }
                        loadingBar.dismiss();
                    }
                });

    }
}
