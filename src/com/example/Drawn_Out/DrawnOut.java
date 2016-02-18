package com.example.Drawn_Out;

import android.app.Application;
import com.parse.Parse;

public class DrawnOut extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Parse.initialize(this, "ODMhxphC6A13ZXcsgb94jMifocUVrdKms3pBedWd", "l8ieMEy07oz5Ui0KkTKP2EElfTLxNjABR9xY5hG3");
    }
}
