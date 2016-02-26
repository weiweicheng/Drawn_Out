package com.example.Drawn_Out;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.*;
import com.parse.*;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;

public class InGameActivity extends Activity {

    private String gameId = "test";
    private String username = "Test Player 1";
    private TextView waitingMessage;
    private TextView countdownTimer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            gameId = extras.getString("gameId");
            username = extras.getString("username");
        }

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                play();
            }
        };
        handler.postDelayed(runnable, 1000);
    }

    public void submitDrawing(View view) {
        DrawView drawView = (DrawView) view.getRootView().findViewById(R.id.DrawView);
        Bitmap drawing = drawView.getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        drawing.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] data = stream.toByteArray();
        Log.i("InGameActivity", "Finished converting image to byte array of size " + data.length);

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Game");
        query.whereEqualTo("Id", gameId);

        try {
            ParseObject cloudGame = query.find().get(0);
            ParseFile file = new ParseFile(data);
            cloudGame.put("CurrentPicture", file);
            cloudGame.save();
        } catch (ParseException ex) {
            Log.e("InGameActivity", ex.getMessage());
        }
    }

    public void submitGuess(View view) {
        String userGuess = ((EditText) findViewById(R.id.userGuess)).toString();
        if (userGuess.equals(getCurrentWord())) {
            incrementUsersWaiting();
            //TODO: Show +1 score
        } else {
            //TODO: Show false
        }
        incrementUsersWaiting();
        checkIfRoundIsDone();
    }

    private void play() {
        String gamePhase = getGamePhase();
        if(username.equals(getCurrentArtist())) {
            if("DRAWING".equals(gamePhase)) {
                setContentView(R.layout.drawing);
                countdownTimer = (TextView) findViewById(R.id.drawingTimer);
                new CountDownTimer(60000, 1000) {
                    public void onTick(long millisUntilFinished) {
                        countdownTimer.setText("seconds remaining: " + millisUntilFinished / 1000);
                    }

                    public void onFinish() {
                        countdownTimer.setText("seconds remaining: 0");
                        submitDrawing(findViewById(R.id.button3));
                    }
                }.start();
                changePhase("GUESSING");
            } else if("GUESSING".equals(gamePhase)) {
                setContentView(R.layout.waiting);
                waitingMessage.setText("Waiting for players to finish guessing.");
            }
        } else {
            if("DRAWING".equals(gamePhase)) {
                setContentView(R.layout.waiting);
                waitingMessage = (TextView) findViewById(R.id.waitingMessage);
                waitingMessage.setText("Waiting on " + getCurrentArtist() + " to finish drawing.");
            } else if("GUESSING".equals(gamePhase)) {
                setContentView(R.layout.guessing);
                ImageView currentPicture = (ImageView) findViewById(R.id.currentPicture);
                byte[] data = getCurrentPicture();
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                currentPicture.setImageBitmap(bitmap);
                countdownTimer = (TextView) findViewById(R.id.guessingTimer) ;
                new CountDownTimer(20000, 1000) {

                    public void onTick(long millisUntilFinished) {
                        countdownTimer.setText("Seconds remaining: " + millisUntilFinished / 1000);
                    }
                    public void onFinish() {
                        countdownTimer.setText("Seconds remaining: 0");
                        submitGuess(findViewById(R.id.userGuess));
                    }
                }.start();
            }
        }
    }

    private byte[] getCurrentPicture() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Game");
        query.whereEqualTo("Id", gameId);
        try {
            ParseObject cloudGame = query.find().get(0);
            ParseFile file = cloudGame.getParseFile("CurrentPicture");
            return file.getData();
        }  catch (ParseException ex) {
            Log.e("InGameActivity", ex.getMessage());
        }
        return null;
    }

    private String getCurrentWord() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Game");
        query.whereEqualTo("Id", gameId);
        try {
            ParseObject cloudGame = query.find().get(0);
            return (String) cloudGame.get("CurrentWord");

        }  catch (ParseException ex) {
            Log.e("InGameActivity", ex.getMessage());
        } catch (IndexOutOfBoundsException ex) {
            Log.e("InGameActivity", ex.getMessage());
        }
        return null;
    }

    private String getGamePhase() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Game");
        query.whereEqualTo("Id", gameId);
        try {
            ParseObject cloudGame = query.find().get(0);
            return (String) cloudGame.get("GamePhase");

        }  catch (ParseException ex) {
            Log.e("InGameActivity", ex.getMessage());
        } catch (IndexOutOfBoundsException ex) {
            Log.e("InGameActivity", ex.getMessage());
        }
        return null;
    }

    private String getCurrentArtist() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Game");
        query.whereEqualTo("Id", gameId);
        try {
            ParseObject cloudGame = query.find().get(0);
            return (String) cloudGame.get("CurrentArtist");

        }  catch (ParseException ex) {
            Log.e("InGameActivity", ex.getMessage());
        }
        return null;
    }

    private void changePhase(String phase) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Game");
        query.whereEqualTo("Id", gameId);
        try {
            ParseObject cloudGame = query.find().get(0);
            cloudGame.put("GamePhase", phase);
        }  catch (ParseException ex) {
            Log.e("InGameActivity", ex.getMessage());
        }
    }

    private void incrementUsersWaiting() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Game");
        query.whereEqualTo("Id", gameId);
        try {
            ParseObject cloudGame = query.find().get(0);
            cloudGame.increment("NumOfPlayersWaiting");
        }  catch (ParseException ex) {
            Log.e("InGameActivity", ex.getMessage());
        }
    }

    private void incrementUsersScore() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Game");
        query.whereEqualTo("Id", gameId);
        try {
            ParseObject cloudGame = query.find().get(0);
            List<String> players = cloudGame.getList("Players");
            List<Integer> scores = cloudGame.getList("Scores");
            scores.set(players.indexOf(username), scores.get(players.indexOf(username)) + 1);
            cloudGame.put("Scores", scores);
            cloudGame.save();
        }  catch (ParseException ex) {
            Log.e("InGameActivity", ex.getMessage());
        }
    }

    private void checkIfRoundIsDone() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Game");
        query.whereEqualTo("Id", gameId);
        try {
            ParseObject cloudGame = query.find().get(0);
            List<String> players = cloudGame.getList("Players");
            Integer numOfPlayersWaiting = (Integer) cloudGame.get("NumOfPlayersWaiting");
            if (numOfPlayersWaiting.equals(players.size())) {
                HashMap<String,String> params = new HashMap<>();
                params.put("Id",gameId);
                ParseCloud.callFunction("updateCurrentWord", params);
                ParseCloud.callFunction("updateCurrentArtist", params);
                if ((cloudGame.get("RoundsLeft")).equals(0)) {
                    showScoreBoard();
                } else {
                    cloudGame.put("RoundsLeft", (Integer) cloudGame.get("RoundsLeft") - 1);
                    cloudGame.put("GamePhase", "DRAWING");
                }

            }
        }  catch (ParseException ex) {
            Log.e("InGameActivity", ex.getMessage());
        }
    }

    private void showScoreBoard() {
        setContentView(R.layout.score_board);

        TableLayout stk = (TableLayout) findViewById(R.id.table_main);
        TableRow tbrow0 = new TableRow(this);
        TextView tv0 = new TextView(this);
        tv0.setText("Player");
        tv0.setTextColor(Color.BLACK);
        tv0.setTextSize(30);
        tv0.setWidth(500);
        tv0.setGravity(Gravity.CENTER);
        tbrow0.addView(tv0);
        TextView tv1 = new TextView(this);
        tv1.setText("Score");
        tv1.setTextColor(Color.BLACK);
        tv1.setTextSize(30);
        tbrow0.addView(tv1);
        stk.addView(tbrow0);

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Game");
        query.whereEqualTo("Id", gameId);
        try {
            ParseObject cloudGame = query.find().get(0);
            List<String> players = cloudGame.getList("Players");
            List<Integer> scores = cloudGame.getList("Scores");
            for (int i = 0; i < players.size(); i++) {
                TableRow tbrow = new TableRow(this);
                TextView playerName = new TextView(this);
                playerName.setText(players.get(i));
                playerName.setTextColor(Color.GRAY);
                playerName.setGravity(Gravity.CENTER);
                tbrow.addView(playerName);
                TextView score = new TextView(this);
                score.setText(scores.get(i).toString());
                score.setTextColor(Color.GRAY);
                score.setGravity(Gravity.CENTER);
                tbrow.addView(score);
                stk.addView(tbrow);
            }
        }  catch (ParseException ex) {
            Log.e("InGameActivity", ex.getMessage());
        }
    }

}