plugins {
    kotlin("jvm") version "2.1.10"
}

group = "kcl.seg.rtt"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("info.picocli:picocli:4.7.1")
    implementation("com.squareup.okhttp3:okhttp:5.0.0-alpha.14")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}