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
import kotlin.Unit;
import kotlin.jvm.functions.Function0;

/**
 * Click handler of the "X Patches" entry injected into the navigation drawer.
 * Instantiated from patched code.
 */
@SuppressWarnings("unused")
public final class OpenSettingsAction implements Function0<Unit> {
    @Override
    public Unit invoke() {
        try {
            // Start from the foreground activity so the settings join the task of X
            // and "back" returns to the drawer instead of leaving the app.
            Activity activity = Utils.getCurrentActivity();
            if (activity != null) {
                activity.startActivity(new Intent(activity, SettingsActivity.class));
                return Unit.INSTANCE;
            }
            Context context = Utils.getContext();
            if (context != null) {
                Intent intent = new Intent(context, SettingsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        } catch (Exception e) {
            Log.e(Utils.LOG_TAG, "Failed to open the settings", e);
        }
        return Unit.INSTANCE;
    }
}
