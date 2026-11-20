package app.template.patches.spotify.misc

import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.resourcePatch
import app.morphe.patcher.patch.stringOption
import app.template.patches.shared.Constants.SPOTIFY_COMPATIBILITY
import org.w3c.dom.Element
import org.w3c.dom.NodeList

private const val DEFAULT_PACKAGE_NAME = "com.spotify.music.morphe"
private const val DEFAULT_APP_NAME = "Spotify Morphe"

private val PACKAGE_NAME_REGEX = Regex("^[a-zA-Z][a-zA-Z0-9_]*(\\.[a-zA-Z][a-zA-Z0-9_]*)+$")

// Install a cloned Spotify beside the official app.
//
// The manifest package, custom permissions, provider authorities and launcher
// label are rewritten in finalize{}, so every other patch still sees the
// original package name. DEX component class names are left alone (renaming
// android:name on components would produce ClassNotFoundException at startup).
//
// Three Spotify-specific details:
//   • Provider authorities are device-unique: keeping them causes
//     INSTALL_FAILED_CONFLICTING_PROVIDER. Authorities that are not derived
//     from the package name (e.g. com.spotify.mobile.android.mediaapi) are
//     prefixed with the new package name instead.
//   • Some authorities are given as @string/ references and are read through
//     R.string too, so the string resources are rewritten alongside.
//   • Custom permissions are device-unique as well: keeping them causes
//     INSTALL_FAILED_DUPLICATE_PERMISSION.
//
// Known limitations: the clone is re-signed, so Play Billing, Cast and Google
// sign-in may reject it — those flows are signature-bound and cannot be fixed
// in the manifest. Use the clone for side-by-side installs and testing.

@Suppress("unused")
val spotifyChangePackageNamePatch = resourcePatch(
    name = "Change package name",
    description = "Changes the package name and app name so the patched app can be installed " +
        "alongside the original Spotify app.",
    default = false,
) {
    compatibleWith(SPOTIFY_COMPATIBILITY)

    val packageNameOption by stringOption(
        key = "spotifyPackageName",
        default = DEFAULT_PACKAGE_NAME,
        values = mapOf("Default" to DEFAULT_PACKAGE_NAME),
        title = "Package name",
        description = "The package name of the patched app. It must differ from the original one.",
        required = true,
    ) { it != null && PACKAGE_NAME_REGEX.matches(it) }

    val appNameOption by stringOption(
        key = "spotifyAppName",
        default = DEFAULT_APP_NAME,
        values = mapOf("Default" to DEFAULT_APP_NAME),
        title = "App name",
        description = "The name shown in the launcher.",
        required = true,
    ) { !it.isNullOrBlank() }

    // Finalize so other patches still see the original package name.
    finalize {
        val newPackageName = packageNameOption ?: DEFAULT_PACKAGE_NAME
        val appName = appNameOption ?: DEFAULT_APP_NAME

        document("AndroidManifest.xml").use { document ->
            val manifest = document.getElementsByTagName("manifest").item(0) as Element
            val oldPackageName = manifest.getAttribute("package")

            if (newPackageName == oldPackageName) {
                throw PatchException("The new package name must differ from \"$oldPackageName\"")
            }

            fun renamed(value: String) =
                if (value.startsWith("$oldPackageName.")) newPackageName + value.removePrefix(oldPackageName) else value

            // Authorities not based on the package name (e.g.
            // com.spotify.mobile.android.mediaapi) still collide with the
            // original app, so they get the new package name as prefix.
            fun renamedAuthority(value: String) =
                if (value.startsWith("$oldPackageName.")) renamed(value) else "$newPackageName.$value"

            manifest.setAttribute("package", newPackageName)

            // Custom permissions are unique per device: keeping them causes
            // INSTALL_FAILED_DUPLICATE_PERMISSION.
            val declaredPermissions = mutableSetOf<String>()
            document.getElementsByTagName("permission").forEachElement { permission ->
                val name = permission.getAttribute("android:name")
                if (name.startsWith("$oldPackageName.")) declaredPermissions += name
            }

            // Provider authorities are unique per device: keeping them causes
            // INSTALL_FAILED_CONFLICTING_PROVIDER. Only the app's own providers
            // are rewritten, not the <queries> entries that point at other apps.
            val application = document.getElementsByTagName("application").item(0) as Element
            val authorityStrings = mutableSetOf<String>()
            application.getElementsByTagName("provider").forEachElement { provider ->
                val authorities = provider.getAttribute("android:authorities")
                when {
                    authorities.isEmpty() -> Unit
                    // The app reads these through R.string too, so the string
                    // itself is renamed below.
                    authorities.startsWith("@string/") -> authorityStrings += authorities.removePrefix("@string/")
                    else -> provider.setAttribute(
                        "android:authorities",
                        authorities.split(';').joinToString(";") { renamedAuthority(it) },
                    )
                }
            }

            if (authorityStrings.isNotEmpty()) {
                document("res/values/strings.xml").use { strings ->
                    val found = mutableSetOf<String>()
                    strings.getElementsByTagName("string").forEachElement { string ->
                        val name = string.getAttribute("name")
                        if (name in authorityStrings) {
                            string.textContent = string.textContent.split(';')
                                .joinToString(";") { renamedAuthority(it) }
                            found += name
                        }
                    }
                    val missing = authorityStrings - found
                    if (missing.isNotEmpty()) {
                        throw PatchException("Provider authority strings not found: $missing")
                    }
                }
            }

            val permissionAttributes = listOf(
                "android:name",
                "android:permission",
                "android:readPermission",
                "android:writePermission",
            )
            document.getElementsByTagName("*").forEachElement { element ->
                permissionAttributes.forEach { attribute ->
                    val value = element.getAttribute(attribute)
                    if (value in declaredPermissions) element.setAttribute(attribute, renamed(value))
                }
            }

            // Launcher name: the application and every component that inherits
            // its label.
            val appLabel = application.getAttribute("android:label")
            application.setAttribute("android:label", appName)
            if (appLabel.isNotEmpty()) {
                listOf("activity", "activity-alias").forEach { tag ->
                    application.getElementsByTagName(tag).forEachElement { component ->
                        if (component.getAttribute("android:label") == appLabel) {
                            component.setAttribute("android:label", appName)
                        }
                    }
                }
            }
        }
    }
}

private inline fun NodeList.forEachElement(block: (Element) -> Unit) {
    for (index in 0 until length) {
        (item(index) as? Element)?.let(block)
    }
}
