package com.example.Drawn_Out;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.parse.*;

import java.util.List;

public class JoinGameActivity extends Activity{
    public String username;

    TextView msg;
    Button button;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            username = extras.getString("username");
        }
        setContentView(R.layout.join_game);
        Parse.initialize(this);
    }

    public void confirm(View view) {

        msg = (TextView) findViewById(R.id.msgBox);
        button = (Button) findViewById(R.id.button3);
        EditText id = (EditText) findViewById(R.id.gameid);
        String game = id.getText().toString();

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Game");
        query.whereEqualTo("Id",game);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                TextView msg = (TextView) findViewById(R.id.msgBox);
                if (e == null) {
                    //Add user to game
                    if (list.size() != 0) {
                        int appendNum = 1;
                        String originalName = username;
                        ParseObject g = list.get(0);
                        List p = g.getList("Players");

                        while (p.contains(username)) {
                            appendNum++;
                            username = originalName + '_' + appendNum;
                        }

                        g.add("Players", username);
                        g.increment("NumOfPlayersWaiting");
                        msg.setText("Waiting for host to press Start...");
                        button.setEnabled(false);
                        try {
                            g.save();
                            // Next activity "Wait room"
                        } catch (ParseException ee) { //had error?
                            //Error handle
                            msg.setText("Server Busy. Try again later.");
                            Log.e("JoinGameActivity","Could not save game");
                        }

                    } else {
                        //Not a valid game
                        msg.setText("Invalid Game ID");
                    }
                }
            }
        });

    }
}
