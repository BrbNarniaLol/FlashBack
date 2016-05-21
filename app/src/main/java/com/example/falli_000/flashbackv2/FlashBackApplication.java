package com.example.falli_000.flashbackv2;

/**
 * Created by Falli_000 on 5/20/2016.
 */

import android.support.multidex.MultiDexApplication;

import com.firebase.client.Firebase;

//import com.firebase.client.Firebase;

/**
 * Created by Falli_000 on 5/6/2016.
 */
public class FlashBackApplication extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
//        // other setup code
        Firebase myFirebaseRef = new Firebase("https://torrid-heat-4805.firebaseio.com/");
    }

}