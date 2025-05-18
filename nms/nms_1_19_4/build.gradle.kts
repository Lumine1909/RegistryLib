plugins {
    id("io.papermc.paperweight.userdev")
}

val reobfJar = tasks.named("reobfJar")
val reobfFile = layout.buildDirectory.file("libs/${project.name}-${version}-reobf.jar")

dependencies {
    paperweight.paperDevBundle("1.19.4-R0.1-SNAPSHOT")
    implementation(project(":api"))
    implementation(project(":common"))
}

configurations {
    create("reobfArtifact")
}

artifacts {
    add("reobfArtifact", reobfFile) {
        builtBy(reobfJar)
    }
}

tasks.assemble {
    dependsOn(tasks.reobfJar)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}