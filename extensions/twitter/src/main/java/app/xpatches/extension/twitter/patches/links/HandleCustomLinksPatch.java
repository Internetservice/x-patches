/*
 * Part of X Patches - https://github.com/Internetservice/x-patches
 * Licensed under the GNU General Public License v3.0.
 */

package app.xpatches.extension.twitter.patches.links;

import android.content.Intent;
import android.net.Uri;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import app.xpatches.extension.twitter.XLog;
import app.xpatches.extension.twitter.settings.Settings;

/**
 * Rewrites links of the link fixing services to x.com links before X handles them.
 */
@SuppressWarnings("unused")
public final class HandleCustomLinksPatch {
    private static final Set<String> HOSTS = new HashSet<>(Arrays.asList(
            "fxtwitter.com", "www.fxtwitter.com",
            "vxtwitter.com", "www.vxtwitter.com",
            "fixupx.com", "www.fixupx.com",
            "fixvx.com", "www.fixvx.com",
            "twittpr.com", "www.twittpr.com"
    ));

    private HandleCustomLinksPatch() {
    }

    /**
     * @return If this patch was included during patching. Modified during patching.
     */
    public static boolean isPatchIncluded() {
        return false;
    }

    /**
     * Injection point. Called with the intents the main activity is created or resumed with.
     */
    public static void rewriteIntent(Intent intent) {
        try {
            if (intent == null || !Settings.HANDLE_CUSTOM_LINKS.get()) return;
            Uri data = intent.getData();
            if (data == null || data.getHost() == null) return;
            if (!HOSTS.contains(data.getHost().toLowerCase())) return;

            Uri rewritten = data.buildUpon().scheme("https").authority("x.com").build();
            intent.setData(rewritten);
            XLog.i("Rewrote " + data + " to " + rewritten);
        } catch (Exception e) {
            XLog.e("Could not rewrite the link", e);
        }
    }
}
