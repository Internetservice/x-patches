/*
 * Ported from ReVanced Patches (GPLv3) - https://github.com/ReVanced/revanced-patches
 * Part of X Patches - https://github.com/Internetservice/x-patches
 */

package app.xpatches.extension.twitter.patches.hook.okhttp;

import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.GZIPInputStream;

import app.xpatches.extension.twitter.Utils;
import app.xpatches.extension.twitter.patches.hook.json.JsonHookPatch;
import app.xpatches.extension.twitter.utils.stream.StreamUtils;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * OkHttp network interceptor that runs the timeline JSON responses through the JSON hooks.
 * Instantiated from the hooked OkHttpClient.Builder.build() method.
 */
@SuppressWarnings("unused")
public final class CustomNetworkInterceptorPatch implements Interceptor {
    private static final List<String> URL_FILTER_KEYWORD_LIST = List.of(
            "HomeTimeline",
            "ConversationTimeline",
            "UserTweets", // Old user timeline
            "UserProfileOriginalsTimeline" // New user timeline
    );

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();

        boolean isTargetUrl = false;
        String path = request.url().encodedPath();
        for (String keyword : URL_FILTER_KEYWORD_LIST) {
            if (path.contains(keyword)) {
                isTargetUrl = true;
                break;
            }
        }

        if (!isTargetUrl) {
            return chain.proceed(request);
        }

        Response response;
        try {
            response = chain.proceed(request);
        } catch (Exception e) {
            Log.e(Utils.LOG_TAG, "<-- [FAILED] " + path + " : " + e.getMessage());
            throw e;
        }

        ResponseBody body = response.body();
        if (body == null) {
            return response;
        }

        try {
            String encoding = response.header("Content-Encoding");
            boolean isGzip = encoding != null && encoding.equalsIgnoreCase("gzip");

            InputStream responseStream = body.byteStream();
            if (isGzip) {
                responseStream = new GZIPInputStream(responseStream);
            }
            byte[] rawBytes = StreamUtils.readAllBytes(responseStream);

            InputStream modifiedStream = JsonHookPatch.parseJsonHook(new ByteArrayInputStream(rawBytes));
            byte[] modifiedData = StreamUtils.readAllBytes(modifiedStream);

            if (modifiedData.length == 0) {
                Log.w(Utils.LOG_TAG, "JsonHookPatch returned empty stream, falling back to raw data.");
                modifiedData = rawBytes;
            }

            MediaType contentType = body.contentType();
            Response.Builder responseBuilder = response.newBuilder();

            if (isGzip) {
                responseBuilder.removeHeader("Content-Encoding");
            }

            return responseBuilder
                    .body(ResponseBody.create(modifiedData, contentType))
                    .build();

        } catch (Exception e) {
            Log.e(Utils.LOG_TAG, "Failed to intercept timeline response", e);
            return response;
        }
    }
}
