package dev.chieppa.webgpu.build

/**
 * One wgpu-native release archive for a platform: its asset name, the SHA-256 it must have, the library file in
 * it, and the platform name that labels its jar and its folder inside that jar.
 */
data class WgpuArchive(val asset: String, val sha256: String, val library: String, val platform: String) {
    /** The platform as a task name suffix: `windows-x64` becomes `WindowsX64`. */
    val taskSuffix: String = platform.split('-').joinToString("") { part -> part.replaceFirstChar { it.uppercase() } }

    /** The classifier of this platform's jar. */
    val classifier: String = "natives-$platform"

    /** The folder inside the jar that holds the library; the loader looks for it at the same path. */
    val resourceFolder: String = "$RESOURCE_ROOT/$platform"

    /** Where this archive is downloaded from, for a wgpu-native release tag such as `v29.0.1.1`. */
    fun url(release: String): String = "https://github.com/gfx-rs/wgpu-native/releases/download/$release/$asset"

    companion object {
        const val RESOURCE_ROOT = "dev/chieppa/webgpu/native"
    }
}
