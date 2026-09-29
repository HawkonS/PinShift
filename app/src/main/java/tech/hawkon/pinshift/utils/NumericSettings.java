package tech.hawkon.pinshift.utils;

import android.content.SharedPreferences;

import java.util.Map;
import java.util.regex.Pattern;

/** Shared validation for numeric preferences, including values saved by older versions. */
public final class NumericSettings {
    private static final int MAX_INPUT_LENGTH = 64;
    private static final Pattern DECIMAL = Pattern.compile(
            "[+-]?(?:[0-9]+(?:[.][0-9]*)?|[.][0-9]+)(?:[eE][+-]?[0-9]+)?");

    public enum Setting {
        // Application limits: speed in m/s, altitude/offset in meters, retention in days.
        WALK_SPEED("setting_walk", "1.2", 0, 100),
        RUN_SPEED("setting_run", "3.6", 0, 100),
        BIKE_SPEED("setting_bike", "10.0", 0, 100),
        ALTITUDE("setting_altitude", "55.0", -1000, 20000),
        LATITUDE_OFFSET("setting_lat_max_offset", "10.0", 0, 100000),
        LONGITUDE_OFFSET("setting_lon_max_offset", "10.0", 0, 100000),
        HISTORY_EXPIRATION("setting_history_expiration", "7", 1, 3650);

        public final String key;
        public final String defaultText;
        public final double defaultValue;
        public final double minimum;
        public final double maximum;

        Setting(String key, String defaultText, double minimum, double maximum) {
            this.key = key;
            this.defaultText = defaultText;
            this.defaultValue = Double.parseDouble(defaultText);
            this.minimum = minimum;
            this.maximum = maximum;
        }
    }

    private NumericSettings() {
    }

    private static Double parse(Object value, Setting setting) {
        if (!(value instanceof String)) {
            return null;
        }
        String text = ((String) value).trim();
        if (text.isEmpty() || text.length() > MAX_INPUT_LENGTH
                || !DECIMAL.matcher(text).matches()) {
            return null;
        }
        try {
            double number = Double.parseDouble(text);
            return Double.isFinite(number)
                    && number >= setting.minimum && number <= setting.maximum
                    ? number : null;
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    public static boolean isValid(Object value, Setting setting) {
        return parse(value, setting) != null;
    }

    /** Never trusts the format or storage type of a persisted numeric setting. */
    public static double getDouble(SharedPreferences preferences, Setting setting) {
        try {
            Double value = parse(preferences.getString(setting.key, setting.defaultText), setting);
            return value == null ? setting.defaultValue : value;
        } catch (ClassCastException ignored) {
            return setting.defaultValue;
        }
    }

    /** Run before inflating EditTextPreference, which otherwise casts stored values to String. */
    public static boolean repairInvalidValues(SharedPreferences preferences) {
        Map<String, ?> values = preferences.getAll();
        SharedPreferences.Editor editor = null;
        for (Setting setting : Setting.values()) {
            if (values.containsKey(setting.key) && !isValid(values.get(setting.key), setting)) {
                if (editor == null) {
                    editor = preferences.edit();
                }
                editor.putString(setting.key, setting.defaultText);
            }
        }
        if (editor == null) {
            return false;
        }
        editor.apply();
        return true;
    }
}
