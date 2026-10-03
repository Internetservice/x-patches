/*
 * Part of X Patches - https://github.com/Internetservice/x-patches
 * Licensed under the GNU General Public License v3.0.
 */

package app.xpatches.extension.twitter;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;

import java.lang.ref.WeakReference;

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
            currentActivity = new WeakReference<>((Activity) appContext);
        }
        registerActivityTracking();
    }

    /**
     * @return The activity in the foreground, or null if none is known.
     */
    public static Activity getCurrentActivity() {
        return currentActivity.get();
    }

    private static synchronized void registerActivityTracking() {
        if (lifecycleRegistered || !(context instanceof Application)) return;
        lifecycleRegistered = true;
        ((Application) context).registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityResumed(Activity activity) {
                currentActivity = new WeakReference<>(activity);
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
