package tech.hawkon.pinshift;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.provider.ProviderProperties;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.core.app.ActivityCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LocationDiagnosticActivity extends BaseActivity implements LocationListener {
    private static final int REQUEST_LOCATION_PERMISSION = 1001;
    private static final String DIAGNOSTIC_PROVIDER = "pinshift_diagnostic";

    private LocationManager locationManager;
    private TextView summaryView;
    private TextView gpsView;
    private TextView networkView;
    private TextView logView;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary, getTheme()));
        setContentView(R.layout.activity_location_diagnostic);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        summaryView = findViewById(R.id.diagnostic_summary);
        gpsView = findViewById(R.id.diagnostic_gps);
        networkView = findViewById(R.id.diagnostic_network);
        logView = findViewById(R.id.diagnostic_log);

        Button refreshButton = findViewById(R.id.diagnostic_refresh);
        refreshButton.setOnClickListener(v -> refreshDiagnostic());

        Button settingsButton = findViewById(R.id.diagnostic_open_mock_settings);
        settingsButton.setOnClickListener(v -> openDeveloperSettings());

        Button clearButton = findViewById(R.id.diagnostic_clear_log);
        clearButton.setOnClickListener(v -> logView.setText(R.string.diagnostic_waiting));

        refreshDiagnostic();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshDiagnostic();
        startLocationUpdates();
    }

    @Override
    protected void onPause() {
        stopLocationUpdates();
        super.onPause();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void refreshDiagnostic() {
        boolean fineGranted = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        boolean coarseGranted = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        boolean gpsEnabled = isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean networkEnabled = isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        boolean mockAllowed = canInjectMockLocation();

        String summary = getString(
                R.string.diagnostic_summary_format,
                Build.VERSION.RELEASE,
                Build.VERSION.SDK_INT,
                getPackageName(),
                fineGranted ? getString(R.string.diagnostic_yes) : getString(R.string.diagnostic_no),
                coarseGranted ? getString(R.string.diagnostic_yes) : getString(R.string.diagnostic_no),
                gpsEnabled ? getString(R.string.diagnostic_enabled) : getString(R.string.diagnostic_disabled),
                networkEnabled ? getString(R.string.diagnostic_enabled) : getString(R.string.diagnostic_disabled),
                mockAllowed ? getString(R.string.diagnostic_allowed) : getString(R.string.diagnostic_not_allowed));
        summaryView.setText(summary);

        if (!fineGranted && !coarseGranted) {
            gpsView.setText(R.string.diagnostic_permission_missing);
            networkView.setText(R.string.diagnostic_permission_missing);
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
            return;
        }

        showLastKnownLocation(LocationManager.GPS_PROVIDER, gpsView);
        showLastKnownLocation(LocationManager.NETWORK_PROVIDER, networkView);
    }

    private boolean isProviderEnabled(String provider) {
        try {
            return locationManager != null && locationManager.isProviderEnabled(provider);
        } catch (Exception ignored) {
            return false;
        }
    }

    @SuppressLint("WrongConstant")
    private boolean canInjectMockLocation() {
        if (locationManager == null) {
            return false;
        }

        boolean added = false;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                locationManager.addTestProvider(
                        DIAGNOSTIC_PROVIDER,
                        false,
                        false,
                        false,
                        false,
                        true,
                        true,
                        true,
                        ProviderProperties.POWER_USAGE_LOW,
                        ProviderProperties.ACCURACY_FINE);
            } else {
                locationManager.addTestProvider(
                        DIAGNOSTIC_PROVIDER,
                        false,
                        false,
                        false,
                        false,
                        true,
                        true,
                        true,
                        Criteria.POWER_LOW,
                        Criteria.ACCURACY_FINE);
            }
            added = true;
            return true;
        } catch (SecurityException | IllegalArgumentException exception) {
            appendLog(getString(R.string.diagnostic_mock_check_failed, exception.getClass().getSimpleName()));
            return false;
        } finally {
            if (added) {
                try {
                    locationManager.removeTestProvider(DIAGNOSTIC_PROVIDER);
                } catch (Exception ignored) {
                    // Diagnostic provider is temporary and may already have been removed.
                }
            }
        }
    }

    private void startLocationUpdates() {
        if (locationManager == null) {
            return;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500L, 0f, this);
            appendLog(getString(R.string.diagnostic_listening, LocationManager.GPS_PROVIDER));
        } catch (Exception exception) {
            appendLog(getString(
                    R.string.diagnostic_listen_failed,
                    LocationManager.GPS_PROVIDER,
                    exception.getClass().getSimpleName()));
        }

        try {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 500L, 0f, this);
            appendLog(getString(R.string.diagnostic_listening, LocationManager.NETWORK_PROVIDER));
        } catch (Exception exception) {
            appendLog(getString(
                    R.string.diagnostic_listen_failed,
                    LocationManager.NETWORK_PROVIDER,
                    exception.getClass().getSimpleName()));
        }
    }

    private void stopLocationUpdates() {
        if (locationManager == null) {
            return;
        }
        try {
            locationManager.removeUpdates(this);
        } catch (SecurityException ignored) {
            // Permission may have been revoked while the page was open.
        }
    }

    private void showLastKnownLocation(String provider, TextView target) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            target.setText(R.string.diagnostic_permission_missing);
            return;
        }

        try {
            Location location = locationManager.getLastKnownLocation(provider);
            target.setText(location == null
                    ? getString(R.string.diagnostic_no_location, provider)
                    : formatLocation(location));
        } catch (Exception exception) {
            target.setText(getString(
                    R.string.diagnostic_read_failed,
                    provider,
                    exception.getClass().getSimpleName()));
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        String formatted = formatLocation(location);
        if (LocationManager.GPS_PROVIDER.equals(location.getProvider())) {
            gpsView.setText(formatted);
        } else if (LocationManager.NETWORK_PROVIDER.equals(location.getProvider())) {
            networkView.setText(formatted);
        }
        appendLog(getString(
                R.string.diagnostic_update_received,
                location.getProvider(),
                location.getLatitude(),
                location.getLongitude(),
                isMock(location) ? getString(R.string.diagnostic_yes) : getString(R.string.diagnostic_no)));
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        appendLog(getString(R.string.diagnostic_provider_enabled, provider));
        refreshDiagnostic();
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        appendLog(getString(R.string.diagnostic_provider_disabled, provider));
        refreshDiagnostic();
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onStatusChanged(String provider, int status, Bundle extras) {
        appendLog(getString(R.string.diagnostic_provider_status, provider, status));
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            refreshDiagnostic();
            startLocationUpdates();
        }
    }

    private String formatLocation(Location location) {
        return getString(
                R.string.diagnostic_location_format,
                location.getProvider(),
                location.getLatitude(),
                location.getLongitude(),
                location.getAccuracy(),
                location.hasAltitude() ? location.getAltitude() : 0.0,
                location.hasSpeed() ? location.getSpeed() : 0.0f,
                location.hasBearing() ? location.getBearing() : 0.0f,
                timeFormat.format(new Date(location.getTime())),
                isMock(location) ? getString(R.string.diagnostic_yes) : getString(R.string.diagnostic_no));
    }

    @SuppressWarnings("deprecation")
    private boolean isMock(Location location) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return location.isMock();
        }
        return location.isFromMockProvider();
    }

    private void appendLog(String message) {
        if (logView == null) {
            return;
        }
        String current = logView.getText().toString();
        if (getString(R.string.diagnostic_waiting).equals(current)) {
            current = "";
        }
        String line = timeFormat.format(new Date()) + "  " + message;
        String updated = current.isEmpty() ? line : line + "\n" + current;
        if (updated.length() > 12000) {
            updated = updated.substring(0, 12000);
        }
        logView.setText(updated);
    }

    private void openDeveloperSettings() {
        try {
            startActivity(new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS));
        } catch (Exception exception) {
            appendLog(getString(R.string.diagnostic_settings_failed, exception.getClass().getSimpleName()));
        }
    }
}
