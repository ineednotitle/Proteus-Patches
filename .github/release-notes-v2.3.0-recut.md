Proteus Patches v2.3.0 (pre-release re-cut)

> Supersedes the earlier `v2.3.0` pre-release re-cut by adding the **Instagram (`com.instagram.android` `439.0.0.37.89`)** patch suite alongside the Meesho package targeting fixes, Amazon Business + WebView AdBlock/autoplay + screenshot-detection fixes, and Mereframe `0.8.4` (`vc 118`) BTI/PAC-safe native `.so` integrity and Pro patches. No on-device runtime testing has been performed in this re-cut — marked as **pre-release**; `v2.2.9` remains the latest full release.

## Changes in this re-cut

### Instagram (`com.instagram.android` `439.0.0.37.89`, APKM)
Added 18 patches (`ARM64_V8A: 384510827`, `ARMEABI_V7A: 384510838`), 4 shared helpers, and 5 pure Android SDK extension classes under `app.template.extension.extension`:
- **Distraction-Free (15 patches)**: `Hide ads`, `Hide suggested content` (`hideSuggestedReels`, `hideSuggestedStories`, `hideHighlightStories`, `hideSuggestedAccount`), `Limit feed to following profiles`, `Hide explore feed`, `Hide navigation buttons` (`hideHome`, `hideReels`, `hideDirect`, `hideSearch`, `hideProfile`, `hideCreate`), `Disable Reels scrolling`, `Disable video autoplay`, `Disable story auto flipping`, `Hide all stories`, `Hide feed content`, `Hide notes tray`, `Hide Instants`, `Hide reshare button`, `Hide Reels save button`, and `Hide Threads profile button`.
- **Misc (3 patches)**: `Bypass signature check`, `Remove build expired popup`, and `Clone` (package/app-name renaming with `CONST_STRING` + `CONST_STRING_JUMBO`, permissions, `android:taskAffinity`, and provider authority rewriting).
- **Attributions**: Added GPLv3 §7(b) notices in `META-INF/PROTEUS-NOTICE.txt` for `brosssh/morphe-patches`, `brosssh/instagram-morphe-patches-library`, and `crimera/piko`.
- **Limits**: Viewing an arbitrary specific user's liked Reels and no-login browsing remain server-enforced and are not included. Not yet verified against an APK/APKM binary on physical hardware.

### Mereframe (`app.unnamed.rawrecording` `0.8.4` / vc `118`)
- **Bypass native integrity checks** (`#5` / PR `#33`): Patches all five ARM64 integrity check entry points in `lib/arm64-v8a/libunnamed_raw.so` (`isIntegrityOk@0x23f3d8`, `checkApkAssets@0x2a5ecc`, `checkMapsDbg@0x2bf304`, `checkSoMaps@0x2e5fb0`, `checkEnvSig@0x2e1b54`) using BTI/PAC-safe `bti jc; mov w0, #1; ret` stubs verified at globally unique 96-byte prologues, while keeping `MereframeIntegrityHook.openSessionWithFallback` active on `B1e6ad8077f6880e309d8da4b;->d(ZZZZFZ)[J`.
- **Unlock Pro features**: Unlocks `LQu;->k` & `LQu;->m`, all `Bf1c089a4a0abf0794ad7a6f9` license/session gates (`e`, `g`, `b`, `c`, `i`, `h`, `d` returning valid `Leq(1L, Lvy.u(p1))`), and native gates `verifySessionHandle@0x242048` + `isProEntitled@0x244680` in `libunnamed_raw.so`.

### Amazon (`com.amazon.mShop.android.shopping`, `com.amazon.mShop.android.business.shopping`, `in.amazon.mShop.android.shopping`)
- **Amazon Business + Shopping `32.19.0.100`** (`#21` / PR `#31`): Added `com.amazon.mShop.android.business.shopping` (`32.19.0.451`, `32.20.0.451`) across all Amazon patches and added Shopping `32.19.0.100`.
- **Disable screenshot detection**: New default-on patch neutralizing `Activity.registerScreenCaptureCallback` / `unregisterScreenCaptureCallback` with `nop` (avoiding single-instruction `try` block `VerifyError`).
- **Remove ads**: Expanded `AmazonHelper.AD_SELECTORS` to 102 unique CSS selectors (merging `hxreborn/amznkiller` selectors) and hooked `MShopWebViewContainer.onPageFinished` + `InteractionWebFragment.onFragmentPostShown`.
- **Disable video autoplay**: Added DOM/JS autoplay guard (`AmazonHelper.injectDisableVideoAutoplay`) alongside `setMediaPlaybackRequiresUserGesture(true)` across `MShopWebViewClient.onPageFinished`, `MShopWebViewContainer.onPageFinished`, and `InteractionWebFragment.onFragmentPostShown`.

### Meesho (`com.meesho.supply`)
- **Package ID fix** (`#32`): Updated Meesho target package from `com.meesho.shopping` to `com.meesho.supply` (`29.5`–`29.9`) across `Constants.kt`, scoped patch descriptions, `docs/MEESHO.md`, and generated catalog metadata.

## Verification
- CI compiled `:extensions:extension` and `:patches:buildAndroid`, passed `test_pinterest_video_urls.sh`, `test_instagram_extension.sh`, `:patches:check` (`testPermissionRemoval`, `testBravePrivacy`, `testInstagram`), `generatePatchesList`, `.github/scripts` Python tests, `verify_patch_source.py`, and `verify_android_bundle.py`.
- Not yet tested on physical Android hardware.
