plugins {
    kotlin("jvm") version "2.1.10"
    kotlin("plugin.serialization") version "1.6.0"
}

group = "kcl.seg.rtt"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven ("https://jitpack.io")
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation("info.picocli:picocli:4.7.1")
    implementation("com.squareup.okhttp3:okhttp:5.0.0-alpha.14")
    implementation("io.github.java-diff-utils:java-diff-utils:4.15")
    implementation ("com.github.kotlin-inquirer:kotlin-inquirer:0.1.0")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}