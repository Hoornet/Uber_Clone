package com.thenewboston.backendless_test;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.async.callback.BackendlessCallback;
import com.backendless.exceptions.BackendlessFault;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Registration extends AppCompatActivity {
    EditText username, password;
    TextView choice;
    Button signUp;
    String riderOrDriver;

    public void redirectUser(){
        if(Backendless.UserService.CurrentUser().getProperty("Type").equals("Rider")){
            Intent i = new Intent(this, YourLocation.class);
            startActivity(i);
        }else{
            Intent i = new Intent(this, ViewRequests.class);
            startActivity(i);
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        username = (EditText)findViewById(R.id.username);
        password = (EditText)findViewById(R.id.password);
        password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        choice = (TextView)findViewById(R.id.choice);
        signUp = (Button)findViewById(R.id.signUp);
        Intent i = getIntent();
        riderOrDriver = i.getStringExtra("riderOrDriver");
        choice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(signUp.getText().toString().equals("SIGN UP")){
                    choice.setText("SIGN UP");
                    signUp.setText("LOG IN");
                }else{
                    choice.setText("LOG IN");
                    signUp.setText("SIGN UP");
                }
            }
        });
    }

    public void signUpOrLogin(View view){
        if(signUp.getText().toString().equals("SIGN UP")){

        BackendlessUser user = new BackendlessUser();
        user.setEmail(username.getText().toString());
        user.setPassword(password.getText().toString());
        user.setProperty("Type", riderOrDriver);
        Backendless.UserService.register(user, new BackendlessCallback<BackendlessUser>() {
            @Override
            public void handleResponse(BackendlessUser backendlessUser) {
                Log.i("Success: ", backendlessUser.getEmail());
            }
        });
        }else{
            Backendless.UserService.login(username.getText().toString(), password.getText().toString(), new AsyncCallback<BackendlessUser>() {
                @Override
                public void handleResponse(BackendlessUser backendlessUser) {
                    redirectUser();
                    Log.i("Login", "Success");
                }

                @Override
                public void handleFault(BackendlessFault backendlessFault) {
                    Log.i("Login", "Failed");
                }
            });
        }
    }

    public void fbLogin(View view){
        Backendless.UserService.loginWithFacebook( Registration.this, new AsyncCallback<BackendlessUser>()
        {
            @Override
            public void handleResponse( BackendlessUser loggedInUser )
            {
                // user logged in successfully
                Log.i("Login with fb", "Successful");
                redirectUser();
            }

            @Override
            public void handleFault( BackendlessFault fault )
            {
                // failed to log in
                Log.i("Login with fb", "failed");
            }
        } );
    }

}
