import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType

plugins {
    id("java")
    id("org.jetbrains.intellij.platform") version "2.19.0"
}

group = "io.github.mdgfox"
version = "2.0.0"

repositories {
    mavenCentral()

    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        // Build against the latest IDE, stay compatible with older ones via sinceBuild below
        create(IntelliJPlatformType.IntellijIdea, "2026.2.4")

        pluginVerifier()
    }
}

java {
    toolchain {
        // IDE 2026.2 classes are Java 25, so compiling against it needs JDK 25
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

tasks.withType<JavaCompile> {
    // Emit Java 21 bytecode so the plugin still loads in older IDEs (2024.3+ run on Java 21)
    options.release.set(21)
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild.set("243")
            // No untilBuild: the plugin only uses stable UI APIs, so it keeps working on newer IDEs
            untilBuild.set(provider { null })
        }
    }

    pluginVerification {
        ides {
            // The lowest supported IDE and the one the plugin is built against
            create(IntelliJPlatformType.IntellijIdea, "2024.3")
            create(IntelliJPlatformType.IntellijIdea, "2026.2.4")
            // Other IDEs on the same platform
            create(IntelliJPlatformType.Rider, "2026.2.4") {
                // Rider can't be installed with the installer yet
                useInstaller = false
            }
            create(IntelliJPlatformType.WebStorm, "2026.2.4")
        }
    }

    signing {
        certificateChain.set(providers.environmentVariable("CERTIFICATE_CHAIN"))
        privateKey.set(providers.environmentVariable("PRIVATE_KEY"))
        password.set(providers.environmentVariable("PRIVATE_KEY_PASSWORD"))
    }

    publishing {
        token.set(providers.environmentVariable("PUBLISH_TOKEN"))
    }
}
