/*
 * Part of X Patches - https://github.com/Internetservice/x-patches
 * Licensed under the GNU General Public License v3.0.
 */

package app.xpatches.extension.twitter.settings;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BooleanSupplier;

import app.xpatches.extension.twitter.Utils;
import app.xpatches.extension.twitter.patches.hook.patch.ads.HideAdsHook;
import app.xpatches.extension.twitter.patches.hook.patch.downloads.UnlockDownloadsHook;
import app.xpatches.extension.twitter.patches.hook.patch.grok.HideGrokHook;
import app.xpatches.extension.twitter.patches.hook.patch.metrics.HidePostMetricsHook;
import app.xpatches.extension.twitter.patches.hook.patch.notes.HideCommunityNotesHook;
import app.xpatches.extension.twitter.patches.hook.patch.premium.HidePremiumUpsellHook;
import app.xpatches.extension.twitter.patches.hook.patch.promote.HidePromoteButtonHook;
import app.xpatches.extension.twitter.patches.hook.patch.recommendation.RecommendedUsersHook;
import app.xpatches.extension.twitter.patches.hook.patch.sensitive.ShowSensitiveMediaHook;
import app.xpatches.extension.twitter.patches.hook.patch.social.HideSocialContextHook;
import app.xpatches.extension.twitter.patches.hook.patch.suggested.HideSuggestedContentHook;
import app.xpatches.extension.twitter.patches.hook.patch.translate.ForceTranslateHook;
import app.xpatches.extension.twitter.patches.hook.patch.verified.HideVerifiedBadgesHook;
import app.xpatches.extension.twitter.patches.hook.patch.video.ForceHdVideoHook;
import app.xpatches.extension.twitter.patches.hook.patch.viewcount.HideViewCountHook;
import app.xpatches.extension.twitter.patches.links.CustomizeSharingLinkPatch;
import app.xpatches.extension.twitter.patches.toggles.DisableAnalyticsPatch;
import app.xpatches.extension.twitter.patches.toggles.HideExtraHomeTabsPatch;
import app.xpatches.extension.twitter.patches.toggles.HideSpacesAndLivePatch;
import app.xpatches.extension.twitter.patches.toggles.KeepTimelinePositionPatch;
import app.xpatches.extension.twitter.patches.toggles.OpenLinksExternallyPatch;

/**
 * The runtime switches of the patches. Every included patch gets a switch here, so patching
 * with everything selected and choosing in the app is the intended way to use them.
 * Recommended patches are on by default, the others off.
 */
public final class Settings {
    public static final String PREFERENCES_NAME = "xpatches";

    /**
     * A boolean switch bound to a patch.
     */
    public static final class Toggle {
        public final String key;
        public final String category;
        public final String title;
        public final String summary;
        /**
         * Whether the switch is on until the user changes it. Recommended patches are on.
         */
        public final boolean defaultValue;
        private final BooleanSupplier patchIncluded;

        Toggle(String key, String category, String title, String summary, boolean defaultValue, BooleanSupplier patchIncluded) {
            this.key = key;
            this.category = category;
            this.title = title;
            this.summary = summary;
            this.defaultValue = defaultValue;
            this.patchIncluded = patchIncluded;
            ALL.add(this);
        }

        /**
         * @return If the patch was included while patching.
         */
        public boolean isAvailable() {
            return patchIncluded.getAsBoolean();
        }

        /**
         * @return If the patch was included and the switch is on.
         */
        public boolean get() {
            if (!isAvailable()) return false;
            SharedPreferences preferences = preferences();
            return preferences == null ? defaultValue : preferences.getBoolean(key, defaultValue);
        }
    }

    private static final List<Toggle> ALL = new ArrayList<>();

    public static final String CATEGORY_TIMELINE = "Timeline";
    public static final String CATEGORY_POSTS = "Posts";
    public static final String CATEGORY_PREMIUM = "Premium";
    public static final String CATEGORY_APP = "App";
    public static final String CATEGORY_SHARING = "Sharing";

    public static final Toggle HIDE_ADS = new Toggle("hide_ads", CATEGORY_TIMELINE,
            "Hide ads", "Promoted posts, promoted trends, video pre-rolls and third party ads", true, HideAdsHook::isPatchIncluded);
    public static final Toggle HIDE_RECOMMENDED_USERS = new Toggle("hide_recommended_users", CATEGORY_TIMELINE,
            "Hide recommended users", "\"Who to follow\" and \"Who to subscribe\"", true, RecommendedUsersHook::isPatchIncluded);
    public static final Toggle HIDE_SUGGESTED_CONTENT = new Toggle("hide_suggested_content", CATEGORY_TIMELINE,
            "Hide suggested content", "Communities to join, related posts, Today's news, top people", true, HideSuggestedContentHook::isPatchIncluded);
    public static final Toggle HIDE_SOCIAL_CONTEXT = new Toggle("hide_social_context", CATEGORY_TIMELINE,
            "Hide social context", "\"X follows\", \"Liked by\" and similar lines above posts", false, HideSocialContextHook::isPatchIncluded);
    public static final Toggle KEEP_TIMELINE_POSITION = new Toggle("keep_timeline_position", CATEGORY_TIMELINE,
            "Keep timeline position", "Do not jump to the top of \"For you\" when the app is reopened", true, KeepTimelinePositionPatch::isPatchIncluded);
    public static final Toggle HIDE_EXTRA_HOME_TABS = new Toggle("hide_extra_home_tabs", CATEGORY_TIMELINE,
            "Hide extra home tabs", "Subscribed, ranked Following, sports and pinned tabs", false, HideExtraHomeTabsPatch::isPatchIncluded);

    public static final Toggle HIDE_VIEW_COUNT = new Toggle("hide_view_count", CATEGORY_POSTS,
            "Hide view count", null, false, HideViewCountHook::isPatchIncluded);
    public static final Toggle HIDE_POST_METRICS = new Toggle("hide_post_metrics", CATEGORY_POSTS,
            "Hide post metrics", "Reply, repost, like and bookmark counts", false, HidePostMetricsHook::isPatchIncluded);
    public static final Toggle HIDE_COMMUNITY_NOTES = new Toggle("hide_community_notes", CATEGORY_POSTS,
            "Hide Community Notes", null, false, HideCommunityNotesHook::isPatchIncluded);
    public static final Toggle SHOW_SENSITIVE_MEDIA = new Toggle("show_sensitive_media", CATEGORY_POSTS,
            "Show sensitive media", "Without the warning overlays", false, ShowSensitiveMediaHook::isPatchIncluded);
    public static final Toggle HIDE_VERIFIED_BADGES = new Toggle("hide_verified_badges", CATEGORY_POSTS,
            "Hide verified badges", "Checkmarks and affiliation badges", false, HideVerifiedBadgesHook::isPatchIncluded);
    public static final Toggle HIDE_PROMOTE_BUTTON = new Toggle("hide_promote_button", CATEGORY_POSTS,
            "Hide promote button", "On your own posts", true, HidePromoteButtonHook::isPatchIncluded);
    public static final Toggle FORCE_TRANSLATE = new Toggle("force_translate", CATEGORY_POSTS,
            "Force enable translate", "Offer the translate action on every post", false, ForceTranslateHook::isPatchIncluded);
    public static final Toggle UNLOCK_DOWNLOADS = new Toggle("unlock_downloads", CATEGORY_POSTS,
            "Unlock downloads", "Offer the download action on every video, also when the author disallowed it", true, UnlockDownloadsHook::isPatchIncluded);
    public static final Toggle FORCE_HD_VIDEO = new Toggle("force_hd_video", CATEGORY_POSTS,
            "Force HD video", "Play videos in their highest available quality", false, ForceHdVideoHook::isPatchIncluded);

    public static final Toggle REMOVE_PREMIUM_UPSELL = new Toggle("remove_premium_upsell", CATEGORY_PREMIUM,
            "Remove premium upsell", "Upsell sheets, prompts and cards", true, HidePremiumUpsellHook::isPatchIncluded);

    public static final Toggle HIDE_GROK = new Toggle("hide_grok", CATEGORY_APP,
            "Hide Grok", "Grok tab, buttons, image generation and translations", false, HideGrokHook::isPatchIncluded);
    public static final Toggle HIDE_SPACES_AND_LIVE = new Toggle("hide_spaces_and_live", CATEGORY_APP,
            "Hide Spaces and live", "Spaces and the live stream pills", false, HideSpacesAndLivePatch::isPatchIncluded);
    public static final Toggle DISABLE_ANALYTICS = new Toggle("disable_analytics", CATEGORY_APP,
            "Disable analytics", "Drop the client event uploads", false, DisableAnalyticsPatch::isPatchIncluded);
    public static final Toggle OPEN_LINKS_EXTERNALLY = new Toggle("open_links_externally", CATEGORY_APP,
            "Open links externally", "Always use the external browser", false, OpenLinksExternallyPatch::isPatchIncluded);

    /**
     * Sharing settings, shown when the sharing link patch is included.
     */
    public static final String KEY_SHARE_DOMAIN = "share_domain";
    public static final String KEY_SHARE_USERNAME = "share_username";

    private Settings() {
    }

    public static List<Toggle> all() {
        return Collections.unmodifiableList(ALL);
    }

    /**
     * @return The preferences, or null if no context is available yet.
     */
    public static SharedPreferences preferences() {
        Context context = Utils.getContext();
        return context == null ? null : context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    public static boolean isSharingLinkPatchIncluded() {
        return CustomizeSharingLinkPatch.isPatchIncluded();
    }

    public static String shareDomain() {
        SharedPreferences preferences = preferences();
        String value = preferences == null ? null : preferences.getString(KEY_SHARE_DOMAIN, null);
        return value == null || value.trim().isEmpty() ? CustomizeSharingLinkPatch.defaultShareDomain() : value.trim();
    }

    public static boolean shareUsername() {
        SharedPreferences preferences = preferences();
        return preferences == null
                ? CustomizeSharingLinkPatch.defaultReturnUsername()
                : preferences.getBoolean(KEY_SHARE_USERNAME, CustomizeSharingLinkPatch.defaultReturnUsername());
    }
}
