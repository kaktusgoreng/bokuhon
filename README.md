<div align="center">

# Bokuhon

### Mihon, rebuilt for e-ink

Bokuhon is a fork of [Mihon](https://github.com/mihonapp/mihon) for e-ink Android e-readers —
Boox, Kindle-adjacent tablets, and anything else that draws with slow, monochrome pixels.
It keeps Mihon's features and rebuilds the shell around them for speed and legibility on an
e-ink panel.

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
* **Reader aimed at e-ink.** Static page-number badge that hides itself, centred loading
  indicator, no page-turn animation, and bars that show the system status and navigation bars.
* **Screen-aware layout.** Any window that is near square counts as a tablet, so the two-pane
  layout follows the device rather than a fixed size.
* **Follows the system language**, including when it is set in Mandarin and other locales.

## Requirements

* Android 8.0 or higher.
* Developed and tested against Boox hardware (Go 6 Gen II, Palma) in emulators.

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
