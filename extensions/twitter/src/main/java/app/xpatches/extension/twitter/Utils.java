/*
 * Part of X Patches - https://github.com/Internetservice/x-patches
 * Licensed under the GNU General Public License v3.0.
 */

package app.xpatches.extension.twitter;

import android.content.Context;

/**
 * Holds the application context. {@link #setContext(Context)} is called from the
 * hooked X main activity, see the shared extension patch.
 */
@SuppressWarnings("unused")
public final class Utils {
    public static final String LOG_TAG = "XPatches";

    private static volatile Context context;

    private Utils() {
    }

    /**
     * Injection point.
     */
    public static void setContext(Context appContext) {
        if (appContext == null) return;

        Context applicationContext = appContext.getApplicationContext();
        context = applicationContext != null ? applicationContext : appContext;
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
