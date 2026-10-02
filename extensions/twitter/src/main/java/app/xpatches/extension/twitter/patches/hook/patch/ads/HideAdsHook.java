/*
 * Ported from ReVanced Patches (GPLv3) - https://github.com/ReVanced/revanced-patches
 * Part of X Patches - https://github.com/Internetservice/x-patches
 */

package app.xpatches.extension.twitter.patches.hook.patch.ads;

import org.json.JSONObject;

import app.xpatches.extension.twitter.patches.hook.json.BaseJsonHook;
import app.xpatches.extension.twitter.patches.hook.json.JsonParser;

/**
 * Strips promoted posts from timeline JSON responses.
 */
public final class HideAdsHook extends BaseJsonHook {
    public static final HideAdsHook INSTANCE = new HideAdsHook();

    private HideAdsHook() {
    }

    @Override
    public void apply(JSONObject json) {
        JsonParser.INSTANCE.hidePromotedMetadata(json);
    }
}
