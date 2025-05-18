import com.vanniktech.maven.publish.SonatypeHost

plugins {
    id("java-library")
    id("signing")
    id("com.vanniktech.maven.publish") version "0.31.0"
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.16.5-R0.1-SNAPSHOT")
    compileOnly("org.jetbrains:annotations:26.0.2")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}


mavenPublishing {
    coordinates(
        groupId = group as String,
        artifactId = "registrylib-api",
        version = version as String
    )
    pom {
        name.set("registrylib-api")
        description.set("API for RegistryLib plugin.")
        url.set("https://github.com/Lumine1909/RegistryLib")
        licenses {
            license {
                name.set("Apache 2.0 License")
            }
        }

        developers {
            developer {
                id.set("Lumine1909")
                name.set("Lumine1909")
                email.set("133463833+Lumine1909@users.noreply.github.com")
            }
        }

        scm {
            connection.set("scm:git:git://github.com/Lumine1909/RegistryLib.git")
            developerConnection.set("scm:git:ssh://github.com/Lumine1909/RegistryLib.git")
            url.set("https://github.com/Lumine1909/RegistryLib")
        }
    }

    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()
}