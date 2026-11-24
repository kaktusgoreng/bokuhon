# Material 3 → foundation UI migration

Branch `bionic/no-material3`. Backup of the shipping alpha 2 build: tag `alpha2-backup`
(commit `18159ab39`, branch `bionic/alpha2`). Nothing is pushed.

## Done

- `presentation-core/.../bokuhon/` is the whole UI layer now: four-tone palette (paper #F7F7F7,
  mist #E3E3E3, ash #767676, ink #141414), typography scale, and drop-in primitives
  named/parameterised like the Material ones. Split by family: `BokuhonUi` (text, icons, dividers,
  buttons, icon buttons), `Chip`, `Checkbox`, `ProgressIndicators`, `ListItem`, `Surface` (+ cards),
  `Badge`, `Menu`, `Dialogs`, `Snackbar`, `TextField`, `Slider`, `SegmentedButton`,
  `FloatingActionButton`, `TopAppBar`, `Scaffold`, `Tabs`, `DatePicker`, `PullRefresh`, `Scrollbar`.
- `LocalBokuhonColors/Typography/TextStyle/ContentColor` replace `MaterialTheme` and its locals;
  every `MaterialTheme.colorScheme/typography/shapes` read (452) and `MaterialTheme.padding` (210)
  is gone. Shapes are square everywhere, elevation has no tint, ripples are off.
- Home rail + phone bottom bar hand-built; `material3-adaptive-navigation-suite` dependency removed.
- Migrated call sites: text 477, icons 139, dividers 41, icon buttons 57, buttons 136, chips 30,
  checkboxes 10, progress indicators 19, list items 6, plus every overlay and app bar.
- `implementation(libs.androidx.compose.material3)` removed from `app` and `presentation-core`; the
  Material theme wrapper and `LocalRippleConfiguration` bridge are gone.
- Notes use `BasicRichText`/`BasicRichTextEditor`; the licences screen is a plain list over
  AboutLibraries data; `materialKolor` (unused) dropped.
- Dropdowns size to their content (`IntrinsicSize.Max`) like Material's did. Without it a menu
  opened from inside a dialog measured at window width, which pinned it to the window edge.
- Nothing animates, for the e-ink panel: `components/motion` holds no-op stand-ins for the Compose
  animation APIs, progress indicators are static, tabs call `scrollToPage` and pagers snap. Dialogs
  and sheets draw in the activity window through `BokuhonDialogHost`, because a dialog *window* is
  scaled and faded in by the window manager and the app can't switch that off.

## Remaining

No app source imports or calls Material 3, and the release APK contains no
`androidx.compose.material3` classes at all — R8 strips what the libraries below don't use. Two
libraries still declare the artifact at compile time: `richeditor-compose` (its own Material 3 UI
classes) and `aboutlibraries-compose-m3`. Removing it from the graph means replacing those two
libraries; switching AboutLibraries to the `aboutlibraries-compose` (core) module is the cheap
half, but it needs a `gradle/libs.versions.toml` edit.

Known pre-existing issue: the manga action row wraps its labels ("In / library") on a half-width
pane because the new buttons hard-code 16dp horizontal padding and ignore `contentPadding`.

## Build / run

```
export JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home
export ANDROID_HOME=/opt/homebrew/share/android-commandlinetools
./gradlew check                     # must stay green
./gradlew :app:assembleRelease      # app/build/outputs/apk/release/app-arm64-v8a-release.apk
emulator -avd boox_go6_gen2         # 1072x1448 @300dpi, Android 13, tablet mode
adb install -r app/build/outputs/apk/release/app-arm64-v8a-release.apk
```

`app.mihon.dev` on the emulator is the stock 0.21.3-8251 build, not alpha 2; use the
`alpha2-backup` tag for A/B screenshots.
Release APK is debug-signed; version stays `0.21.3_alpha2` (bump when a new alpha is cut).
