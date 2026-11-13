Proteus Patches v2.3.0 (pre-release re-cut)

> Supersedes the earlier `v2.3.0` build with Meesho package targeting fixes, Amazon Business + WebView AdBlock/autoplay + screenshot-detection fixes, and Mereframe `0.8.4` (`vc 118`) BTI/PAC-safe native `.so` integrity and Pro patches. No on-device runtime testing has been performed in this re-cut — marked as **pre-release**; `v2.2.9` remains the latest full release.

## Changes in this re-cut

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
- CI compiled `:extensions:extension` and `:patches:buildAndroid`, passed `test_pinterest_video_urls.sh`, `:patches:testPermissionRemoval`, `:patches:testBravePrivacy`, `generatePatchesList`, `.github/scripts` Python tests, `verify_patch_source.py`, and `verify_android_bundle.py`.
- Not yet tested on physical Android hardware.
