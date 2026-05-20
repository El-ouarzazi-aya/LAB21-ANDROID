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

import ensa.ma.sensors.views.LineChartView;

public class MotionSensorFragment extends Fragment implements SensorEventListener {

    private static final String ARG_SENSOR_TYPE = "sensor_type";
    private static final String ARG_TITLE       = "title";

    private SensorManager sensorManager;
    private Sensor sensor;

    private TextView xView, yView, zView, normView;
    private LineChartView chartView;

    private int sensorType;
    private String title;

    public static MotionSensorFragment newInstance(int sensorType, String title) {
        MotionSensorFragment f = new MotionSensorFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SENSOR_TYPE, sensorType);
        args.putString(ARG_TITLE, title);
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

        sensorManager = (SensorManager)
                requireActivity().getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(sensorType);

        LinearLayout root = new LinearLayout(requireContext());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#0F0F1A"));
        root.setPadding(32, 32, 32, 32);

        // Titre
        TextView titleView = new TextView(requireContext());
        titleView.setText(getIcon() + "  " + title);
        titleView.setTextSize(20f);
        titleView.setTextColor(Color.parseColor("#E2E8F0"));
        titleView.setTypeface(null, Typeface.BOLD);
        titleView.setPadding(0, 0, 0, 24);
        root.addView(titleView);

        // Axes row
        LinearLayout axesRow = new LinearLayout(requireContext());
        axesRow.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rowParams.bottomMargin = 24;
        axesRow.setLayoutParams(rowParams);

        // Création et ajout direct dans axesRow — une seule fois chacun
        xView = addAxisCard(axesRow, "X", "#EF4444", true);
        yView = addAxisCard(axesRow, "Y", "#10B981", true);
        zView = addAxisCard(axesRow, "Z", "#0EA5E9", false);

        root.addView(axesRow);

        // Norme card
        LinearLayout normCard = new LinearLayout(requireContext());
        normCard.setOrientation(LinearLayout.VERTICAL);
        normCard.setBackgroundColor(Color.parseColor("#16213E"));
        normCard.setPadding(32, 20, 32, 20);
        normCard.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams normParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        normParams.bottomMargin = 24;
        normCard.setLayoutParams(normParams);

        TextView normLabel = new TextView(requireContext());
        normLabel.setText("NORME \u2016v\u2016");
        normLabel.setTextSize(10f);
        normLabel.setTextColor(Color.parseColor("#6B7280"));
        normLabel.setLetterSpacing(0.15f);
        normLabel.setGravity(Gravity.CENTER);

        normView = new TextView(requireContext());
        normView.setText("--");
        normView.setTextSize(32f);
        normView.setTextColor(Color.parseColor("#A78BFA"));
        normView.setTypeface(null, Typeface.BOLD);
        normView.setGravity(Gravity.CENTER);

        normCard.addView(normLabel);
        normCard.addView(normView);
        root.addView(normCard);

        // Graphe
        LinearLayout chartCard = new LinearLayout(requireContext());
        chartCard.setOrientation(LinearLayout.VERTICAL);
        chartCard.setBackgroundColor(Color.parseColor("#1A1A2E"));
        chartCard.setPadding(16, 16, 16, 16);

        TextView chartLabel = new TextView(requireContext());
        chartLabel.setText("NORME \u2014 \u00c9VOLUTION");
        chartLabel.setTextSize(10f);
        chartLabel.setTextColor(Color.parseColor("#6B7280"));
        chartLabel.setLetterSpacing(0.15f);
        chartLabel.setPadding(8, 0, 0, 12);

        chartView = new LineChartView(requireContext());
        chartView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 420));

        chartCard.addView(chartLabel);
        chartCard.addView(chartView);
        root.addView(chartCard);

        return root;
    }

    /**
     * Crée une card axe, l'ajoute directement dans parent, retourne le TextView valeur.
     * Corrige le crash "The specified child already has a parent".
     */
    private TextView addAxisCard(LinearLayout parent, String axis, String color, boolean withMargin) {
        LinearLayout card = new LinearLayout(requireContext());
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundColor(Color.parseColor("#1A1A2E"));
        card.setPadding(16, 20, 16, 20);
        card.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        if (withMargin) p.rightMargin = 12;
        card.setLayoutParams(p);

        TextView axisLabel = new TextView(requireContext());
        axisLabel.setText(axis);
        axisLabel.setTextSize(11f);
        axisLabel.setTextColor(Color.parseColor(color));
        axisLabel.setTypeface(null, Typeface.BOLD);
        axisLabel.setGravity(Gravity.CENTER);
        axisLabel.setLetterSpacing(0.1f);

        TextView valView = new TextView(requireContext());
        valView.setText("--");
        valView.setTextSize(18f);
        valView.setTextColor(Color.parseColor("#E2E8F0"));
        valView.setTypeface(null, Typeface.BOLD);
        valView.setGravity(Gravity.CENTER);
        valView.setPadding(0, 8, 0, 0);

        card.addView(axisLabel);
        card.addView(valView);
        parent.addView(card);  // ajout unique ici

        return valView;
    }

    private String getIcon() {
        if (sensorType == Sensor.TYPE_ACCELEROMETER) return "Acc";
        if (sensorType == Sensor.TYPE_GRAVITY)       return "Grav";
        if (sensorType == Sensor.TYPE_GYROSCOPE)     return "Gyro";
        return "---";
    }

    @Override
    public void onResume() {
        super.onResume();
        if (sensor != null) {
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            normView.setText("Capteur indisponible");
            normView.setTextSize(14f);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        float norm = (float) Math.sqrt(x * x + y * y + z * z);

        xView.setText(String.format("%.2f", x));
        yView.setText(String.format("%.2f", y));
        zView.setText(String.format("%.2f", z));
        normView.setText(String.format("%.3f", norm));

        chartView.addValue(norm);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}