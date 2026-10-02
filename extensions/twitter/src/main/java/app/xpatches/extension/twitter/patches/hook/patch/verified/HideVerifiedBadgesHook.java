/*
 * Part of X Patches - https://github.com/Internetservice/x-patches
 * Licensed under the GNU General Public License v3.0.
 */

package app.xpatches.extension.twitter.patches.hook.patch.verified;

import org.json.JSONObject;

import app.xpatches.extension.twitter.patches.hook.json.BaseJsonHook;
import app.xpatches.extension.twitter.patches.hook.json.JsonParser;
import app.xpatches.extension.twitter.settings.Settings;

/**
 * Removes the verification checkmarks and affiliation badges from users.
 */
public final class HideVerifiedBadgesHook extends BaseJsonHook {
    public static final HideVerifiedBadgesHook INSTANCE = new HideVerifiedBadgesHook();

    private HideVerifiedBadgesHook() {
    }

    /**
     * @return If this patch was included during patching. Modified during patching.
     */
    public static boolean isPatchIncluded() {
        return false;
    }

    @Override
    public void apply(JSONObject json) {
        if (!Settings.HIDE_VERIFIED_BADGES.get()) return;

        JsonParser.INSTANCE.forEachObject(json, object -> {
            JSONObject verification = object.optJSONObject("verification");
            if (verification != null) {
                for (String key : new String[]{"is_blue_verified", "is_verified_organization", "is_verified_organization_affiliate", "verified"}) {
                    if (verification.has(key)) JsonParser.put(verification, key, false);
                }
                if (verification.has("verified_type")) JsonParser.put(verification, "verified_type", JSONObject.NULL);
            }
            if (object.has("affiliates_highlighted_label") && !object.isNull("affiliates_highlighted_label")) {
                JsonParser.put(object, "affiliates_highlighted_label", JSONObject.NULL);
            }
        });
    }
}
