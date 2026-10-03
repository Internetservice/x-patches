/*
 * Part of X Patches - https://github.com/Internetservice/x-patches
 * Licensed under the GNU General Public License v3.0.
 */

package app.xpatches.extension.twitter.settings;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

/**
 * Shows the settings as a full screen dialog over the X activity. A dialog stays in the
 * window of X, so "back" simply dismisses it and X is exactly where it was left.
 */
public final class SettingsDialog {
    private SettingsDialog() {
    }

    public static void show(Activity activity) {
        boolean night = SettingsView.isNight(activity);
        Context context = SettingsView.themedContext(activity);
        Dialog dialog = new Dialog(context, SettingsView.theme(night));
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        SettingsView settings = new SettingsView(context, dialog::dismiss);
        dialog.setContentView(settings.getView(), new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
            styleSystemBars(window, night);
        }
        dialog.show();
    }

    /**
     * Draws the content behind the system bars, with icons readable on the day or night background.
     */
    static void styleSystemBars(Window window, boolean night) {
        window.setStatusBarColor(android.graphics.Color.TRANSPARENT);
        window.setNavigationBarColor(android.graphics.Color.TRANSPARENT);
        View decor = window.getDecorView();
        int flags = decor.getSystemUiVisibility()
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        if (!night) {
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        } else {
            flags &= ~(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        }
        decor.setSystemUiVisibility(flags);
    }
}
