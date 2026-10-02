/*
 * Ported from ReVanced Patches (GPLv3) - https://github.com/ReVanced/revanced-patches
 * Part of X Patches - https://github.com/Internetservice/x-patches
 */

package app.xpatches.extension.twitter.patches.hook.patch.recommendation;

import org.json.JSONObject;

import app.xpatches.extension.twitter.patches.hook.json.BaseJsonHook;
import app.xpatches.extension.twitter.patches.hook.json.JsonParser;

/**
 * Strips the "Who to follow" and "Who to subscribe" recommendations.
 */
public final class RecommendedUsersHook extends BaseJsonHook {
    public static final RecommendedUsersHook INSTANCE = new RecommendedUsersHook();

    private RecommendedUsersHook() {
    }

    @Override
    public void apply(JSONObject json) {
        JsonParser.INSTANCE.removeTimelineEntries(json, entry -> {
            String entryId = JsonParser.entryId(entry);
            return entryId.startsWith("whoToFollow-")
                    || entryId.startsWith("who-to-follow-")
                    || entryId.startsWith("connect-module-")
                    || entryId.startsWith("who-to-subscribe-");
        });
    }
}
