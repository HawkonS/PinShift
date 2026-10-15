package tech.hawkon.pinshift.utils;

/** Validates coordinate text independently of the keyboard's character filter. */
public final class CoordinateParser {
    private CoordinateParser() {
    }

    public static Double parseLongitude(String text) {
        return parse(text, 180.0);
    }

    public static Double parseLatitude(String text) {
        return parse(text, 90.0);
    }

    private static Double parse(String text, double limit) {
        if (text == null) {
            return null;
        }
        try {
            double value = Double.parseDouble(text.trim());
            return Double.isFinite(value) && value >= -limit && value <= limit
                    ? value : null;
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
