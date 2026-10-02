# 🧩 X Patches

Patches for **X (formerly Twitter)** for use with [Morphe](https://morphe.software).

## ❓ About

These patches target X 12.30 and newer (the Compose based client). They started as a port of the X / Twitter patches from
[ReVanced Patches](https://github.com/ReVanced/revanced-patches)
([GitLab mirror](https://gitlab.com/revanced/revanced-patches)) to the Morphe patcher,
so they can be applied with Morphe Manager or Morphe Desktop.

Included patches:

- **Hide ads** - hides promoted posts, promoted trends, real-time-bidding ads and video pre-rolls.
- **Hide recommended users** - hides the "Who to follow" and "Who to subscribe" recommendations.
- **Hide suggested content** - hides communities to join, related posts, Today's news and the top people module.
- **Remove premium upsell** - removes the premium upsell sheets, prompts and cards.
- **Hide Community Notes** - hides the Community Notes attached to posts (off by default).
- **Show sensitive media** - shows sensitive media without the warning overlays (off by default).
- **Hide Grok** - turns off the Grok tab, buttons, image generation and translations (off by default).
- **Hide Spaces and live** - turns off Spaces and the live stream pills (off by default).
- **Disable analytics** - drops the client event uploads about your activity (off by default).
- **Bring back Twitter** - the Twitter bird launcher icon and app name (off by default).
- **Customize sharing link** - changes the domain used when sharing links (for example FxTwitter) and
  optionally includes the username in the link.
- **Unlock downloads** - unlocks the ability to download any video. GIFs can be downloaded via the menu on long press.
- **Dynamic color** - replaces the X blue with the Material You palette (Android 12+).
- **Hide view count** - hides the view count of posts (off by default).

The patches that work on the response data and feature switches of the app were modelled on
[piko](https://github.com/crimera/piko), whose own patches target the previous, pre-12.30 X client.

### How to use these patches

Click here to add these patches to Morphe: https://morphe.software/add-source?github=Internetservice/x-patches

Or in Morphe Manager open *Patch sources*, add a GitHub source and enter this repository.
Pre-releases from the `dev` branch can be used by enabling *pre-release* on the source.

## 🩹 Patches list

<!-- PATCHES_START EXPANDED -->
> **[v1.0.0-dev.5](https://github.com/Internetservice/x-patches/releases/tag/v1.0.0-dev.5)**&nbsp;&nbsp;•&nbsp;&nbsp;`dev`&nbsp;&nbsp;•&nbsp;&nbsp;15 patches total
<details open>
<summary>📦 X&nbsp;&nbsp;•&nbsp;&nbsp;15 patches</summary>
<br>

**🎯 Supported versions:**

| 12.30.0-prod.01 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Bring back Twitter](#bring-back-twitter) | Brings back the Twitter bird launcher icon and the Twitter app name. |  |
| [Customize sharing link](#customize-sharing-link) | Changes the domain name used when sharing links, and optionally includes the username in the link. | • Return username<br>• Domain name |
| [Disable analytics](#disable-analytics) | Drops the client event uploads (scribes) X sends about your activity in the app. |  |
| [Dynamic color](#dynamic-color) | Replaces the default X (Formerly Twitter) Blue with the user's Material You palette. |  |
| [Hide Community Notes](#hide-community-notes) | Hides the Community Notes attached to posts. |  |
| [Hide Grok](#hide-grok) | Turns off the Grok features: the Grok tab and sidebar entry, the Grok buttons on posts, Grok image generation and Grok translations. |  |
| [Hide Spaces and live](#hide-spaces-and-live) | Turns off Spaces and the live stream pills and avatar rings. |  |
| [Hide ads](#hide-ads) | Hides promoted posts in the timelines. |  |
| [Hide recommended users](#hide-recommended-users) | Hides the 'Who to follow' recommendations in the timelines. |  |
| [Hide suggested content](#hide-suggested-content) | Hides the suggestion modules X injects into timelines: communities to join, related posts under a post, Today's news stories and the top people module in search. |  |
| [Hide view count](#hide-view-count) | Hides the view count of posts. |  |
| [Remove premium upsell](#remove-premium-upsell) | Removes the premium upsell sheets, the premium prompts in timelines and the upsell cards in the drawer and on profiles. |  |
| [Sanitize sharing links](#sanitize-sharing-links) | Removes the tracking query parameters from shared links. X 12.30 and later no longer add them. |  |
| [Show sensitive media](#show-sensitive-media) | Shows media marked as sensitive directly, without the warning overlays. |  |
| [Unlock downloads](#unlock-downloads) | Unlocks the ability to download any video. GIFs can be downloaded via the menu on long press. |  |

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
