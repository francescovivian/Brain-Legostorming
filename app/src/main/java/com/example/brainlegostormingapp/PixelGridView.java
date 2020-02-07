package com.example.brainlegostormingapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class PixelGridView extends View {
    private int numColumns, numRows;
    private int cellWidth, cellHeight;
    private Paint textPaint = new Paint();
    private Paint blackPaint = new Paint();
    private Paint redPaint = new Paint();
    private Paint greenPaint = new Paint();
    private Paint yellowPaint = new Paint();
    private Paint bluePaint = new Paint();
    private int[][] cellChecked;
    boolean start = false;
    private char orientation;

    public PixelGridView(Context context, char orientation) {
        this(context, null, orientation);
    }

    public PixelGridView(Context context, AttributeSet attrs, char orientation) {
        super(context, attrs);
        this.orientation = orientation;
        textPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(25);
        blackPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        blackPaint.setColor(Color.BLACK);
        redPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        redPaint.setColor(Color.RED);
        greenPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        greenPaint.setColor(Color.GREEN);
        yellowPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        yellowPaint.setColor(Color.YELLOW);
        bluePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        bluePaint.setColor(Color.BLUE);
    }

    public void setNumRows(int numRows) {
        this.numRows = numRows + 1;
        calculateDimensions();
    }

    public void setNumColumns(int numColumns) {
        this.numColumns = numColumns + 1;
        calculateDimensions();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        calculateDimensions();
    }

    private void calculateDimensions() {
        if (numColumns < 1 || numRows < 1) return;

        cellWidth = getWidth() / numColumns;
        cellHeight = getHeight() / numRows;

        if (!start) {
            cellChecked = new int[numColumns][numRows];
            start = true;
        }

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.GRAY);

        if (numColumns == 0 || numRows == 0) {
            return;
        }

        int width = getWidth();
        int height = getHeight();

        for (int i = 0; i < numColumns; i++) {
            for (int j = 0; j < numRows; j++) {
                if (cellChecked[i][j] == 1) {
                    canvas.drawRect(i * cellWidth, j * cellHeight,
                            (i + 1) * cellWidth, (j + 1) * cellHeight,
                            redPaint);
                }
            }
        }

        for (int i = 0; i < numColumns; i++) {
            for (int j = 0; j < numRows; j++) {
                if (cellChecked[i][j] == 2) {
                    canvas.drawRect(i * cellWidth, j * cellHeight,
                            (i + 1) * cellWidth, (j + 1) * cellHeight,
                            greenPaint);
                }
            }
        }

        for (int i = 0; i < numColumns; i++) {
            for (int j = 0; j < numRows; j++) {
                if (cellChecked[i][j] == 3) {
                    canvas.drawRect(i * cellWidth, j * cellHeight,
                            (i + 1) * cellWidth, (j + 1) * cellHeight,
                            yellowPaint);
                }
            }
        }

        for (int i = 0; i < numColumns; i++) {
            for (int j = 0; j < numRows; j++) {
                if (cellChecked[i][j] == 4) {
                    canvas.drawRect(i * cellWidth, j * cellHeight,
                            (i + 1) * cellWidth, (j + 1) * cellHeight,
                            bluePaint);
                }
            }
        }

        for (int i = 0; i < numColumns; i++) {
            canvas.drawLine(i * cellWidth, 0, i * cellWidth, height, blackPaint);
        }

        for (int i = 0; i < numRows; i++) {
            canvas.drawLine(0, i * cellHeight, width, i * cellHeight, blackPaint);
        }

        Integer nc;
        for (Integer i = 1; i < numColumns; i++) {
            nc = i - 1;
            canvas.drawText(nc.toString(), (i + 0.4f) * cellWidth, (numRows - 1 + 0.7f) * cellHeight, textPaint);
        }

        Integer nr = 0;
        for (Integer j = numRows - 2; j >= 0; j--) {
            canvas.drawText(nr.toString(), (0.4f) * cellWidth, (j + 0.7f) * cellHeight, textPaint);
            nr++;
        }
    }

    public Position convert(int x, int y)
    {
        int r=y, c=x;
        if (orientation == 's')
        {
            r = x + 1;
            c = numRows - y - 2;
        }
        if (orientation == 'n')
        {
            r = numColumns - x - 1;
            c = y;
        }
        if (orientation == 'e')
        {
            r = numColumns - y - 1;
            c = numRows - x - 2;
        }
        if (orientation == 'o')
        {
            r = y + 1;
            c = x;
        }

        return new Position(r,c);
    }
    public void cellCheck(int x, int y, String item) {
        int r,c;
        Position p = convert(x,y);
        r = p.getX();
        c = p.getY();

        if (item.equals("ROBOT"))
        {
            if (cellChecked[r][c] == 1) cellChecked[r][c] = 3;
            else cellChecked[r][c] = 2;
        }
        else if (item.equals("MINA"))
        {
            if (cellChecked[r][c] == 2) cellChecked[r][c] = 3;
            else cellChecked[r][c] = 1;
        }

        invalidate();
    }

    public void cellUncheck(int x, int y, String item) {
        int r,c;
        Position p = convert(x,y);
        r = p.getX();
        c = p.getY();

        if (item.equals("ROBOT"))
        {
            if (cellChecked[r][c] == 3) cellChecked[r][c] = 1;
            else cellChecked[r][c] = 0;
        }
        else if (item.equals("MINA"))
        {
            if (cellChecked[r][c] == 3) cellChecked[r][c] = 2;
            else cellChecked[r][c] = 0;
        }

        invalidate();
    }

    public void stopSignal()
    {
        if (cellChecked[0][numRows-1] == 0) cellChecked[0][numRows-1] = 4;
        else cellChecked[0][numRows-1] = 0;

        invalidate();
    }
}
