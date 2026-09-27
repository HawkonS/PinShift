package tech.hawkon.pinshift;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

public final class BaiduKeyInfo {
    private BaiduKeyInfo() {
    }

    public static String getApplicationInfo(Context context) {
        String signingSha1 = getSigningSha1(context);
        boolean debugBuild = (context.getApplicationInfo().flags
                & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        String developmentSha1 = debugBuild
                ? signingSha1
                : context.getString(R.string.setting_map_sha1_use_debug_build);

        return context.getString(
                R.string.setting_map_application_info_format,
                context.getPackageName(),
                developmentSha1,
                signingSha1);
    }

    public static String getApplicationInfoWithPurpose(Context context) {
        return getApplicationInfo(context)
                + System.lineSeparator()
                + System.lineSeparator()
                + context.getString(R.string.setting_map_application_info_usage);
    }

    public static boolean copyApplicationInfo(Context context) {
        ClipboardManager clipboard =
                (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard == null) {
            return false;
        }
        clipboard.setPrimaryClip(ClipData.newPlainText(
                context.getString(R.string.setting_map_application_info_title),
                getApplicationInfoWithPurpose(context)));
        return true;
    }

    @SuppressWarnings("deprecation")
    private static String getSigningSha1(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo;
            Signature[] signatures;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo = packageManager.getPackageInfo(
                        context.getPackageName(),
                        PackageManager.GET_SIGNING_CERTIFICATES);
                signatures = packageInfo.signingInfo == null
                        ? null
                        : packageInfo.signingInfo.getApkContentsSigners();
            } else {
                packageInfo = packageManager.getPackageInfo(
                        context.getPackageName(),
                        PackageManager.GET_SIGNATURES);
                signatures = packageInfo.signatures;
            }

            if (signatures == null || signatures.length == 0) {
                return context.getString(R.string.setting_map_sha1_unavailable);
            }

            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] fingerprint = digest.digest(signatures[0].toByteArray());
            StringBuilder result = new StringBuilder(fingerprint.length * 3 - 1);
            for (int index = 0; index < fingerprint.length; index++) {
                if (index > 0) {
                    result.append(':');
                }
                result.append(String.format(
                        Locale.US,
                        "%02X",
                        fingerprint[index] & 0xFF));
            }
            return result.toString();
        } catch (PackageManager.NameNotFoundException | NoSuchAlgorithmException exception) {
            return context.getString(R.string.setting_map_sha1_unavailable);
        }
    }
}
