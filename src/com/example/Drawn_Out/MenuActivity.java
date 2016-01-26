package com.example.Drawn_Out;

import android.app.Activity;
import android.os.Bundle;
import com.parse.Parse;
import com.parse.ParseObject;

public class MenuActivity extends Activity {
    /**
     * Called when the activity is first created.
     */
    // hi
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Parse.initialize(this);
        ParseObject testObject = new ParseObject("TestObject");
        testObject.put("foo", "bar");
        testObject.saveInBackground();

    }
}
