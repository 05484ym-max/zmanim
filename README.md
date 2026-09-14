# זמנים בנעילה — Android live wallpaper

Live wallpaper: an engraved brass/bronze zmanim plaque (analog clock, 8-cell
zmanim grid, weekly parsha, daily hilula) over either a photo you pick or a
vintage Jerusalem-postcard scene by default. Redraws every second so the
clock's second hand actually moves.

## Project layout

```
app/src/main/java/com/zmanim/lockscreen/
  data/      ZmanimSettings.kt          - SharedPreferences: location + optional background photo URI
  zmanim/    ZmanimProvider.kt          - wraps the KosherJava zmanim library
  wallpaper/ ZmanimWallpaperService.kt  - WallpaperService/Engine, redraw scheduling, day-data caching
             GlassCard.kt               - the brass plaque: clock, grid, parsha/hilula lines
             SkyPalette.kt              - fallback-background palette (sky/silhouette/dome/olive) by time of day
             VintageScene.kt            - draws the fallback background art
             GrainTexture.kt            - film-grain overlay tile (fallback background only)
  ui/        SettingsActivity.kt        - city picker, GPS button, background-photo picker, "set as wallpaper"
```

## Building it

CI compiles the debug APK on every push (`.github/workflows/android-build.yml`).
Two links:
- **Stable install link** (always the latest `main`, never changes):
  `https://github.com/05484ym-max/zmanim/releases/download/nightly/app-debug.apk`
- Per-run build: [Actions tab](https://github.com/05484ym-max/zmanim/actions) → latest run → Artifacts.

The repo is private, so downloading either link requires being signed in to
GitHub as an account with access, in the same browser.

To work on the code: open this folder in Android Studio (Koala+), let it
generate the Gradle wrapper jar on first sync (the wrapper *properties* file
is committed, not the binary jar), then run on a device/emulator, API 26+.

## What's real

- **Zmanim calculation** — via `com.kosherjava:zmanim`, confirmed compiling
  in CI. Alos, netz, sof zman shma (GRA), sof zman tefila (GRA), chatzos,
  mincha gedola, mincha ketana, plag hamincha, shkia, tzais.
- **Hebrew date, weekly parsha, candle lighting, Rosh Chodesh countdown,
  curated daily hilulot** — all real, computed in `ZmanimProvider` from
  `JewishCalendar`/`HebrewDateFormatter`. Candle lighting is the upcoming
  Friday's sunset minus 20 minutes; Rosh Chodesh scans forward day-by-day
  (capped at 35 days); the hilula line falls back to the Rosh Chodesh
  countdown on days with neither.
- **Background** — a user-picked photo (persisted permission, center-cropped)
  if one is chosen in settings; otherwise a vintage Jerusalem scene whose
  palette shifts with sunrise/sunset.
- **Redraw scheduling** — every second (for the live second hand), but the
  actual astronomical/Jewish-calendar computation is cached and only redone
  once a day or on a location change — see `dayFor()` in
  `ZmanimWallpaperService`. Drawing stops entirely while the wallpaper isn't
  visible.
- **GPS location** — reads the last known location; no active location
  request or reverse-geocoded city name.

## History note

This repo briefly had two parallel, conflicting implementations after some
work was done directly on GitHub outside this session (`GlassCard.kt` and
`TransparentGlassCard.kt`, only one of which was actually wired into the
wallpaper service) — the current `GlassCard.kt` consolidates that into one
renderer matching the approved reference design, and the duplicate file is
gone. If you're driving changes through an AI tool with direct GitHub
access, pushing straight to `main` from two places at once is how that
happened — worth avoiding.

## Not included (by design, to match the approved reference exactly)

- No swipe-to-browse-other-days gesture, no next-zman box, no 7-day date
  strip, no next-zman gold highlight in the grid — the reference image has
  none of these, so they were dropped for one-to-one fidelity rather than
  kept as hidden/undiscoverable features.
