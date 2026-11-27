# Handoff — Bokuhon `main`, at 0.21.3_beta4

Written 2026-11-27. Read this, then `AGENTS.md` (repo conventions). For BOOX/e-ink background see
`HANDOFF-eink.md` and `EINK-DEVICES.md`, which live in the workspace root one level above this repo
(they are reference notes, not part of the build).

## What this project is

Bokuhon is a fork of Mihon (Tachiyomi) for Android e-readers, stripped to a four-grey, square,
animation-free UI and tuned for e-paper speed. Upstream code is Apache-2.0; the README credits
Mihon and its contributors.

## Where the work happens

- **`main/`** — the Git working copy on branch `main`, pushed to
  `https://github.com/kaktusgoreng/bokuhon`. **Work here and only here.**
- **`comix/`** — a different agent's copy on a Comix branch. Do not touch it.
- `HANDOFF.md` (this file) is in the repo. `HANDOFF-eink.md` and `EINK-DEVICES.md` sit in the
  workspace root, one level above it.

Build environment:

```sh
export JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home
export ANDROID_HOME=/opt/homebrew/share/android-commandlinetools
cd main && ./gradlew :app:assembleRelease     # APK: app/build/outputs/apk/release/app-arm64-v8a-release.apk
./gradlew check                               # required before committing
```

Rule from `AGENTS.md`: one concern per commit, `./gradlew check` first, comment non-obvious "why"
only, no gratuitous refactors.

## Devices

| Device | Serial | Notes |
|---|---|---|
| BOOX Go 6 Gen II (physical) | `3733d` | The real target. **USB drops constantly** — if `adb devices` loses it, ask the user to replug; never assume it is present. |
| `boox_go6_gen2` AVD | `emulator-5554` | 1072×1448 @ 300 dpi, same metrics as the Go 6, so tap coordinates match. |
| BOOX Palma AVD | — | Only when the user asks for it. |

The emulator is **not** e-ink as far as the app knows (`Build.MANUFACTURER` is Google), so it
exercises the *curl* path, while the Go 6 exercises the *instant* path. That split is how the two
page-turn behaviours are tested.

## Current state

- Version **0.21.3_beta4** (`versionCode = 44`) in `app/build.gradle.kts`.
- Working tree clean except three untracked APKs in the repo root
  (`bokuhon-0.21.3_beta2/3/4.apk`); those are deliberate local artefacts, not committed.
- `main` == `origin/main` at `675c8c8d6`.

Recent commits, newest first:

| Commit | What |
|---|---|
| `675c8c8d6` | Reading modes renamed: Comic / Manga / Vertical Page / Webtoon / Gapped Webtoon (English labels only) |
| `9652c0d71` | Fix: e-paper page turn posted out of the touch event (see gotchas) |
| `ca7202a14` | Bump to beta 4 |
| `f30683a51` | E-paper pages swap instantly, everything else curls |
| `b4e0d8473` | `TabbedDialog` tab row white |
| `451a6095d` | Reader dropdowns: hug, right-align, ink selection |
| `ddcd5d733` | Reader choices became a dropdown list |

## The feature just built: page turns (beta 4)

Two behaviours, chosen by device — **not** by preference:

1. **E-ink → instant.** A horizontal (or, for the vertical pager, vertical) drag is taken away from
   `ViewPager` on the first movement past the touch slop, and the page is swapped with no scroll.
   Nothing follows the finger, because every intermediate frame would ghost.
2. **Everything else → page curl.** The outgoing page is snapshotted, the incoming one too, and a
   sheet is folded back about a crease that follows the finger: flat part stays, the folded part is
   drawn mirrored as the back of the paper with a gradient, the gap shows the incoming page, plus a
   shadow on it. Release past ~30% or a flick finishes the fold; otherwise the page falls back.
   The pager does not move until the fold is done, so an abandoned curl never turns the page.

Long strip (Webtoon / Gapped Webtoon) is untouched: the gesture only exists in `PagerViewer`.

### Files

| Path | Role |
|---|---|
| `core/common/.../util/system/DeviceUtil.kt` | `isEink` (new) matched on vendor names from `EINK-DEVICES.md`; `isOnyx` drives the BOOX SDK features. Lenovo and "Minimal" are excluded on purpose — too generic to match safely. |
| `app/.../reader/viewer/pager/Pager.kt` | `DragHandler` interface + `handleDrag()`: claims a drag from `ViewPager`, reports start/move/end with velocity. `pagerIsHorizontal` exposes the axis. |
| `app/.../reader/viewer/pager/PagerViewer.kt` | The handler itself: `onDragStart` → instant turn (posted) or `startCurl`; `turnPage`, `curlAnchor`, `curlTravel`, `startCurl`, `snapshot`, `holderFor`, `isPagePannable`, `isForwardSwipe`, `animateTurns`. `CURL_*` constants sit at the bottom of the file. |
| `app/.../reader/viewer/pager/PageCurlView.kt` | The curl drawing: `begin/ setProgress / animateTo / clear`, mirror matrix, gradient shading, shadow. Recycles its bitmaps. |
| `app/.../reader/viewer/webgpu/WebGpuViewer.kt` | The HQ renderer's transitions forced to `TransitionNone` on e-ink. |

### Gotchas worth keeping

- **Never move the pager from inside a touch event.** `setCurrentItem` called inside
  `onInterceptTouchEvent` left `ViewPager` to settle a page of its own, so one swipe turned two pages
  on the Boox. It is `pager.post { turnPage(...) }` now — keep it that way.
- The curl uses **snapshots of the page holders**, not the pager, so the pager can stay put. If a
  snapshot fails the gesture falls back to the normal pager slide rather than drawing a hole.
- A **zoomed page keeps its drag** (`isPagePannable`), so panning still works.
- `PageCurlView.animateTo` uses a generation token so a replaced animation cannot fire its callback
  (that bug stranded the curl on screen once).
- Release builds drop `logcat { }` output — for on-device diagnosis, use `android.util.Log` and
  remove it before committing.

## Open threads

1. **Install beta 4 on the physical Go 6.** The label-rename build was never installed there: the
   device dropped off USB. `adb -s 3733d install -r bokuhon-0.21.3_beta4.apk` when it is back, then
   confirm the labels and the instant turns on the panel.
2. **Two-page ("centre") curl is wired but unreachable.** `PageCurlView` takes an `anchor` fraction
   (0.5 = gutter) and `curlAnchor()` is the single place to feed it, but the classic paged viewer has
   no two-page spread. The HQ renderer (`highQualityRenderer`, default **off**) does have a dual page
   view, and its transitions come from the `ca.mpreg.webgpuviewer` library, which has no curl — and
   its drag still follows the finger on e-ink. If the user wants centre curls, that renderer is the
   place to look, and it is a much bigger job.
3. **Non-English labels still say "paged / long strip".** Only the base (English) strings were
   renamed. The user asked for exactly that, but offered to extend it — ask before touching the
   other `moko-resources/<locale>` files.
4. **BOOX front light tint is still unverified on the panel.** Known from the earlier handoff: the
   tint problem is blocked on the user setting their warmth once in BOOX's own light panel so the app
   can learn it.

## How to drive the app for testing

Reader UI taps (identical on both devices, 1072×1448):

```sh
A=$ANDROID_HOME/platform-tools/adb
$A -s 3733d shell am start -n app.mihon/eu.kanade.tachiyomi.ui.main.MainActivity
sleep 6
$A -s 3733d shell input tap 280 660      # first manga in the library
sleep 5
$A -s 3733d shell input tap 965 1240     # "Start reading"
sleep 12                                 # first load is slow
$A -s 3733d shell input tap 536 700      # toggles the reader menu (page counter bottom right)
$A -s 3733d shell input tap 913 1318     # settings gear, when the menu is up
```

**Curl check (emulator):** capture mid-gesture — the fold only exists while the finger is down.

```sh
($A -s emulator-5554 shell input swipe 900 700 250 700 2000 &) ; sleep 0.9
$A -s emulator-5554 exec-out screencap -p > /tmp/curl.png
```

**Instant check (Go 6):** swipe and read the page counter before/after; it must move by exactly one.

```sh
$A -s 3733d shell input tap 536 700                              # menu, note the counter
$A -s 3733d shell input tap 536 700                              # close it
$A -s 3733d shell input swipe 900 700 250 700 1000
sleep 3; $A -s 3733d shell input tap 536 700                     # menu again, counter must be +1
```

**To exercise the e-ink path on the emulator**, temporarily make `DeviceUtil.isEink` return `true`,
build, test, and **revert it before committing** (it was done once already; the tree must not keep it).

Screenshots come back at 1072×1448; `attach_file`/`view_images` render them fine.

## The house style (accumulated from the user)

- E-ink UI: no animations at all, square controls, 1 dp black outlines, four shades of grey, selected
  = white on black, no shadows, no rounded corners, no ripple-y chrome.
- Settings are dropdown rows that hug their value; the menu drops from the row, right-aligned, chosen
  option inked; a multi-select row says "Multiple".
- No pull-to-refresh; an explicit refresh button that inverts while busy.
- Text sizes/paddings are tuned by eye on the panel; expect to iterate with screenshots.
- Workflow: terse instructions, verify on the device with screenshots (not just the emulator), commit
  and push to `main` when done, bump the version only when asked. Do not create branches, do not push
  elsewhere, do not touch `comix/`.
