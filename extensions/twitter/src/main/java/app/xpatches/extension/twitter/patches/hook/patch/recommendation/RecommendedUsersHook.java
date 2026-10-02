/*
 * Ported from ReVanced Patches (GPLv3) - https://github.com/ReVanced/revanced-patches
 * Part of X Patches - https://github.com/Internetservice/x-patches
 */

package app.xpatches.extension.twitter.patches.hook.patch.recommendation;

import org.json.JSONObject;

import app.xpatches.extension.twitter.patches.hook.json.BaseJsonHook;
import app.xpatches.extension.twitter.patches.hook.json.JsonParser;
import app.xpatches.extension.twitter.settings.Settings;

/**
 * Strips the "Who to follow" and "Who to subscribe" recommendations.
 */
public final class RecommendedUsersHook extends BaseJsonHook {
    public static final RecommendedUsersHook INSTANCE = new RecommendedUsersHook();

    private RecommendedUsersHook() {
    }

    /**
     * @return If this patch was included during patching. Modified during patching.
     */
    public static boolean isPatchIncluded() {
        return false;
    }

    @Override
    public void apply(JSONObject json) {
        if (!Settings.HIDE_RECOMMENDED_USERS.get()) return;

        JsonParser.INSTANCE.removeTimelineEntries(json, entry -> {
            String entryId = JsonParser.entryId(entry);
            return entryId.startsWith("whoToFollow-")
                    || entryId.startsWith("who-to-follow-")
                    || entryId.startsWith("connect-module-")
                    || entryId.startsWith("who-to-subscribe-");
        });
    }
}
