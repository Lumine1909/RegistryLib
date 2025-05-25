plugins {
    java
    id("com.gradleup.shadow")
}

allprojects {
    plugins.apply("java")

    group = "io.github.lumine1909"
    version = "beta-1.0.2"
    description = "Minecraft Registry manage plugin"

    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://repo.codemc.io/repository/nms-local/")
    }

    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    }
}