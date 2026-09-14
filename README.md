# זמנים בנעילה — Android live wallpaper

Live wallpaper that redraws a zmanim (halachic day times) glass card once a
minute, over a vintage Jerusalem-postcard scene that shifts with the time of
day - dawn/day/dusk/night palettes, a city-wall/dome silhouette, cypress
trees, an olive branch, film grain, and a real (cheap downscale/upscale)
backdrop blur behind the card.

## Project layout

```
app/src/main/java/com/zmanim/lockscreen/
  data/      ZmanimSettings.kt          - SharedPreferences: which location is selected
  zmanim/    ZmanimProvider.kt          - wraps the KosherJava zmanim library
  wallpaper/ ZmanimWallpaperService.kt  - WallpaperService/Engine + redraw scheduling
             SkyPalette.kt              - full vintage palette (sky/silhouette/dome/olive/duotone) by time of day
             VintageScene.kt            - draws the background art
             GrainTexture.kt            - film-grain overlay tile
             GlassCard.kt               - blurred glass card + zmanim rows, next zman in gold
  ui/        SettingsActivity.kt        - city picker, GPS button, "set as wallpaper" button
```

## Building it

CI compiles the debug APK on every push (`.github/workflows/android-build.yml`)
— check the [Actions tab](https://github.com/05484ym-max/zmanim/actions) for
the latest run and download the `zmanim-lockscreen-debug` artifact if you just
want an installable APK without setting up Android Studio at all.

To work on the code:

1. Open this folder in Android Studio (Koala or newer). It will offer to
   generate the Gradle wrapper jar on first sync — accept it (the wrapper
   *properties* file is committed, but not the binary jar).
2. Sync Gradle, then Run on a device/emulator running API 26+.
3. The app itself is just the settings screen. To see the wallpaper: open it,
   pick a city, tap **הגדר כטפט חי** — that hands off to the system's live
   wallpaper picker with this wallpaper pre-selected.

## What's real vs. stubbed

- **Zmanim calculation** — real, via `com.kosherjava:zmanim`, and confirmed
  compiling in CI: `ComplexZmanimCalendar`, `GeoLocation`, `JewishCalendar`
  and `HebrewDateFormatter` are all being used correctly as of the pinned
  `2.5.0` version.
- **Hebrew date** — real, via `HebrewDateFormatter`.
- **Background time-of-day gradient** — real, driven by today's actual
  sunrise/sunset.
- **Redraw scheduling** — real: redraws every 60s, and only while the
  wallpaper is actually visible (stops when the screen is off / another app
  is in front), so it isn't burning battery in the background.
- **Card visuals** — the full mockup layout: title + location, an analog
  clock (real hour/minute hands from the device clock), a 7-day date strip
  with today highlighted, a "הזמן הבא" (next zman) box, an 8-cell zmanim grid
  with the upcoming one highlighted gold and past ones dimmed, and a
  Shabbat-candle-lighting / Rosh-Chodesh row. All over a real blurred
  backdrop, warm tint and border. The blur is a manual downscale/upscale
  trick (`GlassCard.drawBlurredBackdrop`), not `RenderEffect.createBlurEffect`
  (API 31+ only) — chosen so it works on the full minSdk 26 range.
- **Candle lighting / Rosh Chodesh** — real: candle lighting is the upcoming
  Friday's sunset minus 20 minutes (today's, if today is Friday); Rosh
  Chodesh searches forward day-by-day via `JewishCalendar.isRoshChodesh`
  (capped at 35 days) and names the month via `HebrewDateFormatter.formatMonth`
  — both written from memory like the rest of `ZmanimProvider`, so double
  check them the same way once CI (or Android Studio) compiles this.
- **GPS location** — real but minimal: reads the last known location, no
  active location request/geocoded city name.
- **App/launcher icon** — placeholder vector, not final branding.

## Deliberate deviations from the mockup

- The mockup's zmanim strip was 5 icons in one row (it dropped a few times to
  fit); this keeps all 8 real values as a 4×2 icon grid instead, so nothing
  computed goes unshown.
- The "tagline" flourish line under the card isn't included — decorative only,
  easy to add back later if wanted.

## Performance note

`ZmanimWallpaperService.render()` allocates a full-screen ARGB_8888 bitmap
and does the blur downscale/upscale on the main thread once a minute — fine
at that cadence, but if this ever needs to redraw more often, move that work
to a background thread and post the finished bitmap instead.
