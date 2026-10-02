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

    public static Context getContext() {
        return context;
    }
}
