package tech.hawkon.pinshift;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import tech.hawkon.pinshift.utils.GoUtils;

public class MapKeyActivity extends AppCompatActivity {
    private EditText keyInput;
    private TextView keyStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // AppTheme 为欢迎页使用了透明状态栏；普通功能页需要主动恢复主题蓝色。
        getWindow().setStatusBarColor(
                getResources().getColor(R.color.colorPrimary, getTheme()));

        setContentView(R.layout.activity_map_key);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        TextView applicationInfo = findViewById(R.id.map_key_application_info);
        applicationInfo.setText(BaiduKeyInfo.getApplicationInfo(this));

        findViewById(R.id.map_key_copy_info).setOnClickListener(view -> {
            if (BaiduKeyInfo.copyApplicationInfo(this)) {
                GoUtils.DisplayToast(this, getString(R.string.setting_map_application_info_copied));
            }
        });

        keyInput = findViewById(R.id.map_key_input);
        keyStatus = findViewById(R.id.map_key_status);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String currentKey = preferences.getString(PinShiftApplication.PREF_MAP_KEY, "");
        keyInput.setText(currentKey == null ? "" : currentKey);
        keyInput.setSelection(keyInput.length());
        updateKeyStatus();

        CheckBox showKey = findViewById(R.id.map_key_show_value);
        showKey.setOnCheckedChangeListener((button, checked) -> {
            int selection = keyInput.getSelectionStart();
            keyInput.setInputType(InputType.TYPE_CLASS_TEXT
                    | (checked
                    ? InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    : InputType.TYPE_TEXT_VARIATION_PASSWORD));
            keyInput.setSelection(Math.max(0, Math.min(selection, keyInput.length())));
        });

        Button saveButton = findViewById(R.id.map_key_save);
        saveButton.setOnClickListener(view -> saveKey(false));
        Button saveAndRestartButton = findViewById(R.id.map_key_save_restart);
        saveAndRestartButton.setOnClickListener(view -> saveKey(true));
    }

    @SuppressLint("ApplySharedPref")
    private void saveKey(boolean restart) {
        String key = keyInput.getText().toString().trim();
        if (TextUtils.isEmpty(key)) {
            keyInput.setError(getString(R.string.app_error_input_null));
            keyInput.requestFocus();
            return;
        }

        boolean saved = PreferenceManager.getDefaultSharedPreferences(this)
                .edit()
                .putString(PinShiftApplication.PREF_MAP_KEY, key)
                .commit();
        if (!saved) {
            keyInput.setError(getString(R.string.setting_map_key_save_failed));
            return;
        }

        updateKeyStatus();
        if (restart) {
            RestartActivity.restartApplication(this);
        } else {
            GoUtils.DisplayToast(this, getString(R.string.setting_map_key_saved_restart_later));
        }
    }

    private void updateKeyStatus() {
        String key = PreferenceManager.getDefaultSharedPreferences(this)
                .getString(PinShiftApplication.PREF_MAP_KEY, "");
        keyStatus.setText(TextUtils.isEmpty(key)
                ? R.string.map_key_status_missing
                : R.string.map_key_status_configured);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
