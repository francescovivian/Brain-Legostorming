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
    private int black_lower = 0;
    private int black_upper = 30;

    private Mat frame,fr;

    public LineFinder(Mat frame) {
        this.frame = frame.clone();
    }

    public ArrayList<Line> findLines() {
        Mat hsv = new Mat();
        List<Mat> split_hsv = new ArrayList<>();

        Imgproc.cvtColor(frame, hsv, Imgproc.COLOR_RGB2HSV);

        Core.split(hsv, split_hsv);

        Mat hue = split_hsv.get(0);

        Mat edges = new Mat();
        Mat matLines = new Mat();

        Mat grey = new Mat();
        Mat greyFiltered = new Mat();

        Imgproc.cvtColor(frame, grey, Imgproc.COLOR_RGB2GRAY);
        Imgproc.bilateralFilter(grey, greyFiltered, 10, 10, 10);

        Imgproc.Canny(greyFiltered, edges, 50, 90);
        Imgproc.HoughLinesP(edges, matLines, 1, Math.PI / 180, 10, 100, 5);

        ArrayList<Line> lines = new ArrayList<>();
        Line line;

        for (int i = 0; i < matLines.rows(); i++) {
            Point p1 = new Point(matLines.get(i, 0)[0], matLines.get(i, 0)[1]),
                  p2 = new Point(matLines.get(i, 0)[2], matLines.get(i, 0)[3]);

            int area_hue_1 = (int) hue.get((int) p1.y, (int) p1.x)[0];
            int area_hue_2 = (int) hue.get((int) p2.y, (int) p2.x)[0];
            int area_hue_3 = (int) hue.get((int) (p1.y + p2.y)/2, (int) (p1.x + p2.x)/2)[0];
            int area_hue = (area_hue_1 + area_hue_2 + area_hue_3) / 3;
            String color;

            if (area_hue >= black_lower && area_hue <= black_upper) color = "black";
            else color = "unknown";

            //double dx = Math.abs(p1.x - p2.x);
            double dy = Math.abs(p1.y - p2.y);

            if (color.equals("black") && p1.x != p2.x && dy <= 100) {
                line = new Line(p1, p2);
                lines.add(line);
            }
        }

        frame.release();
        grey.release();
        greyFiltered.release();
        edges.release();
        matLines.release();

        System.gc();

        return lines;
    }

    public Mat getFrame()
    {
        return fr;
    }
}
