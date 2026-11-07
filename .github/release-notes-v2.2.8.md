v2.2.8 (pre-release) — 2026-11-07 (re-cut)

What's in this build (supersedes the earlier v2.2.8 assets):

- New universal patch: "Disable video autoplay (ExoPlayer/Media3)" — flips
  literal setPlayWhenReady(true) call sites to false. Heuristic; reflection or
  obfuscated player setup is not covered; may stop playback entirely in apps
  whose only play path is such a literal (turn it off if videos never start).
  Off by default; unverified on device.
- New app-scoped entries (off by default; unverified on device):
  - "Mint: Block analytics & tracking SDKs" — com.htmedia.mint (5.7.5, 5.8.4)
  - "Hindustan Times: Block analytics & tracking SDKs" — com.ht.news (4.9.16)
  - "Mint: Disable video autoplay (ExoPlayer)"
  - "Hindustan Times: Disable video autoplay (ExoPlayer)"
  - "Mint: Prefer external browser over Custom Tabs"
  - "Hindustan Times: Prefer external browser over Custom Tabs"
- Indian-market tracker markers in the universal analytics patch: WebEngage,
  CleverTap, Flurry, Branch, Kochava.
- New analysis doc in-repo: docs/HT-NEWS-APPS.md — candidate patches and
  exactly what decompiling the APKs must confirm.
- Everything from the v2.2.7 re-replace: Amazon Shopping/India AMOLED dark
  mode, Reddit listing performance fix, six experimental off-by-default Brave
  patches (adapted from kveld9/kveld-morphe-patches), Reddit AutoMod detection
  broadening, "Show deleted comments" lookup logging.

Known limitations:
- Brave patches are unverified on device.
- ExoPlayer autoplay heuristic, Amazon AMOLED and the Reddit listing fix are
  unverified on device.
- No binary-specific Mint/HT patches yet — they require the APKs
  (see docs/HT-NEWS-APPS.md). Not attempted: sponsored/in-feed ad suppression,
  ePaper cleanup, AMOLED reading surface, rating-nag removal.
- "Mint Premium" / "HT Premium" cannot be unlocked by a client-side patch:
  premium content is served behind server-side entitlement.
- CompatibleWith lists cover the store versions observed on 2026-11-06 only.

v2.2.4 remains the latest full release. All v2.2.5+ builds are pre-releases, unchanged.
