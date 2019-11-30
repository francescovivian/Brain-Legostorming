package com.example.brainlegostormingapp;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class BallFinder
{
    private int sat_lower = 96;
    private int sat_upper = 255;
    private int red_lower = 160;
    private int red_upper = 180;
    private int blue_lower = 105;
    private int blue_upper = 120;
    private int yellow_lower = 16;
    private int yellow_upper = 25;

    private Mat frame;

    public BallFinder(Mat frame)
    {
        this.frame = frame.clone();
    }

    public ArrayList<Ball> findBalls()
    {
        Mat hsv = new Mat();
        List <Mat> split_hsv = new ArrayList<>();

        Imgproc.cvtColor(frame, hsv, Imgproc.COLOR_RGB2HSV);

        Core.split(hsv, split_hsv);

        Mat mask_sat = new Mat();
        Imgproc.threshold(split_hsv.get(1), mask_sat, sat_lower, sat_upper, Imgproc.THRESH_BINARY);

        Mat kernel = new Mat(new Size(3, 3), CvType.CV_8UC1, new Scalar(255));
        Imgproc.morphologyEx(mask_sat, mask_sat, Imgproc.MORPH_OPEN, kernel);

        Mat hue = split_hsv.get(0);
        Mat mask_red = new Mat();
        Mat mask_blue = new Mat();
        Mat mask_yellow = new Mat();

        Core.inRange(hsv, new Scalar(red_lower, 0, 0), new Scalar(red_upper, 255, 255), mask_red);
        Core.inRange(hsv, new Scalar(blue_lower, 0, 0), new Scalar(blue_upper, 255, 255), mask_blue);
        Core.inRange(hsv, new Scalar(yellow_lower, 0, 0), new Scalar(yellow_upper, 255, 255), mask_yellow);

        Mat mask_hue = new Mat();
        Mat mask = new Mat();

        Core.bitwise_or(mask_red, mask_blue, mask_hue);
        Core.bitwise_or(mask_hue, mask_yellow, mask_hue);
        Core.bitwise_and(mask_sat, mask_hue, mask);

        Mat grey = new Mat();
        Mat greyBlur = new Mat();
        Mat circles = new Mat();

        Imgproc.cvtColor(frame, grey, Imgproc.COLOR_RGB2GRAY);
        Imgproc.GaussianBlur(grey,greyBlur, new Size(9,9),2,2);
        Imgproc.HoughCircles(greyBlur,circles, Imgproc.CV_HOUGH_GRADIENT,1,greyBlur.rows()/8,200,100,0,0);
        //Imgproc.HoughCircles(greyBlur,circles, Imgproc.CV_HOUGH_GRADIENT,2.0,greyBlur.rows()/8,100,300,20,400);


        ArrayList<Ball> balls = new ArrayList<>();

        for(int i = 0; i < circles.cols() ; i++)
        {
            Point center = new Point(circles.get(0,i)[0], circles.get(0,i)[1]);
            Float radius = (float) circles.get(0,i)[2];

            int area_hue = (int) hue.get((int) center.y, (int) center.x)[0];
            String color;

            if (area_hue >= red_lower && area_hue <= red_upper)
                color = "red";
            else if (area_hue >= blue_lower && area_hue <= blue_upper)
                color = "blue";
            else if (area_hue >= yellow_lower && area_hue <= yellow_upper)
                color = "yellow";
            else
                color = "unknown";

            Ball b = new Ball(center,radius,color);
            balls.add(b);

            /*Scalar color_rgb;

            if (color == "red")
                color_rgb = new Scalar(255, 0, 0);
            else if (color == "blue")
                color_rgb = new Scalar(0, 0, 255);
            else if (color == "yellow")
                color_rgb = new Scalar(255, 255, 0);
            else
                color_rgb = new Scalar(0, 0, 0);

            Imgproc.circle(frame, center,radius.intValue(),color_rgb,8);*/
        }

        frame.release();
        hsv.release();
        split_hsv.removeAll(split_hsv);
        hue.release();
        kernel.release();
        mask_sat.release();
        mask_red.release();
        mask_yellow.release();
        mask_blue.release();
        mask_hue.release();
        mask.release();
        grey.release();
        greyBlur.release();
        circles.release();

        System.gc();

        return balls;
    }
}