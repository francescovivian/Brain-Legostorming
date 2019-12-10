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

    private Mat frame, fr;

    public LineFinder(Mat frame) {
        this.frame = frame.clone();
    }

    public ArrayList<Line> findLines() {
        Mat edges = new Mat();
        Mat matLines = new Mat();

        Mat grey = new Mat();
        Mat greyFiltered = new Mat();

        Imgproc.cvtColor(frame, grey, Imgproc.COLOR_RGB2GRAY);
        Imgproc.bilateralFilter(grey, greyFiltered, 10, 10, 10);

        Imgproc.Canny(greyFiltered, edges, 50, 90);
        Imgproc.HoughLinesP(edges, matLines, 1, Math.PI / 180, 10, 100, 20);

        fr = edges.clone();

        ArrayList<Line> lines = new ArrayList<>();
        Line line;

        for (int i = 0; i < matLines.cols(); i++) {
            Point p1 = new Point(matLines.get(0, i)[0], matLines.get(0, i)[1]);
            Point p2 = new Point(matLines.get(0, i)[2], matLines.get(0, i)[3]);

            line = new Line(p1, p2);
            lines.add(line);
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
