package ensa.ma.sensors.fragments;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import ensa.ma.sensors.views.LineChartView;

public class SensorGraphFragment extends Fragment implements SensorEventListener {

    private static final String ARG_SENSOR_TYPE = "sensor_type";
    private static final String ARG_TITLE       = "title";
    private static final String ARG_MODE        = "mode";

    private SensorManager sensorManager;
    private Sensor sensor;

    private TextView valueView;
    private TextView statusView;
    private LineChartView chartView;

    private int sensorType;
    private String title;
    private String mode;

    private final Handler simulationHandler = new Handler(Looper.getMainLooper());
    private float simulationTime = 0f;

    public static SensorGraphFragment newInstance(int sensorType, String title, String mode) {
        SensorGraphFragment f = new SensorGraphFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SENSOR_TYPE, sensorType);
        args.putString(ARG_TITLE, title);
        args.putString(ARG_MODE, mode);
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup parent,
                             @Nullable Bundle savedInstanceState) {

        sensorType = requireArguments().getInt(ARG_SENSOR_TYPE);
        title      = requireArguments().getString(ARG_TITLE);
        mode       = requireArguments().getString(ARG_MODE);

        sensorManager = (SensorManager)
                requireActivity().getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(sensorType);

        // Root
        LinearLayout root = new LinearLayout(requireContext());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#0F0F1A"));
        root.setPadding(32, 32, 32, 32);

        // Header card
        LinearLayout headerCard = new LinearLayout(requireContext());
        headerCard.setOrientation(LinearLayout.VERTICAL);
        headerCard.setBackgroundColor(Color.parseColor("#1A1A2E"));
        headerCard.setPadding(32, 28, 32, 28);
        LinearLayout.LayoutParams headerParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        headerParams.bottomMargin = 24;
        headerCard.setLayoutParams(headerParams);

        TextView titleView = new TextView(requireContext());
        titleView.setText(getIcon(sensorType) + "  " + title);
        titleView.setTextSize(20f);
        titleView.setTextColor(Color.parseColor("#E2E8F0"));
        titleView.setTypeface(null, Typeface.BOLD);
        titleView.setPadding(0, 0, 0, 16);

        statusView = new TextView(requireContext());
        statusView.setText(sensor != null ? "● Capteur actif" : "● Simulation");
        statusView.setTextSize(12f);
        statusView.setTextColor(sensor != null
                ? Color.parseColor("#10B981")
                : Color.parseColor("#F59E0B"));

        headerCard.addView(titleView);
        headerCard.addView(statusView);

        // Value card
        LinearLayout valueCard = new LinearLayout(requireContext());
        valueCard.setOrientation(LinearLayout.VERTICAL);
        valueCard.setBackgroundColor(Color.parseColor("#16213E"));
        valueCard.setPadding(32, 24, 32, 24);
        valueCard.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams valueParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        valueParams.bottomMargin = 24;
        valueCard.setLayoutParams(valueParams);

        TextView valueLabel = new TextView(requireContext());
        valueLabel.setText("VALEUR ACTUELLE");
        valueLabel.setTextSize(10f);
        valueLabel.setTextColor(Color.parseColor("#6B7280"));
        valueLabel.setLetterSpacing(0.15f);
        valueLabel.setGravity(Gravity.CENTER);
        valueLabel.setPadding(0, 0, 0, 8);

        valueView = new TextView(requireContext());
        valueView.setText("--");
        valueView.setTextSize(36f);
        valueView.setTextColor(Color.parseColor("#A78BFA"));
        valueView.setTypeface(null, Typeface.BOLD);
        valueView.setGravity(Gravity.CENTER);

        valueCard.addView(valueLabel);
        valueCard.addView(valueView);

        // Chart card
        LinearLayout chartCard = new LinearLayout(requireContext());
        chartCard.setOrientation(LinearLayout.VERTICAL);
        chartCard.setBackgroundColor(Color.parseColor("#1A1A2E"));
        chartCard.setPadding(16, 16, 16, 16);
        LinearLayout.LayoutParams chartParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        chartCard.setLayoutParams(chartParams);

        TextView chartLabel = new TextView(requireContext());
        chartLabel.setText("ÉVOLUTION");
        chartLabel.setTextSize(10f);
        chartLabel.setTextColor(Color.parseColor("#6B7280"));
        chartLabel.setLetterSpacing(0.15f);
        chartLabel.setPadding(8, 0, 0, 12);

        chartView = new LineChartView(requireContext());
        chartView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 500));

        chartCard.addView(chartLabel);
        chartCard.addView(chartView);

        root.addView(headerCard);
        root.addView(valueCard);
        root.addView(chartCard);

        return root;
    }

    private String getIcon(int type) {
        if (type == Sensor.TYPE_AMBIENT_TEMPERATURE) return "🌡️";
        if (type == Sensor.TYPE_RELATIVE_HUMIDITY)   return "💧";
        if (type == Sensor.TYPE_PROXIMITY)            return "📡";
        if (type == Sensor.TYPE_MAGNETIC_FIELD)       return "🧲";
        return "📊";
    }

    @Override
    public void onResume() {
        super.onResume();
        if (sensor != null) {
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            startSimulation();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        simulationHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float value = extractValue(event.values);
        updateUi(value);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    private float extractValue(float[] values) {
        if ("MAGNITUDE".equals(mode)) {
            return (float) Math.sqrt(
                    values[0] * values[0] + values[1] * values[1] + values[2] * values[2]);
        }
        return values[0];
    }

    private void updateUi(float value) {
        String unit = getUnit(sensorType);
        valueView.setText(String.format("%.2f %s", value, unit));
        chartView.addValue(value);
    }

    private String getUnit(int type) {
        if (type == Sensor.TYPE_AMBIENT_TEMPERATURE) return "°C";
        if (type == Sensor.TYPE_RELATIVE_HUMIDITY)   return "%";
        if (type == Sensor.TYPE_PROXIMITY)            return "cm";
        if (type == Sensor.TYPE_MAGNETIC_FIELD)       return "µT";
        return "";
    }

    private void startSimulation() {
        simulationHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                simulationTime++;
                float value;
                if (sensorType == Sensor.TYPE_AMBIENT_TEMPERATURE)
                    value = 24f + (float) Math.sin(simulationTime / 5f) * 3f;
                else if (sensorType == Sensor.TYPE_RELATIVE_HUMIDITY)
                    value = 55f + (float) Math.sin(simulationTime / 7f) * 15f;
                else if (sensorType == Sensor.TYPE_PROXIMITY)
                    value = simulationTime % 6 < 3 ? 0f : 5f;
                else if (sensorType == Sensor.TYPE_MAGNETIC_FIELD)
                    value = 45f + (float) Math.sin(simulationTime / 4f) * 10f;
                else
                    value = (float) Math.sin(simulationTime);

                updateUi(value);
                simulationHandler.postDelayed(this, 1000);
            }
        }, 1000);
    }
}
