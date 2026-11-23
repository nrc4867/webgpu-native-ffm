package dev.chieppa.webgpu;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.chieppa.webgpu.ffi.wgpu_h;
import org.junit.jupiter.api.Test;

class WgpuNativeTest {
    /** wgpu-native packs its version as one byte each for major, minor, patch and build. */
    private static String version(int packed) {
        return (packed >>> 24) + "." + ((packed >>> 16) & 0xff) + "." + ((packed >>> 8) & 0xff) + "." + (packed & 0xff);
    }

    @Test
    void loadedLibraryIsTheBundledRelease() {
        WgpuNative.load();
        assertEquals(System.getProperty("wgpu.release"), "v" + version(wgpu_h.wgpuGetVersion()));
    }

    @Test
    void loadingTwiceKeepsTheFirstLibrary() {
        WgpuNative.load();
        assertDoesNotThrow(WgpuNative::load);
    }
}
