package tech.hawkon.pinshift;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;

/** Uses a short-lived secondary process to reliably restart the main app process. */
public class RestartActivity extends Activity {
    private static final String EXTRA_OLD_PROCESS_ID = "old_process_id";

    public static void restartApplication(Context context) {
        Intent intent = new Intent(context, RestartActivity.class);
        intent.putExtra(EXTRA_OLD_PROCESS_ID, Process.myPid());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int oldProcessId = getIntent().getIntExtra(EXTRA_OLD_PROCESS_ID, -1);
        if (oldProcessId > 0 && oldProcessId != Process.myPid()) {
            Process.killProcess(oldProcessId);
        }

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent launchIntent = new Intent(this, WelcomeActivity.class);
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(launchIntent);
            finishAndRemoveTask();
        }, 350);
    }
}
