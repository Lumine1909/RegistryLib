plugins {
    id("io.papermc.paperweight.userdev")
}

paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION

val mojangNamedJar = tasks.named("jar")

dependencies {
    paperweight.paperDevBundle("1.21.4-R0.1-SNAPSHOT")
    implementation(project(":api"))
    implementation(project(":common"))
}

configurations {
    create("mojangNamedArtifact")
}

artifacts {
    add("mojangNamedArtifact", layout.buildDirectory.file("libs/${project.name}-${version}.jar")) {
        builtBy(mojangNamedJar)
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}