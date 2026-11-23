import dev.chieppa.webgpu.build.DownloadWgpuNative
import dev.chieppa.webgpu.build.GenerateWgpuBindings
import dev.chieppa.webgpu.build.WgpuArchives

plugins {
    `java-library`
    `maven-publish`
}

group = "dev.chieppa"
version = providers.gradleProperty("releaseVersion").getOrElse("0.1.0-SNAPSHOT")

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(libs.versions.jvmToolchain.get().toInt()))
    withSourcesJar()
}

val wgpuRelease: String = libs.versions.wgpu.native.get()

/** The GitHub repository this library lives in and publishes to. */
val githubRepository = "nrc4867/webgpu-native-ffm"

/** Per platform: download its wgpu-native archive and pack the library, with wgpu-native's licenses, in its own jar. */
val platforms = WgpuArchives.all.associateWith { archive ->
    val download = tasks.register<DownloadWgpuNative>("downloadWgpuNative${archive.taskSuffix}") {
        url = archive.url(wgpuRelease)
        sha256 = archive.sha256
        destination = layout.buildDirectory.dir("wgpu-native/${archive.platform}")
    }
    val jar = tasks.register<Jar>("nativesJar${archive.taskSuffix}") {
        group = "build"
        description = "Packs wgpu-native for ${archive.platform}."
        archiveClassifier = archive.classifier
        from(download.map { it.destination.dir("lib") }) {
            include(archive.library)
            into(archive.resourceFolder)
        }
        from(layout.projectDirectory.dir("licenses")) { into("META-INF/licenses") }
    }
    download to jar
}

val nativesJars by tasks.registering {
    group = "build"
    description = "Packs wgpu-native for every platform."
    dependsOn(platforms.values.map { it.second })
}
tasks.assemble { dependsOn(nativesJars) }

dependencies {
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.launcher)
    // Tests load the library the way an app does: from the natives jar of the machine running them.
    testRuntimeOnly(files(platforms.getValue(WgpuArchives.host).second))
}

tasks.test {
    useJUnitPlatform()
    jvmArgs("--enable-native-access=ALL-UNNAMED")
    systemProperty("wgpu.release", wgpuRelease)
}

// Regenerates src/main/java from the downloaded headers: ./gradlew generateBindings -Pjextract.home=<jextract>
tasks.register<GenerateWgpuBindings>("generateBindings") {
    group = "build"
    jextractHome = providers.gradleProperty("jextract.home")
    headers = platforms.getValue(WgpuArchives.host).first.flatMap { it.destination.dir("include") }
    includes = layout.projectDirectory.file("bindings.includes")
    targetPackage = "dev.chieppa.webgpu.ffi"
    output = layout.projectDirectory.dir("src/main/java")
}

// Publishes to GitHub Packages in the repository named by GITHUB_REPOSITORY (set by Actions). Credentials come from
// ORG_GRADLE_PROJECT_GitHubPackagesUsername and ORG_GRADLE_PROJECT_GitHubPackagesPassword.
publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/${providers.environmentVariable("GITHUB_REPOSITORY").getOrElse(githubRepository)}")
            credentials(PasswordCredentials::class)
        }
    }
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            platforms.values.forEach { artifact(it.second) }
            pom {
                name = rootProject.name
                description = "Java bindings for wgpu-native over the Foreign Function & Memory API, with the " +
                    "native library in a jar per platform. Bundles wgpu-native $wgpuRelease."
                url = "https://github.com/$githubRepository"
                licenses {
                    license {
                        name = "MIT"
                        url = "https://opensource.org/license/mit"
                    }
                }
                scm { url = "https://github.com/$githubRepository" }
            }
        }
    }
}
