package ensa.ma.sensors.utils;

import android.hardware.Sensor;

public class SensorFormatter {

    public static String format(Sensor sensor) {
        return "  ID          " + sensor.getId() + "\n"
                + "  Nom         " + sensor.getName() + "\n"
                + "  Fabricant   " + sensor.getVendor() + "\n"
                + "  Version     " + sensor.getVersion() + "\n"
                + "  Type        " + sensor.getStringType() + "\n"
                + "  Int Type    " + sensor.getType() + "\n"
                + "  Résolution  " + sensor.getResolution() + "\n"
                + "  Énergie     " + sensor.getPower() + " mA\n"
                + "  Max Range   " + sensor.getMaximumRange() + "\n"
                + "  Min Délai   " + sensor.getMinDelay() + " µs\n";
    }
}
