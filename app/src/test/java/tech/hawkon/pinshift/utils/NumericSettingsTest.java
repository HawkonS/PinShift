package tech.hawkon.pinshift.utils;

import android.content.SharedPreferences;

import org.junit.Test;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class NumericSettingsTest {
    @Test
    public void missingSettingsUseValidDefaultsWithoutWriting() {
        PreferenceStore store = new PreferenceStore();
        for (NumericSettings.Setting setting : NumericSettings.Setting.values()) {
            assertTrue(NumericSettings.isValid(setting.defaultText, setting));
            assertEquals(setting.defaultValue, NumericSettings.getDouble(store.preferences, setting), 0);
        }
        assertTrue(store.values.isEmpty());
        assertEquals(0, store.applyCount);
    }

    @Test
    public void acceptsBothRangeBoundariesForEverySetting() {
        for (NumericSettings.Setting setting : NumericSettings.Setting.values()) {
            assertTrue(setting.key, NumericSettings.isValid(Double.toString(setting.minimum), setting));
            assertTrue(setting.key, NumericSettings.isValid(Double.toString(setting.maximum), setting));
        }
    }

    @Test
    public void rejectsValuesImmediatelyOutsideEveryRange() {
        for (NumericSettings.Setting setting : NumericSettings.Setting.values()) {
            assertFalse(setting.key, NumericSettings.isValid(
                    Double.toString(Math.nextDown(setting.minimum)), setting));
            assertFalse(setting.key, NumericSettings.isValid(
                    Double.toString(Math.nextUp(setting.maximum)), setting));
        }
    }

    @Test
    public void rejectsMalformedNonFiniteOverflowAndNonStringInputs() {
        Object[] invalid = {null, "", " ", "abc", "1,5", "1.2.3", "--1", "NaN",
                "Infinity", "+Infinity", "-Infinity", "1e309", "-1e309",
                "0x1.0p1", "1f", "1d", "1 2", true, 12, 12L, 12.0f};
        for (NumericSettings.Setting setting : NumericSettings.Setting.values()) {
            for (Object input : invalid) {
                assertFalse(setting.key + ": " + input, NumericSettings.isValid(input, setting));
            }
            assertFalse(NumericSettings.isValid("0." + new String(new char[65]).replace((char) 0, '0'), setting));
        }
    }

    @Test
    public void acceptsWhitespaceDecimalsAndScientificNotation() {
        assertTrue(NumericSettings.isValid("  -125.5  ", NumericSettings.Setting.ALTITUDE));
        assertTrue(NumericSettings.isValid(".5", NumericSettings.Setting.WALK_SPEED));
        assertTrue(NumericSettings.isValid("+1.25e2", NumericSettings.Setting.LATITUDE_OFFSET));
        assertTrue(NumericSettings.isValid("1.5", NumericSettings.Setting.HISTORY_EXPIRATION));
    }

    @Test
    public void readsValidSavedValue() {
        PreferenceStore store = new PreferenceStore();
        store.values.put(NumericSettings.Setting.ALTITUDE.key, " -125.5 " );
        assertEquals(-125.5, NumericSettings.getDouble(
                store.preferences, NumericSettings.Setting.ALTITUDE), 0);
    }

    @Test
    public void invalidSavedTextFallsBackWithoutOverwritingIt() {
        PreferenceStore store = new PreferenceStore();
        for (NumericSettings.Setting setting : NumericSettings.Setting.values()) {
            for (String value : new String[]{"bad", "NaN", "Infinity", "1e309",
                    Double.toString(setting.minimum - 1), Double.toString(setting.maximum + 1)}) {
                store.values.put(setting.key, value);
                assertEquals(setting.defaultValue, NumericSettings.getDouble(store.preferences, setting), 0);
                assertEquals(value, store.values.get(setting.key));
            }
        }
        assertEquals(0, store.applyCount);
    }

    @Test
    public void nullAndWrongStorageTypesFallBack() {
        PreferenceStore store = new PreferenceStore();
        for (NumericSettings.Setting setting : NumericSettings.Setting.values()) {
            for (Object value : new Object[]{null, 55, 55L, 55.0f, true}) {
                store.values.put(setting.key, value);
                assertEquals(setting.defaultValue, NumericSettings.getDouble(store.preferences, setting), 0);
            }
        }
    }

    @Test
    public void repairChangesOnlyInvalidNumericSettingsInOneWrite() {
        PreferenceStore store = new PreferenceStore();
        store.values.put(NumericSettings.Setting.ALTITUDE.key, "NaN");
        store.values.put(NumericSettings.Setting.LATITUDE_OFFSET.key, 3L);
        store.values.put(NumericSettings.Setting.HISTORY_EXPIRATION.key, "-7");
        store.values.put(NumericSettings.Setting.LONGITUDE_OFFSET.key, "10.5");
        store.values.put("unrelated_setting", true);
        assertTrue(NumericSettings.repairInvalidValues(store.preferences));
        assertEquals("55.0", store.values.get(NumericSettings.Setting.ALTITUDE.key));
        assertEquals("10.0", store.values.get(NumericSettings.Setting.LATITUDE_OFFSET.key));
        assertEquals("7", store.values.get(NumericSettings.Setting.HISTORY_EXPIRATION.key));
        assertEquals("10.5", store.values.get(NumericSettings.Setting.LONGITUDE_OFFSET.key));
        assertEquals(true, store.values.get("unrelated_setting"));
        assertEquals(5, store.values.size());
        assertEquals(1, store.applyCount);
    }

    @Test
    public void repairDoesNotWriteValidOrMissingSettings() {
        PreferenceStore store = new PreferenceStore();
        assertFalse(NumericSettings.repairInvalidValues(store.preferences));
        for (NumericSettings.Setting setting : NumericSettings.Setting.values()) {
            store.values.put(setting.key, setting.defaultText);
        }
        assertFalse(NumericSettings.repairInvalidValues(store.preferences));
        assertEquals(0, store.applyCount);
    }

    /** Exercises the preference boundary without needing an Android runtime in local tests. */
    private static final class PreferenceStore {
        final Map<String, Object> values = new HashMap<>();
        final SharedPreferences preferences;
        int applyCount;

        PreferenceStore() {
            Map<String, Object> pending = new HashMap<>();
            SharedPreferences.Editor editor = (SharedPreferences.Editor) Proxy.newProxyInstance(
                    SharedPreferences.Editor.class.getClassLoader(),
                    new Class<?>[]{SharedPreferences.Editor.class}, (proxy, method, args) -> {
                        switch (method.getName()) {
                            case "putString":
                                pending.put((String) args[0], args[1]);
                                return proxy;
                            case "apply":
                                values.putAll(pending);
                                pending.clear();
                                applyCount++;
                                return null;
                            default:
                                throw new UnsupportedOperationException(method.getName());
                        }
                    });
            preferences = (SharedPreferences) Proxy.newProxyInstance(
                    SharedPreferences.class.getClassLoader(), new Class<?>[]{SharedPreferences.class},
                    (proxy, method, args) -> {
                        switch (method.getName()) {
                            case "getString":
                                Object value = values.containsKey(args[0]) ? values.get(args[0]) : args[1];
                                return (String) value;
                            case "getAll":
                                return new HashMap<>(values);
                            case "edit":
                                return editor;
                            default:
                                throw new UnsupportedOperationException(method.getName());
                        }
                    });
        }
    }
}
