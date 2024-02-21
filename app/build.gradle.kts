import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.7.17"
    id("io.spring.dependency-management") version "1.1.3"
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.spring") version "1.9.22"
}

apply(plugin = "io.spring.dependency-management")

group = "com.lindar"
version = "0.0.1-SNAPSHOT"

java {
}

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    compileOnly("javax.servlet:javax.servlet-api:4.0.1")

    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")

    implementation("org.jetbrains.kotlin:kotlin-reflect")

    implementation("com.slack.api:bolt:1.35.0")
    implementation("com.slack.api:bolt-socket-mode:1.35.0")
    implementation("com.slack.api:bolt-servlet:1.35.0")
    implementation("org.glassfish.tyrus.bundles:tyrus-standalone-client:1.19")
    implementation("com.lindar:well-rested-client:2.3.4")
    implementation("org.json:json:20231013")

    implementation("com.github.ben-manes.caffeine:caffeine:2.9.3")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    implementation(kotlin("stdlib-jdk8"))

    implementation("com.google.apis:google-api-services-sheets:v4-rev20230227-2.0.0")
    implementation("com.google.api-client:google-api-client:2.2.0")
    implementation("com.google.oauth-client:google-oauth-client-jetty:1.34.1")
    implementation("com.google.auth:google-auth-library-oauth2-http:1.2.1")
    implementation("com.google.http-client:google-http-client-jackson2:1.34.2")

}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "17"
    }
}


tasks.withType<Test> {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}