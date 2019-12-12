package com.example.brainlegostormingapp;

import android.util.Log;
import android.view.SurfaceView;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Mat;

public class Camera implements CameraBridgeViewBase.CvCameraViewListener2 {

    private Mat frame;

    public Camera()
    {
        super();
        frame = new Mat();
    }

    public Mat getFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame)
    {
        return frame;
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        frame = inputFrame.rgba();
        return frame;
    }

    public Mat getFrame()
    {
        return frame;
    }
}
