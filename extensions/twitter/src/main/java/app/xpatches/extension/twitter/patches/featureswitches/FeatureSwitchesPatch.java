/*
 * Part of X Patches - https://github.com/Internetservice/x-patches
 * Licensed under the GNU General Public License v3.0.
 */

package app.xpatches.extension.twitter.patches.featureswitches;

import app.xpatches.extension.twitter.patches.home.SnoozeTopicsPatch;
import app.xpatches.extension.twitter.settings.Settings;

/**
 * Overrides the boolean feature switches the X client reads. The client gates most of its
 * UI behind these server provided switches, which makes them the cleanest way to turn
 * features off in the Compose based app.
 */
@SuppressWarnings("unused")
public final class FeatureSwitchesPatch {

    private FeatureSwitchesPatch() {
    }

    /**
     * Injection point.
     *
     * @param key The feature switch key.
     * @return The forced value of the integer switch, or null to use the value the app resolved.
     */
    public static Integer getIntOverride(String key) {
        if (key == null) return null;

        if (key.equals(SnoozeTopicsPatch.SNOOZE_PERIOD_SWITCH)) {
            int minutes = SnoozeTopicsPatch.snoozeMinutes();
            if (minutes > 0) return minutes;
        }

        return null;
    }

    /**
     * Injection point.
     *
     * @param key The feature switch key.
     * @return The forced value of the switch, or null to use the value the app resolved.
     */
    public static Boolean getBooleanOverride(String key) {
        if (key == null) return null;

        boolean hideGrok = Settings.HIDE_GROK.get();
        boolean hideSpacesAndLive = Settings.HIDE_SPACES_AND_LIVE.get();
        boolean removePremiumUpsell = Settings.REMOVE_PREMIUM_UPSELL.get();
        boolean disableAnalytics = Settings.DISABLE_ANALYTICS.get();
        boolean hideAds = Settings.HIDE_ADS.get();
        boolean keepTimelinePosition = Settings.KEEP_TIMELINE_POSITION.get();
        boolean hideExtraHomeTabs = Settings.HIDE_EXTRA_HOME_TABS.get();

        if (key.equals("xlite_video_playback_speed_hold") && Settings.HOLD_TO_CHANGE_SPEED.get()) {
            return true;
        }

        // Translations are Grok powered and rolled out per account. Checked before Hide Grok,
        // which would turn them off along with the rest of Grok.
        if (key.startsWith("grok_translations_")) {
            if (key.contains("auto_translation")) {
                if (Settings.AUTO_TRANSLATE.get()) return true;
            } else if (Settings.FORCE_TRANSLATE.get()) {
                return true;
            }
        }

        if (hideGrok && (key.startsWith("grok_") || key.contains("_grok_"))) {
            return false;
        }

        if (hideSpacesAndLive && (key.startsWith("x_lite_spaces_")
                || key.startsWith("x_lite_live")
                || key.startsWith("android_audio_spaces_"))) {
            return false;
        }

        if (removePremiumUpsell && key.startsWith("subscriptions_upsells_") && key.endsWith("_enabled")) {
            return false;
        }

        if (disableAnalytics && key.contains("scribe") && key.endsWith("_enabled")) {
            return false;
        }

        // Google and other third party ad networks served through the SSP.
        if (hideAds && key.startsWith("ssp_ads_") && key.endsWith("_enabled")) {
            return false;
        }

        // Jumping back to the top of "For you" and refreshing when the app is reopened.
        if (keepTimelinePosition && key.startsWith("android_home_back_")) {
            return false;
        }

        // The extra home tabs next to "For you" and "Following".
        if (hideExtraHomeTabs && (key.equals("android_timeline_subscribed_tab_enabled")
                || key.equals("ranked_following_home_timeline_tab_enabled")
                || key.equals("android_x_lite_nfl_hub_home_tab_enabled")
                || key.startsWith("hometimeline_pinned_tabs_"))) {
            return false;
        }

        return null;
    }
}
