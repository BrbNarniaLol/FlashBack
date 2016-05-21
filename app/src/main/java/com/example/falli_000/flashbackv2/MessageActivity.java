package com.example.falli_000.flashbackv2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.EditText;

/**
 * Created by Falli_000 on 5/20/2016.
 */
public class MessageActivity extends Activity {
    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        //get pixel size

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;
        //text box
        editText = (EditText)findViewById(R.id.messageBox);

        getWindow().setLayout((int)width,(int)(height*.2));
    }
    //Button click
    public void onMessage(View v){
        Intent i = new Intent();
        i.putExtra("message",editText.getText().toString());
        setResult(RESULT_OK,i);
        finish();
    }
}
