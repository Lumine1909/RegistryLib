plugins {
    id("com.gradleup.shadow")
}

dependencies {
    compileOnly("com.destroystokyo.paper:paper-api:1.16.5-R0.1-SNAPSHOT")
    compileOnly("org.jetbrains:annotations:26.0.2")
    implementation(project(":api"))
    implementation(project(":common"))

    implementation(project(":nms:nms_1_16_5"))
    //implementation(project(":nms:nms_1_18_2", configuration = "reobfArtifact"))
    implementation(project(":nms:nms_1_19_4", configuration = "reobfArtifact"))
    //implementation(project(":nms:nms_1_20_4", configuration = "reobfArtifact"))
    //implementation(project(":nms:nms_1_20_6", configuration = "mojangNamedArtifact"))
    implementation(project(":nms:nms_1_21", configuration = "mojangNamedArtifact"))
    implementation(project(":nms:nms_1_21_3", configuration = "mojangNamedArtifact"))
    implementation(project(":nms:nms_1_21_5", configuration = "mojangNamedArtifact"))
}

tasks {
    assemble {
        dependsOn(shadowJar)
    }
    shadowJar {
        archiveFileName.set("RegistryLib-${version}-MC-1.16.5-1.21.7.jar")
        minimize()
        dependsOn(project(":nms:nms_1_19_4").tasks.named("reobfJar"))
    }
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
    processResources {
        filteringCharset = Charsets.UTF_8.name()
        val props = mapOf(
            "name" to rootProject.name,
            "version" to project.version,
            "description" to project.description,
            "apiVersion" to "1.16"
        )
        inputs.properties(props)
        filesMatching("plugin.yml") {
            expand(props)
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}