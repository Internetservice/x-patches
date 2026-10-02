/*
 * Ported from ReVanced Patches (GPLv3) - https://github.com/ReVanced/revanced-patches
 * Part of X Patches - https://github.com/Internetservice/x-patches
 */

package app.xpatches.extension.twitter.patches.hook.okhttp;

import android.util.Log;

import org.brotli.dec.BrotliInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.GZIPInputStream;

import app.xpatches.extension.twitter.Utils;
import app.xpatches.extension.twitter.patches.hook.json.JsonHookPatch;
import app.xpatches.extension.twitter.patches.toggles.DisableAnalyticsPatch;
import app.xpatches.extension.twitter.utils.stream.StreamUtils;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * OkHttp network interceptor that runs the GraphQL responses through the JSON hooks
 * and optionally drops the analytics uploads.
 * Instantiated from the hooked OkHttpClient.Builder.build() method.
 */
@SuppressWarnings("unused")
public final class CustomNetworkInterceptorPatch implements Interceptor {
    private static final boolean DISABLE_ANALYTICS = DisableAnalyticsPatch.isPatchIncluded();

    /**
     * Legacy (X 12.10) REST style paths. X 12.30 routes everything through /graphql/.
     */
    private static final List<String> URL_FILTER_KEYWORD_LIST = List.of(
            "/graphql/",
            "HomeTimeline",
            "ConversationTimeline",
            "UserTweets", // Old user timeline
            "UserProfileOriginalsTimeline" // New user timeline
    );

    private static final String ANALYTICS_PATH_KEYWORD = "/jot/";

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        String path = request.url().encodedPath();

        if (DISABLE_ANALYTICS && path.contains(ANALYTICS_PATH_KEYWORD)) {
            // Pretend the upload succeeded so the app discards the queued events.
            return new Response.Builder()
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body(ResponseBody.create(new byte[0], MediaType.parse("application/json")))
                    .build();
        }

        boolean isTargetUrl = false;
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

        MediaType contentType = body.contentType();
        if (contentType != null && !"json".equalsIgnoreCase(contentType.subtype())) {
            return response;
        }

        try {
            String encoding = response.header("Content-Encoding");
            InputStream responseStream = body.byteStream();
            boolean decoded = false;
            if ("gzip".equalsIgnoreCase(encoding)) {
                responseStream = new GZIPInputStream(responseStream);
                decoded = true;
            } else if ("br".equalsIgnoreCase(encoding)) {
                responseStream = new BrotliInputStream(responseStream);
                decoded = true;
            }
            byte[] rawBytes = StreamUtils.readAllBytes(responseStream);

            InputStream modifiedStream = JsonHookPatch.parseJsonHook(new ByteArrayInputStream(rawBytes));
            byte[] modifiedData = StreamUtils.readAllBytes(modifiedStream);

            if (modifiedData.length == 0) {
                Log.w(Utils.LOG_TAG, "JsonHookPatch returned empty stream, falling back to raw data.");
                modifiedData = rawBytes;
            }

            Response.Builder responseBuilder = response.newBuilder();

            if (decoded) {
                responseBuilder.removeHeader("Content-Encoding");
                responseBuilder.removeHeader("Content-Length");
            }

            return responseBuilder
                    .body(ResponseBody.create(modifiedData, contentType))
                    .build();

        } catch (Exception e) {
            Log.e(Utils.LOG_TAG, "Failed to intercept response of " + path, e);
            return response;
        }
    }
}
