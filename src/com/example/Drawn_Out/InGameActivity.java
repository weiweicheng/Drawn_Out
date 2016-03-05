package com.example.Drawn_Out;

import android.app.Activity;
import android.app.AlertDialog;
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
    private TextView countdownTimerText;
    private Handler handler;
    private Runnable runnable;
    private Runnable alertDialogRunnable;
    private Runnable guessDoneRunnable;
    private CountDownTimer countDownTimer;
    private AlertDialog alertDialog;

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
        handler = new Handler();
        guessDoneRunnable = new Runnable() {
            @Override
            public void run() {
                String gamePhase = getGamePhase();
                if("DRAWING".equals(gamePhase) || "GAMEOVER".equals(gamePhase)) {
                    handler.postDelayed(alertDialogRunnable, 100);
                    handler.postDelayed(runnable, 1000);
                } else {
                    handler.postDelayed(this, 1000);
                }
            }
        };
        init();
    }

    private void init() {
        runnable = new Runnable() {
            @Override
            public void run() {
                String gamePhase = getGamePhase();
                if (gamePhase.equals("GAMEOVER")) {
                    showScoreBoard();
                }
                if(username.equals(getCurrentArtist())) {
                    if("DRAWING".equals(gamePhase)) {
                        setContentView(R.layout.drawing);
                        countdownTimerText = (TextView) findViewById(R.id.drawingTimer);
                        ((TextView) findViewById(R.id.currentWord)).setText("Your word is: " + getCurrentWord());
                        countDownTimer = new CountDownTimer(60000, 1000) {
                            public void onTick(long millisUntilFinished) {
                                countdownTimerText.setText("seconds remaining: " + millisUntilFinished / 1000);
                            }
                            public void onFinish() {
                                countdownTimerText.setText("seconds remaining: 0");
                                ((TextView) findViewById(R.id.guessSubmit)).setEnabled(false);
                                submitDrawing(findViewById(R.id.button3));
                            }
                        };
                        countDownTimer.start();
                    } else if("GUESSING".equals(gamePhase)) {
                        setContentView(R.layout.waiting);
                        waitingMessage = (TextView) findViewById(R.id.waitingMessage);
                        waitingMessage.setText("Waiting for players to finish guessing.");
                        handler.postDelayed(this, 1000);
                    }
                } else {
                    if("DRAWING".equals(gamePhase)) {
                        setContentView(R.layout.waiting);
                        waitingMessage = (TextView) findViewById(R.id.waitingMessage);
                        waitingMessage.setText("Waiting on " + getCurrentArtist() + " to finish drawing.");
                        handler.postDelayed(this, 1000);
                    } else if("GUESSING".equals(gamePhase)) {
                        setContentView(R.layout.guessing);
                        ImageView currentPicture = (ImageView) findViewById(R.id.currentPicture);
                        byte[] data = getCurrentPicture();
                        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                        currentPicture.setImageBitmap(bitmap);
                        countdownTimerText = (TextView) findViewById(R.id.guessingTimer) ;
                        countDownTimer = new CountDownTimer(30000, 1000) {
                            public void onTick(long millisUntilFinished) {
                                countdownTimerText.setText("Seconds remaining: " + millisUntilFinished / 1000);
                            }
                            public void onFinish() {
                                countdownTimerText.setText("Seconds remaining: 0");
                                ((TextView) findViewById(R.id.guessSubmit)).setEnabled(false);
                                submitGuess(findViewById(R.id.userGuess));
                            }
                        };
                        countDownTimer.start();
                    }
                }
            }
        };
        handler.postDelayed(runnable, 1000);
    }

    public void submitDrawing(View view) {
        countDownTimer.cancel();
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
            cloudGame.put("GamePhase", "GUESSING");
            cloudGame.save();
        } catch (ParseException ex) {
            Log.e("InGameActivity", ex.getMessage());
        }
        setContentView(R.layout.waiting);
        waitingMessage = (TextView) findViewById(R.id.waitingMessage);
        waitingMessage.setText("Waiting for players to finish guessing.");
        handler.postDelayed(InGameActivity.this.guessDoneRunnable, 1000);
    }

    public void submitGuess(View view) {
        countDownTimer.cancel();
        String userGuess = ((EditText) findViewById(R.id.userGuess)).getText().toString();
        alertDialog = new AlertDialog.Builder(InGameActivity.this).create();
        if (userGuess.toLowerCase().equals(getCurrentWord().toLowerCase())) {
            incrementUsersScore();
            alertDialog.setMessage("Correct! Waiting for other players to finish");
            alertDialog.setCancelable(true);
            alertDialog.show();
        } else {
            alertDialog.setMessage("False! Waiting for other players to finish");
            alertDialog.setCancelable(true);
            alertDialog.show();
        }

        incrementUsersWaiting();
        checkIfRoundIsDone();
        handler.postDelayed(InGameActivity.this.guessDoneRunnable, 1000);

        alertDialogRunnable = new Runnable() {
            @Override
            public void run() {
                alertDialog.dismiss();
            }
        };
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
            cloudGame.save();
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
            cloudGame.save();
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
            scores.set(players.indexOf(username), scores.get(players.indexOf(username)) + 2);
            scores.set(players.indexOf(getCurrentArtist()), scores.get(players.indexOf(getCurrentArtist()))+1);
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
            if (numOfPlayersWaiting >= (players.size())) {
                handler.postDelayed(alertDialogRunnable, 1000);
                HashMap<String,String> params = new HashMap<>();
                params.put("Id",gameId);
                ParseCloud.callFunction("updateCurrentWord", params);
                ParseCloud.callFunction("updateCurrentArtist", params);
                if ((cloudGame.get("RoundsLeft")).equals(1)) {
                    cloudGame.put("GamePhase", "GAMEOVER");
                    cloudGame.save();
                } else {
                    cloudGame.put("RoundsLeft", (Integer) cloudGame.get("RoundsLeft") - 1);
                    cloudGame.put("NumOfPlayersWaiting", 1);
                    cloudGame.put("GamePhase", "DRAWING");
                    cloudGame.save();
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