package app.template.patches.mereframe

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.rawResourcePatch
import app.template.patches.shared.Constants.MEREFRAME_COMPATIBILITY
import app.template.patches.shared.clearBody
import app.template.patches.shared.ensureRegisters
import app.template.patches.shared.logUniversal

private const val SO_ARM64_PATH = "lib/arm64-v8a/libunnamed_raw.so"

/**
 * Five globally-unique (`count == 1`) 96-byte function prologues for the
 * self-integrity verification routines in `lib/arm64-v8a/libunnamed_raw.so`
 * (Mereframe 0.8.4 / vc 118). All 117 native integrity call sites across the
 * 17 `app.unnamed.rawrecording.b.B*` bridge classes funnel through these five
 * checks (`0x23f3d8`, `0x2a5ecc`, `0x2bf304`, `0x2e5fb0`, `0x2e1b54`).
 */
private val INTEGRITY_ARM64_GATES = listOf(
    arm64Gate(
        "isIntegrityOk@0x23f3d8",
        "3f 23 03 d5 ff 03 03 d1 fd 7b 06 a9 fc 6f 07 a9 fa 67 08 a9 f8 5f 09 a9 " +
            "f6 57 0a a9 f4 4f 0b a9 fd 83 01 91 48 d0 3b d5 a9 83 00 d1 4a e4 97 52 " +
            "e8 03 00 f9 ea c9 aa 72 79 af 98 52 08 15 40 f9 15 6c 9e 52 db d3 94 52 " +
            "7c 9d 81 52 f8 dd 87 52 5a ad 84 52 a8 83 1f f8 a8 63 00 d1 f3 03 00 aa",
    ),
    arm64Gate(
        "checkApkAssets@0x2a5ecc",
        "3f 23 03 d5 ff 43 03 d1 fd 7b 07 a9 fc 6f 08 a9 fa 67 09 a9 f8 5f 0a a9 " +
            "f6 57 0b a9 f4 4f 0c a9 fd c3 01 91 48 d0 3b d5 a9 83 00 d1 f3 03 00 aa " +
            "e8 07 00 f9 08 15 40 f9 a8 83 1f f8 a8 63 00 d1 a8 27 3e a9 25 08 00 94 " +
            "e0 17 00 b9 1a 72 89 52 f6 65 9e 52 a8 83 5e b8 a9 03 5e b8 3c f8 9b 52",
    ),
    arm64Gate(
        "checkMapsDbg@0x2bf304",
        "3f 23 03 d5 ff 43 04 d1 fd 7b 0b a9 fc 6f 0c a9 fa 67 0d a9 f8 5f 0e a9 " +
            "f6 57 0f a9 f4 4f 10 a9 fd c3 02 91 48 d0 3b d5 a9 e3 00 d1 ca e7 8a 52 " +
            "e8 03 00 f9 ea 53 ad 72 d5 b4 8e 52 08 15 40 f9 3b 77 97 52 03 16 84 52 " +
            "64 74 9d 52 96 09 9b 52 b7 8f 93 52 a8 03 1f f8 a8 c3 00 d1 5c fa 98 52",
    ),
    arm64Gate(
        "checkSoMaps@0x2e5fb0",
        "3f 23 03 d5 ff 43 03 d1 fd 7b 07 a9 fc 6f 08 a9 fa 67 09 a9 f8 5f 0a a9 " +
            "f6 57 0b a9 f4 4f 0c a9 fd c3 01 91 48 d0 3b d5 a9 83 00 d1 aa 4b 82 52 " +
            "e8 07 00 f9 ca 0f a3 72 d9 43 88 52 08 15 40 f9 9b 62 8e 52 9a f5 84 52 " +
            "38 41 87 52 55 c9 8b 52 56 ea 88 52 a8 83 1f f8 a8 63 00 d1 f3 03 00 aa",
    ),
    arm64Gate(
        "checkEnvSig@0x2e1b54",
        "3f 23 03 d5 ff 43 05 d1 fd 7b 0f a9 fc 6f 10 a9 fa 67 11 a9 f8 5f 12 a9 " +
            "f6 57 13 a9 f4 4f 14 a9 fd c3 03 91 48 d0 3b d5 a9 63 01 d1 10 23 00 94 " +
            "e8 03 00 f9 08 15 40 f9 a8 03 1f f8 a8 43 01 d1 a8 a7 3a a9 ac 7f f5 97 " +
            "f2 eb ff 97 e0 1f 00 b9 bc 7c 84 52 9a 8c 90 52 a8 03 5b b8 a9 83 5a b8",
    ),
)

private val bypassNativeSoIntegrityPatch = rawResourcePatch {
    execute {
        val soFile = runCatching { get(SO_ARM64_PATH, true) }.getOrNull()
        if (soFile == null || !soFile.exists() || !soFile.isFile) {
            logUniversal("Bypass native integrity: $SO_ARM64_PATH absent — relying on DEX session fallback only.")
            return@execute
        }

        val patched = patchArm64Gates(soFile, INTEGRITY_ARM64_GATES, "Bypass native integrity")
        logUniversal("Bypass native integrity: patched $patched ARM64 integrity gates in $SO_ARM64_PATH (BTI/PAC-safe).")
    }
}

/**
 * The app's native pipeline (`libunnamed_raw.so`) self-verifies the APK,
 * `assets/unnamed.protection`, `/proc/self/maps`, loaded shared objects, and
 * `prctl` state, JNI-throwing `SecurityException("app integrity check failed")`
 * across `B1e6ad...` and sibling native bridge classes as soon as preview frames
 * flow or capture initializes.
 *
 * Strategy:
 * 1. Patch all five ARM64 integrity check entry points in `lib/arm64-v8a/libunnamed_raw.so`
 *    to `bti jc; mov w0, #1; ret` so every native bridge call executes genuine hardware/ISP
 *    logic without integrity throws or per-frame reflection overhead.
 * 2. Keep `B1e6ad8077f6880e309d8da4b;->d(ZZZZFZ)[J` wrapped in `MereframeIntegrityHook`
 *    as a defense-in-depth JVM `try/catch` safety net for session opening.
 */
@Suppress("unused")
val bypassNativeIntegrityPatch = bytecodePatch(
    name = "Bypass native integrity checks",
    description = "Neutralizes libunnamed_raw.so's five ARM64 self-integrity gates (BTI/PAC-safe) " +
        "so camera preview, ISP processing, and RAW capture run normally on re-signed builds, while " +
        "keeping the JVM session fallback active. Required together with 'Allow re-signed/sideloaded installs'.",
    default = true,
) {
    compatibleWith(MEREFRAME_COMPATIBILITY)
    dependsOn(bypassNativeSoIntegrityPatch)
    extendWith("extensions/extension.mpe")

    execute {
        Fingerprint(
            definingClass = "Lapp/unnamed/rawrecording/b/B1e6ad8077f6880e309d8da4b;",
            name = "d",
            returnType = "[J",
            parameters = listOf("Z", "Z", "Z", "Z", "F", "Z"),
        ).method.apply {
            ensureRegisters(7)
            clearBody()
            addInstructions(
                0,
                """
                invoke-static/range {p0 .. p5}, Lapp/template/extension/extension/MereframeIntegrityHook;->openSessionWithFallback(ZZZZFZ)[J
                move-result-object v0
                return-object v0
                """,
            )
        }
        logUniversal("Bypass native integrity: native ARM64 gates + MereframeIntegrityHook fallback active.")
    }
}
