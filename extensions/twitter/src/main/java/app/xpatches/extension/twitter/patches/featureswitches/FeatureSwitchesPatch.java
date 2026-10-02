/*
 * Part of X Patches - https://github.com/Internetservice/x-patches
 * Licensed under the GNU General Public License v3.0.
 */

package app.xpatches.extension.twitter.patches.featureswitches;

import app.xpatches.extension.twitter.patches.toggles.DisableAnalyticsPatch;
import app.xpatches.extension.twitter.patches.toggles.HideGrokPatch;
import app.xpatches.extension.twitter.patches.toggles.HideSpacesAndLivePatch;
import app.xpatches.extension.twitter.patches.toggles.RemovePremiumUpsellPatch;

/**
 * Overrides the boolean feature switches the X client reads. The client gates most of its
 * UI behind these server provided switches, which makes them the cleanest way to turn
 * features off in the Compose based app.
 */
@SuppressWarnings("unused")
public final class FeatureSwitchesPatch {
    private static final boolean HIDE_GROK = HideGrokPatch.isPatchIncluded();
    private static final boolean HIDE_SPACES_AND_LIVE = HideSpacesAndLivePatch.isPatchIncluded();
    private static final boolean REMOVE_PREMIUM_UPSELL = RemovePremiumUpsellPatch.isPatchIncluded();
    private static final boolean DISABLE_ANALYTICS = DisableAnalyticsPatch.isPatchIncluded();

    private FeatureSwitchesPatch() {
    }

    /**
     * Injection point.
     *
     * @param key The feature switch key.
     * @return The forced value of the switch, or null to use the value the app resolved.
     */
    public static Boolean getBooleanOverride(String key) {
        if (key == null) return null;

        if (HIDE_GROK && (key.startsWith("grok_") || key.contains("_grok_"))) {
            return false;
        }

        if (HIDE_SPACES_AND_LIVE && (key.startsWith("x_lite_spaces_")
                || key.startsWith("x_lite_live")
                || key.startsWith("android_audio_spaces_"))) {
            return false;
        }

        if (REMOVE_PREMIUM_UPSELL && key.startsWith("subscriptions_upsells_") && key.endsWith("_enabled")) {
            return false;
        }

        if (DISABLE_ANALYTICS && key.contains("scribe") && key.endsWith("_enabled")) {
            return false;
        }

        return null;
    }
}
