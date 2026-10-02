/*
 * Ported from ReVanced Patches (GPLv3) - https://github.com/ReVanced/revanced-patches
 * Part of X Patches - https://github.com/Internetservice/x-patches
 */

package app.xpatches.extension.twitter.patches.links;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import app.xpatches.extension.twitter.settings.Settings;

@SuppressWarnings("unused")
public final class CustomizeSharingLinkPatch {
    private static final String LINK_FORMAT = "https://%s/%s/status/%s";

    private static final Pattern STATUS_LINK = Pattern.compile(
            "^https?://(?:www\\.|mobile\\.)?(?:x\\.com|twitter\\.com)/([^/?#]+)/status/(\\d+)(?:[/?#].*)?$");

    /**
     * Share request remembered by the share sheet hook, used to resolve the username.
     */
    private static volatile Object pendingShareRequest;

    /**
     * @return If this patch was included during patching. Modified during patching.
     */
    public static boolean isPatchIncluded() {
        return false;
    }

    /**
     * The domain chosen while patching. Modified during patching.
     */
    public static String defaultShareDomain() {
        return "x.com";
    }

    /**
     * The username choice made while patching. Modified during patching.
     */
    public static boolean defaultReturnUsername() {
        return true;
    }

    private static String getShareDomain() {
        return Settings.shareDomain();
    }

    private static boolean isReturnUsernameEnabled() {
        return Settings.shareUsername();
    }

    /**
     * Injection point.
     *
     * Remembers the share request of the external share sheet so the username can be resolved
     * from the post it contains.
     */
    public static void setShareRequest(Object shareRequest) {
        pendingShareRequest = shareRequest;
    }

    /**
     * Injection point.
     *
     * Rewrites a post link the app built. Links that are not post links are returned unchanged.
     *
     * @param url A link such as https://x.com/username/status/123 or https://x.com/i/status/123.
     */
    public static String formatShareUrl(String url) {
        if (url == null) return null;

        Matcher matcher = STATUS_LINK.matcher(url);
        if (!matcher.matches()) return url;

        String username = matcher.group(1);
        String postId = matcher.group(2);
        if (!isReturnUsernameEnabled() || username == null || username.isEmpty()) {
            username = "i";
        }

        return String.format(LINK_FORMAT, getShareDomain(), username, postId);
    }

    /**
     * Injection point.
     *
     * The external share sheet builds https://x.com/i/status/id. If the username is wanted,
     * the canonical post link is resolved from the remembered share request.
     */
    public static String formatExternalShareSheetLink(String url) {
        Object shareRequest = pendingShareRequest;
        pendingShareRequest = null;

        if (isReturnUsernameEnabled() && shareRequest != null) {
            try {
                String canonicalUrl = ReflectionHelper.findPostUrl(shareRequest, 0);
                if (canonicalUrl != null) {
                    return formatShareUrl(canonicalUrl);
                }
            } catch (Exception ignored) {
            }
        }

        return formatShareUrl(url);
    }

    /**
     * Simplifies Reflection API usage by locating and invoking members based on their types.
     */
    public static class ReflectionHelper {
        /**
         * Invokes a method by name, searching the entire class hierarchy including interfaces.
         *
         * @param object     The target object to invoke on.
         * @param methodName The name of the method to be invoked.
         * @return The result of the invocation if successful; {@code null} otherwise.
         */
        public static Object invoke(Object object, String methodName) {
            if (object == null) return null;
            try {
                for (Method m : object.getClass().getMethods()) {
                    if (m.getName().equals(methodName) && m.getParameterCount() == 0) {
                        m.setAccessible(true);
                        return m.invoke(object);
                    }
                }
            } catch (Exception ignored) {
            }
            return null;
        }

        /**
         * Looks for a post link in an object graph: the object itself or one of its
         * (non static) fields exposes a {@code getUrl()} method returning a post link.
         *
         * @param object The object to inspect.
         * @param depth  The current recursion depth.
         * @return The post link if found; {@code null} otherwise.
         */
        static String findPostUrl(Object object, int depth) {
            if (object == null || depth > 2) return null;

            Object url = invoke(object, "getUrl");
            if (url instanceof String && STATUS_LINK.matcher((String) url).matches()) {
                return (String) url;
            }

            for (Field f : object.getClass().getDeclaredFields()) {
                if (Modifier.isStatic(f.getModifiers()) || f.getType().isPrimitive()) continue;
                try {
                    f.setAccessible(true);
                    String found = findPostUrl(f.get(object), depth + 1);
                    if (found != null) return found;
                } catch (Exception ignored) {
                }
            }

            return null;
        }
    }
}
