v2.2.8 (pre-release) — 2026-11-07 (re-cut #4)

Fixes in this build (supersedes the earlier v2.2.8 assets):

- Amazon Shopping / Amazon India AMOLED Dark mode: the resource pass no
  longer writes Material attribute names the reandroid resource encoder
  cannot re-resolve. Root-caused on a real build failure:
  XmlEncodeException at res/values/styles.xml line 58 —
  "Unknown attribute name: <item name=\"colorSurface\">#ff000000</item>"
  on Amazon mShop 32.19.0.300. Theme-item writes are now restricted to
  android: framework attributes (with a hard guard); Material surfaces go
  black via the <color>-token pass and the WebView CSS. Unverified on device.

- Mereframe diagnostics (realme RMX5110 / Android 16 field crash,
  "app integrity check failed" at camera preview):
  - the diagnostic crash log now prints the app's versionName/versionCode
    in the report header;
  - the "Bypass native integrity checks" patch now logs when its catch is
    planted. The reported crash frame is exactly the frame this patch wraps,
    so its absence from the field build means it was not effective there
    (version mismatch/stale bundle). See docs/MEREFRAME.md for the checklist
    (bundle must report v2.2.8; APK must be 0.8.4/vc118; Allow re-signed +
    Bypass on; Unlock Pro OFF).

Unchanged from previous re-cuts: Disable video autoplay (ExoPlayer/Media3)
universal heuristic; Mint/HT app-scoped entries (Block analytics & tracking
SDKs, Disable video autoplay (ExoPlayer), Prefer external browser over
Custom Tabs); Indian-market tracker markers; the Mint & Hindustan Times
analysis doc; all v2.2.7 content (Amazon AMOLED, Reddit listing performance,
Brave patches, Amazon listing perf fix).

Known limitations: everything remains unverified on device; no binary-specific
Mint/HT patches until the APKs are provided; Premium unlocks are impossible
(server-side).

v2.2.4 remains the latest full release. All v2.2.5+ builds are pre-releases, unchanged.
