package ensa.ma.sensors.fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

public class StepCounterFragment extends Fragment implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor stepSensor;

    private TextView totalStepsView;
    private TextView sessionStepsView;
    private TextView statusView;

    private float initialSteps = -1;

    private final ActivityResultLauncher<String> permLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    granted -> {
                        if (granted) startSensor();
                        else statusView.setText("⛔  Permission refusée");
                    });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup parent,
                             @Nullable Bundle savedInstanceState) {

        LinearLayout root = new LinearLayout(requireContext());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#0F0F1A"));
        root.setPadding(32, 48, 32, 32);
        root.setGravity(Gravity.CENTER_HORIZONTAL);

        // Icône grande
        TextView icon = new TextView(requireContext());
        icon.setText("👟");
        icon.setTextSize(64f);
        icon.setGravity(Gravity.CENTER);
        icon.setPadding(0, 0, 0, 24);
        root.addView(icon);

        // Titre
        TextView title = new TextView(requireContext());
        title.setText("Compteur de pas");
        title.setTextSize(22f);
        title.setTextColor(Color.parseColor("#E2E8F0"));
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 8);
        root.addView(title);

        // Status
        statusView = new TextView(requireContext());
        statusView.setText("● Initialisation...");
        statusView.setTextSize(12f);
        statusView.setTextColor(Color.parseColor("#F59E0B"));
        statusView.setGravity(Gravity.CENTER);
        statusView.setPadding(0, 0, 0, 48);
        root.addView(statusView);

        // Card total depuis démarrage
        LinearLayout totalCard = makeMetricCard("DEPUIS LE REDÉMARRAGE", "#7C3AED");
        totalStepsView = (TextView) totalCard.getChildAt(1);
        LinearLayout.LayoutParams totalParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        totalParams.bottomMargin = 20;
        totalCard.setLayoutParams(totalParams);
        root.addView(totalCard);

        // Card session
        LinearLayout sessionCard = makeMetricCard("CETTE SESSION", "#10B981");
        sessionStepsView = (TextView) sessionCard.getChildAt(1);
        root.addView(sessionCard);

        sensorManager = (SensorManager)
                requireActivity().getSystemService(Context.SENSOR_SERVICE);
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        return root;
    }

    private LinearLayout makeMetricCard(String label, String accentColor) {
        LinearLayout card = new LinearLayout(requireContext());
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundColor(Color.parseColor("#1A1A2E"));
        card.setPadding(32, 28, 32, 28);
        card.setGravity(Gravity.CENTER);

        TextView labelView = new TextView(requireContext());
        labelView.setText(label);
        labelView.setTextSize(11f);
        labelView.setTextColor(Color.parseColor(accentColor));
        labelView.setLetterSpacing(0.15f);
        labelView.setTypeface(null, Typeface.BOLD);
        labelView.setGravity(Gravity.CENTER);

        TextView valueView = new TextView(requireContext());
        valueView.setText("0");
        valueView.setTextSize(56f);
        valueView.setTextColor(Color.parseColor("#F1F5F9"));
        valueView.setTypeface(null, Typeface.BOLD);
        valueView.setGravity(Gravity.CENTER);

        TextView unitView = new TextView(requireContext());
        unitView.setText("pas");
        unitView.setTextSize(14f);
        unitView.setTextColor(Color.parseColor("#6B7280"));
        unitView.setGravity(Gravity.CENTER);

        card.addView(labelView);
        card.addView(valueView);
        card.addView(unitView);
        return card;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (stepSensor == null) {
            statusView.setText("⛔  Capteur indisponible");
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                && ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            permLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION);
        } else {
            startSensor();
        }
    }

    private void startSensor() {
        statusView.setText("● Capteur actif");
        statusView.setTextColor(Color.parseColor("#10B981"));
        sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float total = event.values[0];
        if (initialSteps < 0) initialSteps = total;
        int session = (int) (total - initialSteps);

        totalStepsView.setText(String.valueOf((int) total));
        sessionStepsView.setText(String.valueOf(session));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}
