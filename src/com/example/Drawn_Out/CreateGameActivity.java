package com.example.Drawn_Out;

import android.app.Activity;
import android.os.Bundle;

public class CreateGameActivity extends Activity{
    private String username;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            username = extras.getString("username");
        }
        setContentView(R.layout.create_game);
    }
}
