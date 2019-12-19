package com.example.brainlegostormingapp.Utility;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.view.View;

import java.io.IOException;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;


public class Utility {
    //utility creata per non dover scrivere ogni volta try e catch per una sleep
    public static void sleep(long millis){
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //utility creata per non dover scrivere un elementToggle per ogni elemenento di una view
    public static void elementToggle(View... v) {
        for(View view : v)
            view.setEnabled(!view.isEnabled());
    }

    //utility creata per non dover scrivere un elementVisibilityToggle per ogni elemenento di una view
    public static void elementVisibilityToggle(View... v){
        for(View view : v){
            if(view.getVisibility() == View.VISIBLE)
                view.setVisibility(View.GONE);
            else if(view.getVisibility() == View.GONE)
                view.setVisibility(View.VISIBLE);
        }
    }

    //utility per creare un player e riprodurre un file audio mp3
    public static void playMp3Audio(Context context, String filename) {
        try {
            AssetFileDescriptor afd = context.getAssets().openFd(filename);
            MediaPlayer player = new MediaPlayer();
            player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            player.prepare();
            player.start();
        } catch (
                IOException e) {

        }
    }

}
