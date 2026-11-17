# Gemini patches — APK analysis & feasibility

Subject of analysis: `com.google.android.apps.bard` **1.0.795460806** (versionCode 272),
minSdk 28 / targetSdk 35, arm64-v8a, obtained from a Play "fused" export
(`com.android.dynamic.apk.fused.modules = base`, `com.android.vending.splits` present).

## What the APK actually is

Gemini on Play is a **dynamic-feature shell**:

- Base APK is ~5.4 MB, 2 DEX, ~5 500 classes — of which only **6** are
  `com.google.android.apps.bard.*` (application class, launcher entry activity,
  error activity, in-app-review activity, 2 widget receivers). Everything else is
  AndroidX + Google infrastructure libraries.
- The manifest declares **only three permissions**: `WAKE_LOCK`,
  `ACCESS_NETWORK_STATE`, `GET_PACKAGE_SIZE`. No camera, mic, phone, contacts or
  storage declarations at all.
- The chat UI, voice capture, image input and their permissions are delivered as
  **Play-delivered feature modules** (`<queries>` only references GSA,
  `com.google.android.googlequicksearchbox`).

## Feasibility verdicts

### 1. "Run Gemini without the Google app(s)" — PARTIALLY POSSIBLE, not fully

| Layer | Status |
|---|---|
| Play Services (GmsCore) presence check | **Patchable.** The shell reads `com.google.android.gms.version` and carries `GoogleApiActivity` + Google Sign-In types. The scoped **"MicroG integration (GmsCore)"** patch (see below) — or the universal **"GmsCore support (MicroG)"** patch — rewrites the GMS strings in the shell DEX to the MicroG package, injects the spoofing meta-data + `FAKE_PACKAGE_SIGNATURE` + `<queries>` entry, stubs the "Google Play Services not available" / availability checks to success and injects the runtime `checkGmsCore()` into the launcher activity. With MicroG RE installed the app can start without Google Play Services. |
| Google **account** sign-in | **Not patchable.** Gemini chat requires a signed-in Google account, and this build uses modern auth (targetSdk 35) — exactly the class the GmsCore patch documentation flags as unreliable (Credential Manager / custom auth + Play Integrity). Without a real Google account (or a MicroG account setup that happens to work for this app) the app opens but cannot chat. |
| Play Store presence | Not a hard dependency of the shell; the universal **"Block in-app update & review prompts"** patch removes the Play Core update-dialog path. |

Bottom line: *"Gemini without the Google apps"* = **app boots without Play
Services via MicroG RE**, but **a Google account is still required for chat**.
A patch cannot remove the account requirement.

### 2. Strip camera / phone / contacts / mic, keep storage — POSSIBLE with a caveat

- In the standard fused Play build these permissions are **not in the base
  manifest** — they arrive with the feature modules. So:
  - Applying the patch to the **shell APK**: safe no-op (logs "nothing to
    remove").
  - Applying it to a **full module APK** (e.g. an unpacked XAPK base): the
    declarations are stripped for real, while every storage/media permission
    (`READ/WRITE_EXTERNAL_STORAGE`, `READ_MEDIA_*`, `MANAGE_EXTERNAL_STORAGE`)
    is explicitly excluded from the removal set.
- Runtime enforcement is still the system's: even with the declarations, the
  features only work after the user grants the runtime prompt.

### 3. Privacy patch — POSSIBLE (shell scope) + universal coverage

The shell's own telemetry surface is Clearcut/Primes/Phenotype
(`ClearcutMetricSnapshotTransmitter`, `PackageStatsCapture`,
`LifeboatReceiver`, `PhenotypeMetadataHolderService`,
`android.net.http.EnableTelemetry = true`).

- **Scoped patch "Gemini privacy hardening"**: sets
  `EnableTelemetry=false` and disables the Primes `LifeboatReceiver`.
  Done at the manifest root on purpose: the DEX snapshot processor returns a
  live result object and the stats callback must release a `Semaphore`, so
  bytecode-neutralizing either risks NPEs / a startup hang.
- **Universal patches** (already apply to Gemini): "Block advertising & device
  IDs" (ad ID, limited tracking, Android ID) and "Block crash-reporting SDKs".

Caveat: chat/voice/image telemetry lives in the feature modules and is not
reachable from the shell APK.

## Patches shipped for Gemini (`com.google.android.apps.bard`)

| Patch | Type | Default |
|---|---|---|
| Strip camera, mic, phone & contacts permissions | manifest | on |
| Gemini privacy hardening | manifest | on |
| MicroG integration (GmsCore) | bytecode + manifest | off |
| GmsCore support (MicroG) *(universal alternative)* | bytecode + manifest | off |
| Block advertising & device IDs *(universal)* | bytecode | off |
| Block crash-reporting SDKs *(universal)* | bytecode | off |
| Block in-app update & review prompts *(universal)* | bytecode | off |

The **MicroG integration (GmsCore)** patch is the scoped equivalent of the
universal GmsCore patch, preconfigured for Gemini (launcher activity
`BardEntryPointActivity`, no package rename). The spoofed signature defaults
to the standard Google Play release certificate
(SHA-1 `24bb24c05e47e0aefa68a58a766179d9b613a600` — the same key the Google
Photos scoped patch ships with); the "Provide Original app certificate" patch
overrides it if Gemini is signed with a different key. It depends on that
certificate patch being *available* in the session, not on it being applied.

Both targets (1.0.795460806 / vc 272, 1.0.913571982 / vc 296) are registered
as **experimental** — static layout verified against the 1.0.795460806 shell;
on-device behaviour (especially MicroG sign-in) may vary and has not been
verified on a device yet.

## Method

- Manifest: full AXML dump (permissions, queries, components, meta-data).
- DEX: class/method inventory + disassembly of
  `ClearcutMetricSnapshotTransmitter.a` and
  `PackageStatsCapture$PackageStatsCallback.onGetStatsCompleted`.
- Feature-module contents could not be inspected: the 4-arch XAPK release
  asset is not downloadable from this environment (release-asset host is
  outside the network allowlist).
