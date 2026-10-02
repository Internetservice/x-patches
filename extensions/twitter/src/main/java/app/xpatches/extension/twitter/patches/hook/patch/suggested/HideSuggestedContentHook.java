/*
 * Entry filters based on piko (GPLv3) - https://github.com/crimera/piko
 * Part of X Patches - https://github.com/Internetservice/x-patches
 */

package app.xpatches.extension.twitter.patches.hook.patch.suggested;

import org.json.JSONObject;

import app.xpatches.extension.twitter.patches.hook.json.BaseJsonHook;
import app.xpatches.extension.twitter.patches.hook.json.JsonParser;

/**
 * Strips the suggestion modules X injects between posts: communities to join,
 * related posts under a post, "Today's news" stories and the top people module in search.
 */
public final class HideSuggestedContentHook extends BaseJsonHook {
    public static final HideSuggestedContentHook INSTANCE = new HideSuggestedContentHook();

    private HideSuggestedContentHook() {
    }

    @Override
    public void apply(JSONObject json) {
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
