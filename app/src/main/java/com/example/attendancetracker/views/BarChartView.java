package com.example.attendancetracker.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import java.util.ArrayList;
import java.util.List;

public class BarChartView extends View {
    private Paint barPaint, textPaint;
    private List<Float> monthlyPercentages = new ArrayList<>();
    private List<String> monthLabels = new ArrayList<>();

    public BarChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        barPaint.setColor(Color.parseColor("#2196F3")); // Blue

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.DKGRAY);
        textPaint.setTextSize(28f);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    public void setData(List<Float> percentages, List<String> labels) {
        this.monthlyPercentages = percentages;
        this.monthLabels = labels;
        invalidate(); // Redraw chart
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (monthlyPercentages == null || monthlyPercentages.isEmpty()) return;

        int width = getWidth();
        int height = getHeight();
        int numBars = monthlyPercentages.size();
        float barWidth = (width / (float) numBars) * 0.5f;
        float space = (width / (float) numBars) * 0.5f;

        for (int i = 0; i < numBars; i++) {
            float pct = monthlyPercentages.get(i);
            float barHeight = (pct / 100f) * (height - 80f);

            float left = space / 2 + i * (barWidth + space);
            float top = height - 50f - barHeight;
            float right = left + barWidth;
            float bottom = height - 50f;

            // Draw Bar
            canvas.drawRect(left, top, right, bottom, barPaint);

            // Draw Percentage text above bar
            canvas.drawText(String.format("%.0f%%", pct), left + barWidth / 2, top - 10f, textPaint);

            // Draw Month Label below bar
            if (i < monthLabels.size()) {
                canvas.drawText(monthLabels.get(i), left + barWidth / 2, height - 10f, textPaint);
            }
        }
    }
}