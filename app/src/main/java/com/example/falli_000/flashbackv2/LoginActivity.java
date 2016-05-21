package com.example.falli_000.flashbackv2;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

/**
 * Created by Falli_000 on 5/20/2016.
 */
public class LoginActivity extends Activity {

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    //idk what protected means
    //user is an email
    protected EditText mUser;
    protected EditText mPass;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //get values
        //This is actually an email idc enough to change it.
        mUser=(EditText)findViewById(R.id.userBox);
        mPass=(EditText)findViewById(R.id.passBox);
        Button mloginbutton=(Button)findViewById(R.id.loginButton);
        mloginbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = mUser.getText().toString();
                String password = mPass.getText().toString();
                username.trim();
                password.trim();
                CheckBox checkBox = (CheckBox) findViewById(R.id.checkbox);
//                if(checkBox.isChecked()){
//                }
                if(username.isEmpty() || password.isEmpty()) {
                    //throw error
                    AlertDialog.Builder alert = new AlertDialog.Builder(LoginActivity.this); //This is to make pop up errors
                    alert.setMessage(R.string.signUpErrorText)
                            .setTitle(R.string.signUpErrorTitle)
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = alert.create();
                    dialog.show();
                }
                else {
                    Firebase myFirebaseRef = new Firebase("https://torrid-heat-4805.firebaseio.com");
                    myFirebaseRef.authWithPassword(username, password, new Firebase.AuthResultHandler() {
                        @Override
                        public void onAuthenticated(AuthData authData) {
                            System.out.println("User ID: " + authData.getUid() + ", Provider: " + authData.getProvider());
                            //Start the actual fucking app jfc
                            //Request Permissions here
                            while(ContextCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){

                                ActivityCompat.requestPermissions(LoginActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

                            }
                            if(ContextCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                                Intent intent = new Intent(LoginActivity.this, MapsActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            }


                        }

                        @Override
                        public void onAuthenticationError(FirebaseError firebaseError) {
                            // there was an error
                            //fuck
                            AlertDialog.Builder alert = new AlertDialog.Builder(LoginActivity.this); //This is to make pop up errors
                            alert.setMessage(R.string.wrongPasswordUserText)
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {

            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION:

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    Intent intent = new Intent(LoginActivity.this, MapsActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }

                else {
                    // Permission Denied
                    Toast.makeText(LoginActivity.this, "ACCESS_FINE_LOCATION Denied", Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
    public void onSignUp(View view){
        Intent intent= new Intent(this,SignUpActivity.class);
        startActivity(intent);
    }
    public void onForgotPassword(View view){
        Firebase myFirebaseRef = new Firebase("https://torrid-heat-4805.firebaseio.com");
        mUser=(EditText)findViewById(R.id.userBox);
        String email = mUser.getText().toString();

        if(email.isEmpty()){
            AlertDialog.Builder alert = new AlertDialog.Builder(LoginActivity.this);
            alert.setMessage(R.string.forgotPasswordErrorText)
                    .setTitle(R.string.signUpErrorTitle)
                    .setPositiveButton(android.R.string.ok,null);
            AlertDialog dialog = alert.create();
            dialog.show();
        }
        else {
            myFirebaseRef.resetPassword(email, new Firebase.ResultHandler() {
                @Override
                public void onSuccess() {
                    AlertDialog.Builder alert = new AlertDialog.Builder(LoginActivity.this);
                    alert.setMessage(R.string.forgotPasswordSuccessText)
                            .setTitle(R.string.signUpCompleteTitle)
                            .setPositiveButton(android.R.string.ok,null);
                    AlertDialog dialog = alert.create();
                    dialog.show();

                }

                @Override
                public void onError(FirebaseError firebaseError) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(LoginActivity.this); //This is to make pop up errors
                    alert.setMessage(R.string.randomErrorText)
                            .setTitle(R.string.signUpErrorTitle)
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = alert.create();
                    dialog.show();
                    //TODO start new reset password activity.
                }
            });
        }
    }
}
