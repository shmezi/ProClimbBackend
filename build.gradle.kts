val ktorVersion: String by project
val kotlinVersion: String by project
val logbackVersion: String by project

plugins {
    kotlin("jvm") version "1.8.10"
    id("io.ktor.plugin") version "2.2.3"
    kotlin("plugin.serialization") version "1.8.10"
}

group = "me.alexirving"
version = "0.0.1"
application {
    mainClass.set("me.alexirving.ApplicationKt")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=true")
}

repositories {
    mavenCentral()
    flatDir {
        dirs("libs")
    }
}

dependencies {

    implementation("org.litote.kmongo:kmongo-coroutine-serialization:4.8.0")
    implementation("org.springframework.security:spring-security-crypto:6.0.2")
    implementation("commons-logging:commons-logging:1.2")
    implementation("org.bouncycastle:bcpkix-jdk15on:1.70")
    implementation("AlexLib:database:3.4.4.2")
    implementation("AlexLib:utilities:3.1")

    implementation("io.ktor:ktor-server-html-builder:$ktorVersion")
    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-auth-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion")
    implementation("io.ktor:ktor-server-sessions:$kotlinVersion")
    implementation("io.ktor:ktor-network-tls-certificates:$kotlinVersion")
    implementation("io.ktor:ktor-server-freemarker:$kotlinVersion")
    implementation("io.ktor:ktor-server-websockets:$kotlinVersion")

}