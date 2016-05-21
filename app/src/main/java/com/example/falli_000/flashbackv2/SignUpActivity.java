package com.example.falli_000.flashbackv2;

/**
 * Created by Falli_000 on 5/20/2016.
 */

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.util.Map;


public class SignUpActivity extends AppCompatActivity {

    protected EditText mUser;
    protected EditText mPass0;
    protected EditText mPass1;
    protected EditText mEmail;
    protected Button mSignUpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        mUser= (EditText)findViewById(R.id.userBox);
        mPass0= (EditText)findViewById(R.id.passBox);
        mPass1=(EditText)findViewById(R.id.passboxconfirm);
        mEmail= (EditText)findViewById(R.id.emailBox);
        mSignUpButton= (Button)findViewById(R.id.signUpButton);
        mSignUpButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                final String username = mUser.getText().toString();
                final String password0 = mPass0.getText().toString();
                final String password1= mPass1.getText().toString();
                final String email = mEmail.getText().toString();


                username.trim();
                password0.trim();
                password1.trim();
                email.trim();

                AlertDialog.Builder alert = new AlertDialog.Builder(SignUpActivity.this); //This is to make pop up errors
                //No empty shit
                if(username.isEmpty() || password0.isEmpty() || password1.isEmpty() || email.isEmpty()){
                    alert.setMessage(R.string.signUpErrorText)
                            .setTitle(R.string.signUpErrorTitle)
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = alert.create();
                    dialog.show();

                }
                else if(password0.compareTo(password1)!=0){
                    alert.setMessage(R.string.passwordErrorText)
                            .setTitle(R.string.signUpErrorTitle)
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = alert.create();
                    dialog.show();
                    //Type in the same password dumb ass
                }
                else {
                    //do shit
                    //successful sigh up!
                    final Firebase myFirebaseRef = new Firebase("https://torrid-heat-4805.firebaseio.com/");
                    myFirebaseRef.createUser(email, password0, new Firebase.ValueResultHandler<Map<String, Object>>() {
                        @Override
                        public void onSuccess(Map<String, Object> result) {
                            //Successful SIGN UP
                            System.out.println("Successfully created user account with uid: " + result.get("uid"));
                            myFirebaseRef.authWithPassword(email, password0, new Firebase.AuthResultHandler() {
                                @Override
                                public void onAuthenticated(AuthData authData) {
                                    //IT FUCKING SIGNED UP
                                    //alert user that they now have an account
                                    AlertDialog.Builder alert = new AlertDialog.Builder(SignUpActivity.this); //This is to make pop up errors
                                    alert.setMessage(R.string.createdUserText)
                                            .setTitle(R.string.signUpCompleteTitle)
                                            .setPositiveButton(android.R.string.ok, null);
                                    AlertDialog dialog = alert.create();
                                    dialog.show();

                                    System.out.println("User ID: " + authData.getUid() + ", Provider: " + authData.getProvider());
                                    //add user to their tree
                                    myFirebaseRef.child("users").child(authData.getUid()).setValue(username);
                                    myFirebaseRef.addAuthStateListener(new Firebase.AuthStateListener() {
                                        @Override
                                        public void onAuthStateChanged(AuthData authData) {
                                            if(authData!=null){
                                                //logged in
                                            }
                                            else{
                                                onDestroy();
                                                //not logged in
                                            }
                                        }
                                    });
                                    //Start the actual app
                                    Intent intent = new Intent(SignUpActivity.this,MapsActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                }

                                @Override
                                public void onAuthenticationError(FirebaseError firebaseError) {
                                    // there was an error
                                    //TODO: Pop random error
                                    AlertDialog.Builder alert = new AlertDialog.Builder(SignUpActivity.this); //This is to make pop up errors
                                    alert.setMessage(R.string.randomErrorText)
                                            .setTitle(R.string.signUpErrorTitle)
                                            .setPositiveButton(android.R.string.ok, null);
                                    AlertDialog dialog = alert.create();
                                    dialog.show();


                                }
                            });

                        }
                        @Override
                        public void onError(FirebaseError firebaseError) {
                            // there was an error

                            AlertDialog.Builder alert = new AlertDialog.Builder(SignUpActivity.this); //This is to make pop up errors
                            alert.setMessage(R.string.randomErrorText)
                                    .setTitle(R.string.signUpErrorTitle)
                                    .setPositiveButton(android.R.string.ok, null);
                            AlertDialog dialog = alert.create();
                            dialog.show();

                        }
                    });
                }
            }
        });
    }
}
