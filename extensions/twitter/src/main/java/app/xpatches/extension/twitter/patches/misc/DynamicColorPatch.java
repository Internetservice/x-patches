/*
 * Ported from ReVanced Patches (GPLv3) - https://github.com/ReVanced/revanced-patches
 * Part of X Patches - https://github.com/Internetservice/x-patches
 */

package app.xpatches.extension.twitter.patches.misc;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import app.xpatches.extension.twitter.Utils;

@SuppressWarnings("unused")
public final class DynamicColorPatch {

    /**
     * Injection point.
     *
     * @param fallbackArgb The original X blue design token.
     * @return The Material You accent color, or the original color if unavailable.
     */
    public static long getDynamicColor(long fallbackArgb) {
        // Dynamic color is only supported on Android 12+
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return fallbackArgb;
        }

        try {
            Context context = Utils.getContext();
            if (context == null) {
                return fallbackArgb;
            }

            // Overridden by the resource patch to point at the system accent color.
            int resourceId = context.getResources().getIdentifier("twitter_blue", "color", context.getPackageName());
            if (resourceId == 0) {
                return fallbackArgb;
            }

            int colorInt = context.getColor(resourceId);
            return ((long) colorInt) & 0xFFFFFFFFL;
        } catch (Exception e) {
            Log.e(Utils.LOG_TAG, "Failed to resolve dynamic color", e);
            return fallbackArgb;
        }
    }
}
