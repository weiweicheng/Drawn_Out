package com.example.Drawn_Out;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import com.parse.*;

import java.io.ByteArrayOutputStream;

public class InGameActivity extends Activity {

    private String gameId;
    private String username;

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
        if(username.equals(getCurrentArtist())) {
            //If drawing
            setContentView(R.layout.in_game);
            //If waiting
        } else {
            //If drawing
            //If waiting
        }
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

}