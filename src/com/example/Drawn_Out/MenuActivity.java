package com.example.Drawn_Out;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import java.util.Random;


public class MenuActivity extends Activity {
    /**
     * Called when the activity is first created.
     */

    private String username;
    private EditText usernameEditText;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        usernameEditText = (EditText) findViewById(R.id.editText);

        Random rand = new Random();
        username = "Player" +  rand.nextInt(1000);
        usernameEditText.setText(username);

    }

    public void createGame(View view) {
        username = usernameEditText.getText().toString();
        Intent intent = new Intent(this, CreateGameActivity.class);
        intent.putExtra("username", username);
        startActivity(intent);
    }

    public void joinGame(View view) {
        username = usernameEditText.getText().toString();
        Intent intent = new Intent(this, JoinGameActivity.class);
        intent.putExtra("username", username);
        startActivity(intent);
    }
}
