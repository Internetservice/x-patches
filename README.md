# 🧩 X Patches

Patches for **X (formerly Twitter)** for use with [Morphe](https://morphe.software).

## ❓ About

These patches target X 12.30 and newer (the Compose based client). They started as a port of the X / Twitter patches from
[ReVanced Patches](https://github.com/ReVanced/revanced-patches)
([GitLab mirror](https://gitlab.com/revanced/revanced-patches)) to the Morphe patcher,
so they can be applied with Morphe Manager or Morphe Desktop.

Patch with everything selected: every patch gets a switch in the **X Patches settings**
screen the *Settings* patch adds, recommended ones on and the rest off, so you choose in the
app instead of patching again. Open it: open the navigation drawer and tap "X Patches" below "Help Center",
long press the X app icon and pick "X Patches settings", or open `xpatches://settings`. Changes apply after restarting X.

Included patches:

- **Hide ads** - hides promoted posts, promoted trends, real-time-bidding ads and video pre-rolls.
- **Hide recommended users** - hides the "Who to follow" and "Who to subscribe" recommendations.
- **Hide suggested content** - hides communities to join, related posts, Today's news and the top people module.
- **Remove premium upsell** - removes the premium upsell sheets, prompts and cards.
- **Hide Community Notes** - hides the Community Notes attached to posts.
- **Show sensitive media** - shows sensitive media without the warning overlays.
- **Hide Grok** - turns off the Grok tab, buttons, image generation and translations.
- **Hide Spaces and live** - turns off Spaces and the live stream pills.
- **Disable analytics** - drops the client event uploads about your activity.
- **Bring back Twitter** - the Twitter bird launcher icon and app name.
- **Hide social context** - hides "X follows", "Liked by" and similar lines above posts.
- **Hide verified badges** - hides the verification checkmarks and affiliation badges.
- **Hide post metrics** - hides the reply, repost, like and bookmark counts.
- **Hide promote button** - hides the "Promote" button on your own posts.
- **Force enable translate** - offers the translate action on every post, also when X has not enabled
  translations for the account, and can turn on the automatic translation of posts.
- **Force HD video** - plays videos in their highest available quality.
- **Keep timeline position** - stops the jump to the top of "For you" when the app is reopened.
- **Hide extra home tabs** - hides the Subscribed, ranked Following and pinned tabs.
- **Open links externally** - always opens links in the external browser.
- **Customize sharing link** - changes the domain used when sharing links (for example FxTwitter) and
  optionally includes the username in the link.
- **Unlock downloads** - unlocks the ability to download any video, also when the author disallowed it, and lets you pick the quality or copy the video link before downloading. GIFs can be downloaded via the menu on long press.
- **Dynamic color** - replaces the X blue with the Material You palette (Android 12+).
- **Hide view count** - hides the view count of posts.
- **No shortened URL** - opens and copies the real link of a post instead of the t.co short link.
- **Hide hidden replies** - hides the "Show more replies" and "Show additional replies" prompts.
- **Show poll results** - shows poll results without voting (polls are shown as final while on).
- **Customize side bar items** - hides entries of the side menu, picked in the X Patches settings.
- **Handle custom twitter links** - opens fxtwitter, vxtwitter, fixupx, fixvx and twittpr links in X.
- **Hide new posts pill** - hides the "New posts" pill at the top of the timelines.
- **Hide navigation bar badges** - hides the unread counts and dots on the navigation bar icons.
- **Block update screen** - blocks the in-app update prompts.
- **Custom download folder** - lets you choose where media is downloaded to.
- **Swipe to close media** - closes a full screen photo or video by swiping right, on the first photo of
  a post or anywhere on a video.
- **Hide immersive feed** - keeps the full screen video player on the video you opened, with no
  swiping to more videos.
- **Snooze topics longer** - lets you snooze topics in "For you" for longer than X allows, up to forever.
- **Video speed** - remembers the playback speed, lets you set your own speed levels, such as
  0.25, 0.5, 0.75, 1, 1.5, 2, 2.5 and 3, and changes the speed while a video is held: the right
  half speeds up, the left half slows down, both adjustable.

The patches that work on the response data and feature switches of the app were modelled on
[piko](https://github.com/crimera/piko), whose own patches target the previous, pre-12.30 X client.

### How to use these patches

Click here to add these patches to Morphe: https://morphe.software/add-source?github=Internetservice/x-patches

Or in Morphe Manager open *Patch sources*, add a GitHub source and enter this repository.
Pre-releases from the `dev` branch can be used by enabling *pre-release* on the source.

## 🩹 Patches list

<!-- PATCHES_START EXPANDED -->
> **[v1.0.0-dev.29](https://github.com/Internetservice/x-patches/releases/tag/v1.0.0-dev.29)**&nbsp;&nbsp;•&nbsp;&nbsp;`dev`&nbsp;&nbsp;•&nbsp;&nbsp;38 patches total
<details open>
<summary>📦 X&nbsp;&nbsp;•&nbsp;&nbsp;38 patches</summary>
<br>

**🎯 Supported versions:**

| 12.30.0-prod.01 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Block update screen](#block-update-screen) | Blocks the in-app update prompts and the "Update your X app" screen. |  |
| [Bring back Twitter](#bring-back-twitter) | Brings back the Twitter bird launcher icon and the Twitter app name. |  |
| [Control video auto advance](#control-video-auto-advance) | Lets you stop the immersive video player from advancing to the next video on its own. |  |
| [Custom download folder](#custom-download-folder) | Lets you choose the folder media is downloaded to, instead of Download/X. |  |
| [Customize sharing link](#customize-sharing-link) | Changes the domain name used when sharing links, and optionally includes the username in the link. | • Return username<br>• Domain name |
| [Customize side bar items](#customize-side-bar-items) | Lets you hide entries of the side menu, such as Premium, Communities or Spaces, from the X Patches settings. |  |
| [Disable analytics](#disable-analytics) | Drops the client event uploads (scribes) X sends about your activity in the app. Off by default in the X Patches settings. |  |
| [Dynamic color](#dynamic-color) | Replaces the default X (Formerly Twitter) Blue with the user's Material You palette. |  |
| [Enable debug menu](#enable-debug-menu) | Shows the hidden Debug Menu entry of X in the side menu. Off by default in the X Patches settings. |  |
| [Force HD video](#force-hd-video) | Plays videos in their highest available quality by removing the lower quality variants. Off by default in the X Patches settings. |  |
| [Force enable translate](#force-enable-translate) | Offers the translate action on every post, also when X has not enabled translations for the account, and can turn on the automatic translation of posts. Off by default in the X Patches settings. |  |
| [Handle custom twitter links](#handle-custom-twitter-links) | Opens fxtwitter, vxtwitter, fixupx, fixvx and twittpr links in X. On Android 12 and newer the links have to be enabled under "Open by default" in the app info. |  |
| [Hide Community Notes](#hide-community-notes) | Hides the Community Notes attached to posts. Off by default in the X Patches settings. |  |
| [Hide Grok](#hide-grok) | Turns off the Grok features: the Grok tab and sidebar entry, the Grok buttons on posts, Grok image generation and Grok translations. Off by default in the X Patches settings. |  |
| [Hide Spaces and live](#hide-spaces-and-live) | Turns off Spaces and the live stream pills and avatar rings. Off by default in the X Patches settings. |  |
| [Hide ads](#hide-ads) | Hides promoted posts, promoted trends, video pre-rolls and third party ads in the timelines. |  |
| [Hide extra home tabs](#hide-extra-home-tabs) | Hides the Subscribed, ranked Following, sports and pinned tabs next to "For you" and "Following". |  |
| [Hide hidden replies](#hide-hidden-replies) | Hides the "Show more replies" and "Show additional replies" prompts under posts. |  |
| [Hide navigation bar badges](#hide-navigation-bar-badges) | Hides the unread counts and dots on the navigation bar icons. |  |
| [Hide new posts pill](#hide-new-posts-pill) | Hides the "New posts" pill at the top of the timelines. |  |
| [Hide post metrics](#hide-post-metrics) | Hides the reply, repost, like and bookmark counts of posts. Off by default in the X Patches settings. |  |
| [Hide promote button](#hide-promote-button) | Hides the "Promote" button on your own posts. |  |
| [Hide recommended users](#hide-recommended-users) | Hides the 'Who to follow' recommendations in the timelines. |  |
| [Hide social context](#hide-social-context) | Hides the context lines above posts, such as "X follows", "Liked by" and "You might like". |  |
| [Hide suggested content](#hide-suggested-content) | Hides the suggestion modules X injects into timelines: communities to join, related posts under a post, Today's news stories and the top people module in search. |  |
| [Hide verified badges](#hide-verified-badges) | Hides the verification checkmarks and affiliation badges of users. Off by default in the X Patches settings. |  |
| [Hide view count](#hide-view-count) | Hides the view count of posts. Off by default in the X Patches settings. |  |
| [Keep timeline position](#keep-timeline-position) | Stops the app from jumping back to the top of "For you" and refreshing when it is reopened. |  |
| [No shortened URL](#no-shortened-url) | Opens and copies the real link of a post instead of the t.co short link. |  |
| [Open links externally](#open-links-externally) | Always opens links in the external browser instead of the in-app browser, regardless of the link opening setting. Off by default in the X Patches settings. |  |
| [Remove premium upsell](#remove-premium-upsell) | Removes the premium upsell sheets, the premium prompts in timelines and the upsell cards in the drawer and on profiles. |  |
| [Settings](#settings) | Adds the X Patches settings screen, reachable from the navigation drawer, by long pressing the app icon or by opening xpatches://settings. Every other patch can be switched off there without patching again. |  |
| [Show poll results](#show-poll-results) | Shows the results of polls without voting. Polls are shown as final while the setting is on, so voting is not possible. |  |
| [Show sensitive media](#show-sensitive-media) | Shows media marked as sensitive directly, without the warning overlays. Off by default in the X Patches settings. |  |
| [Snooze topics longer](#snooze-topics-longer) | Lets you snooze topics in "For you" for longer than X allows, up to forever. |  |
| [Swipe to close media](#swipe-to-close-media) | Closes a full screen photo or video by swiping right, on the first photo of a post or anywhere on a video. Also lets you disable the swipe up to the immersive video player. |  |
| [Unlock downloads](#unlock-downloads) | Unlocks the ability to download any video, including videos whose author disallowed downloads, and lets you pick the quality or copy the video link before downloading. GIFs can be downloaded via the menu on long press. |  |
| [Video speed](#video-speed) | Remembers the playback speed for every video, lets you set your own speed levels, such as 0.25, 0.5, 0.75, 1, 1.5, 2, 2.5 and 3, and changes the speed while a video is held: the right half speeds up, the left half slows down, both adjustable. |  |

</details>

<!-- PATCHES_END -->

## 🛠️ Building locally

1. Add a GitHub personal access token with the `read:packages` scope to `~/.gradle/gradle.properties`
   as `gpr.user` and `gpr.key`, as described in the
   [Morphe patcher setup](https://github.com/MorpheApp/morphe-patcher/blob/main/docs/2_1_setup.md#-prepare-the-environment).
2. Run `./gradlew buildAndroid`.
3. The built patches `.mpp` file is found in `patches/build/libs/patches-*.mpp`.
   Patch the APK with [Morphe Desktop](https://github.com/MorpheApp/morphe-desktop) or
   import the file as a local source in Morphe Manager.

## 🧑‍💻 Development

- Make all changes on the `dev` branch. Use [semantic commit](https://kapeli.com/cheat_sheets/Semantic_Commits.docset/Contents/Resources/Documents/index)
  messages (`feat:`, `fix:`, `chore:`). `feat:` and `fix:` commits create a pre-release automatically.
- Merge `dev` into `main` (merge commit, no squash) for a stable release.
- Releases are created by `release.yml`. Do not create releases or edit `patches-list.json`,
  `patches-bundle.json` or `CHANGELOG.md` by hand.

## 📜 License

X Patches are licensed under the [GNU General Public License v3.0](LICENSE).

The patches and extension code are derived from
[ReVanced Patches](https://github.com/ReVanced/revanced-patches) and
[piko](https://github.com/crimera/piko), both licensed under the GPLv3. The Twitter launcher icon
images come from piko.
The project layout and release tooling come from the
[Morphe patches template](https://github.com/MorpheApp/morphe-patches-template), see [NOTICE](NOTICE).
This project is not affiliated with Morphe or ReVanced.
