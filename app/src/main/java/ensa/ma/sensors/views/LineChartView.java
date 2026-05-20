package ensa.ma.sensors.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class LineChartView extends View {

    private final List<Float> values = new ArrayList<>();
    private final int maxPoints = 60;

    private final Paint gridPaint   = new Paint();
    private final Paint linePaint   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fillPaint   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dotPaint    = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bgPaint     = new Paint(Paint.ANTI_ALIAS_FLAG);

    public LineChartView(Context context) {
        super(context);

        bgPaint.setColor(Color.parseColor("#1E1E2E"));
        bgPaint.setStyle(Paint.Style.FILL);

        gridPaint.setColor(Color.parseColor("#2A2A3E"));
        gridPaint.setStrokeWidth(1f);
        gridPaint.setStyle(Paint.Style.STROKE);

        linePaint.setColor(Color.parseColor("#7C3AED"));
        linePaint.setStrokeWidth(4f);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        linePaint.setStrokeJoin(Paint.Join.ROUND);

        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setAlpha(80);

        dotPaint.setColor(Color.parseColor("#A78BFA"));
        dotPaint.setStyle(Paint.Style.FILL);

        labelPaint.setColor(Color.parseColor("#6B7280"));
        labelPaint.setTextSize(26f);
        labelPaint.setAntiAlias(true);
    }

    public void addValue(float value) {
        if (values.size() >= maxPoints) values.remove(0);
        values.add(value);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int w = getWidth();
        int h = getHeight();
        float padL = 60f, padR = 20f, padT = 20f, padB = 40f;

        // Background arrondi
        RectF bg = new RectF(0, 0, w, h);
        canvas.drawRoundRect(bg, 24f, 24f, bgPaint);

        // Grille horizontale (4 lignes)
        for (int i = 1; i <= 4; i++) {
            float y = padT + (h - padT - padB) * i / 4f;
            canvas.drawLine(padL, y, w - padR, y, gridPaint);
        }

        if (values.size() < 2) {
            Paint waitPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            waitPaint.setColor(Color.parseColor("#4B5563"));
            waitPaint.setTextSize(32f);
            waitPaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("En attente...", w / 2f, h / 2f, waitPaint);
            return;
        }

        float min = Float.MAX_VALUE, max = -Float.MAX_VALUE;
        for (float v : values) { min = Math.min(min, v); max = Math.max(max, v); }
        if (max == min) max = min + 1;

        float chartW = w - padL - padR;
        float chartH = h - padT - padB;

        // Gradient fill sous la courbe
        fillPaint.setShader(new LinearGradient(0, padT, 0, h - padB,
                Color.parseColor("#7C3AED"), Color.TRANSPARENT, Shader.TileMode.CLAMP));

        Path linePath = new Path();
        Path fillPath = new Path();

        for (int i = 0; i < values.size(); i++) {
            float x = padL + i * (chartW / (maxPoints - 1));
            float norm = (values.get(i) - min) / (max - min);
            float y = padT + chartH * (1f - norm);

            if (i == 0) {
                linePath.moveTo(x, y);
                fillPath.moveTo(x, h - padB);
                fillPath.lineTo(x, y);
            } else {
                linePath.lineTo(x, y);
                fillPath.lineTo(x, y);
            }
        }

        // Fermer le fill
        float lastX = padL + (values.size() - 1) * (chartW / (maxPoints - 1));
        fillPath.lineTo(lastX, h - padB);
        fillPath.close();

        canvas.drawPath(fillPath, fillPaint);
        canvas.drawPath(linePath, linePaint);

        // Point actuel (dernier)
        float lastNorm = (values.get(values.size() - 1) - min) / (max - min);
        float lastY = padT + chartH * (1f - lastNorm);
        canvas.drawCircle(lastX, lastY, 8f, dotPaint);

        // Labels min/max
        canvas.drawText(String.format("%.1f", max), 4f, padT + 10f, labelPaint);
        canvas.drawText(String.format("%.1f", min), 4f, h - padB + 10f, labelPaint);
    }
}
