plugins {
    kotlin("jvm") version "2.1.10"
    kotlin("plugin.serialization") version "1.6.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    application
}

group = "kcl.seg.rtt"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven ("https://jitpack.io")
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:2.1.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("info.picocli:picocli:4.7.1")
    implementation("com.squareup.okhttp3:okhttp:5.0.0-alpha.14")
    implementation("io.github.java-diff-utils:java-diff-utils:4.15")
    implementation ("com.github.kotlin-inquirer:kotlin-inquirer:0.1.0")
    testImplementation(kotlin("test"))

}

tasks.test {
    useJUnitPlatform()
}
application {
    mainClass.set("HeadchefCLIKt")
}


tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "HeadchefCLIKt"
    }
}

tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    archiveBaseName.set("command-line-client")
    archiveClassifier.set("")
}

kotlin {
    jvmToolchain(21)
}