package com.example.Drawn_Out;

import android.app.Activity;
import android.os.Bundle;

public class JoinGameActivity extends Activity{
    public String username;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            username = extras.getString("username");
        }
        setContentView(R.layout.join_game);
    }
}
