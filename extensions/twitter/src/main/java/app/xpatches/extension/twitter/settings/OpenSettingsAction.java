/*
 * Part of X Patches - https://github.com/Internetservice/x-patches
 * Licensed under the GNU General Public License v3.0.
 */

package app.xpatches.extension.twitter.settings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import app.xpatches.extension.twitter.Utils;
import kotlin.jvm.functions.Function0;

/**
 * Click handler of the "X Patches" entry injected into the navigation drawer.
 * Instantiated from patched code.
 * <p>
 * Declared as returning Object rather than kotlin.Unit: the shrunk Kotlin runtime of X has
 * no Unit.INSTANCE field, so touching it kills the app. The caller discards the result.
 */
@SuppressWarnings("unused")
public final class OpenSettingsAction implements Function0<Object> {
    @Override
    public Object invoke() {
        try {
            // Shown as a dialog over the X activity, so leaving the settings never leaves X.
            Activity activity = Utils.getCurrentActivity();
            if (activity != null && !activity.isFinishing()) {
                activity.runOnUiThread(() -> SettingsDialog.show(activity));
                return null;
            }

            Log.w(Utils.LOG_TAG, "No foreground activity, opening the settings activity");
            Context context = Utils.getContext();
            if (context != null) {
                Intent intent = new Intent(context, SettingsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        } catch (Throwable e) {
            Log.e(Utils.LOG_TAG, "Failed to open the settings", e);
        }
        return null;
    }
}
