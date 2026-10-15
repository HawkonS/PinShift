package tech.hawkon.pinshift;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.SystemClock;

import androidx.test.runner.lifecycle.ActivityLifecycleCallback;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.Stage;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.baidu.mapapi.model.LatLng;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;

import tech.hawkon.pinshift.service.ServiceGo;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

/**
 * Run on a device with a configured map AK, location/overlay permissions,
 * and PinShift selected as the mock-location app. Existing app data is retained.
 */
@RunWith(AndroidJUnit4.class)
public class MainActivityServiceBindingTest {
    private static final long TIMEOUT_MS = 10000;
    private Context context;
    private final AtomicReference<MainActivity> currentActivity = new AtomicReference<>();
    private final ActivityLifecycleCallback lifecycleCallback = (activity, stage) -> {
        if (activity instanceof MainActivity) {
            android.util.Log.i("ServiceBindingTest", "Lifecycle: " + stage);
        }
        if (activity instanceof MainActivity && stage == Stage.RESUMED) {
            currentActivity.set((MainActivity) activity);
        }
    };

    @Before
    public void launchActivity() {
        android.util.Log.i("ServiceBindingTest", "Setup entered");
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertTrue("Configure a map AK before running device tests",
                PinShiftApplication.isMapSdkReady());
        onMain(() -> {
            ActivityLifecycleMonitorRegistry.getInstance().addLifecycleCallback(lifecycleCallback);
            android.util.Log.i("ServiceBindingTest", "Requesting MainActivity");
            context.startActivity(new Intent(context, MainActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        });
        // Observe real lifecycle events with a deadline; do not wait indefinitely
        // in Instrumentation.startActivitySync for this singleTop Activity.
        android.util.Log.i("ServiceBindingTest", "Launch request returned");
        waitUntil("MainActivity did not resume", () -> currentActivity.get() != null);
        assertServiceStopped();
        android.util.Log.i("ServiceBindingTest", "Setup complete");
    }

    @After
    public void cleanUp() {
        android.util.Log.i("ServiceBindingTest", "Cleanup entered");
        MainActivity activity = currentActivity.get();
        if (activity != null) {
            onMain(() -> {
                invoke(activity, "stopGoLocation");
                // Also clean up an intentionally reproduced leak on unfixed builds.
                try {
                    activity.unbindService((ServiceConnection) get(activity, "mConnection"));
                } catch (IllegalArgumentException ignored) {
                    // A passing test has already released the registration.
                }
                activity.finish();
            });
            waitUntil("Activity did not finish during cleanup", () -> activityFlag("destroyed"));
        }
        onMain(() -> ActivityLifecycleMonitorRegistry.getInstance()
                .removeLifecycleCallback(lifecycleCallback));
        if (context != null) {
            context.stopService(new Intent(context, ServiceGo.class));
            assertServiceStopped();
        }
    }

    @Test
    public void stopBeforeConnectionReleasesPendingBinding() {
        for (int attempt = 0; attempt < 3; attempt++) {
            onActivity(activity -> {
                start(activity);
                // The main looper cannot deliver onServiceConnected until this returns.
                assertFalse((Boolean) get(activity, "isServiceBound"));
                invoke(activity, "stopGoLocation");
            });
            // Keep the Activity alive: framework Activity cleanup must not hide a leak.
            assertServiceStopped();
        }
    }

    @Test
    public void expiredConnectionCallbackReleasesItsBinding() {
        onActivity(activity -> {
            start(activity);
            // Reproduce cancellation while a framework connection callback is pending.
            // The real callback must release the still-registered connection itself.
            set(activity, "isServiceBindRequested", false);
            context.stopService(new Intent(context, ServiceGo.class));
        });
        assertServiceStopped();
        onActivity(activity -> {
            assertFalse((Boolean) get(activity, "isServiceBound"));
            assertFalse((Boolean) get(activity, "isMockServStart"));
        });
    }

    @Test
    public void disconnectThenStopReleasesRegisteredBinding() {
        onActivity(MainActivityServiceBindingTest::start);
        awaitReady();
        onActivity(activity -> {
            ServiceConnection connection = (ServiceConnection) get(activity, "mConnection");
            connection.onServiceDisconnected(new ComponentName(context, ServiceGo.class));
            invoke(activity, "stopGoLocation");
        });
        assertServiceStopped();
    }

    @Test
    public void startThenImmediatelyFinishStopsService() {
        onActivity(activity -> {
            start(activity);
            activity.finish();
        });
        waitUntil("Activity did not finish", () -> activityFlag("destroyed"));
        assertServiceStopped();
    }

    @Test
    public void startThenImmediatelyRecreateStopsService() {
        MainActivity original = currentActivity.get();
        onActivity(activity -> {
            start(activity);
            activity.recreate();
        });
        awaitReplacement(original);
        assertServiceStopped();
    }

    @Test
    public void startThenImmediatelyRotateStopsService() {
        MainActivity original = currentActivity.get();
        onActivity(activity -> {
            int orientation = activity.getResources().getConfiguration().orientation;
            start(activity);
            activity.setRequestedOrientation(orientation == Configuration.ORIENTATION_LANDSCAPE
                    ? ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    : ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        });
        awaitReplacement(original);
        assertServiceStopped();
    }

    @Test
    public void startThenImmediatelyBackCanStopAfterReturning() {
        onActivity(activity -> {
            start(activity);
            activity.onBackPressed();
        });
        // Back intentionally backgrounds this app, keeping the floating joystick active.
        awaitReady();
        context.startActivity(new Intent(context, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP));
        onActivity(activity -> invoke(activity, "stopGoLocation"));
        assertServiceStopped();
    }

    @Test
    public void connectedServiceCanStopAndRestart() {
        for (int attempt = 0; attempt < 3; attempt++) {
            onActivity(MainActivityServiceBindingTest::start);
            awaitReady();
            onActivity(activity -> invoke(activity, "stopGoLocation"));
            assertServiceStopped();
        }
    }

    private static void start(MainActivity activity) {
        set(activity, "mMarkLatLngMap", new LatLng(36.547743718042415, 117.07018449827267));
        invoke(activity, "startGoLocation");
        assertTrue("Binding was not requested", (Boolean) get(activity, "isServiceBindRequested"));
        assertTrue("Start was not pending", (Boolean) get(activity, "isMockServStarting"));
    }

    private void awaitReady() {
        waitUntil("Service did not become ready; check mock-location and overlay permissions",
                () -> activityFlag("isMockServStart"));
        assertTrue(isServiceRunning());
    }

    private void awaitReplacement(MainActivity original) {
        waitUntil("Activity was not recreated", () -> {
            return currentActivity.get() != original;
        });
        assertNotSame(original, currentActivity.get());
    }

    private void onActivity(java.util.function.Consumer<MainActivity> action) {
        onMain(() -> {
            MainActivity activity = currentActivity.get();
            if (activity == null) {
                throw new AssertionError("MainActivity is not available");
            }
            action.accept(activity);
        });
    }

    private boolean activityFlag(String name) {
        AtomicBoolean value = new AtomicBoolean();
        onMain(() -> value.set("destroyed".equals(name)
                ? currentActivity.get().isDestroyed()
                : (Boolean) get(currentActivity.get(), name)));
        return value.get();
    }

    private void assertServiceStopped() {
        waitUntil("ServiceGo remained running/bound after cancellation",
                () -> !isServiceRunning());
        waitUntil("Location worker survived service shutdown", () -> {
            ThreadGroup group = Thread.currentThread().getThreadGroup();
            while (group.getParent() != null) {
                group = group.getParent();
            }
            Thread[] threads = new Thread[group.activeCount() * 2 + 16];
            group.enumerate(threads, true);
            for (Thread thread : threads) {
                if (thread != null && "ServiceGoLocation".equals(thread.getName()) && thread.isAlive()) {
                    return false;
                }
            }
            return true;
        });
        NotificationManager notifications = context.getSystemService(NotificationManager.class);
        waitUntil("Foreground notification survived service shutdown", () -> {
            for (android.service.notification.StatusBarNotification notification
                    : notifications.getActiveNotifications()) {
                if (notification.getId() == 1) {
                    return false;
                }
            }
            return true;
        });
    }

    @SuppressWarnings("deprecation")
    private boolean isServiceRunning() {
        ActivityManager manager = context.getSystemService(ActivityManager.class);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (ServiceGo.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private static void waitUntil(String message, BooleanSupplier condition) {
        long deadline = SystemClock.uptimeMillis() + TIMEOUT_MS;
        while (SystemClock.uptimeMillis() < deadline) {
            if (condition.getAsBoolean()) {
                return;
            }
            SystemClock.sleep(50);
        }
        assertTrue(message, condition.getAsBoolean());
    }

    private static void onMain(Runnable action) {
        java.util.concurrent.CountDownLatch finished = new java.util.concurrent.CountDownLatch(1);
        AtomicReference<Throwable> failure = new AtomicReference<>();
        new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
            try {
                action.run();
            } catch (Throwable error) {
                failure.set(error);
            } finally {
                finished.countDown();
            }
        });
        try {
            assertTrue("Main-thread action timed out",
                    finished.await(TIMEOUT_MS, java.util.concurrent.TimeUnit.MILLISECONDS));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError(e);
        }
        if (failure.get() != null) {
            throw new AssertionError("Main-thread action failed", failure.get());
        }
    }

    private static Object get(MainActivity activity, String name) {
        try {
            Field field = MainActivity.class.getDeclaredField(name);
            field.setAccessible(true);
            return field.get(activity);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    private static void set(MainActivity activity, String name, Object value) {
        try {
            Field field = MainActivity.class.getDeclaredField(name);
            field.setAccessible(true);
            field.set(activity, value);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    private static void invoke(MainActivity activity, String name) {
        try {
            Method method = MainActivity.class.getDeclaredMethod(name);
            method.setAccessible(true);
            method.invoke(activity);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }
}
