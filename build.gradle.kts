val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val postgres_version : String by project
val h2_version : String by project
val exposed_version : String by project
val slf4j_version : String by project

plugins {
    kotlin("jvm") version "1.8.20"
    id("io.ktor.plugin") version "2.2.4"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.8.20"
    id("org.flywaydb.flyway") version "9.8.1"
}

flyway {
    url = "jdbc:postgresql://localhost:5432/crawler"
    user = "t.eichholz"
    password = ""
    defaultSchema = "crawler"
}

ktor {
    docker {
        localImageName.set("192.168.0.189:32000/novel-crawler-kt")
        imageTag.set("latest")
    }
}

group = "com.novel-crawler"
version = "0.0.1"
application {
    mainClass.set("com.novelcrawler.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {
    // database
    implementation("com.h2database:h2:$h2_version")
    implementation("org.postgresql:postgresql:$postgres_version")

    // https://mvnrepository.com/artifact/org.flywaydb/flyway-core
    implementation("org.flywaydb:flyway-core:9.16.3")

    // selenium
    implementation("org.seleniumhq.selenium:selenium-java:4.9.0")

    // exposed orm
    implementation("org.jetbrains.exposed", "exposed-core", "0.40.1")
    implementation("org.jetbrains.exposed", "exposed-dao", "0.40.1")
    implementation("org.jetbrains.exposed", "exposed-jdbc", "0.40.1")
    implementation("org.jetbrains.exposed:exposed-java-time:0.40.1")

    // logging
    implementation("org.slf4j:slf4j-api:$slf4j_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("ch.qos.logback:logback-core:$logback_version")

    // arrow-kt
    implementation("io.arrow-kt:arrow-core:1.2.0-RC")
    implementation("io.arrow-kt:arrow-resilience:1.2.0-RC")
    implementation("io.arrow-kt:arrow-fx-coroutines:1.2.0-RC")
    implementation("io.arrow-kt:suspendapp-jvm:0.4.0")
    implementation("io.arrow-kt:suspendapp-ktor-jvm:0.4.0")

    // hoplite config
    implementation("com.sksamuel.hoplite:hoplite-core:2.7.3")
    runtimeOnly("com.sksamuel.hoplite:hoplite-yaml:2.7.3")

    // koin
    runtimeOnly("io.insert-koin:koin-core:3.4.0")
    implementation("io.insert-koin:koin-ktor:3.4.0")
    implementation("io.insert-koin:koin-logger-slf4j:3.4.0")
    implementation("io.insert-koin:koin-test:3.4.0")
    // testImplementation("io.insert-koin:koin-test-junit5:3.4.0")

    // ktor
    implementation("io.ktor:ktor-server-default-headers-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-core-jvm:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-metrics-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-call-logging-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-swagger:$ktor_version")
    implementation("io.ktor:ktor-server-cors-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-auth-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-auth-jwt-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-host-common-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-status-pages-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-resources:$ktor_version")
    implementation("io.ktor:ktor-server-netty-jvm:$ktor_version")
    testImplementation("io.ktor:ktor-server-tests-jvm:$ktor_version")

    // kotest
    testImplementation("io.kotest:kotest-runner-junit5:5.5.5")
    testImplementation("io.kotest:kotest-assertions-core:5.5.5")
    testImplementation("io.kotest.extensions:kotest-extensions-koin:1.1.0")
    testImplementation("io.kotest:kotest-property:5.5.5")

    // mockk
    testImplementation("io.mockk:mockk:1.13.4")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>() {
    kotlinOptions.jvmTarget = "11"
    kotlinOptions.freeCompilerArgs = listOf("-Xcontext-receivers")
}