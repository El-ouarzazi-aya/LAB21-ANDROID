package ensa.ma.sensors.fragments;

import android.content.Context;
import android.graphics.Color;
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

import java.util.LinkedList;
import java.util.Queue;

public class ActivityRecognitionFragment extends Fragment implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;

    private TextView activityView;
    private TextView activityIcon;
    private TextView movementView;
    private TextView xView, yView, zView;

    private final float[] gravity = new float[3];
    private final Queue<Float> window = new LinkedList<>();
    private static final int WINDOW_SIZE = 30;
    private static final float ALPHA = 0.8f;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup parent,
                             @Nullable Bundle savedInstanceState) {

        LinearLayout root = new LinearLayout(requireContext());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#0F0F1A"));
        root.setPadding(32, 32, 32, 32);

        TextView title = new TextView(requireContext());
        title.setText("🏃  Reconnaissance d'activité");
        title.setTextSize(20f);
        title.setTextColor(Color.parseColor("#E2E8F0"));
        title.setTypeface(null, Typeface.BOLD);
        title.setPadding(0, 0, 0, 32);
        root.addView(title);

        // Card activité principale
        LinearLayout actCard = new LinearLayout(requireContext());
        actCard.setOrientation(LinearLayout.VERTICAL);
        actCard.setBackgroundColor(Color.parseColor("#1A1A2E"));
        actCard.setPadding(32, 36, 32, 36);
        actCard.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams actParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        actParams.bottomMargin = 24;
        actCard.setLayoutParams(actParams);

        activityIcon = new TextView(requireContext());
        activityIcon.setText("⏳");
        activityIcon.setTextSize(56f);
        activityIcon.setGravity(Gravity.CENTER);
        activityIcon.setPadding(0, 0, 0, 16);

        TextView actLabel = new TextView(requireContext());
        actLabel.setText("ACTIVITÉ DÉTECTÉE");
        actLabel.setTextSize(10f);
        actLabel.setTextColor(Color.parseColor("#6B7280"));
        actLabel.setLetterSpacing(0.15f);
        actLabel.setGravity(Gravity.CENTER);
        actLabel.setPadding(0, 0, 0, 8);

        activityView = new TextView(requireContext());
        activityView.setText("Calibration...");
        activityView.setTextSize(28f);
        activityView.setTextColor(Color.parseColor("#A78BFA"));
        activityView.setTypeface(null, Typeface.BOLD);
        activityView.setGravity(Gravity.CENTER);

        actCard.addView(activityIcon);
        actCard.addView(actLabel);
        actCard.addView(activityView);
        root.addView(actCard);

        // Card mouvement
        LinearLayout movCard = new LinearLayout(requireContext());
        movCard.setOrientation(LinearLayout.VERTICAL);
        movCard.setBackgroundColor(Color.parseColor("#16213E"));
        movCard.setPadding(32, 20, 32, 20);
        movCard.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams movParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        movParams.bottomMargin = 24;
        movCard.setLayoutParams(movParams);

        TextView movLabel = new TextView(requireContext());
        movLabel.setText("INTENSITÉ MOUVEMENT");
        movLabel.setTextSize(10f);
        movLabel.setTextColor(Color.parseColor("#6B7280"));
        movLabel.setLetterSpacing(0.15f);
        movLabel.setGravity(Gravity.CENTER);

        movementView = new TextView(requireContext());
        movementView.setText("--");
        movementView.setTextSize(32f);
        movementView.setTextColor(Color.parseColor("#10B981"));
        movementView.setTypeface(null, Typeface.BOLD);
        movementView.setGravity(Gravity.CENTER);

        movCard.addView(movLabel);
        movCard.addView(movementView);
        root.addView(movCard);

        // Axes row
        LinearLayout axesRow = new LinearLayout(requireContext());
        axesRow.setOrientation(LinearLayout.HORIZONTAL);

        xView = makeAxisBadge("X", "#EF4444", axesRow);
        yView = makeAxisBadge("Y", "#10B981", axesRow);
        zView = makeAxisBadge("Z", "#0EA5E9", axesRow);
        root.addView(axesRow);

        sensorManager = (SensorManager)
                requireActivity().getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        return root;
    }

    private TextView makeAxisBadge(String axis, String color, LinearLayout parent) {
        LinearLayout card = new LinearLayout(requireContext());
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundColor(Color.parseColor("#1A1A2E"));
        card.setPadding(16, 16, 16, 16);
        card.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        p.rightMargin = 12;
        card.setLayoutParams(p);

        TextView label = new TextView(requireContext());
        label.setText(axis);
        label.setTextSize(11f);
        label.setTextColor(Color.parseColor(color));
        label.setTypeface(null, Typeface.BOLD);
        label.setGravity(Gravity.CENTER);
        label.setLetterSpacing(0.1f);

        TextView val = new TextView(requireContext());
        val.setText("--");
        val.setTextSize(16f);
        val.setTextColor(Color.parseColor("#E2E8F0"));
        val.setGravity(Gravity.CENTER);
        val.setPadding(0, 6, 0, 0);

        card.addView(label);
        card.addView(val);
        parent.addView(card);
        return val;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (accelerometer != null)
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        else
            activityView.setText("Accéléromètre indisponible");
    }

    @Override
    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0], y = event.values[1], z = event.values[2];

        gravity[0] = ALPHA * gravity[0] + (1 - ALPHA) * x;
        gravity[1] = ALPHA * gravity[1] + (1 - ALPHA) * y;
        gravity[2] = ALPHA * gravity[2] + (1 - ALPHA) * z;

        float lx = x - gravity[0], ly = y - gravity[1], lz = z - gravity[2];
        float movement = (float) Math.sqrt(lx * lx + ly * ly + lz * lz);

        if (window.size() >= WINDOW_SIZE) window.poll();
        window.add(movement);

        xView.setText(String.format("%.2f", x));
        yView.setText(String.format("%.2f", y));
        zView.setText(String.format("%.2f", z));
        movementView.setText(String.format("%.3f", movement));

        String activity = classify();
        activityView.setText(activity);
        activityIcon.setText(getActivityIcon(activity));
        activityView.setTextColor(getActivityColor(activity));
    }

    private String classify() {
        if (window.size() < WINDOW_SIZE) return "Calibration...";

        float mean = 0f, max = 0f;
        for (float v : window) { mean += v; max = Math.max(max, v); }
        mean /= window.size();

        float variance = 0f;
        for (float v : window) variance += (v - mean) * (v - mean);
        variance /= window.size();
        float sd = (float) Math.sqrt(variance);

        if (max > 10f)      return "Saut";
        if (sd > 1.2f)      return "Marche";
        if (Math.abs(gravity[2]) > 8f) return "Téléphone à plat";
        if (Math.abs(gravity[1]) > 7f || Math.abs(gravity[0]) > 7f) return "Assis / Debout";
        return "Position stable";
    }

    private String getActivityIcon(String a) {
        if (a.contains("Saut"))    return "🦘";
        if (a.contains("Marche")) return "🚶";
        if (a.contains("plat"))   return "📱";
        if (a.contains("Assis"))  return "🪑";
        if (a.contains("stable")) return "🧘";
        return "⏳";
    }

    private int getActivityColor(String a) {
        if (a.contains("Saut"))    return Color.parseColor("#EF4444");
        if (a.contains("Marche")) return Color.parseColor("#10B981");
        if (a.contains("stable")) return Color.parseColor("#A78BFA");
        return Color.parseColor("#F59E0B");
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}
