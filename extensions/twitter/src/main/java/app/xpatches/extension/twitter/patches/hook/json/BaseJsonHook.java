/*
 * Ported from ReVanced Patches (GPLv3) - https://github.com/ReVanced/revanced-patches
 * Part of X Patches - https://github.com/Internetservice/x-patches
 */

package app.xpatches.extension.twitter.patches.hook.json;

import org.json.JSONObject;

public abstract class BaseJsonHook implements JsonHook {
    /**
     * Abstract method to be implemented by subclasses to modify the JSONObject.
     *
     * @param json The JSONObject to modify.
     */
    public abstract void apply(JSONObject json);

    @Override
    public JSONObject transform(JSONObject json) {
        apply(json);
        return json;
    }
}
