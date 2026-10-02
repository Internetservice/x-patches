/*
 * Ported from ReVanced Patches (GPLv3) - https://github.com/ReVanced/revanced-patches
 * Part of X Patches - https://github.com/Internetservice/x-patches
 */

package app.xpatches.extension.twitter.utils.json;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

import app.xpatches.extension.twitter.utils.stream.StreamUtils;

public final class JsonUtils {
    private JsonUtils() {
    }

    /**
     * Parses a JSON object from an input stream.
     *
     * @param jsonInputStream The input stream to parse.
     * @return The parsed JSONObject.
     * @throws IOException   If an I/O error occurs.
     * @throws JSONException If the stream content is not a valid JSON.
     */
    public static JSONObject parseJson(InputStream jsonInputStream) throws IOException, JSONException {
        return new JSONObject(StreamUtils.toString(jsonInputStream));
    }
}
