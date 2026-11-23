package dev.chieppa.webgpu;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Copies a library out of a jar into the user's cache, in a folder named after its content hash, so programs on
 * different versions never overwrite each other's copy and nothing is written next to the Java runtime, which an
 * installed app cannot write to.
 */
final class LibraryCache {
    private static final String FOLDER = "webgpu-native-ffm";
    private static final int HASH_CHARS = 16;

    private LibraryCache() {}

    /** The cached copy of {@code bytes} named {@code fileName}, written first if it is not there yet. */
    static Path store(String fileName, byte[] bytes) {
        Path target = root().resolve(hash(bytes)).resolve(fileName);
        try {
            if (Files.isRegularFile(target) && Files.size(target) == bytes.length) return target;
            Files.createDirectories(target.getParent());
            Path partial = Files.createTempFile(target.getParent(), fileName, ".part");
            Files.write(partial, bytes);
            Files.move(partial, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            return target;
        } catch (IOException e) {
            throw new UncheckedIOException("Could not cache " + fileName + " at " + target, e);
        }
    }

    private static Path root() {
        String home = System.getProperty("user.home");
        if (Platform.isWindows()) {
            String local = System.getenv("LOCALAPPDATA");
            return Path.of(local != null ? local : home + "/AppData/Local", FOLDER, "Cache");
        }
        if (Platform.isMac()) return Path.of(home, "Library", "Caches", FOLDER);
        String xdg = System.getenv("XDG_CACHE_HOME");
        return Path.of(xdg != null ? xdg : home + "/.cache", FOLDER);
    }

    private static String hash(byte[] bytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes)).substring(0, HASH_CHARS);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Every Java runtime has SHA-256", e);
        }
    }
}
