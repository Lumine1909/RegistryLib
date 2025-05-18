dependencies {
    compileOnly("org.spigotmc:spigot-api:1.16.5-R0.1-SNAPSHOT")
    compileOnly("io.netty:netty-all:4.1.119.Final")
    implementation("net.kyori:adventure-key:4.21.0")
    implementation(project(":api"))
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}