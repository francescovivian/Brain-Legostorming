package com.example.brainlegostormingapp;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.opencv.core.Point;

import java.util.ArrayList;

public class LineFinder {
    private Mat frame,
            edges = new Mat(),
            matLines = new Mat();

    int threshold = 10;
    int minLineSize = 100;
    int lineGap = 20;

    public LineFinder(Mat frame) {
        this.frame = frame;
    }

    public ArrayList<Line> findLines() {
        ArrayList<Line> lines = new ArrayList<>();
        Imgproc.Canny(frame, edges, 50, 90);
        Imgproc.HoughLinesP(edges, matLines, 1, Math.PI / 180, threshold, minLineSize, lineGap);

        for (Integer x = 0; x < matLines.cols(); x++) {
            double[] vec = matLines.get(0, x);
            Point p1 = new Point(vec[0], vec[1]);
            Point p2 = new Point(vec[2], vec[3]);
            Line line = new Line(p1, p2);
            lines.add(line);
        }


        return lines;

    }
}
