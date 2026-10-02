/*
 * Entry filters based on piko (GPLv3) - https://github.com/crimera/piko
 * Part of X Patches - https://github.com/Internetservice/x-patches
 */

package app.xpatches.extension.twitter.patches.hook.patch.suggested;

import org.json.JSONObject;

import app.xpatches.extension.twitter.patches.hook.json.BaseJsonHook;
import app.xpatches.extension.twitter.patches.hook.json.JsonParser;
import app.xpatches.extension.twitter.settings.Settings;

/**
 * Strips the suggestion modules X injects between posts: communities to join,
 * related posts under a post, "Today's news" stories and the top people module in search.
 */
public final class HideSuggestedContentHook extends BaseJsonHook {
    public static final HideSuggestedContentHook INSTANCE = new HideSuggestedContentHook();

    private HideSuggestedContentHook() {
    }

    /**
     * @return If this patch was included during patching. Modified during patching.
     */
    public static boolean isPatchIncluded() {
        return false;
    }

    @Override
    public void apply(JSONObject json) {
        if (!Settings.HIDE_SUGGESTED_CONTENT.get()) return;

        JsonParser.INSTANCE.removeTimelineEntries(json, entry -> {
            String entryId = JsonParser.entryId(entry);
            String kind = JsonParser.entryKind(entryId);
            return entryId.startsWith("community-to-join")
                    || kind.startsWith("tweetdetailrelatedtweets")
                    || entryId.startsWith("stories")
                    || kind.equals("toptabsrpusermodule");
        });
    }
}
