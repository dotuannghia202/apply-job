plugins {
    java
    war
    id("org.springframework.boot") version "3.5.8"
    id("io.spring.dependency-management") version "1.1.7"
    id("io.freefair.lombok") version "8.6"
}

group = "com.dtn"
version = "0.0.1-SNAPSHOT"
description = "World of work"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.api-client:google-api-client:2.2.0")
    implementation("com.google.apis:google-api-services-gmail:v1-rev20230206-2.0.0")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("com.turkraft.springfilter:jpa:3.1.7")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("com.cloudinary:cloudinary-http44:1.36.0")
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    providedRuntime("org.springframework.boot:spring-boot-starter-tomcat")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    runtimeOnly("org.postgresql:postgresql")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
