plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.1.0"
    id("org.jetbrains.intellij.platform") version "2.3.0"
}

group = "it.paol0b"
version = "1.0.0"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        intellijIdeaCommunity("2025.1")
        bundledPlugin("com.intellij.java")
        testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)
    }
    testImplementation("junit:junit:4.13.2")
}

kotlin {
    jvmToolchain(21)
}

intellijPlatform {
    pluginConfiguration {
        id = "it.paol0b.rainbowbrackets"
        name = "Rainbow Brackets"
        version = project.version.toString()
        description = "Colorize matching brackets with rainbow colors for improved readability."
        vendor {
            name = "Paolo Bertinetti"
        }
        ideaVersion {
            sinceBuild = "251"
        }
    }
}

tasks {
    test {
        useJUnit()
        jvmArgs("-Djava.awt.headless=true")
    }

    wrapper {
        gradleVersion = "8.12"
    }
}
