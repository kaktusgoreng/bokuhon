<div align="center">

# Bokuhon

### Mihon, rebuilt for e-ink

Bokuhon is a fork of [Mihon](https://github.com/mihonapp/mihon) for e-ink Android e-readers —
Boox, Bigme, Meebook, Hisense, and anything else that draws with slow, monochrome pixels.
It keeps Mihon's features and rebuilds the shell around them for speed and legibility on an
e-ink panel, and it talks to the panel itself where the vendor allows it.

[![License: Apache-2.0](https://img.shields.io/github/license/kaktusgoreng/bokuhon?labelColor=27303D&color=0877d2)](/LICENSE)

</div>

## What makes it different

* **Nothing animates.** No menu, dialog, page, toolbar or tab transitions. Screens, menus and
  dialogs appear and disappear instantly, because a repaint on e-ink costs a full flash.
* **Four shades of grey only.** No colour anywhere except covers and other content images.
  Selection is white on black, not a tint, so it survives a panel with poor contrast.
* **Square, outlined controls.** Buttons, toggles, menus, dialogs and bars have square corners
  and black outlines that match Boox's own UI, with no shadows or elevation.
* **Fewer, lighter layers.** Material 3 is gone; the UI is drawn with a small set of its own
  composables on the Android SDK, which also brings the APK size down.
* **Screen-aware layout.** Any window that is near square counts as a tablet, so the two-pane
  layout follows the device rather than a fixed size.
* **Fewer taps, denser settings.** Choices with more than a couple of options — reading mode,
  rotation, tap zones, scale type, zoom start — are one row showing the current value, with the
  choices dropping down under it, instead of a wall of chips.
* **The panel's own refresh on BOOX.** On BOOX hardware the reader asks the panel for a real full
  refresh after a page change instead of drawing a black flash over the page, and the reader bar
  gets a front-light switch. Both are detected at runtime and do nothing anywhere else.

## On BOOX hardware

BOOX is the only e-ink vendor with a published interface an app can use, so it is the only
place these features exist. Everything is detected rather than assumed, and every call fails
quietly:

* **Full refresh after a page change** (`Settings → Reader → E-Ink → Use BOOX refresh modes`,
  on by default). Replaces the app's drawn flash; nothing is drawn, the panel does the work. The
  cadence is `Flash every … pages` in the same group.
* **Front-light switch** in the reader's bottom bar, between crop borders and settings: filled
  black with a white bulb when the light is on, a crossed black bulb on paper when it is off.
  It only appears on a device that has a light the app can reach.

Neither touches the panel's resting refresh mode or the per-app settings in BOOX's App
Optimisation; those stay yours. On any other device — including the Go 6 emulator — the app
behaves exactly as it does without them.

## Requirements

* Android 8.0 or higher.
* Any Android e-reader; BOOX is needed only for the two panel features above.

## Building

```sh
./gradlew :app:assembleRelease
```

The APK lands in `app/build/outputs/apk/release/`. A `JAVA_HOME` with JDK 21 and an
`ANDROID_HOME` with the Android SDK are all that is needed.

## Who works on it

This repository is edited by **DeepSeek**, an AI model, and supervised by
**[Kaktus Goreng](https://github.com/kaktusgoreng)**, who decides what gets built, tests every
change on real e-ink hardware, and owns the commits that land here.

## Releases

Builds are attached to the repository as APKs at the root, named for the version they carry:
`bokuhon-0.21.3_beta3.apk` is the current one, for `arm64-v8a` devices (every BOOX of recent
years). Install it with `adb install -r bokuhon-0.21.3_beta3.apk` or by copying it to the device.

## Credits

Bokuhon exists because of [Mihon](https://github.com/mihonapp/mihon) and its contributors, who
built the app this fork started from — the reader, the sources, the trackers, the library and
everything behind them. Thank you, all of you:

<a href="https://github.com/mihonapp/mihon/graphs/contributors">
    <img src="https://contrib.rocks/image?repo=mihonapp/mihon" alt="Mihon app contributors" title="Mihon app contributors" width="800"/>
</a>

Mihon itself descends from Tachiyomi, and this fork carries that lineage with it.

## Disclaimer

Bokuhon hosts no content and has no affiliation with any content provider. What you read with
it, and where it comes from, is up to you.

## License

Bokuhon is distributed under the **Apache License, Version 2.0**, the same licence as Mihon.
The full text is in [LICENSE](/LICENSE).

It bundles two third-party libraries for the BOOX features, both compatible with that licence:

* **onyxsdk-device** `com.onyx.android.sdk:onyxsdk-device:1.3.7` (Onyx International),
  BSD-3-Clause — the vendor SDK behind the panel refresh and the front-light switch. It does not
  ship a copyright notice of its own; the licence is the one published with the artefact on
  `repo.boox.com`.
* **hiddenapibypass** ([LSPosed](https://github.com/LSPosed/AndroidHiddenApiBypass)),
  Apache-2.0 — lets those vendor calls past the hidden-API restrictions Android puts on apps.

The icons are [Material Symbols](https://fonts.google.com/icons) (Google LLC, Apache-2.0).

This is a modified version of Mihon: files have been added, removed and changed throughout the
tree, and every file retains its original copyright header. Where Mihon code was copied or
adapted, the source and licence are named in the file header. The upstream notices are kept
below and apply to the parts of this project that come from Mihon and Tachiyomi:

<pre>
Copyright © 2015 Javier Tomás
Copyright © 2024 Mihon Open Source Project
Copyright © 2026 Kaktus Goreng

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
</pre>
