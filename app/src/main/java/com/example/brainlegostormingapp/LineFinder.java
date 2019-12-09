package com.example.brainlegostormingapp;
        import android.graphics.Bitmap;
        import org.opencv.android.Utils;
        import org.opencv.core.Core;
        import org.opencv.core.CvType;
        import org.opencv.core.Mat;
        import org.opencv.core.Scalar;
        import org.opencv.core.Size;
        import org.opencv.imgproc.Imgproc;
        import org.opencv.core.Point;

        import java.util.ArrayList;

public class LineFinder {
    private Mat frame,
            edges = new Mat(),
            matLines = new Mat();
    Mat mRgba = new Mat(612,816, CvType.CV_8UC1);

    int threshold = 50;
    int minLineSize = 20;
    int lineGap = 20;

    public LineFinder(Mat frame) {
        this.frame = frame;

    }
    public ArrayList<Line> findLines() {
        ArrayList<Line> lines = new ArrayList<>();
        Imgproc.Canny(frame, edges, 50, 90);
        Imgproc.HoughLinesP(edges, matLines, 1, Math.PI / 180, threshold, minLineSize, lineGap);

        for (int x = 0; x < matLines.cols(); x++) {
            double[] vec = matLines.get(0, x);
            Point p1 = new Point(vec[0],vec[1]);
            Point p2 = new Point(vec[2],vec[3]);
            Line line = new Line(p1,p2);
            lines.add(line);
        }
        Bitmap bmp = Bitmap.createBitmap(mRgba.cols(), mRgba.rows(), Bitmap.Config.ARGB_8888);

        Utils.matToBitmap(mRgba, bmp);
        return lines;

    }
}
