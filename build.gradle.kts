plugins {
    kotlin("jvm")
    id("io.ktor.plugin")
    id("io.gitlab.arturbosch.detekt")
    id("com.github.ben-manes.versions")
    jacoco
}

group = "com.gamenews"
version = "0.0.1"

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("com.gamenews.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(Ktor.server.core)
    implementation(Ktor.server.auth)
    implementation(Ktor.server.netty)
    implementation(Ktor.server.freeMarker)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.0-Beta")
    implementation("ch.qos.logback:logback-classic:_")
    implementation("org.litote.kmongo:kmongo:4.8.0")
    implementation("org.litote.kmongo:kmongo-coroutine:4.8.0")

    testImplementation(Kotlin.test.junit)
    testImplementation("io.ktor:ktor-server-tests-jvm:_")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.0-Beta")
    testImplementation("io.mockk:mockk:1.12.0")

    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:_")
}

detekt {
    autoCorrect = true
}

tasks.jacocoTestReport {
    reports {
        xml.required.set(true)
    }
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport) // report is always generated after tests run
}
tasks.jacocoTestReport {
    dependsOn(tasks.test) // tests are required to run before generating the report
}