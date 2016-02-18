package com.example.Drawn_Out;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.parse.*;

import java.util.List;

public class JoinGameActivity extends Activity{
    private String username;
    private TextView msg;
    private Button button;
    private String gameId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            username = extras.getString("username");
        }
        setContentView(R.layout.join_game);
    }

    public void confirm(View view) {

        msg = (TextView) findViewById(R.id.msgBox);
        button = (Button) findViewById(R.id.button3);
        gameId = ((EditText) findViewById(R.id.gameid)).getText().toString().toLowerCase();

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Game");
        query.whereEqualTo("Id", gameId);
        try {
            ParseObject game = query.find().get(0);
            TextView msg = (TextView) findViewById(R.id.msgBox);
            String originalName = username;
            List players = game.getList("Players");
            int appendedNum = 2;
            while(players.contains(username)) {
                username = originalName + appendedNum;
                appendedNum++;
            }
            game.add("Players", username);
            game.increment("NumOfPlayersWaiting");
            game.save();
            msg.setText("Waiting for host to start the game...");
            button.setEnabled(false);
            checkIfGameStarted(gameId);
        } catch (ParseException e) {
            msg.setText("Server is busy, please try again later");
            Log.e("JoinGameActivity", e.getMessage());
        } catch (Exception e) {
            msg.setText("Cannot find game session");
            Log.e("JoinGameActivity", e.getMessage());
        }


    }

    private void checkIfGameStarted(String gameId) {
            Handler handler = new Handler();
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        ParseQuery<ParseObject> query = ParseQuery.getQuery("Game");
                        query.whereEqualTo("Id",gameId);
                        ParseObject game = query.find().get(0);
                        String gamePhase = (String) game.get("GamePhase");
                        if ("DRAWING".equals(gamePhase)) {
                           startGame(gameId);
                        } else {
                            handler.postDelayed(this, 1000);
                        }
                    } catch (ParseException e) {
                        Log.e("CreateGameActivity", e.getMessage());
                    }
                }
            };
            handler.postDelayed(runnable, 1000);
    }

    private void startGame(String gameId) {
        Intent intent = new Intent(this, InGameActivity.class);
        intent.putExtra("gameId", gameId);
        intent.putExtra("username", username);
        startActivity(intent);
    }
}
