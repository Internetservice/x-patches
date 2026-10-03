/*
 * Part of X Patches - https://github.com/Internetservice/x-patches
 * Licensed under the GNU General Public License v3.0.
 */

package app.xpatches.extension.twitter.patches.home;

import android.content.SharedPreferences;

import app.xpatches.extension.twitter.settings.Settings;

/**
 * The duration topics stay snoozed in "For you".
 */
@SuppressWarnings("unused")
public final class SnoozeTopicsPatch {
    /**
     * The feature switch X takes the snooze duration from, in minutes.
     */
    public static final String SNOOZE_PERIOD_SWITCH = "co_timeline_reset_period_minutes";

    public static final String[] LABELS = {
            "X default (1 day)", "3 days", "1 week", "2 weeks", "1 month", "3 months", "1 year", "Forever",
    };
    private static final int DAY = 24 * 60;
    public static final int[] MINUTES = {
            0, 3 * DAY, 7 * DAY, 14 * DAY, 30 * DAY, 90 * DAY, 365 * DAY, 100 * 365 * DAY,
    };

    private SnoozeTopicsPatch() {
    }

    /**
     * @return If this patch was included during patching. Modified during patching.
     */
    public static boolean isPatchIncluded() {
        return false;
    }

    /**
     * @return The chosen snooze duration in minutes, or 0 for the duration of X.
     */
    public static int snoozeMinutes() {
        if (!isPatchIncluded()) return 0;
        SharedPreferences preferences = Settings.preferences();
        return preferences == null ? 0 : preferences.getInt(Settings.KEY_SNOOZE_MINUTES, 0);
    }

    public static void setSnoozeMinutes(int minutes) {
        SharedPreferences preferences = Settings.preferences();
        if (preferences != null) preferences.edit().putInt(Settings.KEY_SNOOZE_MINUTES, minutes).apply();
    }

    public static int selectedIndex() {
        int minutes = snoozeMinutes();
        for (int i = 0; i < MINUTES.length; i++) {
            if (MINUTES[i] == minutes) return i;
        }
        return 0;
    }

    public static String summary() {
        return LABELS[selectedIndex()];
    }
}
