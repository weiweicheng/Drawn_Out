package com.example.Drawn_Out;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import com.example.Drawn_Out.entities.Game;
import com.parse.*;

import java.io.ByteArrayOutputStream;

public class InGameActivity extends Activity {

    private Game game;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //DrawView view = new DrawView(this);

        // create game object here!! :o

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.in_game);
        Parse.initialize(this);
    }

    public void submitDrawing(View view) {
        DrawView drawView = (DrawView) view.getRootView().findViewById(R.id.DrawView);
        Bitmap drawing = drawView.getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        drawing.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] data = stream.toByteArray();
        Log.i("InGameActivity", "Finished converting image to byte array of size " + data.length);

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Game");
        query.whereEqualTo("Id", game.getId());
        ParseObject cloudGame;

        try {
            cloudGame = query.find().get(0);
            ParseFile file = new ParseFile(data);
            cloudGame.put("CurrentPicture", file);
            cloudGame.save();
        } catch (ParseException ex) {
            Log.e("InGameActivity", ex.getMessage());
        }
    }

}