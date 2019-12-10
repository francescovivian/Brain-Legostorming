package com.example.brainlegostormingapp;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.core.Point;

import java.util.ArrayList;
import java.util.List;

public class LineFinder {
    private int sat_lower = 96;
    private int sat_upper = 255;
    private int black_lower = 0;
    private int black_upper = 0;

    private Mat frame,
            edges = new Mat(),
            matLines = new Mat();

    public LineFinder(Mat frame) {
        this.frame = frame;
    }

    public ArrayList<Line> findLines() {
        Mat hsv = new Mat();
        List<Mat> split_hsv = new ArrayList<>();

        Imgproc.cvtColor(frame, hsv, Imgproc.COLOR_RGB2HSV);

        Core.split(hsv, split_hsv);

        Mat mask_sat = new Mat();
        Imgproc.threshold(split_hsv.get(1), mask_sat, sat_lower, sat_upper, Imgproc.THRESH_BINARY);

        Mat kernel = new Mat(new Size(3, 3), CvType.CV_8UC1, new Scalar(255));
        Imgproc.morphologyEx(mask_sat, mask_sat, Imgproc.MORPH_OPEN, kernel);

        Mat hue = split_hsv.get(0);
        Mat mask_black = new Mat();

        Core.inRange(hsv, new Scalar(black_lower, 0, 0), new Scalar(black_upper, 255, 255), mask_black);

        Mat mask = new Mat();

        Core.bitwise_and(mask_sat, mask_black, mask);

        ArrayList<Line> lines = new ArrayList<>();
        //Imgproc.Canny(frame, edges, 50, 90);
        //Imgproc.HoughLinesP(edges, matLines, 1, Math.PI / 180, 10, 100, 20);

        Line line;

        for (Integer x = 0; x < matLines.cols(); x++) {
            double[] vec = matLines.get(0, x);
            Point p1 = new Point(vec[0], vec[1]);
            Point p2 = new Point(vec[2], vec[3]);

            int area_hue_1 = (int) hue.get((int) p1.x, (int) p1.x)[0];
            int area_hue_2 = (int) hue.get((int) p2.x, (int) p2.x)[0];
            int area_hue = (area_hue_1 + area_hue_2)/2;
            String color;

            if (area_hue >= black_lower && area_hue <= black_upper) color = "black";
            else color = "unknown";

            if (!color.equals("unknown"))
            {
                line = new Line(p1, p2);
                lines.add(line);
            }

        }

        frame.release();
        hsv.release();
        split_hsv.get(0).release();
        split_hsv.get(1).release();
        split_hsv.get(2).release();
        hue.release();
        kernel.release();
        mask_sat.release();
        mask.release();
        edges.release();
        matLines.release();

        System.gc();


        return lines;

    }
}
