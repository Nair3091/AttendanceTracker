package com.example.attendancetracker.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;

public class ProgressRingView extends View {
    private Paint backgroundPaint;
    private Paint progressPaint;
    private RectF rectF;
    private float percentage = 0f;
    private int targetPercent = 75;

    public ProgressRingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        rectF = new RectF();

        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setStrokeWidth(24f);
        backgroundPaint.setColor(0xFFE0E0E0); // Light grey background track

        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(24f);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);
        progressPaint.setColor(0xFF4CAF50); // Default Green
    }

    public void setProgress(float percentage, int targetPercent) {
        this.percentage = percentage;
        this.targetPercent = targetPercent;

        // Color coding logic based on design specs: Green, Orange (within 5 below), Red (>5 below)
        if (percentage >= targetPercent) {
            progressPaint.setColor(0xFF4CAF50); // Green
        } else if (percentage >= targetPercent - 5) {
            progressPaint.setColor(0xFFFF9800); // Orange
        } else {
            progressPaint.setColor(0xFFF44336); // Red
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        int diameter = Math.min(width, height);

        float padding = 30f;
        rectF.set(padding, padding, diameter - padding, diameter - padding);

        canvas.drawArc(rectF, 0, 360, false, backgroundPaint);

        float sweepAngle = (percentage / 100f) * 360f;
        canvas.drawArc(rectF, -90, sweepAngle, false, progressPaint);
    }
}