package tech.hawkon.pinshift;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import tech.hawkon.pinshift.utils.GoUtils;

public class WelcomeActivity extends AppCompatActivity {
    private static final String BAIDU_SERVICE_TERMS_URL =
            "https://lbsyun.baidu.com/index.php?title=open/law";
    private static final String BAIDU_PRIVACY_POLICY_URL =
            "https://lbsyun.baidu.com/index.php?title=openprivacy";
    public static final String KEY_ACCEPT_AGREEMENT = "KEY_ACCEPT_AGREEMENT";
    public static final String KEY_ACCEPT_PRIVACY = "KEY_ACCEPT_PRIVACY";
    public static final String CONSENT_PREFERENCES = KEY_ACCEPT_AGREEMENT;

    private static final int SDK_PERMISSION_REQUEST = 127;

    private SharedPreferences consentPreferences;
    private Button mapKeyButton;
    private Button startButton;
    private CheckBox consentCheckBox;
    private TextView mapKeyStatus;
    private TextView consentText;
    private boolean isLaunching;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        PreferenceManager.setDefaultValues(this, R.xml.preferences_main, false);
        consentPreferences = getSharedPreferences(CONSENT_PREFERENCES, MODE_PRIVATE);

        mapKeyStatus = findViewById(R.id.mapKeyStatus);
        mapKeyButton = findViewById(R.id.mapKeyButton);
        startButton = findViewById(R.id.startButton);
        consentCheckBox = findViewById(R.id.consentCheckBox);
        consentText = findViewById(R.id.consentText);

        consentCheckBox.setChecked(isConsentAccepted());
        consentCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveConsent(isChecked);
            updateStartButtonState();
        });

        configureConsentLinks();
        mapKeyButton.setOnClickListener(view -> openMapKeyPage());
        startButton.setOnClickListener(view -> startMainActivity());

        updateMapKeyState();
        updateStartButtonState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateMapKeyState();
        maybeEnterAutomatically();
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != SDK_PERMISSION_REQUEST) {
            return;
        }

        if (hasLocationPermission()) {
            enterMainActivity(true);
        } else {
            GoUtils.DisplayToast(this, getString(R.string.app_error_permission));
        }
    }

    private void configureConsentLinks() {
        String text = getString(R.string.welcome_consent_text);
        SpannableString content = new SpannableString(text);
        applyPolicyLink(
                content,
                text,
                getString(R.string.app_agreement),
                R.string.app_agreement,
                R.string.app_agreement_content);
        applyPolicyLink(
                content,
                text,
                getString(R.string.app_privacy),
                R.string.app_privacy,
                R.string.app_privacy_content);
        consentText.setText(content);
        consentText.setMovementMethod(LinkMovementMethod.getInstance());
        consentText.setHighlightColor(android.graphics.Color.TRANSPARENT);
    }

    private void applyPolicyLink(
            SpannableString content,
            String fullText,
            String linkText,
            int titleRes,
            int contentRes) {
        int start = fullText.indexOf(linkText);
        if (start < 0) {
            return;
        }
        content.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                showPolicyDialog(titleRes, contentRes);
            }

            @Override
            public void updateDrawState(@NonNull TextPaint drawState) {
                super.updateDrawState(drawState);
                drawState.setColor(getResources().getColor(R.color.colorPrimary, getTheme()));
                drawState.setUnderlineText(false);
                drawState.setFakeBoldText(true);
            }
        }, start, start + linkText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private void showPolicyDialog(int titleRes, int contentRes) {
        boolean privacyPolicy = titleRes == R.string.app_privacy;
        new AlertDialog.Builder(this)
                .setTitle(titleRes)
                .setMessage(contentRes)
                .setNeutralButton(
                        privacyPolicy
                                ? R.string.baidu_privacy_policy
                                : R.string.baidu_service_terms,
                        (dialog, which) -> openExternalPolicy(
                                privacyPolicy
                                        ? BAIDU_PRIVACY_POLICY_URL
                                        : BAIDU_SERVICE_TERMS_URL))
                .setPositiveButton(R.string.welcome_policy_close, null)
                .show();
    }

    private void openExternalPolicy(String url) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (ActivityNotFoundException exception) {
            GoUtils.DisplayToast(this, getString(R.string.nav_open_link_failed));
        }
    }

    private boolean isConsentAccepted() {
        return consentPreferences.getBoolean(KEY_ACCEPT_AGREEMENT, false)
                && consentPreferences.getBoolean(KEY_ACCEPT_PRIVACY, false);
    }

    private void saveConsent(boolean accepted) {
        consentPreferences.edit()
                .putBoolean(KEY_ACCEPT_AGREEMENT, accepted)
                .putBoolean(KEY_ACCEPT_PRIVACY, accepted)
                .apply();
    }

    private boolean hasMapKey() {
        String mapKey = PreferenceManager.getDefaultSharedPreferences(this)
                .getString(PinShiftApplication.PREF_MAP_KEY, "");
        return !TextUtils.isEmpty(mapKey == null ? "" : mapKey.trim());
    }

    private boolean hasLocationPermission() {
        return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void updateMapKeyState() {
        if (mapKeyButton == null || mapKeyStatus == null) {
            return;
        }
        boolean configured = hasMapKey();
        mapKeyStatus.setText(configured
                ? R.string.welcome_map_key_status_set
                : R.string.welcome_map_key_status_missing);
        mapKeyButton.setText(configured
                ? R.string.welcome_map_key_button_set
                : R.string.welcome_map_key_button_missing);
    }

    private void updateStartButtonState() {
        if (startButton == null || consentCheckBox == null) {
            return;
        }
        boolean enabled = consentCheckBox.isChecked();
        startButton.setEnabled(enabled);
        startButton.setAlpha(enabled ? 1f : 0.48f);
    }

    private void maybeEnterAutomatically() {
        if (isLaunching
                || !isConsentAccepted()
                || !hasMapKey()
                || !hasLocationPermission()) {
            return;
        }
        enterMainActivity(false);
    }

    private void startMainActivity() {
        if (!consentCheckBox.isChecked()) {
            GoUtils.DisplayToast(this, getString(R.string.app_error_agreement));
            return;
        }

        if (!hasMapKey()) {
            GoUtils.DisplayToast(this, getString(R.string.app_error_map_key_missing));
            openMapKeyPage();
            return;
        }

        if (!hasLocationPermission()) {
            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, SDK_PERMISSION_REQUEST);
            return;
        }

        enterMainActivity(true);
    }

    private void enterMainActivity(boolean showErrors) {
        if (isLaunching) {
            return;
        }

        if (!GoUtils.isNetworkAvailable(this)) {
            if (showErrors) {
                GoUtils.DisplayToast(this, getString(R.string.app_error_network));
            }
            return;
        }

        if (!GoUtils.isGpsOpened(this)) {
            if (showErrors) {
                GoUtils.DisplayToast(this, getString(R.string.app_error_gps));
            }
            return;
        }

        PinShiftApplication application = (PinShiftApplication) getApplication();
        if (!application.initializeMapSdkIfAllowed()) {
            if (showErrors) {
                GoUtils.DisplayToast(this, getString(R.string.app_error_map_init));
            }
            return;
        }

        isLaunching = true;
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void openMapKeyPage() {
        startActivity(new Intent(this, MapKeyActivity.class));
    }
}
