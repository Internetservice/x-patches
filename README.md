# 🧩 X Patches

Patches for **X (formerly Twitter)** for use with [Morphe](https://morphe.software).

## ❓ About

These patches are a port of the X / Twitter patches from
[ReVanced Patches](https://github.com/ReVanced/revanced-patches)
([GitLab mirror](https://gitlab.com/revanced/revanced-patches)) to the Morphe patcher,
so they can be applied with Morphe Manager or Morphe Desktop.

Included patches:

- **Hide ads** - hides promoted posts in the timelines.
- **Hide recommended users** - hides the "Who to follow" recommendations in the timelines.
- **Customize sharing link** - changes the domain used when sharing links (for example FxTwitter) and
  optionally includes the username in the link.
- **Sanitize sharing links** - removes the tracking query parameters from shared links.
- **Unlock downloads** - unlocks the ability to download any video. GIFs can be downloaded via the menu on long press.
- **Dynamic color** - replaces the X blue with the Material You palette (Android 12+).
- **Hide view count** - hides the view count of posts (off by default).

### How to use these patches

Click here to add these patches to Morphe: https://morphe.software/add-source?github=Internetservice/x-patches

Or in Morphe Manager open *Patch sources*, add a GitHub source and enter this repository.
Pre-releases from the `dev` branch can be used by enabling *pre-release* on the source.

## 🩹 Patches list

<!-- PATCHES_START EXPANDED -->

#### A list of the patches will automatically be shown here after the first release is created.

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
[ReVanced Patches](https://github.com/ReVanced/revanced-patches), also licensed under the GPLv3.
The project layout and release tooling come from the
[Morphe patches template](https://github.com/MorpheApp/morphe-patches-template), see [NOTICE](NOTICE).
This project is not affiliated with Morphe or ReVanced.
