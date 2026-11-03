# Getting Proteus Patches listed in the Morphe ecosystem

> Goal: make `ineednotitle/Proteus-Patches` show up on
> [morphe-patches.software](https://morphe-patches.software/) — the "Morphe
> Community Patches" directory linked from the official
> [morphe.software](https://morphe.software/) site — and in the community
> catalogs that share the same data.

## 1. Status snapshot — checked 2026-11-03

| Catalog | Entries | Proteus listed? |
|---|---|---|
| [morphe-patches.software](https://morphe-patches.software/) (Morphe Community Patches) | 387 bundles | ❌ no |
| [Jman-Github/Patch-Bundle-Registry](https://github.com/Jman-Github/Patch-Bundle-Registry) | 550 sources | ❌ no |
| [nvbangg/awesome-morphe](https://github.com/nvbangg/awesome-morphe) | mirrors the above | ❌ no |
| [rushiforai/morphe-archive](https://github.com/rushiforai/morphe-archive) | auto index | ❌ no |

## 2. Why no crawler ever found this repo (root cause)

Every crawler in this ecosystem discovers bundles the same way: a **GitHub code
search for `filename:patches-bundle.json`**, then a check that the
`download_url` in that file really serves an `.mpp`.
(morphe-archive's crawler is public and does exactly this in
`scripts/discover_github_wide.py`; awesome-morphe and the community patches site
behave the same way — they index repos within days of creation, including
0-star repos.)

Diagnostics run against this repo:

```text
q = filename:patches-bundle.json repo:ineednotitle/Proteus-Patches  -> 0 results
q = filename:patches-list.json   repo:ineednotitle/Proteus-Patches  -> 0 results
q = <any term>                   repo:ineednotitle/Proteus-Patches  -> 0 results

control (all return 1):
    AltF4G0x/consumer-clarity      SysAdminDoc/HushPine
    hoo-dles/morphe-patches        zeldrisho/morphe-patches
    0x0day-0wl/morphe-patches
```

So **not a single file of this repository is in GitHub's code-search index** —
which is why the topic `morphe-patches`, the 10 releases and the valid
`patches-bundle.json` never helped.

Most likely cause: a `.gradle/` **build cache was committed** (15 files, ~1.2 MB),
including a 540 KB `executionHistory.bin` and blobs with 69,000-character lines.
GitHub's code search only indexes small text files (per-file size and line-length
limits) and skips binary/generated content; oversized blobs on the default branch
are the most plausible reason the indexer never ingested this repo.

**Fix shipped in this PR:** `.gradle/` is no longer tracked (it was already in
`.gitignore`, but the files had been added before the rule existed).

### How to verify after merging

```bash
gh api -X GET search/code \
  -f q='filename:patches-bundle.json repo:ineednotitle/Proteus-Patches' \
  --jq .total_count
```

- `1` → the repo is indexed; the crawlers will pick it up on their next run
  (morphe-archive runs nightly, the others roughly hourly/daily).
- Still `0` after 24–48 h → open a ticket with
  [GitHub Support](https://support.github.com/contact) asking why
  `ineednotitle/Proteus-Patches` is not in the code search index, or fall back to
  the manual submissions below (they work regardless of GitHub's index).

## 3. Manual submissions (copy & paste)

These are the four catalogs that feed the Morphe ecosystem. Submitting manually
gets you listed even if GitHub's index never catches up.

### 3.1 morphe-patches.software — contact form

Scroll to the bottom of <https://morphe-patches.software/> and use the
**Name / Email Address / Message** form:

```text
Hi,

please add my Morphe patch bundle to the community patches directory.

Repository: https://github.com/ineednotitle/Proteus-Patches
Bundle:     Proteus Patches (patches-2.0.0.mpp)
Content:    118 patches, 24 universal patches, 27 app packages (23 apps)
Metadata:   https://github.com/ineednotitle/Proteus-Patches/blob/main/patches-bundle.json
Patch list: https://github.com/ineednotitle/Proteus-Patches/blob/main/patches-list.json

The repo has the "morphe-patches" topic and 10 releases, each with a
patches-<version>.mpp asset. Note: GitHub code search currently returns 0
results for this repository, so your crawler most likely never saw it -
adding it manually would be much appreciated.

Thanks!
```

### 3.2 Jman's Patch Bundle Registry — PR (preferred) or issue

Preferred: edit `config/sources.json` and append one entry (the file ends with
`yt-music-morphe-pitch-patch`):

```diff
   "yt-music-morphe-pitch-patch": {
     "patches": "https://github.com/AnTo88-svg/YT-Music-Morphe-Pitch-Patch",
     "integrations": false,
     "disabled": false
+  },
+  "proteus": {
+    "patches": "https://github.com/ineednotitle/Proteus-Patches",
+    "integrations": false,
+    "disabled": false
   }
 }
```

Or open an issue with:

```text
Please add this Morphe patch source to the catalog.

Repository: https://github.com/ineednotitle/Proteus-Patches
Format:     Morphe (.mpp)
Channels:   stable releases (v2.0.0 latest) - no dev/pre-release channel
Integrations: none required
Apps:       27 packages / 23 apps (Telegram, MUBI, Meesho, Amazon, Google
            Photos, Pinterest, Reddit, Strava, ChatGPT, Firefox, Excel, ...)
```

### 3.3 awesome-morphe — bundle request issue

Open <https://github.com/nvbangg/awesome-morphe/issues/new?template=bundle-request.yml>
and fill it in:

- **Action:** Add Bundle
- **Repository URL:** `https://github.com/ineednotitle/Proteus-Patches`
- **Description:**

  ```text
  Proteus Patches - a curated Morphe patch bundle: 118 patches, 24 universal
  patches, 27 app packages (23 apps). Ad/analytics removal, privacy hardening
  and misc tweaks for Telegram, MUBI, Meesho, Amazon, Google Photos, Pinterest
  and more. Valid patches-bundle.json on main, releases ship patches-*.mpp.
  ```

- Tick both confirmation boxes.

### 3.4 morphe-archive — one-line PR

Append to `custom_repos.txt`:

```diff
 # Lines starting with # are ignored.
 hxreborn/hxreborn-tiktok-patches
+ineednotitle/Proteus-Patches
```

## 4. Discovery checklist

Done in this PR:

- [x] `patches-bundle.json` on the default branch root, `version` and
      `download_url` synced to the real latest release (**v2.0.0** — it still
      advertised v1.19.0)
- [x] `patches-list.json` `version` field synced to 2.0.0
- [x] `.gradle/` build cache untracked (unblocks GitHub code search)
- [x] README: "Add to Morphe" deep link
      (`https://morphe.software/add-source?github=ineednotitle/Proteus-Patches`)
      and the plain source URL for manual entry
- [x] GitHub topics: `morphe`, `morphe-patches`
- [ ] **You** — set the repo description and topics in
      **Settings → About** (a `chore`/CI token cannot change these):

      ```text
      Description: Proteus Patches — a Morphe patch bundle (.mpp) for Android:
                   removes ads, trackers and annoyances across 23 apps, plus
                   universal patches.

      Topics:      morphe   morphe-patches   android   patches   privacy
      ```

      The current description ("Free yourself from Big tech Corporations ")
      contains no "Morphe", so crawlers that fall back to a keyword search for
      `morphe` (name/description/readme) rank this repo poorly.

Still open / optional:

- [ ] Wait 24–48 h, then re-run the code-search check in §2
- [ ] Optional: add `patches-bundle.png` so Manager and the catalogs show a
      custom icon instead of the GitHub avatar
- [ ] Optional: sign releases (`./gradlew generateSigningKey`, add
      `MORPHE_SIGNING_KEY` to repo secrets) — releases then show as verified and
      `signature_download_url` gets filled in
- [ ] Releases are cut by semantic-release; after the next release, confirm
      `patches-bundle.json` was updated automatically and still points at the
      newest `.mpp`

### ⚠️ Do not do this

Never mark `patches-bundle.json` or `patches-list.json` as
`linguist-generated` / `linguist-vendored` in `.gitattributes`. GitHub's code
search skips generated and vendored files, which would hide the bundle from
every crawler in this ecosystem.
