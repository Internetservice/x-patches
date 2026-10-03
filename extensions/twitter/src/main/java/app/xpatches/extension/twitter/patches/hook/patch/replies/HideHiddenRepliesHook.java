/*
 * Part of X Patches - https://github.com/Internetservice/x-patches
 * Licensed under the GNU General Public License v3.0.
 */

package app.xpatches.extension.twitter.patches.hook.patch.replies;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import app.xpatches.extension.twitter.patches.hook.json.BaseJsonHook;
import app.xpatches.extension.twitter.patches.hook.json.JsonParser;
import app.xpatches.extension.twitter.settings.Settings;

/**
 * Removes the "Show more replies" and "Show additional replies, including those that may
 * contain offensive content" prompts, which are cursor entries of the conversation timeline.
 */
public final class HideHiddenRepliesHook extends BaseJsonHook {
    public static final HideHiddenRepliesHook INSTANCE = new HideHiddenRepliesHook();

    private static final Set<String> CURSOR_TYPES = new HashSet<>(Arrays.asList(
            "ShowMoreThreads",
            "ShowMoreThreadsPrompt"
    ));

    private HideHiddenRepliesHook() {
    }

    /**
     * @return If this patch was included during patching. Modified during patching.
     */
    public static boolean isPatchIncluded() {
        return false;
    }

    @Override
    public void apply(JSONObject json) {
        if (!Settings.HIDE_HIDDEN_REPLIES.get()) return;

        JsonParser.INSTANCE.removeTimelineEntries(json, entry -> hasHiddenRepliesCursor(entry, 0));
    }

    private static boolean hasHiddenRepliesCursor(Object node, int depth) {
        if (depth > 6) return false;
        if (node instanceof JSONObject) {
            JSONObject object = (JSONObject) node;
            String cursorType = object.optString("cursor_type", object.optString("cursorType", null));
            if (cursorType != null && CURSOR_TYPES.contains(cursorType)) return true;
            Iterator<String> keys = object.keys();
            while (keys.hasNext()) {
                if (hasHiddenRepliesCursor(object.opt(keys.next()), depth + 1)) return true;
            }
        } else if (node instanceof JSONArray) {
            JSONArray array = (JSONArray) node;
            for (int i = 0; i < array.length(); i++) {
                if (hasHiddenRepliesCursor(array.opt(i), depth + 1)) return true;
            }
        }
        return false;
    }
}
