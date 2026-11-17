package app.template.patches.gemini

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.resourcePatch
import app.morphe.patcher.patch.stringOption
import app.template.patches.shared.Constants.GEMINI_COMPATIBILITY
import app.template.patches.shared.cert.autoSha1
import app.template.patches.shared.cert.extractApkCertificatePatch
import app.template.patches.shared.gms.DEFAULT_GMS_CORE_VENDOR
import app.template.patches.shared.gms.GMS_ACTIONS
import app.template.patches.shared.gms.GMS_AUTHORITIES
import app.template.patches.shared.gms.GMS_PERMISSIONS
import app.template.patches.shared.gms.GooglePlayUtilityFingerprint
import app.template.patches.shared.gms.IsGooglePlayServicesAvailableFingerprint
import app.template.patches.shared.gms.ServiceCheckFingerprint
import app.template.patches.shared.gms.gmsVendorGroup
import app.template.patches.shared.gms.upsertQueriesPackage
import app.template.patches.shared.logUniversal
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction21c
import com.android.tools.smali.dexlib2.iface.ClassDef
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction21c
import com.android.tools.smali.dexlib2.iface.reference.StringReference
import com.android.tools.smali.dexlib2.immutable.reference.ImmutableStringReference
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.net.URI

// Gemini-scoped MicroG integration — the same mechanism as the universal
// "GmsCore support (MicroG)" patch, preconfigured for com.google.android.apps.bard:
// package name and launcher activity are hardcoded, no package rename, and the
// spoofed signature defaults to the standard Google Play release certificate
// (the one the Google Photos patch ships with). The "Provide Original app
// certificate" patch overrides it when a different key is needed.

private const val GEMINI_PACKAGE = "com.google.android.apps.bard"

// Launcher entry point (MAIN/LAUNCHER) in the shell manifest.
private const val GEMINI_ENTRY_ACTIVITY =
    "Lcom/google/android/apps/bard/shellapp/BardEntryPointActivity;"

private const val EXTENSION_CLASS =
    "Lapp/template/extension/extension/GmsCoreSupportPatch;"

// Standard Google Play release certificate, SHA-1 hex. Same key the Google
// Photos scoped patch hardcodes; most Google Play releases are signed with it.
private const val GOOGLE_RELEASE_CERT_SHA1 = "24bb24c05e47e0aefa68a58a766179d9b613a600"

private val GeminiEntryOnCreateFingerprint = Fingerprint(
    definingClass = GEMINI_ENTRY_ACTIVITY,
    name = "onCreate",
    returnType = "V",
    parameters = listOf("Landroid/os/Bundle;"),
)

private val GmsCoreVendorGroupFingerprint = Fingerprint(
    custom = { method: Method, classDef: ClassDef ->
        classDef.type == EXTENSION_CLASS && method.name == "getGmsCoreVendorGroupId"
    },
)

private val OriginalPackageNameFingerprint = Fingerprint(
    custom = { method: Method, classDef: ClassDef ->
        classDef.type == EXTENSION_CLASS && method.name == "getOriginalPackageName"
    },
)

@Suppress("unused")
val geminiGmsCorePatch = bytecodePatch(
    name = "MicroG integration (GmsCore)",
    description = """
        Routes Gemini's Play Services calls through MicroG RE instead of Google
        Play Services, so the app runs without the Google Play Services app:

        - rewrites the GMS package/permission/action strings in the shell DEX,
        - adds the MicroG spoofing meta-data and FAKE_PACKAGE_SIGNATURE
          permission plus the <queries> entry for the GmsCore package,
        - stubs the "Google Play Services not available" service check and the
          availability checks to success,
        - injects the runtime GmsCore check into the launcher activity.

        Requires MicroG RE installed on the device — pick its package below.
        The spoofed signature defaults to the standard Google Play release
        certificate; apply "Provide Original app certificate" to override it.

        Caveats: sign-in still needs a working Google account (via MicroG
        account setup), and Gemini's backend integrity checks may refuse
        non-Google clients — the app will start and resolve GMS under MicroG,
        but a fully working chat is not guaranteed.
    """.trimIndent(),
    default = false,
) {
    compatibleWith(GEMINI_COMPATIBILITY)
    extendWith("extensions/extension.mpe")

    val gmsCoreVendorOption by stringOption(
        key = "gmsCoreVendor",
        default = "app.revanced.android.gms",
        values = mapOf(
            "app.revanced.android.gms (default)" to "app.revanced.android.gms",
            "app.morphe.android.gms" to "app.morphe.android.gms",
            "app.morphe.manager" to "app.morphe.manager",
            "app.revanced.manager" to "app.revanced.manager",
            "app.rvx.manager" to "app.rvx.manager",
            "com.google.android.gms" to "com.google.android.gms",
            "org.microg.gms" to "org.microg.gms",
        ),
        title = "MicroG package name",
        description = "Which MicroG / GmsCore app is installed on your device. " +
            "Check your device's app list for \"MicroG\" or \"GmsCore\" if unsure — " +
            "most users should leave this as the default.",
        required = true,
    )

    dependsOn(extractApkCertificatePatch,
        resourcePatch {
            execute {
                val vendor = gmsCoreVendorOption ?: DEFAULT_GMS_CORE_VENDOR
                val vendorGroup = gmsVendorGroup(vendor)
                // User-provided original certificate wins; otherwise fall back to the
                // standard Google Play release certificate.
                val sig = autoSha1 ?: GOOGLE_RELEASE_CERT_SHA1

                document("AndroidManifest.xml").use { doc ->
                    val manifestPackage = (doc.getElementsByTagName("manifest").item(0) as Element)
                        .getAttribute("package")
                    if (manifestPackage != GEMINI_PACKAGE) {
                        logUniversal(
                            "Gemini GmsCore: unexpected package $manifestPackage " +
                                "(expected $GEMINI_PACKAGE) — continuing.",
                        )
                    }

                    val manifestNode = doc.getElementsByTagName("manifest").item(0)
                    val applicationNode = doc.getElementsByTagName("application").item(0) as? Element
                        ?: return@use

                    // FAKE_PACKAGE_SIGNATURE permission — MicroG RE reads this to
                    // enable signature spoofing.
                    val existingPerms = doc.getElementsByTagName("uses-permission")
                    val permAlreadyAdded = (0 until existingPerms.length).any { i ->
                        existingPerms.item(i).attributes
                            ?.getNamedItem("android:name")?.nodeValue ==
                            "org.microg.gms.permission.FAKE_PACKAGE_SIGNATURE"
                    }
                    if (!permAlreadyAdded) {
                        val perm = doc.createElement("uses-permission")
                        perm.setAttribute("android:name", "org.microg.gms.permission.FAKE_PACKAGE_SIGNATURE")
                        manifestNode.appendChild(perm)
                    }

                    // <queries> entry so the app can resolve the GmsCore package
                    // (Android 11+ package visibility).
                    upsertQueriesPackage(doc, vendor)

                    fun Node.upsertMeta(name: String, value: String) {
                        val existing = (0 until childNodes.length)
                            .map { childNodes.item(it) }
                            .firstOrNull { node ->
                                node.nodeName == "meta-data" &&
                                    node.attributes?.getNamedItem("android:name")?.nodeValue == name
                            } as? Element
                        if (existing != null) {
                            existing.setAttribute("android:value", value)
                            return
                        }
                        val meta = (ownerDocument ?: doc).createElement("meta-data") as Element
                        meta.setAttribute("android:name", name)
                        meta.setAttribute("android:value", value)
                        appendChild(meta)
                    }

                    applicationNode.upsertMeta("$vendor.SPOOFED_PACKAGE_NAME", GEMINI_PACKAGE)
                    applicationNode.upsertMeta("$vendor.SPOOFED_PACKAGE_SIGNATURE", sig)
                    applicationNode.upsertMeta("$vendor.MICROG_PACKAGE_NAME", vendor)

                    // Rewrite GMS uses-permissions in the manifest to the vendor
                    // namespace so they match the DEX rewrite below.
                    val isCustomVendor = !vendor.contains("microg") && !vendor.startsWith("com.google")
                    if (isCustomVendor) {
                        (0 until doc.getElementsByTagName("uses-permission").length)
                            .mapNotNull { doc.getElementsByTagName("uses-permission").item(it) as? Element }
                            .forEach { el ->
                                val name = el.getAttribute("android:name")
                                    .takeIf { n -> n.isNotBlank() } ?: return@forEach
                                if (name in GMS_PERMISSIONS) {
                                    el.setAttribute("android:name", name.replace("com.google", vendorGroup))
                                }
                            }
                    }
                }

                // Manifest text rewrites must run AFTER document() closes — closing
                // serialises the DOM back to AndroidManifest.xml, and writing the
                // file inside the use-block would be overwritten.
                val manifestFile = get("AndroidManifest.xml")
                var text = manifestFile.readText()
                if (!vendor.contains("microg") && !vendor.startsWith("com.google")) {
                    text = text.replace("com.google.android.c2dm", "$vendorGroup.android.c2dm")
                }
                manifestFile.writeText(text)

                logUniversal(
                    "Gemini GmsCore: vendor=$vendor signature=" +
                        (if (autoSha1 != null) "user-provided" else "Google release cert (default)"),
                )
            }
        },
    )

    execute {
        val vendor = gmsCoreVendorOption ?: DEFAULT_GMS_CORE_VENDOR
        val vendorGroup = gmsVendorGroup(vendor)

        val rewriteStrings = buildMap<String, String> {
            put("com.google", vendorGroup)
            put("com.google.android.gms", vendor)
            // MicroG RE registers the subscribedfeeds provider under
            // <vendor>.android.gsf.subscribedfeeds.
            put("subscribedfeeds", "$vendorGroup.android.gsf.subscribedfeeds")
            (GMS_PERMISSIONS + GMS_ACTIONS + GMS_AUTHORITIES).forEach { s ->
                if (s != "subscribedfeeds") put(s, s.replace("com.google", vendorGroup))
            }
        }

        fun transformContentUri(str: String): String? {
            if (!str.startsWith("content://")) return null
            return runCatching { URI.create(str) }.getOrNull()?.let { uri ->
                when (uri.authority) {
                    in GMS_AUTHORITIES -> when {
                        uri.authority == "subscribedfeeds" ->
                            str.replace("subscribedfeeds", "$vendorGroup.android.gsf.subscribedfeeds")
                        uri.authority.startsWith("com.google") -> str.replace("com.google", vendorGroup)
                        else -> str.replace(uri.authority, "$vendorGroup.${uri.authority}")
                    }
                    else -> null
                }
            }
        }

        fun transform(str: String): String? {
            rewriteStrings[str]?.let { return it }
            transformContentUri(str)
        }

        var rewritten = 0
        getAllClassesWithStrings().forEach { classDef ->
            val hasMatch = classDef.methods.any { method ->
                method.implementation?.instructions?.any { instr ->
                    val s = ((instr as? Instruction21c)?.reference as? StringReference)?.string
                    s != null && transform(s) != null
                } == true
            }
            if (!hasMatch) return@forEach

            val mutableClass = mutableClassDefBy(classDef)
            for (mutableMethod in mutableClass.methods) {
                val impl = mutableMethod.implementation ?: continue
                val replacements = mutableListOf<Pair<Int, BuilderInstruction21c>>()
                impl.instructions.forEachIndexed { index, instr ->
                    val s = ((instr as? Instruction21c)?.reference as? StringReference)?.string
                        ?: return@forEachIndexed
                    val transformed = transform(s) ?: return@forEachIndexed
                    replacements += index to BuilderInstruction21c(
                        Opcode.CONST_STRING,
                        instr.registerA,
                        ImmutableStringReference(transformed),
                    )
                }
                replacements.forEach { (idx, replacement) ->
                    rewritten++
                    mutableMethod.replaceInstruction(idx, replacement)
                }
            }
        }

        // "Google Play Services not available" guard — let MicroG handle the
        // request instead of crashing when GMS is absent.
        ServiceCheckFingerprint.methodOrNull?.addInstruction(0, "return-void")

        // Play Services version-code check — callers ignore 0, so returning it
        // early prevents GMS-absent crashes (only present when the shell still
        // bundles the old util).
        GooglePlayUtilityFingerprint.methodOrNull?.let { method ->
            method.addInstruction(0, "return v0")
            method.addInstruction(0, "const/4 v0, 0x0")
        }
        // isGooglePlayServicesAvailable equivalent — return CONNECTION_SUCCESS.
        IsGooglePlayServicesAvailableFingerprint.methodOrNull?.let { method ->
            method.addInstruction(0, "return v0")
            method.addInstruction(0, "const/4 v0, 0x0")
        }

        // Extension helpers: hardcode the vendor group and the original package.
        GmsCoreVendorGroupFingerprint.methodOrNull?.apply {
            addInstruction(0, "return-object v0")
            addInstruction(0, "const-string v0, \"$vendorGroup\"")
        } ?: logUniversal("Gemini GmsCore: getGmsCoreVendorGroupId() NOT FOUND — extension may not be merged.")
        OriginalPackageNameFingerprint.methodOrNull?.apply {
            addInstruction(0, "return-object v0")
            addInstruction(0, "const-string v0, \"$GEMINI_PACKAGE\"")
        } ?: logUniversal("Gemini GmsCore: getOriginalPackageName() NOT FOUND — extension may not be merged.")

        // Inject checkGmsCore(context) into the launcher activity. Skip when the
        // extension class is missing: a dangling reference would only produce a
        // NoClassDefFoundError at runtime.
        val extensionAvailable = try {
            classDefBy(EXTENSION_CLASS) != null
        } catch (_: Throwable) {
            false
        }
        val onCreate = GeminiEntryOnCreateFingerprint.methodOrNull
        when {
            onCreate != null && !extensionAvailable ->
                logUniversal("Gemini GmsCore: $EXTENSION_CLASS NOT FOUND — checkGmsCore() not injected.")
            onCreate != null -> {
                onCreate.addInstruction(
                    0,
                    "invoke-static/range { p0 .. p0 }, $EXTENSION_CLASS->checkGmsCore(Landroid/content/Context;)V",
                )
                logUniversal("Gemini GmsCore: checkGmsCore() injected into BardEntryPointActivity.onCreate().")
            }
            else -> logUniversal("Gemini GmsCore: launcher activity onCreate() NOT FOUND — checkGmsCore() not injected.")
        }

        logUniversal("Gemini GmsCore: $rewritten GMS string(s) rewritten in the shell DEX (vendor $vendor).")
    }
}
