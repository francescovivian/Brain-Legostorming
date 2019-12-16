package com.example.brainlegostormingapp.Utility;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.view.View;

import java.io.IOException;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;


public class Utility {
    public static void sleep(long millis){
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public static void elementToggle(View... v) {
        for(View view : v)
            view.setEnabled(!view.isEnabled());
    }
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
