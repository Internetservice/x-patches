/*
 * Ported from ReVanced Patches (GPLv3) - https://github.com/ReVanced/revanced-patches
 * Part of X Patches - https://github.com/Internetservice/x-patches
 */

package app.xpatches.extension.twitter.patches.links;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@SuppressWarnings("unused")
public final class CustomizeSharingLinkPatch {
    private static final String LINK_FORMAT = "https://%s/%s/status/%s";
    private static final String DEFAULT_LINK = "https://x.com/i/status/";

    /**
     * Method is modified during patching. Do not change.
     */
    private static String getShareDomain() {
        return "";
    }

    /**
     * Method is modified during patching. Do not change.
     */
    private static boolean isReturnUsernameEnabled() {
        return false;
    }

    /**
     * Injection point.
     *
     * Formats share sheet link for internal share such as sharing by DM.
     *
     * @param contextualPost The object containing post context.
     * @return A formatted link if successful; the default link otherwise.
     */
    public static String formatInternalShareSheetLink(Object contextualPost) {
        try {
            if (contextualPost == null) {
                return DEFAULT_LINK;
            }
            String username = "i";

            if (isReturnUsernameEnabled()) {
                Object canonicalPost = ReflectionHelper.invoke(contextualPost, "getCanonicalPost");
                Object userResult = ReflectionHelper.invoke(canonicalPost, "getAuthor");
                String fetchedUsername = (String) ReflectionHelper.invoke(userResult, "getScreenName");

                if (fetchedUsername != null && !fetchedUsername.isEmpty()) {
                    username = fetchedUsername;
                }
            }

            return String.format(LINK_FORMAT, getShareDomain(), username, "");
        } catch (Exception e) {
            return DEFAULT_LINK;
        }
    }

    /**
     * Injection point.
     *
     * Formats share sheet link for external share such as {@code Copy link} or {@code Share via...} etc.
     *
     * @param object The root object containing contextual post data.
     * @return A formatted link if successful; the default link otherwise.
     */
    public static String formatExternalShareSheetLink(Object object) {
        Object contextualPost = ReflectionHelper.getFieldValueByType(object, "ContextualPost");

        return formatInternalShareSheetLink(contextualPost);
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
                    if (m.getName().equals(methodName)) {
                        m.setAccessible(true);
                        return m.invoke(object);
                    }
                }
            } catch (Exception ignored) {
            }
            return null;
        }

        /**
         * Retrieves a field's value whose type name contains the specified string.
         *
         * @param object   The target object to inspect.
         * @param typeName The partial or full name of the class type to search for.
         * @return The field's value if found; {@code null} otherwise.
         */
        public static Object getFieldValueByType(Object object, String typeName) {
            if (object == null) return null;
            try {
                for (Field f : object.getClass().getDeclaredFields()) {
                    if (f.getType().getName().contains(typeName)) {
                        f.setAccessible(true);
                        return f.get(object);
                    }
                }
            } catch (Exception ignored) {
            }
            return null;
        }
    }
}
