/*
 * Ported from ReVanced Patches (GPLv3) - https://github.com/ReVanced/revanced-patches
 * Part of X Patches - https://github.com/Internetservice/x-patches
 */

package app.xpatches.extension.twitter.patches.hook.patch.recommendation;

import org.json.JSONObject;

import app.xpatches.extension.twitter.patches.hook.json.BaseJsonHook;
import app.xpatches.extension.twitter.patches.hook.json.JsonParser;

/**
 * Strips recommended users ("Who to follow") from timeline JSON responses.
 */
public final class RecommendedUsersHook extends BaseJsonHook {
    public static final RecommendedUsersHook INSTANCE = new RecommendedUsersHook();

    private RecommendedUsersHook() {
    }

    @Override
    public void apply(JSONObject json) {
        JsonParser.INSTANCE.hideRecommendedUsers(json);
    }
}
