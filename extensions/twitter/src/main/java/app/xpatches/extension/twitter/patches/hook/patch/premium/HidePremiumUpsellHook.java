/*
 * Entry filters based on piko (GPLv3) - https://github.com/crimera/piko
 * Part of X Patches - https://github.com/Internetservice/x-patches
 */

package app.xpatches.extension.twitter.patches.hook.patch.premium;

import org.json.JSONObject;

import app.xpatches.extension.twitter.patches.hook.json.BaseJsonHook;
import app.xpatches.extension.twitter.patches.hook.json.JsonParser;

/**
 * Strips the premium prompts injected into timelines.
 */
public final class HidePremiumUpsellHook extends BaseJsonHook {
    public static final HidePremiumUpsellHook INSTANCE = new HidePremiumUpsellHook();

    private HidePremiumUpsellHook() {
    }

    @Override
    public void apply(JSONObject json) {
        JsonParser.INSTANCE.removeTimelineEntries(json, entry -> {
            String entryId = JsonParser.entryId(entry);
            return entryId.startsWith("messageprompt-") || entryId.contains("upsell");
        });
    }
}
