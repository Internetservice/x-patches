/*
 * Part of X Patches - https://github.com/Internetservice/x-patches
 * Licensed under the GNU General Public License v3.0.
 */

package app.xpatches.extension.twitter.patches.hook.patch.notes;

import org.json.JSONObject;

import app.xpatches.extension.twitter.patches.hook.json.BaseJsonHook;
import app.xpatches.extension.twitter.patches.hook.json.JsonParser;

/**
 * Removes the Community Notes attached to posts.
 */
public final class HideCommunityNotesHook extends BaseJsonHook {
    public static final HideCommunityNotesHook INSTANCE = new HideCommunityNotesHook();

    private HideCommunityNotesHook() {
    }

    @Override
    public void apply(JSONObject json) {
        JsonParser.INSTANCE.forEachObject(json, object -> {
            for (String key : new String[]{"birdwatch_pivot", "birdwatch_tombstone_pivot"}) {
                if (object.has(key) && !object.isNull(key)) {
                    JsonParser.put(object, key, JSONObject.NULL);
                }
            }
        });
    }
}
