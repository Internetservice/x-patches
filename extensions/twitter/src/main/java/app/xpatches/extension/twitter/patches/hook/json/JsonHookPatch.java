/*
 * Ported from ReVanced Patches (GPLv3) - https://github.com/ReVanced/revanced-patches
 * Part of X Patches - https://github.com/Internetservice/x-patches
 */

package app.xpatches.extension.twitter.patches.hook.json;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import app.xpatches.extension.twitter.patches.hook.patch.dummy.DummyHook;
import app.xpatches.extension.twitter.utils.json.JsonUtils;
import app.xpatches.extension.twitter.utils.stream.StreamUtils;

@SuppressWarnings("unused")
public final class JsonHookPatch {
    public static final JsonHookPatch INSTANCE = new JsonHookPatch();

    /**
     * Hooks are registered here during patching. The DummyHook entry is replaced by the
     * real hooks, see the JSON hook patch.
     */
    private static final List<JsonHook> hooks;

    static {
        hooks = new ArrayList<>();
        hooks.add(DummyHook.INSTANCE);
    }

    /**
     * The GraphQL operation of the response being hooked, such as "HomeTimeline".
     */
    private static final ThreadLocal<String> CURRENT_OPERATION = new ThreadLocal<>();

    private JsonHookPatch() {
    }

    public static void setCurrentOperation(String operation) {
        CURRENT_OPERATION.set(operation);
    }

    public static String currentOperation() {
        return CURRENT_OPERATION.get();
    }

    /**
     * Injection point.
     */
    public static InputStream parseJsonHook(InputStream jsonInputStream) {
        JSONObject jsonObject;
        try {
            jsonObject = JsonUtils.parseJson(jsonInputStream);
        } catch (IOException | JSONException ignored) {
            return jsonInputStream;
        }

        for (JsonHook hook : hooks) {
            jsonObject = hook.hook(jsonObject);
        }

        return StreamUtils.fromString(jsonObject.toString());
    }
}
