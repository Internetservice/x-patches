/*
 * Part of X Patches - https://github.com/Internetservice/x-patches
 * Licensed under the GNU General Public License v3.0.
 */

package app.xpatches.extension.twitter;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import java.lang.ref.WeakReference;

import app.xpatches.extension.twitter.patches.links.HandleCustomLinksPatch;
import app.xpatches.extension.twitter.patches.video.VideoSpeedPatch;

/**
 * Holds the application context. {@link #setContext(Context)} is called from the
 * hooked X main activity, see the shared extension patch.
 */
@SuppressWarnings("unused")
public final class Utils {
    public static final String LOG_TAG = "XPatches";

    private static volatile Context context;
    private static volatile WeakReference<Activity> currentActivity = new WeakReference<>(null);
    private static volatile boolean lifecycleRegistered;

    private Utils() {
    }

    /**
     * Injection point.
     */
    public static void setContext(Context appContext) {
        if (appContext == null) return;

        Context applicationContext = appContext.getApplicationContext();
        context = applicationContext != null ? applicationContext : appContext;

        if (appContext instanceof Activity) {
            Activity activity = (Activity) appContext;
            currentActivity = new WeakReference<>(activity);
            if (HandleCustomLinksPatch.isPatchIncluded()) {
                HandleCustomLinksPatch.rewriteIntent(activity.getIntent());
            }
            if (VideoSpeedPatch.isPatchIncluded()) {
                TouchTracker.install(activity);
            }
        }
        registerActivityTracking();
        CrashLog.install(context);
    }

    /**
     * @return The activity in the foreground, or null if none is known.
     */
    public static Activity getCurrentActivity() {
        Activity activity = currentActivity.get();
        if (activity == null) {
            activity = resumedActivity();
            if (activity != null) {
                currentActivity = new WeakReference<>(activity);
            }
        }
        return activity;
    }

    /**
     * @return The resumed activity of the process, read from the activity thread
     * in case the lifecycle tracking missed it.
     */
    private static Activity resumedActivity() {
        try {
            Class<?> activityThread = Class.forName("android.app.ActivityThread");
            Object thread = activityThread.getMethod("currentActivityThread").invoke(null);
            java.lang.reflect.Field activitiesField = activityThread.getDeclaredField("mActivities");
            activitiesField.setAccessible(true);
            java.util.Map<?, ?> activities = (java.util.Map<?, ?>) activitiesField.get(thread);
            if (activities == null) return null;

            Activity fallback = null;
            for (Object record : activities.values()) {
                java.lang.reflect.Field pausedField = record.getClass().getDeclaredField("paused");
                pausedField.setAccessible(true);
                java.lang.reflect.Field activityField = record.getClass().getDeclaredField("activity");
                activityField.setAccessible(true);
                Activity activity = (Activity) activityField.get(record);
                if (activity == null || activity.isFinishing()) continue;
                if (!pausedField.getBoolean(record)) return activity;
                fallback = activity;
            }
            return fallback;
        } catch (Exception e) {
            Log.w(LOG_TAG, "Could not read the resumed activity", e);
            return null;
        }
    }

    private static synchronized void registerActivityTracking() {
        if (lifecycleRegistered || !(context instanceof Application)) return;
        lifecycleRegistered = true;
        ((Application) context).registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityResumed(Activity activity) {
                currentActivity = new WeakReference<>(activity);
                if (VideoSpeedPatch.isPatchIncluded()) {
                    TouchTracker.install(activity);
                }
            }

            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            }

            @Override
            public void onActivityStarted(Activity activity) {
            }

            @Override
            public void onActivityPaused(Activity activity) {
            }

            @Override
            public void onActivityStopped(Activity activity) {
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                if (currentActivity.get() == activity) {
                    currentActivity = new WeakReference<>(null);
                }
            }
        });
    }

    /**
     * @return The version of the patches. Modified during patching.
     */
    public static String getPatchesVersion() {
        return "unknown";
    }

    public static Context getContext() {
        Context current = context;
        if (current == null) {
            // Patched code can run before the main activity is created,
            // for example static initializers of the Compose palettes.
            current = currentApplication();
            if (current != null) {
                context = current;
            }
        }
        return current;
    }

    private static Context currentApplication() {
        try {
            Object application = Class.forName("android.app.ActivityThread")
                    .getMethod("currentApplication")
                    .invoke(null);
            return application instanceof Context ? (Context) application : null;
        } catch (Exception ignored) {
            return null;
        }
    }
}
