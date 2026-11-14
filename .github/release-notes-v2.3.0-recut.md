Proteus Patches v2.3.0 (pre-release re-cut)

> Supersedes the earlier `v2.3.0` pre-release re-cut by adding the **Instagram settings, Ghost Mode, Reels controls, like-animation and browser-tool patches** on top of the Instagram `439.0.0.37.89` suite, the Meesho package targeting fixes, Amazon Business + WebView AdBlock/autoplay + screenshot-detection fixes, and Mereframe `0.8.4` (`vc 118`) BTI/PAC-safe native `.so` integrity and Pro patches. No on-device runtime testing has been performed in this re-cut — marked as **pre-release**; `v2.2.9` remains the latest full release.

## Changes in this re-cut

### Instagram (`com.instagram.android` `439.0.0.37.89`, APKM)
Added 23 patches (`ARM64_V8A: 384510827`, `ARMEABI_V7A: 384510838`), 4 shared helpers, and 15 extension classes under `app.template.extension.extension` (10 new):
- **Distraction-Free (17 patches)**: `Hide ads`, `Hide suggested content` (`hideSuggestedReels`, `hideSuggestedStories`, `hideHighlightStories`, `hideSuggestedAccount`), `Limit feed to following profiles`, `Hide explore feed`, `Hide navigation buttons` (`hideHome`, `hideReels`, `hideDirect`, `hideSearch`, `hideProfile`, `hideCreate`), `Disable Reels scrolling`, `Disable video autoplay`, `Disable story auto flipping`, `Hide all stories`, `Hide feed content`, `Hide notes tray`, `Hide Instants`, `Hide reshare button`, `Hide Reels save button`, `Hide Threads profile button`, `Reels playback speed menu`, and `Reels double-tap seek`.
- **Misc (6 patches)**: `Bypass signature check`, `Remove build expired popup`, `Clone` (package/app-name renaming with `CONST_STRING` + `CONST_STRING_JUMBO`, permissions, `android:taskAffinity`, and provider authority rewriting), `Proteus settings`, `Ghost mode`, and `Change like animation` (experimental, off by default).
- **Proteus settings**: Adds a gear and a ghost button to the main feed action bar. The programmatic dialog controls the chronological feed, Ghost Mode sub-switches, Reels toggles, the like-animation picker, and three WebView tools. Settings live in the app-private `proteus_instagram_settings` file.
- **Ghost mode**: Hides typing indicators (`85450::68`, `85450::69`, `115516::0`, `115516::1`, `99511::0`), suppresses the `mark_thread_seen-` DM receipt path, and best-effort blocks the story seen endpoint in the Tigon request layer.
- **Reels controls**: `Reels playback speed menu` enables `109947::0` / `109947::2`; `Reels double-tap seek` enables `118405::1` / `118405::5` / `118405::6`. Both are switchable at runtime and stay on by default.
- **Chronological feed**: `Limit feed to following profiles` now also disables `p92_android_ranked_following_feed:is_enabled` (`108494::0`) and honours the runtime toggle. Runtime MobileConfig overrides are applied only to flags registered by a selected patch.
- **Like animation**: Experimental port of piko's enum-to-native-renderer bridge with piko's animation list and a random-per-process choice.
- **Like animation — register-pressure fix**: The patch previously failed to apply with `app.morphe.patcher.patch.PatchException: No free registers available` (`FreeRegisterProvider.getFreeRegister`), because `XDTUserActivationMetadataImpl.<init>` is register saturated on `439.0.0.37.89` and the patch asked for two scratch registers. It now requests a single 4-bit register (required by the `move-result-object` 11x instruction), carries the `Boolean` "explicit choice" flag in the `p2` parameter register and writes the selected animation back into the `p1` parameter with `move-object/from16` — the same approach as upstream piko. The original animation is preserved when no animation is selected, and `InstagramLikeAnimation.changeLikeAnimation` returns the incoming default instead of `null` on error.
- **Browser tools**: In-app WebView entry points for logged-out public browsing, a profile's public Reels tab, and your own liked posts.
- **Attributions**: Added GPLv3 §7(b) notices in `META-INF/PROTEUS-NOTICE.txt` for `brosssh/morphe-patches`, `brosssh/instagram-morphe-patches-library`, and `crimera/piko`.
- **Limits**: Another user's liked Reels and true logged-out native app use are server-enforced; the browser tools are the documented fallback. The action-bar, DM-seen and like-animation fingerprints are name/string anchored and not yet device-verified on `439.0.0.37.89`; treat the like-animation patch as experimental.

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
- CI compiled `:extensions:extension` and `:patches:buildAndroid`, passed `test_pinterest_video_urls.sh`, `test_instagram_extension.sh` (including the new runtime-flag, Ghost filter, MobileConfig gating, browser-URL and animation-label checks), `:patches:check` (`testPermissionRemoval`, `testBravePrivacy`, `testInstagram`), `generatePatchesList`, `.github/scripts` Python tests, `verify_patch_source.py`, and `verify_android_bundle.py`.
- `testInstagram` includes a regression test that rebuilds the register-saturated `XDTUserActivationMetadataImpl.<init>` frame, asserts the old two-register request fails with `No free registers available`, and asserts the single 4-bit register request succeeds.
- Not yet tested on physical Android hardware.
