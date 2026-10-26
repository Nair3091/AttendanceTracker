package com.example.attendancetracker.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class PieChartView extends View {
    private Paint presentPaint, absentPaint, cancelledPaint;
    private RectF rectF;
    private float presentAngle = 0, absentAngle = 0, cancelledAngle = 0;

    public PieChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        presentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        presentPaint.setColor(Color.parseColor("#4CAF50")); // Green

        absentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        absentPaint.setColor(Color.parseColor("#F44336")); // Red

        cancelledPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        cancelledPaint.setColor(Color.parseColor("#FF9800")); // Orange

        rectF = new RectF();
    }

    public void setData(int present, int absent, int cancelled) {
        int total = present + absent + cancelled;
        if (total > 0) {
            presentAngle = (present / (float) total) * 360f;
            absentAngle = (absent / (float) total) * 360f;
            cancelledAngle = (cancelled / (float) total) * 360f;
        } else {
            presentAngle = 0;
            absentAngle = 0;
            cancelledAngle = 0;
        }
        invalidate(); // Redraw chart with new data
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float padding = 16f;
        rectF.set(padding, padding, getWidth() - padding, getHeight() - padding);

        float startAngle = -90f; // Start from top

        // Draw Present slice
        canvas.drawArc(rectF, startAngle, presentAngle, true, presentPaint);
        startAngle += presentAngle;

        // Draw Absent slice
        canvas.drawArc(rectF, startAngle, absentAngle, true, absentPaint);
        startAngle += absentAngle;

        // Draw Cancelled slice
        canvas.drawArc(rectF, startAngle, cancelledAngle, true, cancelledPaint);
    }
}