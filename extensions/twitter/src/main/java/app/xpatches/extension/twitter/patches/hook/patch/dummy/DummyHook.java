/*
 * Ported from ReVanced Patches (GPLv3) - https://github.com/ReVanced/revanced-patches
 * Part of X Patches - https://github.com/Internetservice/x-patches
 */

package app.xpatches.extension.twitter.patches.hook.patch.dummy;

import org.json.JSONObject;

import app.xpatches.extension.twitter.patches.hook.json.BaseJsonHook;

/**
 * Dummy hook to reserve a register in JsonHookPatch.hooks list.
 * Removed during patching once the real hooks are added.
 */
public final class DummyHook extends BaseJsonHook {
    public static final DummyHook INSTANCE = new DummyHook();

    private DummyHook() {
    }

    @Override
    public void apply(JSONObject json) {
        // Do nothing.
    }
}
