# זמנים בנעילה — Android live wallpaper skeleton

Live wallpaper that redraws a zmanim (halachic day times) card once a minute,
with the background shifting by time of day. This is the **structural**
skeleton — the visuals are a plain rounded card, not the vintage design from
the mockup. Swap `ZmanimWallpaperService.drawCard()` (and `SkyPalette`) for
the real art direction once it's settled; everything else here stays as-is.

## Project layout

```
app/src/main/java/com/zmanim/lockscreen/
  data/      ZmanimSettings.kt      - SharedPreferences: which location is selected
  zmanim/    ZmanimProvider.kt      - wraps the KosherJava zmanim library
  wallpaper/ ZmanimWallpaperService.kt - the WallpaperService + Canvas rendering
             SkyPalette.kt          - background gradient by time of day
  ui/        SettingsActivity.kt    - city picker, GPS button, "set as wallpaper" button
```

## Opening it

1. Open this folder in Android Studio (Koala or newer). It will offer to
   generate the Gradle wrapper jar on first sync — accept it (the wrapper
   *properties* file is committed, but not the binary jar).
2. Sync Gradle, then Run on a device/emulator running API 26+.
3. The app itself is just the settings screen. To see the wallpaper: open it,
   pick a city, tap **הגדר כטפט חי** — that hands off to the system's live
   wallpaper picker with this wallpaper pre-selected.

## What's real vs. stubbed

- **Zmanim calculation** — real, via `com.kosherjava:zmanim`. The method
  names in `ZmanimProvider` were written from memory, not compiled here (no
  Android SDK in this environment) — double-check them against the pinned
  library version's javadoc on first build; if `ComplexZmanimCalendar`,
  `GeoLocation`, `JewishCalendar` or `HebrewDateFormatter` have moved/renamed
  a method, the fix is local to that one file.
- **Hebrew date** — real, via `HebrewDateFormatter`.
- **Background time-of-day gradient** — real, driven by today's actual
  sunrise/sunset.
- **Redraw scheduling** — real: redraws every 60s, and only while the
  wallpaper is actually visible (stops when the screen is off / another app
  is in front), so it isn't burning battery in the background.
- **Card visuals** — placeholder. No blur, no grain, no gold accents, no
  glassmorphism yet — just a translucent rounded rectangle with text rows, so
  the pipeline (data → drawing) is provable before investing in the finish.
- **GPS location** — real but minimal: reads the last known location, no
  active location request/geocoded city name.
- **App/launcher icon** — placeholder vector, not final branding.

## Next steps (design pass)

Once we lock the visual direction from the mockup, `drawCard()` is where it
lands: rounded card background → gradient/blur, add the olive-branch and
Jerusalem-skyline background art, the analog clock, the date strip, the
Shabbat/Rosh Chodesh row, film-grain overlay, etc. `RenderEffect.createBlurEffect`
(API 31+) is the real backdrop-blur primitive for the glass-card look on
device — for API 26–30 fall back to a plain translucent fill (already what
this skeleton does).
