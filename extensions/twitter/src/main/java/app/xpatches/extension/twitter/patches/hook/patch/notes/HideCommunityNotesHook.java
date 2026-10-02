/*
 * Part of X Patches - https://github.com/Internetservice/x-patches
 * Licensed under the GNU General Public License v3.0.
 */

package app.xpatches.extension.twitter.patches.hook.patch.notes;

import org.json.JSONObject;

import app.xpatches.extension.twitter.patches.hook.json.BaseJsonHook;
import app.xpatches.extension.twitter.patches.hook.json.JsonParser;
import app.xpatches.extension.twitter.settings.Settings;

/**
 * Removes the Community Notes attached to posts.
 */
public final class HideCommunityNotesHook extends BaseJsonHook {
    public static final HideCommunityNotesHook INSTANCE = new HideCommunityNotesHook();

    private HideCommunityNotesHook() {
    }

    /**
     * @return If this patch was included during patching. Modified during patching.
     */
    public static boolean isPatchIncluded() {
        return false;
    }

    @Override
    public void apply(JSONObject json) {
        if (!Settings.HIDE_COMMUNITY_NOTES.get()) return;

        JsonParser.INSTANCE.forEachObject(json, object -> {
            for (String key : new String[]{"birdwatch_pivot", "birdwatch_tombstone_pivot"}) {
                if (object.has(key) && !object.isNull(key)) {
                    JsonParser.put(object, key, JSONObject.NULL);
                }
            }
        });
    }
}
