package com.example.brainlegostormingapp._OLD;

import com.example.brainlegostormingapp.ObjectOfInterest.Ball;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class BallFinder {
    private int sat_lower = 96;
    private int sat_upper = 255;
    private int red_lower = 160;
    private int red_upper = 180;
    private int blue_lower = 105;
    private int blue_upper = 120;
    private int yellow_lower = 16;
    private int yellow_upper = 25;

    private Mat frame,fr;

    public BallFinder(Mat frame) {
        this.frame = frame.clone();
    }

    public ArrayList<Ball> findBalls() {
        Mat hsv = new Mat();
        List<Mat> split_hsv = new ArrayList<>();

        Imgproc.cvtColor(frame, hsv, Imgproc.COLOR_RGB2HSV);

        Core.split(hsv, split_hsv);

        Mat hue = split_hsv.get(0);

        Mat grey = new Mat();
        Mat greyFiltered = new Mat();
        Mat circles = new Mat();

        Imgproc.cvtColor(frame, grey, Imgproc.COLOR_RGB2GRAY);
        //Imgproc.GaussianBlur(grey,greyBlur, new Size(9,9),2,2);
        Imgproc.bilateralFilter(grey, greyFiltered, 10, 10, 10);
        Imgproc.HoughCircles(greyFiltered, circles, Imgproc.CV_HOUGH_GRADIENT, 1, greyFiltered.rows() / 4, 30, 30, 5, 40);

        ArrayList<Ball> balls = new ArrayList<>();
        Ball b;

        for (int i = 0; i < circles.cols(); i++) {
            Point center = new Point(circles.get(0, i)[0], circles.get(0, i)[1]);
            Float radius = (float) circles.get(0, i)[2];

            int area_hue_1 = (int) hue.get((int) (center.y + radius/2), (int) (center.x + radius/2))[0];
            int area_hue_2 = (int) hue.get((int) (center.y - radius/2), (int) (center.x - radius/2))[0];
            int area_hue_3 = (int) hue.get((int) (center.y + radius/2), (int) (center.x - radius/2))[0];
            int area_hue_4 = (int) hue.get((int) (center.y - radius/2), (int) (center.x + radius/2))[0];
            int area_hue = (area_hue_1 + area_hue_2 + area_hue_3 + area_hue_4) / 4;
            String color;

            if (area_hue >= red_lower && area_hue <= red_upper) color = "red";
            else if (area_hue >= blue_lower && area_hue <= blue_upper) color = "blue";
            else if (area_hue >= yellow_lower && area_hue <= yellow_upper) color = "yellow";
            else color = "unknown";

            if (!color.equals("unknown") && center.x != 0 && center.y != 0 && radius != 0) {
                b = new Ball(center, radius, color);
                balls.add(b);
            }
        }

        frame.release();
        hsv.release();
        split_hsv.get(0).release();
        split_hsv.get(1).release();
        split_hsv.get(2).release();
        hue.release();
        grey.release();
        greyFiltered.release();
        circles.release();

        System.gc();

        return balls;
    }

    public Mat getFrame() {
        return fr;
    }
}