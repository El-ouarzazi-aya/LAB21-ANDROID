package ensa.ma.sensors.fragments;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import ensa.ma.sensors.utils.SensorFormatter;

import java.util.List;

public class SensorsListFragment extends Fragment {

    private SensorManager sensorManager;
    private LinearLayout container;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup parent,
                             @Nullable Bundle savedInstanceState) {

        // Fond sombre global
        ScrollView scrollView = new ScrollView(requireContext());
        scrollView.setBackgroundColor(Color.parseColor("#0F0F1A"));

        container = new LinearLayout(requireContext());
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(32, 32, 32, 32);
        scrollView.addView(container);

        // Titre en haut
        TextView title = new TextView(requireContext());
        title.setText("📡  Capteurs disponibles");
        title.setTextSize(22f);
        title.setTextColor(Color.parseColor("#E2E8F0"));
        title.setTypeface(null, Typeface.BOLD);
        title.setPadding(0, 0, 0, 32);
        container.addView(title);

        sensorManager = (SensorManager)
                requireActivity().getSystemService(Context.SENSOR_SERVICE);

        displaySensors();
        return scrollView;
    }

    private void displaySensors() {
        List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ALL);

        // Badge nombre total
        TextView countBadge = new TextView(requireContext());
        countBadge.setText(sensors.size() + " capteurs trouvés");
        countBadge.setTextSize(13f);
        countBadge.setTextColor(Color.parseColor("#7C3AED"));
        countBadge.setBackgroundColor(Color.parseColor("#1E1030"));
        countBadge.setPadding(24, 12, 24, 12);
        countBadge.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams badgeParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        badgeParams.bottomMargin = 32;
        countBadge.setLayoutParams(badgeParams);
        container.addView(countBadge);

        int[] accentColors = {
                Color.parseColor("#7C3AED"),
                Color.parseColor("#0EA5E9"),
                Color.parseColor("#10B981"),
                Color.parseColor("#F59E0B"),
                Color.parseColor("#EF4444"),
                Color.parseColor("#EC4899")
        };

        for (int i = 0; i < sensors.size(); i++) {
            Sensor sensor = sensors.get(i);
            int accent = accentColors[i % accentColors.length];

            // Card container
            LinearLayout card = new LinearLayout(requireContext());
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackgroundColor(Color.parseColor("#1A1A2E"));
            card.setPadding(28, 24, 28, 24);

            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            cardParams.bottomMargin = 20;
            card.setLayoutParams(cardParams);

            // Barre accent gauche
            View accentBar = new View(requireContext());
            accentBar.setBackgroundColor(accent);
            LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(8, ViewGroup.LayoutParams.MATCH_PARENT);
            barParams.rightMargin = 20;

            // Header avec nom
            LinearLayout header = new LinearLayout(requireContext());
            header.setOrientation(LinearLayout.HORIZONTAL);

            View dot = new View(requireContext());
            dot.setBackgroundColor(accent);
            LinearLayout.LayoutParams dotParams = new LinearLayout.LayoutParams(12, 12);
            dotParams.rightMargin = 16;
            dotParams.topMargin = 8;
            dot.setLayoutParams(dotParams);

            TextView nameView = new TextView(requireContext());
            nameView.setText(sensor.getName());
            nameView.setTextSize(15f);
            nameView.setTextColor(Color.parseColor("#F1F5F9"));
            nameView.setTypeface(null, Typeface.BOLD);

            header.addView(dot);
            header.addView(nameView);

            // Séparateur
            View sep = new View(requireContext());
            sep.setBackgroundColor(Color.parseColor("#2D2D4E"));
            LinearLayout.LayoutParams sepParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, 1);
            sepParams.topMargin = 16;
            sepParams.bottomMargin = 16;
            sep.setLayoutParams(sepParams);

            // Contenu formaté
            TextView infoView = new TextView(requireContext());
            infoView.setText(SensorFormatter.format(sensor));
            infoView.setTextSize(12f);
            infoView.setTextColor(Color.parseColor("#94A3B8"));
            infoView.setLineSpacing(6f, 1f);
            infoView.setTypeface(Typeface.MONOSPACE);

            card.addView(header);
            card.addView(sep);
            card.addView(infoView);
            container.addView(card);
        }
    }
}
