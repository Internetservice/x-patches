/*
 * Ported from ReVanced Patches (GPLv3) - https://github.com/ReVanced/revanced-patches
 * Part of X Patches - https://github.com/Internetservice/x-patches
 */

package app.xpatches.extension.twitter.patches.hook.json;

import org.json.JSONObject;

import app.xpatches.extension.twitter.patches.hook.patch.Hook;

public interface JsonHook extends Hook<JSONObject> {
    /**
     * Transform a JSONObject.
     *
     * @param json The JSONObject.
     * @return The transformed JSONObject.
     */
    JSONObject transform(JSONObject json);

    @Override
    default JSONObject hook(JSONObject type) {
        return transform(type);
    }
}
