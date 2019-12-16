package com.example.brainlegostormingapp;

import com.example.brainlegostormingapp.ObjectOfInterest.Ball;
import com.example.brainlegostormingapp.ObjectOfInterest.Line;
import com.example.brainlegostormingapp.ObjectOfInterest.ObjectFind;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ObjectFinder{
    //private int sat_lower = 96;
    //private int sat_upper = 255;

    private Mat frame, grey, greyFiltered, hsv, hue, circles, edges, matLines;
    private List<Mat> split_hsv;


    public ObjectFinder(Mat frame)
    {
        this.frame = frame.clone();
        grey = new Mat();
        hsv = new Mat();
        greyFiltered = new Mat();
        split_hsv = new ArrayList<>();
        Imgproc.cvtColor(frame, hsv, Imgproc.COLOR_RGB2HSV);
        Core.split(hsv, split_hsv);
        hue = split_hsv.get(0);
        Imgproc.cvtColor(frame, grey, Imgproc.COLOR_RGB2GRAY);
        //Imgproc.GaussianBlur(grey,greyBlur, new Size(9,9),2,2);
        Imgproc.bilateralFilter(grey, greyFiltered, 10, 10, 10);
    }

    public ObjectFind findObject() {
        ObjectFind objectFind = new ObjectFind();
        objectFind.setLines(findLines()).setBalls(findBalls());
        cleanMemory();
        return objectFind;
    }
    public ObjectFind findObject(String... items){
        ObjectFind objectFind = new ObjectFind();
        List<String> list = Arrays.asList(items);
        if(list.contains("b"))
            objectFind.setBalls(findBalls());
        if(list.contains("l"))
            objectFind.setLines(findLines());
        cleanMemory();
        return objectFind;
    }

    private ArrayList<Ball> findBalls()
    {
        int red_lower = 160, red_upper = 180, blue_lower = 105, blue_upper = 120, yellow_lower = 16, yellow_upper = 25;
        int area_hue, area_hue_1, area_hue_2, area_hue_3, area_hue_4, area_hue_5;
        circles = new Mat();
        ArrayList<Ball> balls = new ArrayList<>();
        Imgproc.HoughCircles(greyFiltered, circles, Imgproc.CV_HOUGH_GRADIENT, 1, greyFiltered.rows() / 4, 30, 30, 5, 40);
        for (int i = 0; i < circles.cols(); i++) {
            Point center = new Point(circles.get(0, i)[0], circles.get(0, i)[1]);
            Float radius = (float) circles.get(0, i)[2];

            if ((center.x + radius/2) < frame.width() && (center.x - radius/2) >= 0 && (center.y + radius/2) < frame.height() && (center.y - radius/2) >= 0) {
                area_hue_1 = (int) hue.get((int) (center.y), (int) (center.x))[0];
                area_hue_2 = (int) hue.get((int) (center.y + radius / 2), (int) (center.x))[0];
                area_hue_3 = (int) hue.get((int) (center.y - radius / 2), (int) (center.x))[0];
                area_hue_4 = (int) hue.get((int) (center.y), (int) (center.x + radius / 2))[0];
                area_hue_5 = (int) hue.get((int) (center.y), (int) (center.x - radius / 2))[0];
                area_hue = (area_hue_1 + area_hue_2 + area_hue_3 + area_hue_4 + area_hue_5) / 5;
            }
            else area_hue = (int) hue.get((int) (center.y), (int) (center.x))[0];
            String color;

            if (area_hue >= red_lower && area_hue <= red_upper) color = "red";
            else if (area_hue >= blue_lower && area_hue <= blue_upper) color = "blue";
            else if (area_hue >= yellow_lower && area_hue <= yellow_upper) color = "yellow";
            else color = "unknown";

            if (!color.equals("unknown") && center.x != 0 && center.y != 0 && radius != 0)
                balls.add(new Ball(center, radius, color));
        }
        return balls;
    }

    private ArrayList<Line> findLines()
    {
        int black_lower = 0, black_upper = 30;
        int area_hue_1, area_hue_2, area_hue_3, area_hue;
        edges = new Mat();
        matLines = new Mat();
        ArrayList<Line> lines = new ArrayList<>();
        Imgproc.Canny(greyFiltered, edges, 50, 90);
        Imgproc.HoughLinesP(edges, matLines, 1, Math.PI / 180, 10, 100, 5);
        for (int i = 0; i < matLines.rows(); i++) {
            Point p1 = new Point(matLines.get(i, 0)[0], matLines.get(i, 0)[1]),
                    p2 = new Point(matLines.get(i, 0)[2], matLines.get(i, 0)[3]);

            area_hue_1 = (int) hue.get((int) p1.y, (int) p1.x)[0];
            area_hue_2 = (int) hue.get((int) p2.y, (int) p2.x)[0];
            area_hue_3 = (int) hue.get((int) (p1.y + p2.y)/2, (int) (p1.x + p2.x)/2)[0];
            area_hue = (area_hue_1 + area_hue_2 + area_hue_3) / 3;
            String color;

            if (area_hue >= black_lower && area_hue <= black_upper) color = "black";
            else color = "unknown";

            double dx = Math.abs(p1.x - p2.x);
            double dy = Math.abs(p1.y - p2.y);

            if (color.equals("black") && (dx <= 5 || dy <= 100))
                lines.add(new Line(p1, p2));
        }
        return lines;
    }

    public void cleanMemory()
    {
        frame.release();
        hsv.release();
        split_hsv.get(0).release();
        split_hsv.get(1).release();
        split_hsv.get(2).release();
        hue.release();
        grey.release();
        greyFiltered.release();
        if(circles != null)
            circles.release();
        if(edges != null && matLines != null){
            edges.release();
            matLines.release();
        }

        System.gc();
    }

}
