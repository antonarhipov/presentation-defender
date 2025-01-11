import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

group = "org.kotlinlang"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {
    // Note, if you develop a library, you should use compose.desktop.common.
    // compose.desktop.currentOs should be used in launcher-sourceSet
    // (in a separate module for demo project and in testMain).
    // With compose.desktop.common you will also lose @Preview functionality
    implementation(compose.desktop.currentOs)
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "presentation-defender"
            packageVersion = "1.0.0"
            
            macOS {
                // Temporarily disable icon for macOS
                // iconFile.set(project.file("icon.icns"))
                bundleID = "com.example.presentation-defender"
                signing {
                    sign.set(false)
                }
            //   signing {
            //       sign.set(true)
            //       identity.set("Developer ID Application: Your Name (TEAM_ID)")
            //   }
                // notarization {
                //     appleID.set("your.email@example.com")
                //     password.set("@keychain:AC_PASSWORD")
                //     teamID.set("TEAM_ID")
                // }
            }
            
            windows {
                // Temporarily disable icon for Windows
                // iconFile.set(project.file("icon.ico"))
                upgradeUuid = "61c59277-2771-4c68-8c7e-b6ebf499bf3c"
                menuGroup = "Presentation Defender"
                perUserInstall = true
            }
            
            linux {
                // Temporarily disable icon for Linux
                // iconFile.set(project.file("icon.png"))
                packageName = "presentation-defender"
                debMaintainer = "example@example.com"
                menuGroup = "Utilities"
            }

            modules("java.sql")
            
            copyright = " 2025 Your Name"
            vendor = "Your Name"
            description = "A timer application for presentations"
        }
    }
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }

    sourceSets.all {
        languageSettings {
            version = 2.0
        }
    }

}