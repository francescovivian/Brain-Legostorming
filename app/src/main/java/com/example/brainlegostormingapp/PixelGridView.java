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
    private Paint blackPaint = new Paint();
    private Paint textPaint = new Paint();
    private Paint redPaint = new Paint();
    private boolean[][] cellChecked;
    boolean start = false;

    public PixelGridView(Context context) {
        this(context, null);
    }

    public PixelGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        blackPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        blackPaint.setColor(Color.BLACK);
        redPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        redPaint.setColor(Color.RED);
        textPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(25);
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
            cellChecked = new boolean[numColumns][numRows];
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
                if (cellChecked[i][j]) {
                    canvas.drawRect(i * cellWidth, j * cellHeight,
                            (i + 1) * cellWidth, (j + 1) * cellHeight,
                            redPaint);
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

    public void changeCellChecked(int column, int row) {
        cellChecked[column + 1][numRows - row - 2] = !cellChecked[column + 1][numRows - row - 2];
        invalidate();
    }
}
