package com.example.brainlegostormingapp.Utility;

import android.view.View;

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

}
