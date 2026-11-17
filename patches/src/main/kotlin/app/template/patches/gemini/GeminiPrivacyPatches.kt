package app.template.patches.gemini

import app.morphe.patcher.patch.booleanOption
import app.morphe.patcher.patch.resourcePatch
import app.template.patches.shared.Constants.GEMINI_COMPATIBILITY
import app.template.patches.shared.forEachManifestElement
import app.template.patches.shared.logUniversal

// Gemini (com.google.android.apps.bard) ships as a dynamic-feature shell. The
// base APK declares only WAKE_LOCK, ACCESS_NETWORK_STATE and GET_PACKAGE_SIZE;
// the sensing permissions (camera, mic, phone, contacts) are carried by the
// Play-delivered feature modules. The manifest layers below strip what is
// present in whichever build is opened — a no-op (logged) on the shell, a real
// removal on a full module APK. Storage and media permissions are never touched.
//
// Telemetry is disabled at the root via manifest flags rather than by
// bytecode-neutralizing the Clearcut transmitter: the snapshot processor
// returns a live result object and the stats callback holds a Semaphore that
// must be released, so patching either in DEX risks NPEs or a startup hang.

private val CAMERA_PERMISSIONS = setOf(
    "android.permission.CAMERA",
)

private val MIC_PERMISSIONS = setOf(
    "android.permission.RECORD_AUDIO",
    "android.permission.CAPTURE_AUDIO_OUTPUT",
    "android.permission.MODIFY_AUDIO_SETTINGS",
)

private val PHONE_PERMISSIONS = setOf(
    "android.permission.READ_PHONE_STATE",
    "android.permission.READ_PHONE_NUMBERS",
    "android.permission.READ_PRECISE_PHONE_STATE",
    "android.permission.CALL_PHONE",
    "android.permission.ANSWER_PHONE_CALLS",
    "android.permission.CHANGE_PHONE_STATE",
    "android.permission.PROCESS_OUTGOING_CALLS",
    "android.permission.READ_CALL_LOG",
    "android.permission.WRITE_CALL_LOG",
    "android.permission.PROCESS_CALL_LOG",
    "android.permission.MANAGE_OWN_CALLS",
    "android.permission.USE_SIP",
    "android.permission.READ_CALL_SCREENING",
)

private val CONTACTS_PERMISSIONS = setOf(
    "android.permission.READ_CONTACTS",
    "android.permission.WRITE_CONTACTS",
    "android.permission.GET_ACCOUNTS",
    "android.permission.AUTHENTICATE_ACCOUNTS",
    "android.permission.MANAGE_ACCOUNTS",
)

// ── Permissions ─────────────────────────────────────────────────────────────

@Suppress("unused")
val geminiStripSensingPermissionsPatch = resourcePatch(
    name = "Strip camera, mic, phone & contacts permissions",
    description = """
        Removes the camera, microphone, phone and contacts permission declarations
        from the Gemini manifest. Storage and media permissions are always kept.

        The standard Play build delivers these permissions through dynamic feature
        modules, so on the shell APK this patch logs "nothing to remove" — on a full
        module build it strips the declarations for real.

        Voice capture, camera/image input and phone-based features stop working once
        the relevant permissions are gone.
    """.trimIndent(),
    default = true,
) {
    compatibleWith(GEMINI_COMPATIBILITY)

    val stripCamera by booleanOption(
        key = "stripCamera",
        default = true,
        title = "Camera",
        description = "Remove CAMERA. Image input and QR features stop working.",
    )
    val stripMic by booleanOption(
        key = "stripMic",
        default = true,
        title = "Microphone",
        description = "Remove RECORD_AUDIO and related audio permissions. Voice chat stops working.",
    )
    val stripPhone by booleanOption(
        key = "stripPhone",
        default = true,
        title = "Phone",
        description = "Remove phone state, numbers and call log permissions.",
    )
    val stripContacts by booleanOption(
        key = "stripContacts",
        default = true,
        title = "Contacts & accounts",
        description = "Remove contacts and Google account discovery permissions.",
    )

    execute {
        val enabled = listOf(
            (stripCamera ?: true) to CAMERA_PERMISSIONS,
            (stripMic ?: true) to MIC_PERMISSIONS,
            (stripPhone ?: true) to PHONE_PERMISSIONS,
            (stripContacts ?: true) to CONTACTS_PERMISSIONS,
        ).filter { it.first }.flatMap { it.second }

        document("AndroidManifest.xml").use { doc ->
            var removed = 0
            doc.forEachManifestElement(
                "uses-permission",
                "uses-permission-sdk-23",
                "uses-permission-sdk-m",
            ) { el ->
                val name = el.getAttribute("android:name")
                if (name in enabled) {
                    el.parentNode.removeChild(el)
                    removed++
                    logUniversal("Gemini permissions: removed $name")
                }
            }
            if (removed == 0) {
                logUniversal(
                    "Gemini permissions: nothing to remove " +
                        "(this build does not declare camera/mic/phone/contacts in its manifest — " +
                        "normal for the shell APK; those permissions live in the feature modules).",
                )
            }
        }
    }
}

// ── Privacy / telemetry ─────────────────────────────────────────────────────

private const val ENABLE_TELEMETRY_META = "android.net.http.EnableTelemetry"

@Suppress("unused")
val geminiPrivacyPatch = resourcePatch(
    name = "Gemini privacy hardening",
    description = """
        Disables the app shell's telemetry at the root:
        - sets android.net.http.EnableTelemetry to false (turns off HTTP telemetry),
        - disables the Primes "lifeboat" transmitter receiver.

        For device and advertising identifiers, also apply the universal
        "Block advertising & device IDs" patch; for crash SDKs, the universal
        "Block crash-reporting SDKs" patch.

        This only covers what lives in the base shell — the chat, voice and image
        features (and their telemetry) are delivered by Play feature modules.
    """.trimIndent(),
    default = true,
) {
    compatibleWith(GEMINI_COMPATIBILITY)

    val disableHttpTelemetry by booleanOption(
        key = "disableHttpTelemetry",
        default = true,
        title = "HTTP telemetry",
        description = "Set android.net.http.EnableTelemetry to false.",
    )
    val disableLifeboat by booleanOption(
        key = "disableLifeboat",
        default = true,
        title = "Primes lifeboat transmitter",
        description = "Disable the background Primes telemetry receiver.",
    )

    execute {
        val httpEnabled = disableHttpTelemetry ?: true
        val lifeboatEnabled = disableLifeboat ?: true
        var telemetryOff = 0
        var lifeboatOff = 0

        document("AndroidManifest.xml").use { doc ->
            if (httpEnabled) {
                doc.forEachManifestElement("meta-data") { el ->
                    if (el.getAttribute("android:name") == ENABLE_TELEMETRY_META &&
                        el.getAttribute("android:value") != "false"
                    ) {
                        el.setAttribute("android:value", "false")
                        telemetryOff++
                    }
                }
            }
            if (lifeboatEnabled) {
                doc.forEachManifestElement("receiver") { el ->
                    val name = el.getAttribute("android:name")
                    if (name.contains("primes.transmitter.LifeboatReceiver") &&
                        el.getAttribute("android:enabled") != "false"
                    ) {
                        el.setAttribute("android:enabled", "false")
                        lifeboatOff++
                    }
                }
            }
        }

        logUniversal(
            "Gemini privacy: EnableTelemetry off=$telemetryOff " +
                "lifeboat-receiver off=$lifeboatOff " +
                "(0 means the flag was already off / not present in this build).",
        )
    }
}
