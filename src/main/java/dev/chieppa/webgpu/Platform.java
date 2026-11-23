package dev.chieppa.webgpu;

import java.util.Locale;

/** The platform names that label the natives jars, and which of them this JVM runs on. */
final class Platform {
    private Platform() {}

    /** The platform this JVM runs on: {@code windows-x64}, {@code linux-x64}, {@code linux-arm64}, {@code macos-x64} or {@code macos-arm64}. */
    static String current() {
        String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        String arch = System.getProperty("os.arch").toLowerCase(Locale.ROOT);
        boolean arm = arch.equals("aarch64") || arch.equals("arm64");
        if (os.startsWith("windows")) return "windows-x64";
        if (os.startsWith("mac")) return arm ? "macos-arm64" : "macos-x64";
        return arm ? "linux-arm64" : "linux-x64";
    }

    static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase(Locale.ROOT).startsWith("windows");
    }

    static boolean isMac() {
        return System.getProperty("os.name").toLowerCase(Locale.ROOT).startsWith("mac");
    }
}
