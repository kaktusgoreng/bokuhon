# Material 3 → foundation UI migration

Branch `bionic/no-material3`. Backup of the shipping alpha 2 build: tag `alpha2-backup`
(commit `18159ab39`, branch `bionic/alpha2`). Nothing is pushed.

## Done

- `presentation-core/.../bokuhon/BokuhonUi.kt`: four-tone palette (paper #F7F7F7, mist #E3E3E3,
  ash #767676, ink #141414), typography scale, and drop-in primitives named/parameterised like the
  Material ones: `Text` (String + AnnotatedString), `Icon` (Painter + ImageVector),
  `HorizontalDivider`, `VerticalDivider`, `IconButton`, `FilledIconButton`, `Button`, `TextButton`,
  `OutlinedButton`, `BokuhonBadge`.
- Home rail + phone bottom bar hand-built; `material3-adaptive-navigation-suite` dependency removed.
- Migrated call sites: text 477, icons 139, dividers 41, icon buttons 57, buttons 136 → ~860.
- `Text`/`Icon` still read Material's `LocalContentColor` as a bridge while Material owns the theme.
- `Badges.kt` and `ChapterTransition.kt` stay on Material (need `AnnotatedString` + `inlineContent`).

## Remaining (184 files import material3)

| Group | Sites | Nature |
|---|---|---|
| FilterChip | 27 | mechanical |
| Progress indicators | 20 | mechanical |
| Checkbox | 9 | mechanical |
| ListItem | 6 | mechanical |
| Scaffold | 45 | behaviour: insets |
| DropdownMenu + items | 53 | behaviour: popup anchoring, dismissal |
| AlertDialog | 40 | behaviour: window, back |
| OutlinedTextField | 10 | behaviour: IME, selection |
| Snackbar / sheet / tab | ~25 | behaviour |
| MaterialTheme token reads | 143 | mechanical, wide |

## Next steps (in order)

1. Mechanical batch: chips, checkbox, progress, list items → compile, screenshot, commit.
2. Overlays one kind at a time (menu → dialog → snackbar → scaffold → text field), screenshot after each.
3. Replace `MaterialTheme.colorScheme/typography/shapes` reads with the Bokuhon locals.
4. Delete `implementation(libs.androidx.compose.material3)`, drop the Material theme wrapper and
   `LocalRippleConfiguration` bridge, final screen-by-screen diff against `alpha2-backup`.

## Build / run

```
export JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home
export ANDROID_HOME=/opt/homebrew/share/android-commandlinetools
./gradlew check                     # must stay green
./gradlew :app:assembleRelease      # app/build/outputs/apk/release/app-arm64-v8a-release.apk
emulator -avd boox_go6_gen2         # 1072x1448 @300dpi, Android 13, tablet mode
adb install -r app/build/outputs/apk/release/app-arm64-v8a-release.apk
```

Alpha 2 backup build is installed alongside as `app.mihon.dev` for A/B screenshots.
Release APK is debug-signed; version stays `0.21.3_alpha2` (bump when a new alpha is cut).
