package dev.chieppa.webgpu;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

/**
 * Loads wgpu-native from the natives jar for this platform, which must be on the class path beside this one. The
 * bindings in {@code dev.chieppa.webgpu.ffi} find wgpu-native's symbols among the libraries their class loader has
 * loaded, so call {@link #load()} before the first call into them.
 */
public final class WgpuNative {
    private static final String NAME = "wgpu_native";
    private static final String RESOURCE_ROOT = "/dev/chieppa/webgpu/native";

    private static volatile boolean loaded;

    private WgpuNative() {}

    /** Loads the library once; later calls return at once. */
    public static void load() {
        if (loaded) return;
        synchronized (WgpuNative.class) {
            if (loaded) return;
            String file = System.mapLibraryName(NAME);
            System.load(LibraryCache.store(file, read(file)).toAbsolutePath().toString());
            loaded = true;
        }
    }

    /** The platform this JVM runs on, as the classifier of its natives jar names it after {@code natives-}. */
    public static String platform() {
        return Platform.current();
    }

    private static byte[] read(String file) {
        String resource = RESOURCE_ROOT + "/" + platform() + "/" + file;
        try (InputStream in = WgpuNative.class.getResourceAsStream(resource)) {
            if (in == null) {
                throw new IllegalStateException("No wgpu-native for " + platform() + " on the class path; add "
                        + "dev.chieppa:webgpu-native-ffm with classifier natives-" + platform());
            }
            return in.readAllBytes();
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read " + resource, e);
        }
    }
}
