package tech.hawkon.pinshift;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;

import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import tech.hawkon.pinshift.utils.GoUtils;
import tech.hawkon.pinshift.utils.NumericSettings;

import java.util.Objects;

public class FragmentSettings extends PreferenceFragmentCompat {

    private void showRestartDialog(int titleRes, int messageRes) {
        if (!isAdded()) {
            return;
        }
        new AlertDialog.Builder(requireContext())
                .setTitle(titleRes)
                .setMessage(messageRes)
                .setNegativeButton(R.string.setting_map_key_restart_later, null)
                .setPositiveButton(R.string.setting_map_key_restart_now,
                        (dialog, which) -> RestartActivity.restartApplication(requireContext()))
                .show();
    }

    // Validate numeric settings before persistence.
    private void setupDecimalEditTextPreference(EditTextPreference preference,
                                                NumericSettings.Setting setting) {
        if (preference != null) {
            preference.setSummaryProvider((Preference.SummaryProvider<EditTextPreference>) EditTextPreference::getText);
            preference.setOnBindEditTextListener(editText -> {
                int inputType = InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER;
                if (setting.minimum < 0) {
                    inputType |= InputType.TYPE_NUMBER_FLAG_SIGNED;
                }
                editText.setInputType(inputType);
                editText.setSelection(editText.length());
            });
            preference.setOnPreferenceChangeListener((pref, newValue) -> {
                if (!NumericSettings.isValid(newValue, setting)) {
                    GoUtils.DisplayToast(requireContext(), getString(R.string.setting_number_invalid,
                            String.valueOf(setting.minimum), String.valueOf(setting.maximum)));
                    return false;
                }
                return true;
            });
        }
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Repair legacy numeric values before EditTextPreference reads persisted strings.
        boolean repaired = NumericSettings.repairInvalidValues(
                PreferenceManager.getDefaultSharedPreferences(requireContext()));
        addPreferencesFromResource(R.xml.preferences_main);
        if (repaired) {
            GoUtils.DisplayToast(requireContext(), getString(R.string.setting_numbers_repaired));
        }

        ListPreference pfJoystick = findPreference("setting_joystick_type");
        if (pfJoystick != null) {
            // 使用自定义 SummaryProvider
            pfJoystick.setSummaryProvider((Preference.SummaryProvider<ListPreference>) preference -> Objects.requireNonNull(preference.getEntry()));
            pfJoystick.setOnPreferenceChangeListener((preference, newValue) -> !newValue.toString().trim().isEmpty());
        }

        EditTextPreference pfWalk = findPreference("setting_walk");
        setupDecimalEditTextPreference(pfWalk, NumericSettings.Setting.WALK_SPEED);

        EditTextPreference pfRun = findPreference("setting_run");
        setupDecimalEditTextPreference(pfRun, NumericSettings.Setting.RUN_SPEED);

        EditTextPreference pfBike = findPreference("setting_bike");
        setupDecimalEditTextPreference(pfBike, NumericSettings.Setting.BIKE_SPEED);

        EditTextPreference pfAltitude = findPreference("setting_altitude");
        setupDecimalEditTextPreference(pfAltitude, NumericSettings.Setting.ALTITUDE);

        EditTextPreference pfLatOffset = findPreference("setting_lat_max_offset");
        setupDecimalEditTextPreference(pfLatOffset, NumericSettings.Setting.LATITUDE_OFFSET);

        EditTextPreference pfLonOffset = findPreference("setting_lon_max_offset");
        setupDecimalEditTextPreference(pfLonOffset, NumericSettings.Setting.LONGITUDE_OFFSET);

        SwitchPreferenceCompat pLog = findPreference("setting_log_off");
        if (pLog != null) {
            pLog.setOnPreferenceChangeListener((preference, newValue) -> {
                boolean changed = ((SwitchPreferenceCompat) preference).isChecked()
                        != (Boolean) newValue;
                if (changed) {
                    new Handler(Looper.getMainLooper()).postDelayed(
                            () -> showRestartDialog(
                                    R.string.setting_log_saved,
                                    R.string.setting_log_restart_message),
                            200);
                }
                return changed;
            });
        }

        EditTextPreference pfPosHisValid = findPreference("setting_history_expiration");
        setupDecimalEditTextPreference(pfPosHisValid, NumericSettings.Setting.HISTORY_EXPIRATION);

    }
}
