# Spotify Web Android Wrapper

A WebView-based Android wrapper for `https://open.spotify.com/`.

### Included

- API 37 / Android 16 project setup.
- JavaScript, DOM storage, cookies, and WebView media configuration.
- Media-playback foreground service + persistent notification.
- Adaptive launcher icon with a dark/green music mark.
- Your `Spotify 2.js` userscript preserved as `spotify_declutter.user.js`.
- Your `Spotify 1.js` helper preserved as `spotidown_helper.user.js`.
- External URLs and `window.open()` popup destinations are sent to the device browser.

### Note on background playback

The foreground service is the Android-side keep-alive mechanism. Actual Spotify Web Player playback still depends on Spotify Web Player/WebView compatibility and device battery-management rules, so this is not a universal guarantee on every phone.

### Build

Open this generated folder in current Android Studio and build the `app` module. The current Android Gradle Plugin 9.4 line uses Gradle 9.6 and JDK 17.
