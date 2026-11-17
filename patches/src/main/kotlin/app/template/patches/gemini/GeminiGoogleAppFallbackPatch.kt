package app.template.patches.gemini

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.booleanOption
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.GEMINI_COMPATIBILITY
import app.template.patches.shared.logUniversal

// Recent Gemini builds (1.0.795460806 / vc 272 onward) moved the chat UI into
// the Google app (GSA): the bard shell checks for com.google.android.googlequicksearchbox
// in BardEntryPointActivity.onCreate and, when it is missing, builds an intent
// to ErrorActivity (the "Download or enable the Google app" dialog) through
// Lbnf;->a(I)Landroid/content/Intent; — identified by its "error-type" extra.
// Even on the success path the shell deep-links
// https://bard.google.com/android?source=bardshell INTO GSA, so this build
// cannot chat without the Google app; the chat UI is not in the bard APK.
//
// The only "works without the Google app" outcome this build can offer is the
// browser: this patch replaces the error-dialog intent with a VIEW intent for
// the Gemini web app, so the "Google app required" dialog is gone and tapping
// Gemini opens the web version instead. Older pre-migration builds (see
// GEMINI_COMPATIBILITY) contain the chat in-app and do not need this patch.

private const val GEMINI_WEB_URL = "https://gemini.google.com/app"

private val ErrorActivityIntentFingerprint = Fingerprint(
    name = "a",
    parameters = listOf("I"),
    returnType = "Landroid/content/Intent;",
    strings = listOf("error-type"),
)

@Suppress("unused")
val geminiGoogleAppFallbackPatch = bytecodePatch(
    name = "Google app missing: open Gemini in browser",
    description = """
        Recent Gemini builds host the chat inside the Google app: the shell
        checks for it on start and shows "Download or enable the Google app"
        when it is missing — and even when present it deep-links the chat into
        the Google app, so this build cannot chat without it.

        This patch removes the dialog: the error intent is replaced with a
        plain browser intent for gemini.google.com/app, so the app opens
        Gemini in your browser instead of nagging about the Google app.
        A Google account is still needed to chat (sign in in the browser).

        For a Gemini that runs fully in-app without the Google app, install a
        pre-migration build (1.0.776555963 or older) and use the MicroG
        integration patch instead.
    """.trimIndent(),
    default = true,
) {
    compatibleWith(GEMINI_COMPATIBILITY)

    val openInBrowser by booleanOption(
        key = "openInBrowser",
        default = true,
        title = "Open Gemini web instead of the dialog",
        description = "Replace the 'Download or enable the Google app' dialog intent with a browser intent for gemini.google.com/app.",
    )

    execute {
        if (!(openInBrowser ?: true)) return@execute

        var patched = 0
        ErrorActivityIntentFingerprint.methodOrNull?.let { method ->
            // Prepend a full browser-intent build + return; the original body
            // (ErrorActivity intent construction) becomes unreachable. Uses
            // only v0-v2, which are within the method's register range.
            method.addInstructions(
                0,
                """
                    new-instance v0, Landroid/content/Intent;
                    const-string v1, "android.intent.action.VIEW"
                    const-string v2, "$GEMINI_WEB_URL"
                    invoke-static v2, Landroid/net/Uri;->parse(Ljava/lang/String;)Landroid/net/Uri;
                    move-result-object v2
                    invoke-direct v0, v1, v2, Landroid/content/Intent;-><init>(Ljava/lang/String; Landroid/net/Uri;)V
                    const v1, 0x10000000
                    invoke-virtual v0, v1, Landroid/content/Intent;->addFlags(I)Landroid/content/Intent;
                    return-object v0
                """.trimIndent(),
            )
            patched++
        }

        logUniversal(
            "Gemini Google-app fallback: $patched error-intent method(s) patched" +
                (if (patched == 0) " (anchor not found — build layout changed?)" else " → browser intent."),
        )
    }
}
