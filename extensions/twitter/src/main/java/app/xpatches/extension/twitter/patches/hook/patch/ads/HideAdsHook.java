/*
 * Ported from ReVanced Patches (GPLv3) - https://github.com/ReVanced/revanced-patches
 * Entry filters based on piko (GPLv3) - https://github.com/crimera/piko
 * Part of X Patches - https://github.com/Internetservice/x-patches
 */

package app.xpatches.extension.twitter.patches.hook.patch.ads;

import org.json.JSONObject;

import app.xpatches.extension.twitter.patches.hook.json.BaseJsonHook;
import app.xpatches.extension.twitter.patches.hook.json.JsonParser;
import app.xpatches.extension.twitter.settings.Settings;

/**
 * Strips promoted posts, promoted trends, real-time-bidding ads and video pre-rolls.
 */
public final class HideAdsHook extends BaseJsonHook {
    public static final HideAdsHook INSTANCE = new HideAdsHook();

    private HideAdsHook() {
    }

    /**
     * @return If this patch was included during patching. Modified during patching.
     */
    public static boolean isPatchIncluded() {
        return false;
    }

    @Override
    public void apply(JSONObject json) {
        if (!Settings.HIDE_ADS.get()) return;

        JsonParser.INSTANCE.removeTimelineEntries(json, HideAdsHook::isAd);

        // Video pre-roll ads are attached to regular posts.
        JsonParser.INSTANCE.forEachObject(json, object -> {
            if (object.has("preroll_metadata") && !object.isNull("preroll_metadata")) {
                JsonParser.put(object, "preroll_metadata", JSONObject.NULL);
            }
        });
    }

    private static boolean isAd(JSONObject entry) {
        String entryId = JsonParser.entryId(entry);
        String kind = JsonParser.entryKind(entryId);

        if (entryId.contains("promoted") || entryId.contains("rtb")) return true;
        if (kind.equals("superhero") || kind.equals("eventsummary") || kind.equals("pivot")) return true;
        if (entryId.startsWith("main-event-")) return true;

        // Promoted content is also marked explicitly on the item.
        return JsonParser.INSTANCE.containsKey(entry, "promoted_metadata");
    }
}
