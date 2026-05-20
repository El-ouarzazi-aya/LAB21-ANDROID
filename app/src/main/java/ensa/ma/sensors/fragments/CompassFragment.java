package ensa.ma.sensors.fragments;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class CompassFragment extends Fragment implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer, magnetometer;

    private TextView degreeView, directionView, statusView;
    private CompassView compassView;

    private final float[] gravityVals  = new float[3];
    private final float[] magneticVals = new float[3];
    private boolean hasGravity = false, hasMagnetic = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup parent,
                             @Nullable Bundle savedInstanceState) {

        LinearLayout root = new LinearLayout(requireContext());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#0F0F1A"));
        root.setPadding(32, 32, 32, 32);
        root.setGravity(Gravity.CENTER_HORIZONTAL);

        TextView title = new TextView(requireContext());
        title.setText("🧭  Boussole");
        title.setTextSize(22f);
        title.setTextColor(Color.parseColor("#E2E8F0"));
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 8);
        root.addView(title);

        statusView = new TextView(requireContext());
        statusView.setText("● Initialisation...");
        statusView.setTextSize(12f);
        statusView.setTextColor(Color.parseColor("#F59E0B"));
        statusView.setGravity(Gravity.CENTER);
        statusView.setPadding(0, 0, 0, 32);
        root.addView(statusView);

        // Rose des vents dessinée
        compassView = new CompassView(requireContext());
        LinearLayout.LayoutParams compassParams = new LinearLayout.LayoutParams(500, 500);
        compassParams.gravity = Gravity.CENTER_HORIZONTAL;
        compassParams.bottomMargin = 32;
        compassView.setLayoutParams(compassParams);
        root.addView(compassView);

        // Degrés
        LinearLayout infoCard = new LinearLayout(requireContext());
        infoCard.setOrientation(LinearLayout.VERTICAL);
        infoCard.setBackgroundColor(Color.parseColor("#1A1A2E"));
        infoCard.setPadding(32, 24, 32, 24);
        infoCard.setGravity(Gravity.CENTER);
        infoCard.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        degreeView = new TextView(requireContext());
        degreeView.setText("--°");
        degreeView.setTextSize(48f);
        degreeView.setTextColor(Color.parseColor("#A78BFA"));
        degreeView.setTypeface(null, Typeface.BOLD);
        degreeView.setGravity(Gravity.CENTER);

        directionView = new TextView(requireContext());
        directionView.setText("--");
        directionView.setTextSize(18f);
        directionView.setTextColor(Color.parseColor("#94A3B8"));
        directionView.setGravity(Gravity.CENTER);
        directionView.setPadding(0, 8, 0, 0);

        infoCard.addView(degreeView);
        infoCard.addView(directionView);
        root.addView(infoCard);

        sensorManager  = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        accelerometer  = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer   = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (accelerometer != null)
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        if (magnetometer != null)
            sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);

        if (accelerometer == null || magnetometer == null) {
            statusView.setText("⛔  Capteur manquant");
            statusView.setTextColor(Color.parseColor("#EF4444"));
        } else {
            statusView.setText("● Capteurs actifs");
            statusView.setTextColor(Color.parseColor("#10B981"));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, gravityVals, 0, 3);
            hasGravity = true;
        }
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magneticVals, 0, 3);
            hasMagnetic = true;
        }

        if (hasGravity && hasMagnetic) {
            float[] rotMat = new float[9];
            float[] orient = new float[3];
            if (SensorManager.getRotationMatrix(rotMat, null, gravityVals, magneticVals)) {
                SensorManager.getOrientation(rotMat, orient);
                float deg = (float) Math.toDegrees(orient[0]);
                if (deg < 0) deg += 360;

                degreeView.setText(String.format("%.1f°", deg));
                directionView.setText(getDirectionName(deg));
                compassView.setAngle(deg);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    private String getDirectionName(float d) {
        if (d >= 337.5 || d < 22.5)  return "Nord ↑";
        if (d < 67.5)                 return "Nord-Est ↗";
        if (d < 112.5)                return "Est →";
        if (d < 157.5)                return "Sud-Est ↘";
        if (d < 202.5)                return "Sud ↓";
        if (d < 247.5)                return "Sud-Ouest ↙";
        if (d < 292.5)                return "Ouest ←";
        return "Nord-Ouest ↖";
    }

    // Vue boussole personnalisée
    static class CompassView extends View {
        private float angle = 0f;
        private final Paint circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint needlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint textPaint   = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint dotPaint    = new Paint(Paint.ANTI_ALIAS_FLAG);

        public CompassView(Context ctx) {
            super(ctx);
            circlePaint.setStyle(Paint.Style.STROKE);
            circlePaint.setColor(Color.parseColor("#2A2A3E"));
            circlePaint.setStrokeWidth(3f);

            needlePaint.setStyle(Paint.Style.FILL);
            dotPaint.setColor(Color.parseColor("#A78BFA"));
            dotPaint.setStyle(Paint.Style.FILL);

            textPaint.setColor(Color.parseColor("#94A3B8"));
            textPaint.setTextSize(36f);
            textPaint.setTextAlign(Paint.Align.CENTER);
            textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        }

        public void setAngle(float angle) {
            this.angle = angle;
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float cx = getWidth() / 2f;
            float cy = getHeight() / 2f;
            float r  = Math.min(cx, cy) - 20f;

            // Cercle extérieur
            canvas.drawCircle(cx, cy, r, circlePaint);
            canvas.drawCircle(cx, cy, r * 0.7f, circlePaint);

            // Points cardinaux
            String[] dirs = {"N", "E", "S", "O"};
            float[] angles = {0, 90, 180, 270};
            int[] colors = {
                    Color.parseColor("#EF4444"),
                    Color.parseColor("#94A3B8"),
                    Color.parseColor("#94A3B8"),
                    Color.parseColor("#94A3B8")
            };
            for (int i = 0; i < 4; i++) {
                double rad = Math.toRadians(angles[i] - angle);
                float tx = cx + (r - 30f) * (float) Math.sin(rad);
                float ty = cy - (r - 30f) * (float) Math.cos(rad) + 12f;
                textPaint.setColor(colors[i]);
                canvas.drawText(dirs[i], tx, ty, textPaint);
            }

            // Aiguille Nord (rouge)
            needlePaint.setColor(Color.parseColor("#EF4444"));
            drawNeedle(canvas, cx, cy, (float) Math.toRadians(-angle), r * 0.55f);

            // Aiguille Sud (grise)
            needlePaint.setColor(Color.parseColor("#374151"));
            drawNeedle(canvas, cx, cy, (float) Math.toRadians(180 - angle), r * 0.45f);

            // Centre
            canvas.drawCircle(cx, cy, 12f, dotPaint);
        }

        private void drawNeedle(Canvas canvas, float cx, float cy, float rad, float len) {
            float tipX = cx + len * (float) Math.sin(rad);
            float tipY = cy - len * (float) Math.cos(rad);

            android.graphics.Path path = new android.graphics.Path();
            float perpX = (float) Math.cos(rad) * 12f;
            float perpY = (float) Math.sin(rad) * 12f;
            path.moveTo(cx - perpX, cy - perpY);
            path.lineTo(tipX, tipY);
            path.lineTo(cx + perpX, cy + perpY);
            path.close();
            canvas.drawPath(path, needlePaint);
        }
    }
}
