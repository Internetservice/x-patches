/*
 * Entry filters based on piko (GPLv3) - https://github.com/crimera/piko
 * Part of X Patches - https://github.com/Internetservice/x-patches
 */

package app.xpatches.extension.twitter.patches.hook.patch.premium;

import org.json.JSONObject;

import app.xpatches.extension.twitter.patches.hook.json.BaseJsonHook;
import app.xpatches.extension.twitter.patches.hook.json.JsonParser;
import app.xpatches.extension.twitter.settings.Settings;

/**
 * Strips the premium prompts injected into timelines.
 */
public final class HidePremiumUpsellHook extends BaseJsonHook {
    public static final HidePremiumUpsellHook INSTANCE = new HidePremiumUpsellHook();

    private HidePremiumUpsellHook() {
    }

    /**
     * @return If this patch was included during patching. Modified during patching.
     */
    public static boolean isPatchIncluded() {
        return false;
    }

    @Override
    public void apply(JSONObject json) {
        if (!Settings.REMOVE_PREMIUM_UPSELL.get()) return;

        JsonParser.INSTANCE.removeTimelineEntries(json, entry -> {
            String entryId = JsonParser.entryId(entry);
            return entryId.startsWith("messageprompt-") || entryId.contains("upsell");
        });
    }
}
