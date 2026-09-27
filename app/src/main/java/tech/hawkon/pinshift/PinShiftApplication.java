package tech.hawkon.pinshift;

import android.app.Application;
import android.content.pm.ApplicationInfo;

import androidx.preference.PreferenceManager;

import com.baidu.location.LocationClient;
import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;

import com.elvishew.xlog.LogConfiguration;
import com.elvishew.xlog.LogLevel;
import com.elvishew.xlog.XLog;
import com.elvishew.xlog.printer.ConsolePrinter;
import com.elvishew.xlog.printer.Printer;
import com.elvishew.xlog.printer.file.FilePrinter;
import com.elvishew.xlog.printer.file.backup.NeverBackupStrategy;
import com.elvishew.xlog.printer.file.clean.FileLastModifiedCleanStrategy;
import com.elvishew.xlog.printer.file.naming.ChangelessFileNameGenerator;

import java.io.File;

public class PinShiftApplication extends Application {
    public static final String APP_NAME = "PinShift";
    public static final String LOG_FILE_NAME = APP_NAME + ".log";
    public static final String PREF_MAP_KEY = "setting_map_key";
    public static final String PREF_LOG_DISABLED = "setting_log_off";
    private static boolean mapSdkReady = false;
    private static final long MAX_TIME = 1000 * 60 * 60 * 24 * 3; // 3 days

    @Override
    public void onCreate() {
        super.onCreate();

        initXlog();
        initializeMapSdkIfAllowed();
    }

    public synchronized boolean initializeMapSdkIfAllowed() {
        if (mapSdkReady) {
            return true;
        }

        boolean privacyAccepted = getSharedPreferences(
                WelcomeActivity.CONSENT_PREFERENCES,
                MODE_PRIVATE)
                .getBoolean(WelcomeActivity.KEY_ACCEPT_PRIVACY, false);
        if (!privacyAccepted) {
            XLog.i("Baidu map SDK initialization is waiting for privacy consent.");
            return false;
        }

        String mapKey = PreferenceManager.getDefaultSharedPreferences(this)
                .getString(PREF_MAP_KEY, "");
        mapKey = mapKey == null ? "" : mapKey.trim();
        if (mapKey.isEmpty()) {
            XLog.w("Baidu map SDK was not initialized because no map key is configured.");
            return false;
        }

        try {
            // 百度地图 7.5 开始，要求必须同意隐私政策，默认为false
            SDKInitializer.setAgreePrivacy(this, true);
            // 百度定位仍需单独设置隐私授权
            LocationClient.setAgreePrivacy(true);
            SDKInitializer.setApiKey(mapKey);
            SDKInitializer.initialize(this);
            SDKInitializer.setCoordType(CoordType.BD09LL);
            mapSdkReady = SDKInitializer.isInitialized();
        } catch (Throwable throwable) {
            mapSdkReady = false;
            XLog.e("Baidu map SDK initialization failed: " + throwable.getClass().getSimpleName()
                    + ": " + throwable.getMessage());
        }
        return mapSdkReady;
    }

    public static boolean isMapSdkReady() {
        return mapSdkReady;
    }

    /**
     * Initialize XLog.
     */
    private void initXlog() {
        boolean logDisabled = PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(PREF_LOG_DISABLED, false);
        boolean debugBuild = (getApplicationInfo().flags
                & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        int minimumLogLevel = logDisabled
                ? Integer.MAX_VALUE
                : (debugBuild ? LogLevel.ALL : LogLevel.WARN);
        File logPath = getExternalFilesDir("Logs");
        LogConfiguration config = new LogConfiguration.Builder()
                .logLevel(minimumLogLevel)
                .tag(APP_NAME)
                .enableThreadInfo()
                .enableStackTrace(debugBuild ? 2 : 0)
                .build();

        Printer consolePrinter = new ConsolePrinter();
        if (logDisabled || logPath == null) {
            XLog.init(config, consolePrinter);
            return;
        }

        Printer filePrinter = new FilePrinter.Builder(logPath.getPath())
                .fileNameGenerator(new ChangelessFileNameGenerator(LOG_FILE_NAME))
                .backupStrategy(new NeverBackupStrategy())
                .cleanStrategy(new FileLastModifiedCleanStrategy(MAX_TIME))
                .build();
        XLog.init(config, consolePrinter, filePrinter);
    }
}
